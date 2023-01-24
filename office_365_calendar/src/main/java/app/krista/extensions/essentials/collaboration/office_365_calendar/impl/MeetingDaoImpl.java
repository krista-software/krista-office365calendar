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

import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.MeetingDaoHelper.getDateTimeTimeZone;

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
        LinkedList<SearchRequest> requestsList = new LinkedList<SearchRequest>();
        SearchRequest requests = new SearchRequest();
        LinkedList<EntityType> entityTypesList = new LinkedList<EntityType>();
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

    @Override
    public Meeting getMeetingById(String id) throws IOException {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return new MeetingImpl(getCalendarRequestBuilder().events(id).buildRequest().get());
    }

    @Override
    public List<Meeting> getMeetings() {
        try {
            String dateTime = getDateTimeTimeZone(System.currentTimeMillis(), null, clientTimeZone).dateTime;
            EventCollectionPage eventCollection = getCalendarRequestBuilder().events().buildRequest().filter("start/dateTime ge '" + dateTime + "'").get();
            List<Meeting> eventDetails = new ArrayList<>();
            if (eventCollection != null) {
                for (Event event : eventCollection.getCurrentPage()) {
                    User currentUser = clientProvider.getGraphServiceClient().me().buildRequest().get();
                    if (currentUser != null && MeetingDaoHelper.isUpcomingEvent(event) && MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(event, currentUser.mail)) {
                        eventDetails.add(new MeetingImpl(event));
                    }
                }
            }
            return eventDetails;
        } catch (IOException | GraphServiceException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_GET_ALL_UNACCEPTED_EVENTS + cause.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Map<String, Object>> getSchedules(Long dateFrom, Long dateTo, List<String> attendees, String timeZone) throws IOException {
        if (dateFrom > dateTo) {
            throw new IllegalArgumentException(Constants.FAILED_TO_GET_SCHEDULES + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }
        try {
            List<Map<String, Object>> meetingsList = new ArrayList<>();
            LinkedList<String> schedulesList = new LinkedList<>(attendees);
            CalendarGetScheduleCollectionPage page = getCalendarRequestBuilder()
                    .getSchedule(CalendarGetScheduleParameterSet
                            .newBuilder()
                            .withSchedules(schedulesList)
                            .withEndTime(getDateTimeTimeZone(dateTo, timeZone, clientTimeZone))
                            .withStartTime(getDateTimeTimeZone(dateFrom, timeZone, clientTimeZone))
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
        } catch (IOException cause) {
            throw new IOException(Constants.FAILED_TO_GET_SCHEDULES + cause.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
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
                return List.of();
            }
            List<Meeting> eventDetails = new ArrayList<>();
            String dateTime = getDateTimeTimeZone(System.currentTimeMillis(), null, clientTimeZone).dateTime;
            EventCollectionPage eventCollPage = getCalendarRequestBuilder().events().buildRequest().filter("start/dateTime ge '" + dateTime + "'").top(numberOfMeetings).get();
            if (eventCollPage != null && !eventCollPage.getCurrentPage().isEmpty()) {
                for (Event event : eventCollPage.getCurrentPage()) {
                    eventDetails.add(new MeetingImpl(event));
                }
            }
            return eventDetails;
        } catch (IOException | GraphServiceException cause) {
            cause.printStackTrace();
            System.err.println("Failed to get next upcoming meetings " + cause.getMessage());
            return List.of();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Map<String, Object>> getQueryResults(String queryString) throws IOException {
        Validators.validateString(queryString, "query text");
        try {
            List<Map<String, Object>> searchHits = new ArrayList<>();
            LinkedList<SearchRequest> requests = getSearchRequests(queryString);
            SearchEntityQueryCollectionPage searchEntityQueryCollectionPage = getSearchEntityQueryCollectionPage(requests);
            if (searchEntityQueryCollectionPage != null && !searchEntityQueryCollectionPage.getCurrentPage().isEmpty()) {
                int numberOfHits = searchEntityQueryCollectionPage.getCurrentPage().get(0).hitsContainers.get(0).total;
                numberOfHits = Math.min(numberOfHits, 25);
                searchHits = getResultFromSearchResponse(numberOfHits, searchEntityQueryCollectionPage.getCurrentPage().get(0));
            }
            return searchHits;
        } catch (IOException cause) {
            throw new IOException(Constants.FAILED_TO_GET_RESULTS_FOR_GIVEN_QUERY + cause.getMessage());
        }
    }

    @Nullable
    private List<Map<String, Object>> getResultFromSearchResponse(int numberOfHits, SearchResponse searchResponse) {
        List<Map<String, Object>> searchHits = new ArrayList<>();
        if (searchResponse.hitsContainers == null || searchResponse.hitsContainers.isEmpty()) {
            throw new IllegalStateException(Constants.SEARCH_RESPONSE_IS_EMPTY);
        } else {
            final SearchHitsContainer searchHitsContainer = searchResponse.hitsContainers.get(0);
            for (int i = 0; i < numberOfHits; i++) {
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
        searchHitData.put("Event Title", event.subject);
        searchHitData.put("Scheduled Time", new DateRange(getDateInMilliSecond(event.start), getDateInMilliSecond(event.end)));
        return searchHitData;
    }

    @Override
    public List<Map<String, Object>> getFreeSlot(Long dateFrom, Long dateTo, Boolean isOrganizerOptional, List<String> requiredAttendee, List<String> optionalAttendee, String timeZone, String meetingDuration) throws DatatypeConfigurationException, IOException {
        if (dateFrom != null && dateTo != null && dateFrom > dateTo) {
            System.out.println(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
            return List.of();
        }
        try {
            List<Map<String, Object>> meetingTimeSuggestionsList = new ArrayList<>();
            LinkedList<AttendeeBase> attendeesList = MeetingDaoHelper.getRequiredAttendee(requiredAttendee, optionalAttendee);
            TimeConstraint timeConstraint = getTimeConstraints(dateFrom, dateTo, timeZone);
            if (meetingDuration == null || meetingDuration.isEmpty()) {
                meetingDuration = "60M";
            }
            Duration slotDuration = DatatypeFactory.newInstance().newDuration(Constants.PT + meetingDuration);

            MeetingTimeSuggestionsResult meetingTimeSuggestionsResult = clientProvider.getGraphServiceClient().me()
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

        } catch (IOException cause) {
            throw new IOException(Constants.FAILED_TO_GET_FREE_SLOTS + cause.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, Object> getTimeSlot(MeetingTimeSuggestion meetingTimeSuggestion) {
        Map<String, Object> meetingTimeSlot = new HashMap<>();

        if (meetingTimeSuggestion.meetingTimeSlot.start != null && meetingTimeSuggestion.meetingTimeSlot.end != null) {
            meetingTimeSlot.put("Time Slot", new DateRange(getDateInMilliSecond(meetingTimeSuggestion.meetingTimeSlot.start), getDateInMilliSecond(meetingTimeSuggestion.meetingTimeSlot.end)));
        }
        if (meetingTimeSuggestion.attendeeAvailability != null) {
            meetingTimeSlot.put("Attendee Availability", getAttendeesAvailability(meetingTimeSuggestion.attendeeAvailability));
        }
        meetingTimeSlot.put("Confidence", meetingTimeSuggestion.confidence);
        meetingTimeSlot.put("Organizer Availability", meetingTimeSuggestion.organizerAvailability);
        meetingTimeSlot.put("Suggestion Reason", meetingTimeSuggestion.suggestionReason);

        return meetingTimeSlot;
    }

    private TimeConstraint getTimeConstraints(Long dateFrom, Long dateTo, String timeZone) throws IOException, ParseException {

        TimeConstraint timeConstraint = new TimeConstraint();
        timeConstraint.activityDomain = ActivityDomain.WORK;
        LinkedList<TimeSlot> timeSlotsList = new LinkedList<>();
        TimeSlot timeSlots = new TimeSlot();
        timeSlots.start = getDateTimeTimeZone(dateFrom, timeZone, clientTimeZone);
        timeSlots.end = getDateTimeTimeZone(dateTo, timeZone, clientTimeZone);
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
        if (dateTo != null && dateFrom > dateTo) {
            System.out.println(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
            return null;
        }
        try {
            Event event = getEvent(title, location, dateFrom, dateTo, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, timeZone, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, attachments, details, true, isTeamsMeeting);
            Event newEvent = getCalendarRequestBuilder().events()
                    .buildRequest()
                    .post(event);
            for (Attachment attachment : attachments) {
                getCalendarRequestBuilder().events(newEvent.id).attachments().buildRequest().post(attachment);
            }
            if (!attachments.isEmpty()) {
                getCalendarRequestBuilder().events(newEvent.id).buildRequest().patch(newEvent);
            }
            return new MeetingImpl(newEvent);
        } catch (IOException | GraphServiceException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_SCHEDULE_MEETING + cause.getMessage());
        }
    }

    @Override
    public void forwardMeeting(String id, List<String> attendeeEmail) throws IOException {
        LinkedList<Recipient> toRecipientsList = MeetingDaoHelper.getRecipient(attendeeEmail);
        getCalendarRequestBuilder().events(id)
                .forward(EventForwardParameterSet
                        .newBuilder()
                        .withToRecipients(toRecipientsList)
                        .build())
                .buildRequest()
                .post();
    }

    @Override
    public Meeting updateMeeting(String id, String title, String details, String location, Long dateFrom, Long dateTo, String timeZone, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String recurrence, String reminder, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, Boolean isTeamsMeeting) {
        if (dateFrom != null && dateTo != null && dateFrom > dateTo) {
            throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }
        try {
            Event oldEvent = getCalendarRequestBuilder().events(id)
                    .buildRequest()
                    .get();
            SimpleDateFormat sfd = new SimpleDateFormat(Constants.YYYY_MM_DD_T_HH_MM_SS);
            Date date = sfd.parse(oldEvent.start.dateTime);
            long eventStart = date.getTime();
            date = sfd.parse(oldEvent.end.dateTime);
            long eventEnd = date.getTime();
            if (isAllDay != null && isAllDay && dateFrom == null) {
                dateFrom = eventStart;
            }
            if ((dateFrom != null && dateTo == null && dateFrom > eventEnd) || (dateFrom == null && dateTo != null && eventStart > dateTo)) {
                throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
            }
            Event event = getEvent(title, location, dateFrom, dateTo, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, timeZone, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, null, details, false, isTeamsMeeting);
            event.attendees = oldEvent.attendees;
            if (isOldEventOnline(details, oldEvent, event)) {
                Pattern regex = Pattern.compile("\\.{137}");
                String[] result = new String[0];
                if (oldEvent.body != null && oldEvent.body.content != null) {
                    result = oldEvent.body.content.split(String.valueOf(regex), 3);
                }
                event.body = MeetingDaoHelper.getItemBody(result[0] + "" + result[2], Constants.HTML);
            }
            Event newEvent = clientProvider.getGraphServiceClient().me().events(id)
                    .buildRequest()
                    .patch(event);
            return new MeetingImpl(newEvent);
        } catch (IOException | GraphServiceException | ParseException cause) {
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
            Event event = clientProvider.getGraphServiceClient().me().events(id).buildRequest().get();
            getCalendarRequestBuilder().events(id).decline(EventDeclineParameterSet.newBuilder().withSendResponse(true).build()).buildRequest().post();
            return new MeetingImpl(event);
        } catch (IOException | GraphServiceException cause) {
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
            getCalendarRequestBuilder().events(id).buildRequest().delete();
            return new MeetingImpl(event);
        } catch (IOException | ClientException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_CANCEL_EVENT + cause.getMessage());
        }
    }

    @Override
    public Meeting addAttendees(String id, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail) {
        Validators.validateString(id, Constants.MEETING_ID);
        try {
            Event event = getCalendarRequestBuilder().events(id).buildRequest().get();
            List<Attendee> attendeesList = event.attendees;
            attendeesList.addAll(MeetingDaoHelper.getAttendees(requiredAttendeeEmail, optionalAttendeeEmail));
            event.attendees = attendeesList;
            Event updatedEvent = getCalendarRequestBuilder().events(id).buildRequest().patch(event);
            return new MeetingImpl(updatedEvent);
        } catch (IOException | GraphServiceException cause) {
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
        } catch (IOException | GraphServiceException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_REMOVE_ATTENDEE + cause.getMessage());
        }
    }

    private Event getEvent(String title, String location, Long dateFrom, Long dateTo, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String timeZone, String reminder, String recurrenceValue, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, List<Attachment> attachments, String details, Boolean isCreateEvent, Boolean isTeamsMeeting) {

        Validators.validateRecurrenceValues(recurrenceValue, daysOfWeek, recurrenceType, dayOfMonth, repeatDay);
        try {
            Event event = new Event();
            CreateMeetingHelper.setEventSubject(title, event);
            CreateMeetingHelper.setEventBody(details, event);
            CreateMeetingHelper.setEventDateTime(dateFrom, dateTo, isAllDay, timeZone, isCreateEvent, clientTimeZone, event);
            CreateMeetingHelper.setEventLocation(location, event);
            CreateMeetingHelper.setEventAttendees(requiredAttendeeEmail, optionalAttendeeEmail, event);
            CreateMeetingHelper.setEventRecurrence(dateFrom, recurrenceValue, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, event);
            CreateMeetingHelper.setEventReminder(reminder, event);
            CreateMeetingHelper.setIsTeamsMeeting(isTeamsMeeting, event);
            CreateMeetingHelper.setEventTransactionId(isCreateEvent, event);
            return event;
        } catch (IOException | ParseException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_SCHEDULE_MEETING + cause.getMessage());
        }
    }

    private CalendarRequestBuilder getCalendarRequestBuilder() throws IOException {
        return clientProvider.getGraphServiceClient().me().calendar();
    }

    private long getDateInMilliSecond(DateTimeTimeZone dateToParse) {
        try {
            SimpleDateFormat sfd = new SimpleDateFormat(Constants.YYYY_MM_DD_T_HH_MM_SS);
            Date date = sfd.parse(dateToParse.dateTime);
            return date.getTime();
        } catch (ParseException parseException) {
            throw new IllegalStateException("Date :" + dateToParse.dateTime + " can't be parsed. " + parseException.getMessage(), parseException.getCause());
        }
    }
}
