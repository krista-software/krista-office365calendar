package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.executor.RemediationAction;
import app.krista.extension.executor.RemediationActions;
import app.krista.extension.executor.impl.AskAPersonAction;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.CatalogTypes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.NamedFieldFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.CreateMeetingHelper;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.MeetingDaoHelper;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.MeetingDao;
import app.krista.model.base.DateRange;
import app.krista.model.base.File;
import app.krista.model.field.NamedField;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.*;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.MeetingDaoHelper.*;

/**
 * Implementation of the {@link MeetingDao} interface for Office 365 calendar integration.
 * <p>
 * This class provides methods to interact with Microsoft Graph API for managing meetings and events,
 * including scheduling, updating, canceling, querying, and handling attendees and attachments.
 * It handles both user and system authentication contexts, manages time zones, recurrence, and
 * error handling for calendar operations.
 * </p>
 */

@Service
public class MeetingDaoImpl implements MeetingDao {

    public static final int MAX_SIZE = 25;
    private final GraphServiceClientProvider clientProvider;
    private final Office365CalendarAttributes office365CalendarAttributes;
    public static Logger LOGGER = LoggerFactory.getLogger(MeetingDaoImpl.class);

    @Inject
    public MeetingDaoImpl(GraphServiceClientProviderFactory clientProviderFactory, Office365CalendarAttributes office365CalendarAttributes) {
        this(clientProviderFactory.create(), office365CalendarAttributes);
    }

    public MeetingDaoImpl(GraphServiceClientProvider clientProvider, Office365CalendarAttributes office365CalendarAttributes) {
        this.clientProvider = clientProvider;
        this.office365CalendarAttributes = office365CalendarAttributes;
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

    /**
     * Retrieves a meeting by its event ID.
     *
     * @param id the Office 365 Calendar event ID
     * @return the Meeting object for the specified event
     * @throws IOException if there is an error retrieving the event
     */
    @Override
    public Meeting getMeetingById(String id) throws IOException {
        Validators.validateString(id, Constants.MEETING_ID);
        return new MeetingImpl(getCalendarRequestBuilder().events(id).buildRequest().get());
    }

    /**
     * Retrieves a list of upcoming meetings that are neither accepted nor declined by the current user.
     *
     * @return a list of Meeting objects for upcoming events
     */
    @Override
    public List<Meeting> getMeetings() {
        try {
            String dateTime = buildDateTimeTimeZone(System.currentTimeMillis(), null, office365CalendarAttributes.getTimeZone()).dateTime;
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

    /**
     * Retrieves a list of scheduled meeting slots for the given attendees within a date range.
     *
     * @param dateFrom  fetch meeting slots from this date
     * @param dateTo    fetch meeting slots till this date
     * @param attendees fetch meeting slots for these attendees
     * @param timeZone  time zone of the given date and time
     * @return a list of maps containing attendee and their meeting slots
     * @throws IOException if there is an error retrieving schedules
     */
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
                        .withEndTime(buildDateTimeTimeZone(dateTo, timeZone, office365CalendarAttributes.getTimeZone()))
                        .withStartTime(buildDateTimeTimeZone(dateFrom, timeZone, office365CalendarAttributes.getTimeZone()))
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

    /**
     * Retrieves a list of upcoming meetings, limited to the specified number.
     *
     * @param numberOfMeetings the number of meetings to retrieve
     * @return a list of Meeting objects for upcoming events
     */
    @Override
    public List<Meeting> getMeetings(int numberOfMeetings) {
        try {
            if (numberOfMeetings < 1) {
                throw new IllegalArgumentException(Constants.NUMBER_OF_MEETINGS_SHOULD_BE_GREATER_THAN_0);
            }
            List<Meeting> eventDetails = new ArrayList<>();
            String dateTime = buildDateTimeTimeZone(System.currentTimeMillis(), null, office365CalendarAttributes.getTimeZone()).dateTime;
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

    /**
     * Searches for events matching the given query string.
     *
     * @param queryString the search query
     * @return a list of maps containing event ID, title, and scheduled time
     */
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

            searchHits = getResultFromSearchResponse(searchEntityQueryCollectionPage.getCurrentPage().getFirst());
        }
        return searchHits;
    }

    /**
     * Retrieves events for a given user within a specified date range.
     *
     * @param userEmail the user's email address
     * @param fromDate  start date (epoch millis)
     * @param toDate    end date (epoch millis)
     * @return a list of Meeting objects for the user
     */
    @Override
    public List<Meeting> getEventsForUser(String userEmail, Long fromDate, Long toDate) {
        try {
            List<Option> options = getTimeRangeQueryOptions(fromDate, toDate);

            EventCollectionPage eventCollection = clientProvider.getGraphServiceClientForSystem()
                    .users(userEmail)
                    .calendarView()
                    .buildRequest(options)
                    .select("id,subject,start,end,location,body,attendees,originalStartTimeZone,organizer")
                    .get();

            List<Meeting> eventDetails = new ArrayList<>();
            while (eventCollection != null) {
                for (Event event : eventCollection.getCurrentPage()) {
                    eventDetails.add(new MeetingImpl(event));
                }
                eventCollection = eventCollection.getNextPage() != null ? eventCollection.getNextPage().buildRequest().get() : null;
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
    public static List<Option> getTimeRangeQueryOptions(Long startTimestamp, Long endTimestamp) {
        List<Option> options = new ArrayList<>();
        if (startTimestamp == null || endTimestamp == null) {
            throw new IllegalArgumentException("Timestamps cannot be null");
        }
        ZonedDateTime startZoned = Instant.ofEpochMilli(startTimestamp).atZone(ZoneId.of(Constants.UTC));
        ZonedDateTime endZoned = Instant.ofEpochMilli(endTimestamp).atZone(ZoneId.of(Constants.UTC));
        String startTime = startZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String endTime = endZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        options.add(new QueryOption(Constants.START_DATE_TIME, startTime));
        options.add(new QueryOption(Constants.END_DATE_TIME, endTime));
        options.add(new QueryOption(Constants.TOP, "1000"));
        options.add(new QueryOption(Constants.ORDER_BY, "start/dateTime asc"));
        return options;
    }

    @NotNull
    private List<Map<String, Object>> getResultFromSearchResponse(SearchResponse searchResponse) {
        List<Map<String, Object>> searchHits = new ArrayList<>();
        if (searchResponse.hitsContainers == null || searchResponse.hitsContainers.isEmpty()) {
            throw new IllegalStateException(Constants.SEARCH_RESPONSE_IS_EMPTY);
        } else {
            final SearchHitsContainer searchHitsContainer = searchResponse.hitsContainers.getFirst();
            if (searchHitsContainer.hits != null) {
                int numberOfHits = searchHitsContainer.hits.size();
                numberOfHits = Math.min(numberOfHits, MAX_SIZE);

                for (int i = 0; i < numberOfHits; i++) {
                    searchHits.add(getSearchHitContent(searchHitsContainer.hits.get(i)));
                }
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

    /**
     * Retrieves a list of free meeting slots based on the provided criteria.
     *
     * @param dateFrom            date for which free slots need to be fetched from
     * @param dateTo              date for which free slots need to be fetched till
     * @param isOrganizerOptional if the organizer is required for the meeting or optional
     * @param requiredAttendee    list of required attendee email addresses
     * @param optionalAttendee    list of optional attendee email addresses
     * @param timeZone            time zone to get free slots time in required time zones
     * @param meetingDuration     duration of the free slot required
     * @return a list of maps with meeting time suggestions
     * @throws DatatypeConfigurationException if there is an error in dateFrom or dateTo
     */
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
        timeSlots.start = buildDateTimeTimeZone(dateFrom, timeZone, office365CalendarAttributes.getTimeZone());
        timeSlots.end = buildDateTimeTimeZone(dateTo, timeZone, office365CalendarAttributes.getTimeZone());
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

    /**
     * Schedules a new meeting with the provided details and attachments.
     *
     * @return the Meeting object for the scheduled event
     */
    @Override
    public Meeting addMeeting(String title, String location, Long dateFrom, Long dateTo, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String timeZone, String reminder, String recurrence, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, List<Attachment> attachments, String details, Boolean isTeamsMeeting) {
        Validators.validateString(dateFrom.toString(), "date");
        if ((dateTo != null) && (dateFrom > dateTo)) {
            throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }
        try {
            String selectedTimeZone = (timeZone != null && !timeZone.isEmpty()) ? timeZone : office365CalendarAttributes.getTimeZone();
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

    /**
     * Forwards the meeting invitation to the given attendees.
     *
     * @param id            event ID of the Office 365 calendar meeting
     * @param attendeeEmail list of emails for forwarding the meeting invitation
     * @throws IOException if there is an error in the input
     */
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

    /**
     * Updates the meeting with the given details.
     *
     * @return the updated Meeting object
     */
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
            String selectedTimeZone = (timeZone != null && !timeZone.isEmpty()) ? timeZone : office365CalendarAttributes.getTimeZone();
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
                event.body = MeetingDaoHelper.getItemBody(result[0] + result[2], Constants.HTML);
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

    /**
     * Accepts the meeting invitation for the given event ID.
     *
     * @param id the Office 365 calendar meeting ID
     * @return the updated Meeting object
     */
    @Override
    public Meeting acceptMeetingInvitation(String id) {
        Validators.validateString(id, Constants.MEETING_ID);
        try {
            getCalendarRequestBuilder().events(id).accept(EventAcceptParameterSet.newBuilder().withSendResponse(isSendResponse(id)).build()).buildRequest().post();
            return getMeetingById(id);
        } catch (IOException | GraphServiceException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_ACCEPT_INVITATION + cause.getMessage());
        }
    }

    /**
     * Declines the meeting invitation for the given event ID.
     *
     * @param id the Office 365 calendar meeting ID
     * @return the updated Meeting object
     */
    @Override
    public Meeting declineMeetingInvitation(String id) {
        Validators.validateString(id, Constants.MEETING_ID);
        try {
            Event event = clientProvider.getUserRequestBuilder(false, null).events(id).buildRequest().get();
            getCalendarRequestBuilder().events(id).decline(EventDeclineParameterSet.newBuilder().withSendResponse(isSendResponse(id)).build()).buildRequest().post();
            return new MeetingImpl(event);
        } catch (GraphServiceException | IOException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_DECLINE_INVITATION + cause.getMessage());
        }
    }

    /**
     * Tentatively accepts the meeting invitation for the given event ID.
     *
     * @param id the Office 365 calendar meeting ID
     * @return the updated Meeting object
     */
    @Override
    public Meeting tentativeMeetingInvitation(String id) {
        Validators.validateString(id, Constants.MEETING_ID);
        try {
            getCalendarRequestBuilder().events(id).tentativelyAccept(EventTentativelyAcceptParameterSet.newBuilder().withSendResponse(isSendResponse(id)).build()).buildRequest().post();
            return getMeetingById(id);
        } catch (IOException | GraphServiceException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_GET_TENTATIVE_INVITATIONS + cause.getMessage());
        }
    }

    /**
     * Cancels the meeting for the given event ID.
     *
     * @param id the Office 365 calendar meeting ID
     * @return the cancelled Meeting object
     */
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

    /**
     * Adds attendees to the specified meeting.
     *
     * @param id                    Office 365 calendar meeting ID
     * @param requiredAttendeeEmail list of required attendee emails
     * @param optionalAttendeeEmail list of optional attendee emails
     * @return the updated Meeting object
     */
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

    /**
     * Removes attendees from the specified meeting.
     *
     * @param id        Office 365 calendar meeting ID
     * @param attendees list of attendee email addresses to remove
     * @return the updated Meeting object
     */
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
            CreateMeetingHelper.setEventDateTime(dateFrom, dateTo, isAllDay, timeZone, isCreateEvent, office365CalendarAttributes.getTimeZone(), event);
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
            customFormat.setTimeZone(TimeZone.getTimeZone(office365CalendarAttributes.getTimeZone()));
            Date newDate = customFormat.parse(customFormat.format(date));
            return newDate.getTime();
        } catch (ParseException parseException) {
            throw new IllegalStateException("Date :" + dateToParse.dateTime + " can't be parsed. " + parseException.getMessage(), parseException.getCause());
        }
    }

    private boolean isSendResponse(String eventId) throws IOException {
        Event event = getCalendarRequestBuilder().events(eventId).buildRequest().get();
        boolean sendResponse = true;
        if (event != null) {
            sendResponse = Boolean.TRUE.equals(event.responseRequested);
        }
        return sendResponse;
    }

    public ExtensionResponse getAttachmentsForGivenUserWithErrorHandling(String userEmail, String eventID, ValidationOrchestrator validationOrchestrator, CatalogTypes catalogTypes, ErrorHandlingStateManager errorHandlingStateManager) {
        String message = " Details entered are not valid. " + Constants.FAILED_TO_GET_EVENT_ATTACHMENTS;
        ExtensionResponse.Error error = new ExtensionResponse.Error(message, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "MeetingDaoImpl -> getAttachmentsForGivenUserWithValidation");
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(Map.of(Validator.ValidationResource.EMAIL, Validators.getNullOrEmptyDefault(userEmail), Validator.ValidationResource.EVENT_ID, Validators.getNullOrEmptyDefault(eventID)));
            if (validationResults.isEmpty()) {
                List<FileAttachment> fileAttachments = getAttachmentsForGivenUser(userEmail, eventID);
                List<File> attachments = catalogTypes.toKristaFilList(fileAttachments);
                return new ExtensionResponseBuilder().success(Map.of(Constants.ATTACHMENTS, attachments)).build();
            } else {
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(Constants.VALIDATION_RESULTS, validationResults));
                List<NamedField> fields = List.of(NamedFieldFactory.createSwitchField(Constants.RE_ENTER));
                AskAPersonAction askAPersonAction = AskAPersonAction.create(Constants.INVALID_INPUT, RemediationAction.RecipientType.ACTIVE_USER, fields);
                RemediationActions remediationActions = new RemediationActions(List.of(askAPersonAction), Constants.CONFIRM_RE_ENTER_GET_ATTACHMENTS_FOR_GIVEN_USER);
                return new ExtensionResponseBuilder().failure(error, remediationActions, Map.of(Constants.STATE_ID, stateId, Constants.EMAIL, userEmail, Constants.EVENT_ID, eventID)).build();
            }
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (Exception cause) {
            LOGGER.error(Constants.FAILED_TO_GET_EVENT_ATTACHMENTS + " : {}", String.valueOf(cause));
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    public ExtensionResponse getAttachmentsForAuthenticatedUserWithErrorHandling(String eventID, ValidationOrchestrator validationOrchestrator, CatalogTypes catalogTypes, ErrorHandlingStateManager errorHandlingStateManager) {
        String message = " Details entered are not valid. " + Constants.FAILED_TO_GET_EVENT_ATTACHMENTS;
        ExtensionResponse.Error error = new ExtensionResponse.Error(message, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "MeetingDaoImpl -> getAttachmentsForAuthenticatedUserWithValidation");
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(Map.of(Validator.ValidationResource.EVENT_ID, Validators.getNullOrEmptyDefault(eventID)));
            if (validationResults.isEmpty()) {
                List<FileAttachment> fileAttachments = getAttachmentsForAuthenticatedUser(eventID);
                List<File> attachments = catalogTypes.toKristaFilList(fileAttachments);
                return new ExtensionResponseBuilder().success(Map.of(Constants.ATTACHMENTS, attachments)).build();
            } else {
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(Constants.VALIDATION_RESULTS, validationResults));
                List<NamedField> fields = List.of(NamedFieldFactory.createSwitchField(Constants.RE_ENTER));
                AskAPersonAction askAPersonAction = AskAPersonAction.create(Constants.INVALID_INPUT, RemediationAction.RecipientType.ACTIVE_USER, fields);
                RemediationActions remediationActions = new RemediationActions(List.of(askAPersonAction), Constants.CONFIRM_RE_ENTER_GET_ATTACHMENTS);
                return new ExtensionResponseBuilder().failure(error, remediationActions, Map.of(Constants.STATE_ID, stateId, Constants.EVENT_ID, eventID)).build();
            }
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (Exception cause) {
            LOGGER.error(Constants.FAILED_TO_GET_EVENT_ATTACHMENTS + "{}", cause);
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Retrieves all file attachments for a specific event for a given user.
     *
     * @param userEmail the user's email address
     * @param eventId   the event ID
     * @return a list of FileAttachment objects
     */
    @Override
    public List<FileAttachment> getAttachmentsForGivenUser(String userEmail, String eventId) {
        return fetchAttachments(userEmail, eventId, false);
    }

    /**
     * Retrieves all file attachments for a specific event for the authenticated user.
     *
     * @param eventId the event ID
     * @return a list of FileAttachment objects
     */
    @Override
    public List<FileAttachment> getAttachmentsForAuthenticatedUser(String eventId) {
        try {
            return fetchAttachments(null, eventId, true);
        } catch (ClientException cause) {
            LOGGER.error(Constants.FAILED_TO_GET_EVENT_ATTACHMENTS + "{}", String.valueOf(cause));
            return Collections.emptyList();
        }
    }

    List<FileAttachment> fetchAttachments(@Nullable String userEmail, String eventId, boolean isAuthenticatedUser) {
        try {
            return getFileAttachments(userEmail, eventId, isAuthenticatedUser);
        } catch (ClientException cause) {
            LOGGER.error(Constants.FAILED_TO_GET_EVENT_ATTACHMENTS + "{}", String.valueOf(cause));
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves all {@link FileAttachment} objects associated with a specific calendar event.
     * <p>
     * This method fetches all file attachments for the given event, handling pagination if necessary.
     * It determines whether to use the authenticated user's context or system context based on the
     * {@code isAuthenticatedUser} flag.
     * </p>
     *
     * @param userEmail           the email address of the user whose event attachments are to be fetched;
     *                            may be {@code null} if using the authenticated user's context
     * @param eventId             the unique identifier of the calendar event
     * @param isAuthenticatedUser {@code true} to use the authenticated user's context; {@code false} to use system context
     * @return a list of {@link FileAttachment} objects associated with the specified event; never {@code null}
     */
    private List<FileAttachment> getFileAttachments(@Nullable String userEmail, String eventId, boolean isAuthenticatedUser) {
        List<FileAttachment> fileAttachments = new ArrayList<>();
        AttachmentCollectionPage attachmentCollection = buildAttachmentCollectionRequest(userEmail, eventId, isAuthenticatedUser);

        while (attachmentCollection != null) {
            for (Attachment attachment : attachmentCollection.getCurrentPage()) {
                if (attachment instanceof FileAttachment) {
                    fileAttachments.add((FileAttachment) attachment);
                }
            }
            attachmentCollection = attachmentCollection.getNextPage() != null ? attachmentCollection.getNextPage().buildRequest().get() : null;
        }
        return fileAttachments;
    }

    /**
     * Builds and executes a request to retrieve the first page of attachments for a given event.
     * <p>
     * The method chooses between the authenticated user's context and the system context based on
     * the {@code isAuthenticatedUser} flag. For system context, the {@code userEmail} must be provided.
     * </p>
     *
     * @param userEmail           the email address of the user whose event attachments are to be fetched;
     *                            required if {@code isAuthenticatedUser} is {@code false}
     * @param eventId             the unique identifier of the calendar event
     * @param isAuthenticatedUser {@code true} to use the authenticated user's context; {@code false} to use system context
     * @return the first {@link AttachmentCollectionPage} of attachments for the specified event
     */
    private AttachmentCollectionPage buildAttachmentCollectionRequest(String userEmail, String eventId, boolean isAuthenticatedUser) {
        if (isAuthenticatedUser) {
            return clientProvider.getGraphServiceClient().me().events(eventId).attachments().buildRequest().get();
        } else {
            return clientProvider.getGraphServiceClientForSystem().users(userEmail).events(eventId).attachments().buildRequest().get();
        }
    }

    /**
     * Retrieves a list of unaccepted meetings for the current user.
     * This method fetches calendar events that haven't been accepted yet
     * and are scheduled within the next 30 days.
     *
     * @return List of Meeting objects representing unaccepted meetings
     */
    public List<Meeting> getUnAcceptedMeetings() {
        try {
            // Use ZonedDateTime for consistent date/time handling
            ZoneId userTimeZone = ZoneId.of(office365CalendarAttributes.getTimeZone());
            ZonedDateTime now = ZonedDateTime.now(userTimeZone);
            ZonedDateTime endDate = now.plusDays(30);

            // Format dates in ISO format for Graph API
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            String currentTime = now.format(formatter);
            String endDateTime = endDate.format(formatter);

            // Define query parameters
            List<QueryOption> queryOptions = Arrays.asList(
                    new QueryOption("startDateTime", currentTime),
                    new QueryOption("endDateTime", endDateTime),
                    new QueryOption("$filter", "responseStatus/response ne 'accepted'"),
                    new QueryOption("$expand", "instances"),
                    new QueryOption("$orderby", "start/dateTime"),
                    new QueryOption("$top", "50")
            );

            // Fetch events with pagination handling
            List<Meeting> eventDetails = new ArrayList<>();
            EventCollectionPage eventCollection = getCalendarRequestBuilder()
                    .calendarView()
                    .buildRequest(queryOptions)
                    .get();

            if (eventCollection != null) {
                User currentUser = clientProvider.getUserRequestBuilder(false, null).buildRequest().get();
                processEventPages(eventCollection, currentUser, eventDetails);
            }

            return eventDetails;
        } catch (GraphServiceException | IOException cause) {
            throw new IllegalStateException(Constants.FAILED_TO_GET_ALL_UNACCEPTED_EVENTS + cause.getMessage());
        }
    }

    /**
     * Helper method to process event pages recursively with proper pagination handling
     *
     * @param eventCollection The current page of events
     * @param currentUser The current user
     * @param eventDetails The list to populate with meeting details
     */
    private void processEventPages(EventCollectionPage eventCollection, User currentUser, List<Meeting> eventDetails) {
        if (eventCollection == null) {
            return;
        }

        // Process current page
        for (Event event : eventCollection.getCurrentPage()) {
            if (currentUser != null &&
                MeetingDaoHelper.isUpcomingEvent(event) &&
                MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(event, currentUser.mail)) {
                eventDetails.add(new MeetingImpl(event));
            }
        }

        // Process next page if available
        EventCollectionRequestBuilder nextPageRequest = eventCollection.getNextPage();
        if (nextPageRequest != null) {
            try {
                EventCollectionPage nextPage = nextPageRequest.buildRequest().get();
                processEventPages(nextPage, currentUser, eventDetails);
            } catch (GraphServiceException e) {
                throw new IllegalStateException("Failed to retrieve next page of events: " + e.getMessage(), e);
            }
        }
    }
}
