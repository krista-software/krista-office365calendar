package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.CreateMeetingHelper;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.MeetingDaoHelper;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.MeetingDao;
import app.krista.model.base.DateRange;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.MeetingDaoHelper.*;

@Service
public class MeetingDaoImpl implements MeetingDao {

    private final GraphServiceClientProvider clientProvider;
    private final String clientTimeZone;


    @Inject
    public MeetingDaoImpl(GraphServiceClientProviderFactory clientProviderFactory, Office365CalendarAttributes office365CalendarAttributes) {
        this(clientProviderFactory.create(), office365CalendarAttributes);
    }

    public MeetingDaoImpl(GraphServiceClientProvider clientProvider, Office365CalendarAttributes office365CalendarAttributes) {
        this.clientProvider = clientProvider;
        this.clientTimeZone = office365CalendarAttributes.getTimeZone();
    }

    @NotNull
    private static LinkedList<SearchRequest> getSearchRequests(String queryString) {
        LinkedList<SearchRequest> requestsList = new LinkedList<>();
        SearchRequest requests = new SearchRequest();
        LinkedList<EntityType> entityTypesList = new LinkedList<>();
        entityTypesList.add(EntityType.EVENT);
        requests.entityTypes = entityTypesList;
        SearchQuery query = new SearchQuery();
        query.queryString = queryString;
        requests.query = query;
        requestsList.add(requests);
        return requestsList;
    }

    private static boolean isOldEventOnline(String details, Event oldEvent, Event event) {
        return oldEvent.isOnlineMeeting != null && oldEvent.isOnlineMeeting && event.isOnlineMeeting != null && !event.isOnlineMeeting && (details == null || details.isBlank());
    }

    private static void setDateTimeTimeZone(String selectedTimeZone, Event newEvent) {
        if ("UTC".equals(Objects.requireNonNull(Objects.requireNonNull(newEvent).start).timeZone)) {
            convertDateTimeUTCToTimeZone(newEvent, selectedTimeZone, true);
        } else {
            convertDateTimeTimeZoneToUTC(newEvent, Objects.requireNonNull(newEvent.start).timeZone, true);
        }
        if ("UTC".equals(Objects.requireNonNull(Objects.requireNonNull(newEvent).end).timeZone)) {
            convertDateTimeUTCToTimeZone(newEvent, selectedTimeZone, false);
        } else {
            convertDateTimeTimeZoneToUTC(newEvent, Objects.requireNonNull(newEvent.end).timeZone, false);
        }
    }

    @Override
    public Meeting getMeetingById(String id) throws IOException {
        Validators.validateString(id, Constants.MEETING_ID);
        return new MeetingImpl(getCalendarRequestBuilder().events(id).buildRequest().get());
    }

    @Override
    public List<Meeting> getMeetings() {
        try {
            String dateTime = buildDateTimeTimeZone(System.currentTimeMillis(), null, clientTimeZone).dateTime;
            EventCollectionPage eventCollection = getCalendarRequestBuilder().events().buildRequest().filter("start/dateTime ge '" + dateTime + "'").get();
            List<Meeting> eventDetails = new ArrayList<>();
            if (eventCollection != null) {
                for (Event event : eventCollection.getCurrentPage()) {
                    User currentUser = clientProvider.getUserRequestBuilder(false, null).buildRequest().get();
                    if (currentUser != null && MeetingDaoHelper.isUpcomingEvent(event) && MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(event, currentUser.mail)) {
                        eventDetails.add(new MeetingImpl(event));
                    }
                }
            }
            return eventDetails;
        } catch (GraphServiceException | IOException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_GET_ALL_UNACCEPTED_EVENTS + cause.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getSchedules(Long dateFrom, Long dateTo, List<String> attendees, String timeZone) throws IOException {
        Validators.validateString(dateFrom.toString(), "date");
        Validators.validateString(dateTo.toString(), "date");
        Validators.validateStringsList(attendees, "attendees");
        if (dateFrom > dateTo) {
            throw new IllegalArgumentException(Constants.FAILED_TO_GET_SCHEDULES + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }
        List<Map<String, Object>> meetingsList = new ArrayList<>();
        LinkedList<String> schedulesList = new LinkedList<>(attendees);
        CalendarGetScheduleCollectionPage page = getCalendarRequestBuilder()
                .getSchedule(CalendarGetScheduleParameterSet
                        .newBuilder()
                        .withSchedules(schedulesList)
                        .withEndTime(buildDateTimeTimeZone(dateTo, timeZone, clientTimeZone))
                        .withStartTime(buildDateTimeTimeZone(dateFrom, timeZone, clientTimeZone))
                        .build())
                .buildRequest()
                .post();
        while (page != null && !page.getCurrentPage().isEmpty()) {
            for (ScheduleInformation scheduleInformation : page.getCurrentPage()) {
                if (scheduleInformation.scheduleId != null && !scheduleInformation.scheduleId.isEmpty()) {
                    meetingsList.add(getSchedulesInformation(scheduleInformation));
                }
            }
            CalendarGetScheduleCollectionRequestBuilder nextPage = page.getNextPage();
            if (nextPage != null) {
                page = nextPage.buildRequest().post();
            } else {
                break;
            }
        }
        return meetingsList;
    }

    private Map<String, Object> getSchedulesInformation(ScheduleInformation scheduleInformation) {

        Map<String, Object> scheduleInformationObject = new HashMap<>();

        if (scheduleInformation.scheduleItems != null && !scheduleInformation.scheduleItems.isEmpty()) {
            scheduleInformationObject.put(Constants.ATTENDEE, scheduleInformation.scheduleId);
            scheduleInformationObject.put(Constants.SCHEDULES, getScheduleItems(scheduleInformation.scheduleItems));
        }
        return scheduleInformationObject;
    }

    private List<Map<String, Object>> getScheduleItems(List<ScheduleItem> scheduleItems) {
        List<Map<String, Object>> scheduleItemsList = new ArrayList<>();
        if (scheduleItems != null) {
            scheduleItems.forEach(schedule -> {
                Map<String, Object> scheduleMap = new HashMap<>();
                if (schedule.start != null && schedule.end != null) {
                    scheduleMap.put("Scheduled Time", new DateRange(getDateInMilliSecond(schedule.start), getDateInMilliSecond(schedule.end)));
                    scheduleMap.put("Status", schedule.status);
                }
                scheduleItemsList.add(scheduleMap);
            });
        }
        return scheduleItemsList;
    }

    @Override
    public List<Meeting> getMeetings(int numberOfMeetings) {
        try {
            if (numberOfMeetings < 1) {
                throw new IllegalArgumentException(Constants.NUMBER_OF_MEETINGS_SHOULD_BE_GREATER_THAN_0);
            }
            List<Meeting> eventDetails = new ArrayList<>();
            String dateTime = buildDateTimeTimeZone(System.currentTimeMillis(), null, clientTimeZone).dateTime;
            EventCollectionPage eventCollPage = getCalendarRequestBuilder().events().buildRequest().filter("start/dateTime ge '" + dateTime + "'").top(numberOfMeetings).get();
            if (eventCollPage != null && !eventCollPage.getCurrentPage().isEmpty()) {
                for (Event event : eventCollPage.getCurrentPage()) {
                    eventDetails.add(new MeetingImpl(event));
                }
            }
            return eventDetails;
        } catch (GraphServiceException | IOException cause) {
            throw new IllegalStateException("Failed to get upcoming meetings " + cause.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getQueryResults(String queryString) {
        Validators.validateString(queryString, "query text");
        List<Map<String, Object>> searchHits = new ArrayList<>();
        LinkedList<SearchRequest> requests = getSearchRequests(queryString);
        SearchEntityQueryCollectionPage searchEntityQueryCollectionPage;
        try {
            searchEntityQueryCollectionPage = getSearchEntityQueryCollectionPage(requests);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (searchEntityQueryCollectionPage != null && !searchEntityQueryCollectionPage.getCurrentPage().isEmpty()) {

            searchHits = getResultFromSearchResponse(searchEntityQueryCollectionPage.getCurrentPage().get(0));
        }
        return searchHits;
    }

    @Override
    public List<Meeting> getEventsForUser(String userEmail, Long from, Long to) {
        try {
            String startTime = buildDateTimeTimeZone(from, null, clientTimeZone).dateTime;
            String endTime = buildDateTimeTimeZone(to, null, clientTimeZone).dateTime;
            EventCollectionPage eventCollection = clientProvider.getGraphServiceClientForSystem()
                    .users(userEmail).events().buildRequest()
                    .filter("start/dateTime ge '" + startTime + "' and end/dateTime le '" + endTime + "'")
                    .get();
            List<Meeting> eventDetails = new ArrayList<>();
            if (eventCollection != null) {
                for (Event event : eventCollection.getCurrentPage()) {
                    eventDetails.add(new MeetingImpl(event));
                }
            }
            return eventDetails;
        } catch (GraphServiceException cause) {
            String errorMessage = "";
            if (cause.getError() != null && cause.getError().error != null) {
                errorMessage = cause.getError().error.message;
            }
            throw new IllegalStateException(Constants.FAILED_TO_GET_USER_EVENTS + errorMessage, cause);
        }
    }

    @NotNull
    private List<Map<String, Object>> getResultFromSearchResponse(SearchResponse searchResponse) {
        List<Map<String, Object>> searchHits = new ArrayList<>();
        if (searchResponse.hitsContainers == null || searchResponse.hitsContainers.isEmpty()) {
            throw new IllegalStateException(Constants.SEARCH_RESPONSE_IS_EMPTY);
        } else {
            final SearchHitsContainer searchHitsContainer = searchResponse.hitsContainers.get(0);
            int numberOfHits = searchHitsContainer.hits != null ? searchHitsContainer.hits.size() : 0;
            numberOfHits = Math.min(numberOfHits, 25);
            for (int i = 0; i < numberOfHits; i++) {
                assert searchHitsContainer.hits != null;
                searchHits.add(getSearchHitContent(searchHitsContainer.hits.get(i)));
            }
        }
        return searchHits;
    }

    @Nullable
    private SearchEntityQueryCollectionPage getSearchEntityQueryCollectionPage(LinkedList<SearchRequest> requestsList) throws IOException {
        return clientProvider.getGraphServiceClient()
                .search()
                .query(SearchEntityQueryParameterSet
                        .newBuilder()
                        .withRequests(requestsList)
                        .build())
                .buildRequest()
                .post();
    }

    private Map<String, Object> getSearchHitContent(SearchHit searchHit) {
        Map<String, Object> searchHitData = new HashMap<>();
        searchHitData.put("Event ID", searchHit.hitId);
        Event event = (Event) searchHit.resource;
        assert event != null;
        searchHitData.put("Event Title", event.subject);
        assert event.start != null;
        assert event.end != null;
        searchHitData.put("Scheduled Time", new DateRange(getDateInMilliSecond(event.start), getDateInMilliSecond(event.end)));
        return searchHitData;
    }

    @Override
    public List<Map<String, Object>> getFreeSlot(Long dateFrom, Long dateTo, Boolean isOrganizerOptional, List<String> requiredAttendee, List<String> optionalAttendee, String timeZone, String meetingDuration) throws DatatypeConfigurationException {
        Validators.validateString(dateFrom.toString(), "date");
        Validators.validateString(dateTo.toString(), "date");
        Validators.validateStringsList(requiredAttendee, "attendees");
        if (dateFrom > dateTo) {
            throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }
        List<Map<String, Object>> meetingTimeSuggestionsList = new ArrayList<>();
        List<AttendeeBase> attendeesList = MeetingDaoHelper.getRequiredAttendee(requiredAttendee, optionalAttendee);
        TimeConstraint timeConstraint = getTimeConstraints(dateFrom, dateTo, timeZone);
        if (meetingDuration == null || meetingDuration.isEmpty()) {
            meetingDuration = "60M";
        }
        Duration slotDuration = DatatypeFactory.newInstance().newDuration(Constants.PT + meetingDuration);

        MeetingTimeSuggestionsResult meetingTimeSuggestionsResult;
        meetingTimeSuggestionsResult = clientProvider.getUserRequestBuilder(false, null)
                .findMeetingTimes(UserFindMeetingTimesParameterSet
                        .newBuilder()
                        .withAttendees(attendeesList)
                        .withTimeConstraint(timeConstraint)
                        .withMeetingDuration(slotDuration)
                        .withIsOrganizerOptional(isOrganizerOptional)
                        .withReturnSuggestionReasons(true)
                        .build())
                .buildRequest()
                .post();

        if (meetingTimeSuggestionsResult != null && meetingTimeSuggestionsResult.meetingTimeSuggestions != null) {
            for (MeetingTimeSuggestion meetingTimeSuggestion : meetingTimeSuggestionsResult.meetingTimeSuggestions) {
                if (meetingTimeSuggestion.meetingTimeSlot != null) {
                    meetingTimeSuggestionsList.add(getTimeSlot(meetingTimeSuggestion));
                }
            }
        }
        return meetingTimeSuggestionsList;

    }

    private Map<String, Object> getTimeSlot(MeetingTimeSuggestion meetingTimeSuggestion) {
        Map<String, Object> meetingTimeSlot = new HashMap<>();

        assert meetingTimeSuggestion.meetingTimeSlot != null;
        if (meetingTimeSuggestion.meetingTimeSlot.start != null && meetingTimeSuggestion.meetingTimeSlot.end != null) {
            meetingTimeSlot.put("Time Slot", new DateRange(getDateInMilliSecond(meetingTimeSuggestion.meetingTimeSlot.start), getDateInMilliSecond(meetingTimeSuggestion.meetingTimeSlot.end)));
        }
        if (meetingTimeSuggestion.attendeeAvailability != null) {
            meetingTimeSlot.put("Attendee Availability", getAttendeesAvailability(meetingTimeSuggestion.attendeeAvailability));
        }
        meetingTimeSlot.put("Confidence", meetingTimeSuggestion.confidence == null ? null : meetingTimeSuggestion.confidence / 100);
        meetingTimeSlot.put("Organizer Availability", meetingTimeSuggestion.organizerAvailability);
        meetingTimeSlot.put("Suggestion Reason", meetingTimeSuggestion.suggestionReason);

        return meetingTimeSlot;
    }

    private TimeConstraint getTimeConstraints(Long dateFrom, Long dateTo, String timeZone) {

        TimeConstraint timeConstraint = new TimeConstraint();
        timeConstraint.activityDomain = ActivityDomain.WORK;
        LinkedList<TimeSlot> timeSlotsList = new LinkedList<>();
        TimeSlot timeSlots = new TimeSlot();
        timeSlots.start = buildDateTimeTimeZone(dateFrom, timeZone, clientTimeZone);
        timeSlots.end = buildDateTimeTimeZone(dateTo, timeZone, clientTimeZone);
        timeSlotsList.add(timeSlots);
        timeConstraint.timeSlots = timeSlotsList;

        return timeConstraint;
    }

    private List<Map<String, Object>> getAttendeesAvailability(List<AttendeeAvailability> attendeeAvailability) {
        List<Map<String, Object>> attendeesAvailability = new ArrayList<>();
        if (attendeeAvailability != null) {
            attendeeAvailability.forEach(attendee -> {
                Map<String, Object> attendeeMap = new HashMap<>();
                if (attendee.attendee != null && attendee.attendee.emailAddress != null) {
                    attendeeMap.put(Constants.ATTENDEE, attendee.attendee.emailAddress.address);
                    attendeeMap.put(Constants.AVAILABILITY, attendee.availability);
                }
                attendeesAvailability.add(attendeeMap);
            });
        }
        return attendeesAvailability;
    }

    @Override
    public Meeting addMeeting(String title, String location, Long dateFrom, Long dateTo, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String timeZone, String reminder, String recurrence, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, List<Attachment> attachments, String details, Boolean isTeamsMeeting) {
        Validators.validateString(dateFrom.toString(), "date");
        if ((dateTo != null) && (dateFrom > dateTo)) {
            throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }
        try {
            String selectedTimeZone = (timeZone != null && !timeZone.isEmpty()) ? timeZone : clientTimeZone;
            Event event = getEvent(title, location, dateFrom, dateTo, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, timeZone, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, details, true, isTeamsMeeting);
            Event newEvent = getCalendarRequestBuilder().events()
                    .buildRequest()
                    .post(event);
            setDateTimeTimeZone(selectedTimeZone, newEvent);
            assert newEvent.id != null;
            for (Attachment attachment : attachments) {
                getCalendarRequestBuilder().events(newEvent.id).attachments().buildRequest().post(attachment);
            }
            if (!attachments.isEmpty()) {
                getCalendarRequestBuilder().events(newEvent.id).buildRequest().patch(newEvent);
            }
            return new MeetingImpl(newEvent);
        } catch (GraphServiceException | IOException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_SCHEDULE_MEETING + cause.getMessage());
        }
    }

    @Override
    public void forwardMeeting(String id, List<String> attendeeEmail) throws IOException {
        List<Recipient> toRecipientsList = MeetingDaoHelper.getRecipient(attendeeEmail);
        getCalendarRequestBuilder().events(id)
                .forward(EventForwardParameterSet
                        .newBuilder()
                        .withToRecipients(toRecipientsList)
                        .build())
                .buildRequest()
                .post();
    }

    @Override
    public Meeting updateMeeting(String id, String title, String details, String location, Long dateFrom, Long dateTo,
                                 String timeZone, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail,
                                 Boolean isAllDay, String recurrence, String reminder, Double recurrenceInterval,
                                 List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex,
                                 String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, Boolean isTeamsMeeting) {
        if (dateFrom != null && dateTo != null && dateFrom > dateTo) {
            throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }
        try {
            String selectedTimeZone = (timeZone != null && !timeZone.isEmpty()) ? timeZone : clientTimeZone;
            Event oldEvent = getCalendarRequestBuilder().events(id)
                    .buildRequest()
                    .get();
            SimpleDateFormat sfd = new SimpleDateFormat(Constants.YYYY_MM_DD_T_HH_MM_SS);
            Date date = sfd.parse(Objects.requireNonNull(Objects.requireNonNull(oldEvent).start).dateTime);
            long eventStart = convertDateTimeToEpochInTimeZone(convertDateToFormattedString(date), selectedTimeZone);
            date = sfd.parse(Objects.requireNonNull(Objects.requireNonNull(oldEvent).end).dateTime);
            long eventEnd = convertDateTimeToEpochInTimeZone(convertDateToFormattedString(date), selectedTimeZone);
            if (isAllDay != null && isAllDay && dateFrom == null) {
                dateFrom = eventStart;
            }
            if ((dateFrom != null && dateTo == null && dateFrom > eventEnd) || (dateFrom == null && dateTo != null && eventStart > dateTo)) {
                throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
            }

            Event event = getEvent(title, location, dateFrom, dateTo, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, timeZone, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, details, false, isTeamsMeeting);
            if (event.attendees == null || event.attendees.isEmpty()) {
                event.attendees = oldEvent.attendees;
            }
            if (isOldEventOnline(details, oldEvent, event)) {
                Pattern regex = Pattern.compile("\\.{137}");
                String[] result = new String[3];
                if (oldEvent.body != null && oldEvent.body.content != null) {
                    result = oldEvent.body.content.split(String.valueOf(regex), 3);
                }
                event.body = MeetingDaoHelper.getItemBody(result[0] + "" + result[2], Constants.HTML);
            }
            event.changeKey = oldEvent.changeKey;
            event.originalStartTimeZone = Objects.requireNonNull(oldEvent.start).timeZone;
            event.originalEndTimeZone = Objects.requireNonNull(oldEvent.end).timeZone;
            Event newEvent = getCalendarRequestBuilder().events(id)
                    .buildRequest()
                    .patch(event);
            setDateTimeTimeZone(selectedTimeZone, newEvent);
            return new MeetingImpl(newEvent);
        } catch (GraphServiceException | ParseException | IOException | NullPointerException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_UPDATE_EVENT + cause.getMessage());
        }
    }

    @Override
    public Meeting acceptMeetingInvitation(String id) {
        Validators.validateString(id, Constants.MEETING_ID);
        try {
            getCalendarRequestBuilder().events(id).accept(EventAcceptParameterSet.newBuilder().withSendResponse(true).build()).buildRequest().post();
            return getMeetingById(id);
        } catch (IOException | GraphServiceException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_ACCEPT_INVITATION + cause.getMessage());
        }
    }

    @Override
    public Meeting declineMeetingInvitation(String id) {
        Validators.validateString(id, Constants.MEETING_ID);
        try {
            Event event = clientProvider.getUserRequestBuilder(false, null).events(id).buildRequest().get();
            getCalendarRequestBuilder().events(id).decline(EventDeclineParameterSet.newBuilder().withSendResponse(true).build()).buildRequest().post();
            return new MeetingImpl(event);
        } catch (GraphServiceException | IOException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_DECLINE_INVITATION + cause.getMessage());
        }
    }

    @Override
    public Meeting tentativeMeetingInvitation(String id) {
        Validators.validateString(id, Constants.MEETING_ID);
        try {
            getCalendarRequestBuilder().events(id).tentativelyAccept(EventTentativelyAcceptParameterSet.newBuilder().withSendResponse(true).build()).buildRequest().post();
            return getMeetingById(id);
        } catch (IOException | GraphServiceException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_GET_TENTATIVE_INVITATIONS + cause.getMessage());
        }
    }

    @Override
    public Meeting cancelMeeting(String id) {
        Validators.validateString(id, Constants.MEETING_ID);
        try {
            Event event = getCalendarRequestBuilder().events(id).buildRequest().get();
            clientProvider.getUserRequestBuilder(false, null).events(id).buildRequest().delete();
            return new MeetingImpl(event);
        } catch (ClientException | IOException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_CANCEL_EVENT + cause.getMessage());
        }
    }

    @Override
    public Meeting addAttendees(String id, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail) {
        Validators.validateString(id, Constants.MEETING_ID);
        try {
            Event event = getCalendarRequestBuilder().events(id).buildRequest().get();
            List<Attendee> attendeesList = Objects.requireNonNull(event).attendees;
            assert attendeesList != null;
            attendeesList.addAll(MeetingDaoHelper.getAttendees(requiredAttendeeEmail, optionalAttendeeEmail));
            event.attendees = attendeesList;
            Event updatedEvent = getCalendarRequestBuilder().events(id).buildRequest().patch(event);
            return new MeetingImpl(updatedEvent);
        } catch (GraphServiceException | IOException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_ADD_ATTENDEES + cause.getMessage());
        }
    }

    @Override
    public Meeting removeAttendees(String id, List<String> attendees) {
        Validators.validateString(id, Constants.MEETING_ID);
        try {
            Event event = getCalendarRequestBuilder().events(id).buildRequest().get();
            List<Attendee> attendeesList = Objects.requireNonNull(event).attendees;
            MeetingDaoHelper.updateAttendeesList(Objects.requireNonNull(attendeesList), attendees);
            event.attendees = attendeesList;
            Event updatedEvent = getCalendarRequestBuilder().events(id).buildRequest().patch(event);
            return new MeetingImpl(updatedEvent);
        } catch (GraphServiceException | IOException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_REMOVE_ATTENDEE + cause.getMessage());
        }
    }

    private Event getEvent(String title, String location, Long dateFrom, Long dateTo, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String timeZone, String reminder, String recurrenceValue, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, String details, Boolean isCreateEvent, Boolean isTeamsMeeting) {

        try {
            Event event = new Event();
            CreateMeetingHelper.setEventSubject(title, event, isCreateEvent);
            CreateMeetingHelper.setEventBody(details, event);
            CreateMeetingHelper.setEventDateTime(dateFrom, dateTo, isAllDay, timeZone, isCreateEvent, clientTimeZone, event);
            CreateMeetingHelper.setEventLocation(location, event);
            CreateMeetingHelper.setEventAttendees(requiredAttendeeEmail, optionalAttendeeEmail, event);
            if (recurrenceValue != null && !recurrenceValue.isBlank()) {
                Validators.validateRecurrenceValues(recurrenceValue, daysOfWeek, recurrenceType, dayOfMonth, repeatDay);
                CreateMeetingHelper.setEventRecurrence(dateFrom, recurrenceValue, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, event);
            }
            CreateMeetingHelper.setEventReminder(reminder, event);
            CreateMeetingHelper.setIsTeamsMeeting(isTeamsMeeting, event);
            CreateMeetingHelper.setEventTransactionId(isCreateEvent, event);
            return event;
        } catch (ParseException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_SCHEDULE_MEETING + cause.getMessage());
        }
    }

    private CalendarRequestBuilder getCalendarRequestBuilder() throws IOException {
        return clientProvider.getUserRequestBuilder(false, null).calendar();
    }

    private long getDateInMilliSecond(DateTimeTimeZone dateToParse) {
        try {
            SimpleDateFormat sfd = new SimpleDateFormat(Constants.YYYY_MM_DD_T_HH_MM_SS);
            sfd.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sfd.parse(dateToParse.dateTime);
            SimpleDateFormat customFormat = new SimpleDateFormat(Constants.YYYY_MM_DD_T_HH_MM_SS);
            customFormat.setTimeZone(TimeZone.getTimeZone(clientTimeZone));
            Date newDate = customFormat.parse(customFormat.format(date));
            return newDate.getTime();
        } catch (ParseException parseException) {
            throw new IllegalStateException("Date :" + dateToParse.dateTime + " can't be parsed. " + parseException.getMessage(), parseException.getCause());
        }
    }

}
