/*
 * Office365Calendar Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import java.io.InputStream;
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
    public MeetingDaoImpl(GraphServiceClientProviderFactory clientProviderFactory) {
        this(clientProviderFactory.create(), clientProviderFactory.create().getOffice365CalendarAttributes());
        LOGGER.info("MeetingDaoImpl initialized successfully with factory");
    }

    public MeetingDaoImpl(GraphServiceClientProvider clientProvider, Office365CalendarAttributes office365CalendarAttributes) {
        this.clientProvider = clientProvider;
        this.office365CalendarAttributes = office365CalendarAttributes;
    }

    /**
     * Helper method to get effective timezone, defaulting to UTC if null or empty
     */
    private String getEffectiveTimeZone() {
        String timeZone = office365CalendarAttributes.getTimeZone();
        return (timeZone != null && !timeZone.trim().isEmpty()) ? timeZone : "UTC";
    }

    @NotNull
    private static LinkedList<SearchRequest> getSearchRequests(String queryString) {
        LOGGER.debug("→ getSearchRequests: Creating search requests for query: '{}'", queryString);

        LinkedList<SearchRequest> requestsList = new LinkedList<>();
        SearchRequest requests = new SearchRequest();
        LinkedList<EntityType> entityTypesList = new LinkedList<>();
        entityTypesList.add(EntityType.EVENT);
        requests.entityTypes = entityTypesList;
        LOGGER.debug("→ Set entity type to EVENT");

        SearchQuery query = new SearchQuery();
        query.queryString = queryString;
        requests.query = query;
        LOGGER.debug("→ Set query string: '{}'", queryString);

        requestsList.add(requests);
        LOGGER.debug("→ getSearchRequests: Created {} search requests", requestsList.size());

        return requestsList;
    }

    private static boolean isOldEventOnline(String details, Event oldEvent, Event event) {
        boolean oldIsOnline = oldEvent.isOnlineMeeting != null && oldEvent.isOnlineMeeting;
        boolean newIsOnline = event.isOnlineMeeting != null && !event.isOnlineMeeting;
        boolean detailsEmpty = details == null || details.isBlank();
        boolean result = oldIsOnline && newIsOnline && detailsEmpty;

        LOGGER.debug("→ isOldEventOnline: oldIsOnline={}, newIsOnline={}, detailsEmpty={}, result={}",
                oldIsOnline, newIsOnline, detailsEmpty, result);

        return result;
    }

    private static void setDateTimeTimeZone(String selectedTimeZone, Event newEvent) {
        LOGGER.debug("→ setDateTimeTimeZone: Setting timezone to '{}' for event", selectedTimeZone);

        String startTimeZone = Objects.requireNonNull(Objects.requireNonNull(newEvent).start).timeZone;
        if ("UTC".equals(startTimeZone)) {
            LOGGER.debug("→ Converting start time from UTC to {}", selectedTimeZone);
            convertDateTimeUTCToTimeZone(newEvent, selectedTimeZone, true);
        } else {
            LOGGER.debug("→ Converting start time from {} to UTC", startTimeZone);
            convertDateTimeTimeZoneToUTC(newEvent, startTimeZone, true);
        }

        String endTimeZone = Objects.requireNonNull(Objects.requireNonNull(newEvent).end).timeZone;
        if ("UTC".equals(endTimeZone)) {
            LOGGER.debug("→ Converting end time from UTC to {}", selectedTimeZone);
            convertDateTimeUTCToTimeZone(newEvent, selectedTimeZone, false);
        } else {
            LOGGER.debug("→ Converting end time from {} to UTC", endTimeZone);
            convertDateTimeTimeZoneToUTC(newEvent, endTimeZone, false);
        }

        LOGGER.debug("→ setDateTimeTimeZone: Timezone conversion completed");
    }

    /**
     * Retrieves a meeting by its event ID.
     *
     * @param id the Office 365 Calendar event ID
     * @return the Meeting object for the specified event
     * @throws IOException if there is an error retrieving the event
     */
    @Override
    public Meeting getMeetingById(String id) {
        LOGGER.info("→ getMeetingById: Retrieving meeting with ID: {}", id);
        Validators.validateString(id, Constants.MEETING_ID);
        LOGGER.debug("→ Meeting ID validation passed");

        try {
            Event event = getCalendarRequestBuilder().events(id).buildRequest().get();
            LOGGER.debug("→ Retrieved event from Graph API: {}", event.subject);
            LOGGER.info("→ getMeetingById: Successfully retrieved meeting: {}", event.subject);
            return new MeetingImpl(event);
        } catch (GraphServiceException e) {
            String errorMessage = "";
            if (e.getError() != null && e.getError().error != null) {
                errorMessage = e.getError().error.message;
            }
            LOGGER.info("→ getMeetingById: Event with ID {} not found or error occurred ({}), returning null", id, errorMessage);
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retrieves a meeting by its ID from the Office 365 calendar using delegated access.
     *
     * @param id            the Office 365 Calendar event ID
     * @param delegateEmail email of the user to delegate to, can be null or blank for current user
     * @return the Meeting object for the specified event
     * @throws IOException if there is an error retrieving the event
     */
    @Override
    public Meeting getMeetingByIdDelegated(String id, String delegateEmail) throws IOException {
        LOGGER.info("→ getMeetingByIdDelegated: Retrieving meeting with ID: {} for delegate: {}", id, delegateEmail);
        Validators.validateString(id, Constants.MEETING_ID);
        LOGGER.debug("→ Meeting ID validation passed");

        try {
            Event event = getCalendarRequestBuilderDelegated(delegateEmail).events(id).buildRequest().get();
            LOGGER.debug("→ Retrieved event from Graph API: {}", event.subject);
            LOGGER.info("→ getMeetingByIdDelegated: Successfully retrieved meeting: {}", event.subject);
            return new MeetingImpl(event);
        } catch (GraphServiceException e) {
            String errorMessage = "";
            if (e.getError() != null && e.getError().error != null) {
                errorMessage = e.getError().error.message;
            }
            LOGGER.info("→ getMeetingByIdDelegated: Event with ID {} not found or error occurred for delegate {} ({}), returning null", id, delegateEmail, errorMessage);
            return null;
        }
    }

    /**
     * Retrieves a list of upcoming meetings that are neither accepted nor declined by the current user.
     *
     * @return a list of Meeting objects for upcoming events
     */
    @Override
    public List<Meeting> getMeetings() {
        LOGGER.info("→ getMeetings: Retrieving all unaccepted meetings");
        try {
            String dateTime = buildDateTimeTimeZone(System.currentTimeMillis(), null, getEffectiveTimeZone()).dateTime;
            LOGGER.debug("→ Built filter dateTime: {}", dateTime);

            EventCollectionPage eventCollection = getCalendarRequestBuilder().events().buildRequest().filter("start/dateTime ge '" + dateTime + "'").get();
            LOGGER.debug("→ Retrieved event collection from Graph API");

            List<Meeting> eventDetails = new ArrayList<>();
            if (eventCollection != null) {
                LOGGER.debug("→ Processing {} events from collection", eventCollection.getCurrentPage().size());
                for (Event event : eventCollection.getCurrentPage()) {
                    User currentUser = clientProvider.getUserRequestBuilder(false, null).buildRequest().get();
                    LOGGER.debug("→ Retrieved current user: {}", currentUser != null ? currentUser.mail : "null");

                    if (currentUser != null && MeetingDaoHelper.isUpcomingEvent(event) && MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(event, currentUser.mail)) {
                        eventDetails.add(new MeetingImpl(event));
                        LOGGER.debug("→ Added unaccepted event: {}", event.subject);
                    }
                }
            } else {
                LOGGER.debug("→ No event collection returned from Graph API");
            }

            LOGGER.info("→ getMeetings: Successfully retrieved {} unaccepted meetings", eventDetails.size());
            return eventDetails;
        } catch (GraphServiceException | IOException cause) {
            LOGGER.error("→ getMeetings: Failed to retrieve meetings - {}", cause.getMessage(), cause);
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
        LOGGER.info("→ getSchedules: Retrieving schedules from {} to {} for {} attendees in timezone {}", dateFrom, dateTo, attendees.size(), timeZone);

        Validators.validateString(dateFrom.toString(), "date");
        Validators.validateString(dateTo.toString(), "date");
        Validators.validateStringsList(attendees, "attendees");
        LOGGER.debug("→ Input validation passed");

        if (dateFrom > dateTo) {
            LOGGER.error("→ getSchedules: Invalid date range - dateFrom {} > dateTo {}", dateFrom, dateTo);
            throw new IllegalArgumentException(Constants.FAILED_TO_GET_SCHEDULES + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }

        List<Map<String, Object>> meetingsList = new ArrayList<>();
        LinkedList<String> schedulesList = new LinkedList<>(attendees);
        LOGGER.debug("→ Created schedules list with {} attendees", schedulesList.size());

        CalendarGetScheduleCollectionPage page = getCalendarRequestBuilder()
                .getSchedule(CalendarGetScheduleParameterSet
                        .newBuilder()
                        .withSchedules(schedulesList)
                        .withEndTime(buildDateTimeTimeZone(dateTo, timeZone, getEffectiveTimeZone()))
                        .withStartTime(buildDateTimeTimeZone(dateFrom, timeZone, getEffectiveTimeZone()))
                        .build())
                .buildRequest()
                .post();
        LOGGER.debug("→ Retrieved first page of schedules from Graph API");

        while (page != null && !page.getCurrentPage().isEmpty()) {
            LOGGER.debug("→ Processing page with {} schedule items", page.getCurrentPage().size());
            for (ScheduleInformation scheduleInformation : page.getCurrentPage()) {
                if (scheduleInformation.scheduleId != null && !scheduleInformation.scheduleId.isEmpty()) {
                    meetingsList.add(getSchedulesInformation(scheduleInformation));
                    LOGGER.debug("→ Added schedule information for: {}", scheduleInformation.scheduleId);
                }
            }
            CalendarGetScheduleCollectionRequestBuilder nextPage = page.getNextPage();
            if (nextPage != null) {
                page = nextPage.buildRequest().post();
                LOGGER.debug("→ Retrieved next page of schedules");
            } else {
                LOGGER.debug("→ No more pages available");
                break;
            }
        }

        LOGGER.info("→ getSchedules: Successfully retrieved {} schedule entries", meetingsList.size());
        return meetingsList;
    }

    private Map<String, Object> getSchedulesInformation(ScheduleInformation scheduleInformation) {
        LOGGER.debug("→ getSchedulesInformation: Processing schedule information for: {}", scheduleInformation.scheduleId);

        Map<String, Object> scheduleInformationObject = new HashMap<>();

        if (scheduleInformation.scheduleItems != null && !scheduleInformation.scheduleItems.isEmpty()) {
            LOGGER.debug("→ Found {} schedule items for attendee: {}", scheduleInformation.scheduleItems.size(), scheduleInformation.scheduleId);
            scheduleInformationObject.put(Constants.ATTENDEE, scheduleInformation.scheduleId);
            scheduleInformationObject.put(Constants.SCHEDULES, getScheduleItems(scheduleInformation.scheduleItems));
            LOGGER.debug("→ Created schedule information object for attendee");
        } else {
            LOGGER.debug("→ No schedule items found for attendee: {}", scheduleInformation.scheduleId);
        }

        return scheduleInformationObject;
    }

    private List<Map<String, Object>> getScheduleItems(List<ScheduleItem> scheduleItems) {
        LOGGER.debug("→ getScheduleItems: Processing {} schedule items", scheduleItems != null ? scheduleItems.size() : 0);

        List<Map<String, Object>> scheduleItemsList = new ArrayList<>();
        if (scheduleItems != null) {
            scheduleItems.forEach(schedule -> {
                Map<String, Object> scheduleMap = new HashMap<>();
                if (schedule.start != null && schedule.end != null) {
                    long startTime = getDateInMilliSecond(schedule.start);
                    long endTime = getDateInMilliSecond(schedule.end);
                    scheduleMap.put("Scheduled Time", new DateRange(startTime, endTime));
                    scheduleMap.put("Status", schedule.status);
                    LOGGER.debug("→ Added schedule item: {} to {} with status: {}", startTime, endTime, schedule.status);
                }
                scheduleItemsList.add(scheduleMap);
            });
        } else {
            LOGGER.debug("→ No schedule items to process");
        }

        LOGGER.debug("→ getScheduleItems: Created {} schedule item maps", scheduleItemsList.size());
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
        LOGGER.info("→ getMeetings: Retrieving {} upcoming meetings", numberOfMeetings);
        try {
            if (numberOfMeetings < 1) {
                LOGGER.error("→ getMeetings: Invalid numberOfMeetings: {}", numberOfMeetings);
                throw new IllegalArgumentException(Constants.NUMBER_OF_MEETINGS_SHOULD_BE_GREATER_THAN_0);
            }
            LOGGER.debug("→ Number of meetings validation passed");

            List<Meeting> eventDetails = new ArrayList<>();
            String dateTime = buildDateTimeTimeZone(System.currentTimeMillis(), null, getEffectiveTimeZone()).dateTime;
            LOGGER.debug("→ Built filter dateTime: {}", dateTime);

            EventCollectionPage eventCollPage = getCalendarRequestBuilder().events().buildRequest().filter("start/dateTime ge '" + dateTime + "'").top(numberOfMeetings).get();
            LOGGER.debug("→ Retrieved event collection page from Graph API");

            if (eventCollPage != null && !eventCollPage.getCurrentPage().isEmpty()) {
                LOGGER.debug("→ Processing {} events from collection", eventCollPage.getCurrentPage().size());
                for (Event event : eventCollPage.getCurrentPage()) {
                    eventDetails.add(new MeetingImpl(event));
                    LOGGER.debug("→ Added meeting: {}", event.subject);
                }
            } else {
                LOGGER.debug("→ No events found in collection");
            }

            LOGGER.info("→ getMeetings: Successfully retrieved {} meetings", eventDetails.size());
            return eventDetails;
        } catch (GraphServiceException | IOException cause) {
            LOGGER.error("→ getMeetings: Failed to retrieve meetings - {}", cause.getMessage(), cause);
            throw new IllegalStateException(Constants.FAILED_TO_GET_UPCOMING_MEETINGS + cause.getMessage());
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
        LOGGER.info("→ getQueryResults: Searching for events with query: '{}'", queryString);

        Validators.validateString(queryString, "query text");
        LOGGER.debug("→ Query string validation passed");

        List<Map<String, Object>> searchHits = new ArrayList<>();
        LinkedList<SearchRequest> requests = getSearchRequests(queryString);
        LOGGER.debug("→ Created search requests for query");

        SearchEntityQueryCollectionPage searchEntityQueryCollectionPage;
        try {
            searchEntityQueryCollectionPage = getSearchEntityQueryCollectionPage(requests);
            LOGGER.debug("→ Retrieved search results from Graph API");
        } catch (IOException e) {
            LOGGER.error("→ getQueryResults: Failed to execute search query - {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        if (searchEntityQueryCollectionPage != null && !searchEntityQueryCollectionPage.getCurrentPage().isEmpty()) {
            LOGGER.debug("→ Processing search results page with {} items", searchEntityQueryCollectionPage.getCurrentPage().size());
            searchHits = getResultFromSearchResponse(searchEntityQueryCollectionPage.getCurrentPage().getFirst());
        } else {
            LOGGER.debug("→ No search results found");
        }

        LOGGER.info("→ getQueryResults: Successfully found {} search results", searchHits.size());
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
        LOGGER.info("→ getEventsForUser: Retrieving events for user {} from {} to {}", userEmail, fromDate, toDate);

        try {
            List<Option> options = getTimeRangeQueryOptions(fromDate, toDate);
            LOGGER.debug("→ Created time range query options");

            EventCollectionPage eventCollection = clientProvider.getGraphServiceClientForSystem()
                    .users(userEmail)
                    .calendarView()
                    .buildRequest(options)
                    .select("id,subject,start,end,location,body,attendees,originalStartTimeZone,organizer")
                    .get();
            LOGGER.debug("→ Retrieved event collection from Graph API for user");

            List<Meeting> eventDetails = new ArrayList<>();
            while (eventCollection != null) {
                LOGGER.debug("→ Processing event page with {} events", eventCollection.getCurrentPage().size());
                for (Event event : eventCollection.getCurrentPage()) {
                    eventDetails.add(new MeetingImpl(event));
                    LOGGER.debug("→ Added event: {}", event.subject);
                }
                eventCollection = eventCollection.getNextPage() != null ? eventCollection.getNextPage().buildRequest().get() : null;
                if (eventCollection != null) {
                    LOGGER.debug("→ Retrieved next page of events");
                }
            }

            LOGGER.info("→ getEventsForUser: Successfully retrieved {} events for user {}", eventDetails.size(), userEmail);
            return eventDetails;
        } catch (GraphServiceException cause) {
            String errorMessage = "";
            if (cause.getError() != null && cause.getError().error != null) {
                errorMessage = cause.getError().error.message;
            }
            LOGGER.error("→ getEventsForUser: Failed to retrieve events for user {} - {}", userEmail, errorMessage, cause);
            throw new IllegalStateException(Constants.FAILED_TO_GET_USER_EVENTS + errorMessage, cause);
        }
    }

    @NotNull
    public static List<Option> getTimeRangeQueryOptions(Long startTimestamp, Long endTimestamp) {
        LOGGER.debug("→ getTimeRangeQueryOptions: Creating query options for range {} to {}", startTimestamp, endTimestamp);

        List<Option> options = new ArrayList<>();
        if (startTimestamp == null || endTimestamp == null) {
            LOGGER.error("→ getTimeRangeQueryOptions: Null timestamps provided - start: {}, end: {}", startTimestamp, endTimestamp);
            throw new IllegalArgumentException("Timestamps cannot be null");
        }

        ZonedDateTime startZoned = Instant.ofEpochMilli(startTimestamp).atZone(ZoneId.of(Constants.UTC));
        ZonedDateTime endZoned = Instant.ofEpochMilli(endTimestamp).atZone(ZoneId.of(Constants.UTC));
        String startTime = startZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String endTime = endZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        LOGGER.debug("→ Formatted timestamps: start={}, end={}", startTime, endTime);

        options.add(new QueryOption(Constants.START_DATE_TIME, startTime));
        options.add(new QueryOption(Constants.END_DATE_TIME, endTime));
        options.add(new QueryOption(Constants.TOP, "1000"));
        options.add(new QueryOption(Constants.ORDER_BY, "start/dateTime asc"));
        LOGGER.debug("→ getTimeRangeQueryOptions: Created {} query options", options.size());

        return options;
    }

    @NotNull
    private List<Map<String, Object>> getResultFromSearchResponse(SearchResponse searchResponse) {
        LOGGER.debug("→ getResultFromSearchResponse: Processing search response");

        List<Map<String, Object>> searchHits = new ArrayList<>();
        if (searchResponse.hitsContainers == null || searchResponse.hitsContainers.isEmpty()) {
            LOGGER.error("→ getResultFromSearchResponse: Search response is empty or null");
            throw new IllegalStateException(Constants.SEARCH_RESPONSE_IS_EMPTY);
        } else {
            final SearchHitsContainer searchHitsContainer = searchResponse.hitsContainers.getFirst();
            if (searchHitsContainer.hits != null) {
                int numberOfHits = searchHitsContainer.hits.size();
                int originalHits = numberOfHits;
                numberOfHits = Math.min(numberOfHits, MAX_SIZE);
                LOGGER.debug("→ Processing {} hits (limited from {} to max {})", numberOfHits, originalHits, MAX_SIZE);

                for (int i = 0; i < numberOfHits; i++) {
                    searchHits.add(getSearchHitContent(searchHitsContainer.hits.get(i)));
                    LOGGER.debug("→ Processed search hit {}/{}", i + 1, numberOfHits);
                }
            } else {
                LOGGER.debug("→ No hits found in search hits container");
            }
        }

        LOGGER.debug("→ getResultFromSearchResponse: Returning {} search hits", searchHits.size());
        return searchHits;
    }

    @Nullable
    private SearchEntityQueryCollectionPage getSearchEntityQueryCollectionPage(LinkedList<SearchRequest> requestsList) throws IOException {
        LOGGER.debug("→ getSearchEntityQueryCollectionPage: Executing search query with {} requests", requestsList.size());

        SearchEntityQueryCollectionPage result = clientProvider.getGraphServiceClient()
                .search()
                .query(SearchEntityQueryParameterSet
                        .newBuilder()
                        .withRequests(requestsList)
                        .build())
                .buildRequest()
                .post();

        LOGGER.debug("→ getSearchEntityQueryCollectionPage: Search query executed, result: {}", result != null ? "success" : "null");
        return result;
    }

    private Map<String, Object> getSearchHitContent(SearchHit searchHit) {
        LOGGER.debug("→ getSearchHitContent: Processing search hit with ID: {}", searchHit.hitId);

        Map<String, Object> searchHitData = new HashMap<>();
        searchHitData.put("Event ID", searchHit.hitId);
        LOGGER.debug("→ Added Event ID to search hit data");

        Event event = (Event) searchHit.resource;
        assert event != null;
        searchHitData.put("Event Title", event.subject);
        LOGGER.debug("→ Added Event Title: {}", event.subject);

        assert event.start != null;
        assert event.end != null;
        long startTime = getDateInMilliSecond(event.start);
        long endTime = getDateInMilliSecond(event.end);
        searchHitData.put("Scheduled Time", new DateRange(startTime, endTime));
        LOGGER.debug("→ Added Scheduled Time: {} to {}", startTime, endTime);

        LOGGER.debug("→ getSearchHitContent: Completed processing search hit");
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
        LOGGER.debug("→ getTimeSlot: Processing meeting time suggestion with confidence: {}", meetingTimeSuggestion.confidence);

        Map<String, Object> meetingTimeSlot = new HashMap<>();

        assert meetingTimeSuggestion.meetingTimeSlot != null;
        if (meetingTimeSuggestion.meetingTimeSlot.start != null && meetingTimeSuggestion.meetingTimeSlot.end != null) {
            long startTime = getDateInMilliSecond(meetingTimeSuggestion.meetingTimeSlot.start);
            long endTime = getDateInMilliSecond(meetingTimeSuggestion.meetingTimeSlot.end);
            meetingTimeSlot.put("Time Slot", new DateRange(startTime, endTime));
            LOGGER.debug("→ Added time slot: {} to {}", startTime, endTime);
        }

        if (meetingTimeSuggestion.attendeeAvailability != null) {
            meetingTimeSlot.put("Attendee Availability", getAttendeesAvailability(meetingTimeSuggestion.attendeeAvailability));
            LOGGER.debug("→ Added attendee availability for {} attendees", meetingTimeSuggestion.attendeeAvailability.size());
        }

        meetingTimeSlot.put("Confidence", meetingTimeSuggestion.confidence == null ? null : meetingTimeSuggestion.confidence / 100);
        meetingTimeSlot.put("Organizer Availability", meetingTimeSuggestion.organizerAvailability);
        meetingTimeSlot.put("Suggestion Reason", meetingTimeSuggestion.suggestionReason);
        LOGGER.debug("→ Added confidence, organizer availability, and suggestion reason");

        LOGGER.debug("→ getTimeSlot: Completed processing time slot");
        return meetingTimeSlot;
    }

    private TimeConstraint getTimeConstraints(Long dateFrom, Long dateTo, String timeZone) {
        LOGGER.debug("→ getTimeConstraints: Creating time constraints from {} to {} in timezone: {}", dateFrom, dateTo, timeZone);

        TimeConstraint timeConstraint = new TimeConstraint();
        timeConstraint.activityDomain = ActivityDomain.WORK;
        LOGGER.debug("→ Set activity domain to WORK");

        LinkedList<TimeSlot> timeSlotsList = new LinkedList<>();
        TimeSlot timeSlots = new TimeSlot();
        timeSlots.start = buildDateTimeTimeZone(dateFrom, timeZone, getEffectiveTimeZone());
        timeSlots.end = buildDateTimeTimeZone(dateTo, timeZone, getEffectiveTimeZone());
        LOGGER.debug("→ Built start and end date time zones");

        timeSlotsList.add(timeSlots);
        timeConstraint.timeSlots = timeSlotsList;
        LOGGER.debug("→ Added time slot to constraint");

        LOGGER.debug("→ getTimeConstraints: Created time constraint with {} time slots", timeSlotsList.size());
        return timeConstraint;
    }

    private List<Map<String, Object>> getAttendeesAvailability(List<AttendeeAvailability> attendeeAvailability) {
        LOGGER.debug("→ getAttendeesAvailability: Processing availability for {} attendees",
                attendeeAvailability != null ? attendeeAvailability.size() : 0);

        List<Map<String, Object>> attendeesAvailability = new ArrayList<>();
        if (attendeeAvailability != null) {
            attendeeAvailability.forEach(attendee -> {
                Map<String, Object> attendeeMap = new HashMap<>();
                if (attendee.attendee != null && attendee.attendee.emailAddress != null) {
                    attendeeMap.put(Constants.ATTENDEE, attendee.attendee.emailAddress.address);
                    attendeeMap.put(Constants.AVAILABILITY, attendee.availability);
                    LOGGER.debug("→ Added availability for attendee: {} - {}",
                            attendee.attendee.emailAddress.address, attendee.availability);
                }
                attendeesAvailability.add(attendeeMap);
            });
        } else {
            LOGGER.debug("→ No attendee availability data to process");
        }

        LOGGER.debug("→ getAttendeesAvailability: Created {} attendee availability maps", attendeesAvailability.size());
        return attendeesAvailability;
    }

    /**
     * Schedules a new meeting with the provided details and attachments.
     *
     * @return the Meeting object for the scheduled event
     */
    @Override
    public Meeting addMeeting(String title, String location, Long dateFrom, Long dateTo, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String timeZone, String reminder, String recurrence, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, List<Attachment> attachments, String details, Boolean isTeamsMeeting) {
        LOGGER.info("→ addMeeting: Scheduling meeting '{}' from {} to {} in timezone {}", title, dateFrom, dateTo, timeZone);

        Validators.validateString(dateFrom.toString(), "date");
        LOGGER.debug("→ Date validation passed");

        if ((dateTo != null) && (dateFrom > dateTo)) {
            LOGGER.error("→ addMeeting: Invalid date range - dateFrom {} > dateTo {}", dateFrom, dateTo);
            throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }

        try {
            String selectedTimeZone = (timeZone != null && !timeZone.isEmpty()) ? timeZone : getEffectiveTimeZone();
            LOGGER.debug("→ Selected timezone: {}", selectedTimeZone);

            Event event = getEvent(title, location, dateFrom, dateTo, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, timeZone, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, details, true, isTeamsMeeting);
            LOGGER.debug("→ Created event object with {} required attendees and {} optional attendees",
                    requiredAttendeeEmail != null ? requiredAttendeeEmail.size() : 0,
                    optionalAttendeeEmail != null ? optionalAttendeeEmail.size() : 0);

            Event newEvent = getCalendarRequestBuilder().events()
                    .buildRequest()
                    .post(event);
            LOGGER.debug("→ Posted event to Graph API, received ID: {}", newEvent.id);

            setDateTimeTimeZone(selectedTimeZone, newEvent);
            LOGGER.debug("→ Set timezone for event");

            assert newEvent.id != null;
            for (Attachment attachment : attachments) {
                getCalendarRequestBuilder().events(newEvent.id).attachments().buildRequest().post(attachment);
                LOGGER.debug("→ Added attachment: {}", attachment.name);
            }


            LOGGER.info("→ addMeeting: Successfully scheduled meeting '{}' with ID: {}", title, newEvent.id);
            return new MeetingImpl(newEvent);
        } catch (GraphServiceException | IOException cause) {
            LOGGER.error("→ addMeeting: Failed to schedule meeting '{}' - {}", title, cause.getMessage(), cause);
            throw new IllegalStateException(Constants.FAILED_TO_SCHEDULE_MEETING + cause.getMessage());
        }

    }

    // Overload: schedule in organizer's delegated calendar
    @Override
    public Meeting addMeeting(String organizerEmail, String title, String location, Long dateFrom, Long dateTo, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String timeZone, String reminder, String recurrence, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, List<Attachment> attachments, String details, Boolean isTeamsMeeting) {
        LOGGER.info("→ addMeeting(delegated): Scheduling meeting for organizer: {}", organizerEmail);
        Validators.validateString(dateFrom.toString(), "date");
        if ((dateTo != null) && (dateFrom > dateTo)) {
            throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }
        try {
            String selectedTimeZone = (timeZone != null && !timeZone.isEmpty()) ? timeZone : getEffectiveTimeZone();
            Event event = getEvent(title, location, dateFrom, dateTo, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, timeZone, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, details, true, isTeamsMeeting);
            Event newEvent = getCalendarRequestBuilderDelegated(organizerEmail)
                    .events()
                    .buildRequest()
                    .post(event);
            setDateTimeTimeZone(selectedTimeZone, newEvent);
            for (Attachment attachment : attachments) {
                getCalendarRequestBuilder().events(newEvent.id).attachments().buildRequest().post(attachment);
            }
            if (!attachments.isEmpty()) {
                getCalendarRequestBuilder().events(newEvent.id).buildRequest().patch(newEvent);
            }
            return new MeetingImpl(newEvent);
        } catch (GraphServiceException | IOException cause) {
            LOGGER.error("→ addMeeting(delegated): Failed - {}", cause.getMessage(), cause);
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
        LOGGER.info("→ forwardMeeting: Forwarding meeting {} to {} attendees", id, attendeeEmail.size());

        List<Recipient> toRecipientsList = MeetingDaoHelper.getRecipient(attendeeEmail);
        LOGGER.debug("→ Created recipient list with {} recipients", toRecipientsList.size());

        getCalendarRequestBuilder().events(id)
                .forward(EventForwardParameterSet
                        .newBuilder()
                        .withToRecipients(toRecipientsList)
                        .build())
                .buildRequest()
                .post();
        LOGGER.debug("→ Meeting forwarded via Graph API");

        LOGGER.info("→ forwardMeeting: Successfully forwarded meeting {} to {} attendees", id, attendeeEmail.size());
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
        LOGGER.info("→ updateMeeting: Updating meeting with ID: {} - title: '{}'", id, title);

        if (dateFrom != null && dateTo != null && dateFrom > dateTo) {
            LOGGER.error("→ updateMeeting: Invalid date range - dateFrom {} > dateTo {}", dateFrom, dateTo);
            throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }

        try {
            String selectedTimeZone = (timeZone != null && !timeZone.isEmpty()) ? timeZone : getEffectiveTimeZone();
            LOGGER.debug("→ Selected timezone: {}", selectedTimeZone);

            Event oldEvent = getCalendarRequestBuilder().events(id)
                    .buildRequest()
                    .get();
            LOGGER.debug("→ Retrieved existing event: {}", oldEvent.subject);

            SimpleDateFormat sfd = new SimpleDateFormat(Constants.YYYY_MM_DD_T_HH_MM_SS);
            Date date = sfd.parse(Objects.requireNonNull(Objects.requireNonNull(oldEvent).start).dateTime);
            long eventStart = convertDateTimeToEpochInTimeZone(convertDateToFormattedString(date), selectedTimeZone);
            LOGGER.debug("→ Parsed existing event start time: {}", eventStart);

            date = sfd.parse(Objects.requireNonNull(Objects.requireNonNull(oldEvent).end).dateTime);
            long eventEnd = convertDateTimeToEpochInTimeZone(convertDateToFormattedString(date), selectedTimeZone);
            LOGGER.debug("→ Parsed existing event end time: {}", eventEnd);

            if (isAllDay != null && isAllDay && dateFrom == null) {
                dateFrom = eventStart;
                LOGGER.debug("→ Set dateFrom to existing start time for all-day event: {}", dateFrom);
            }

            if ((dateFrom != null && dateTo == null && dateFrom > eventEnd) || (dateFrom == null && dateTo != null && eventStart > dateTo)) {
                LOGGER.error("→ updateMeeting: Invalid date range after processing - dateFrom: {}, dateTo: {}, eventStart: {}, eventEnd: {}",
                        dateFrom, dateTo, eventStart, eventEnd);
                throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
            }

            Event event = getEvent(title, location, dateFrom, dateTo, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, timeZone, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, details, false, isTeamsMeeting);
            LOGGER.debug("→ Created updated event object");

            if (event.attendees == null || event.attendees.isEmpty()) {
                event.attendees = oldEvent.attendees;
                LOGGER.debug("→ Preserved existing attendees from old event");
            }


            if (isOldEventOnline(details, oldEvent, event)) {
                Pattern regex = Pattern.compile("\\.{137}");
                String[] result = new String[3];
                if (oldEvent.body != null && oldEvent.body.content != null) {
                    result = oldEvent.body.content.split(String.valueOf(regex), 3);
                }
                event.body = MeetingDaoHelper.getItemBody(result[0] + result[2], Constants.HTML);
                LOGGER.debug("→ Processed online meeting body content");
            }

            event.changeKey = oldEvent.changeKey;
            event.originalStartTimeZone = Objects.requireNonNull(oldEvent.start).timeZone;
            event.originalEndTimeZone = Objects.requireNonNull(oldEvent.end).timeZone;
            LOGGER.debug("→ Set change key and original timezones");

            Event newEvent = getCalendarRequestBuilder().events(id)
                    .buildRequest()
                    .patch(event);
            LOGGER.debug("→ Patched event via Graph API");

            setDateTimeTimeZone(selectedTimeZone, newEvent);
            LOGGER.debug("→ Set timezone for updated event");

            LOGGER.info("→ updateMeeting: Successfully updated meeting with ID: {}", id);
            return new MeetingImpl(newEvent);
        } catch (GraphServiceException | ParseException | IOException | NullPointerException cause) {
            LOGGER.error("→ updateMeeting: Failed to update meeting with ID: {} - {}", id, cause.getMessage(), cause);
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
        LOGGER.info("→ acceptMeetingInvitation: Accepting meeting invitation for ID: {}", id);

        Validators.validateString(id, Constants.MEETING_ID);
        LOGGER.debug("→ Meeting ID validation passed");

        try {
            boolean sendResponse = isSendResponse(id);
            LOGGER.debug("→ Send response setting: {}", sendResponse);

            getCalendarRequestBuilder().events(id).accept(EventAcceptParameterSet.newBuilder().withSendResponse(sendResponse).build()).buildRequest().post();
            LOGGER.debug("→ Accepted meeting invitation via Graph API");

            Meeting meeting = getMeetingById(id);
            LOGGER.info("→ acceptMeetingInvitation: Successfully accepted meeting invitation for: {}", id);
            return meeting;
        } catch (IOException | GraphServiceException cause) {
            LOGGER.error("→ acceptMeetingInvitation: Failed to accept invitation for ID: {} - {}", id, cause.getMessage(), cause);
            throw new IllegalStateException(Constants.FAILED_TO_ACCEPT_INVITATION + cause.getMessage());
        }
    }

    /**
     * Declines the meeting invitation for the given event ID.
     *
     * @param id the Office 365 calendar meeting ID
     * @return the updated Meeting object
     */

    // Overload: cancel meeting using delegate's email (delegated calendar)
    @Override
    public Meeting cancelMeeting(String id, String delegateEmail) {
        LOGGER.info("→ cancelMeeting(delegated): Cancelling meeting {} for delegate {}", id, delegateEmail);
        Validators.validateString(id, Constants.MEETING_ID);
        try {
            CalendarRequestBuilder calendarRequestBuilderDelegated = getCalendarRequestBuilderDelegated(delegateEmail);
            Event event = calendarRequestBuilderDelegated.events(id).buildRequest().get();
            calendarRequestBuilderDelegated.events(id).buildRequest().delete();
            LOGGER.info("→ cancelMeeting(delegated): Successfully cancelled meeting for delegate {}", delegateEmail);
            return new MeetingImpl(event);
        } catch (GraphServiceException e) {
            String errorMessage = "";
            if (e.getError() != null && e.getError().error != null) {
                errorMessage = e.getError().error.message;
            }
            LOGGER.error("→ cancelMeeting(delegated): Event with ID {} not found or error occurred for delegate {} ({}), returning null", id, delegateEmail, errorMessage);
            return null;
        } catch (ClientException | IOException cause) {
            LOGGER.error("→ cancelMeeting(delegated): Error occurred - {}, returning null", cause.getMessage());
            return null;
        }
    }

    // Overload: update in organizer's delegated calendar
    @Override
    public Meeting updateMeeting(String organizerEmail, String id, String title, String details, String location, Long dateFrom, Long dateTo,
                                 String timeZone, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail,
                                 Boolean isAllDay, String recurrence, String reminder, Double recurrenceInterval,
                                 List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex,
                                 String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, Boolean isTeamsMeeting) {
        LOGGER.info("→ updateMeeting(delegated): Updating meeting {} for organizer {}", id, organizerEmail);
        if (dateFrom != null && dateTo != null && dateFrom > dateTo) {
            throw new IllegalArgumentException(Constants.FAILED_TO_SCHEDULE_MEETING + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
        }
        try {
            String selectedTimeZone = (timeZone != null && !timeZone.isEmpty()) ? timeZone : getEffectiveTimeZone();
            CalendarRequestBuilder calendarRequestBuilderDelegated = getCalendarRequestBuilderDelegated(organizerEmail);
            Event oldEvent = calendarRequestBuilderDelegated.events(id)
                    .buildRequest()
                    .get();
            ;

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
            event.changeKey = oldEvent.changeKey;
            event.originalStartTimeZone = Objects.requireNonNull(oldEvent.start).timeZone;
            event.originalEndTimeZone = Objects.requireNonNull(oldEvent.end).timeZone;

            Event newEvent = calendarRequestBuilderDelegated.events(id)
                    .buildRequest()
                    .patch(event);
            setDateTimeTimeZone(selectedTimeZone, newEvent);
            return new MeetingImpl(newEvent);
        } catch (GraphServiceException | ParseException | IOException | NullPointerException cause) {
            LOGGER.error("→ updateMeeting(delegated): Failed - {}", cause.getMessage(), cause);
            throw new IllegalStateException(Constants.FAILED_TO_UPDATE_EVENT + cause.getMessage());
        }
    }

    @Override
    public Meeting declineMeetingInvitation(String id) {
        LOGGER.info("→ declineMeetingInvitation: Declining meeting invitation for ID: {}", id);

        Validators.validateString(id, Constants.MEETING_ID);
        LOGGER.debug("→ Meeting ID validation passed");

        try {
            Event event = clientProvider.getUserRequestBuilder(false, null).events(id).buildRequest().get();
            LOGGER.debug("→ Retrieved event to decline: {}", event.subject);

            boolean sendResponse = isSendResponse(id);
            LOGGER.debug("→ Send response setting: {}", sendResponse);

            getCalendarRequestBuilder().events(id).decline(EventDeclineParameterSet.newBuilder().withSendResponse(sendResponse).build()).buildRequest().post();
            LOGGER.debug("→ Declined meeting invitation via Graph API");

            LOGGER.info("→ declineMeetingInvitation: Successfully declined meeting invitation for: {}", event.subject);
            return new MeetingImpl(event);
        } catch (GraphServiceException | IOException cause) {
            LOGGER.error("→ declineMeetingInvitation: Failed to decline invitation for ID: {} - {}", id, cause.getMessage(), cause);
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
        LOGGER.info("→ tentativeMeetingInvitation: Tentatively accepting meeting invitation for ID: {}", id);

        Validators.validateString(id, Constants.MEETING_ID);
        LOGGER.debug("→ Meeting ID validation passed");

        try {
            boolean sendResponse = isSendResponse(id);
            LOGGER.debug("→ Send response setting: {}", sendResponse);

            getCalendarRequestBuilder().events(id).tentativelyAccept(EventTentativelyAcceptParameterSet.newBuilder().withSendResponse(sendResponse).build()).buildRequest().post();
            LOGGER.debug("→ Tentatively accepted meeting invitation via Graph API");

            Meeting meeting = getMeetingById(id);
            LOGGER.info("→ tentativeMeetingInvitation: Successfully tentatively accepted meeting invitation for: {}", id);
            return meeting;
        } catch (IOException | GraphServiceException cause) {
            LOGGER.error("→ tentativeMeetingInvitation: Failed to tentatively accept invitation for ID: {} - {}", id, cause.getMessage(), cause);
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
        LOGGER.info("→ cancelMeeting: Cancelling meeting with ID: {}", id);

        Validators.validateString(id, Constants.MEETING_ID);
        LOGGER.debug("→ Meeting ID validation passed");

        try {
            Event event = getCalendarRequestBuilder().events(id).buildRequest().get();
            LOGGER.debug("→ Retrieved event to cancel: {}", event.subject);

            clientProvider.getUserRequestBuilder(false, null).events(id).buildRequest().delete();
            LOGGER.debug("→ Deleted event via Graph API");

            LOGGER.info("→ cancelMeeting: Successfully cancelled meeting: {}", event.subject);
            return new MeetingImpl(event);
        } catch (GraphServiceException e) {
            String errorMessage = "";
            if (e.getError() != null && e.getError().error != null) {
                errorMessage = e.getError().error.message;
            }
            LOGGER.error("→ cancelMeeting: Event with ID {} not found or error occurred ({}), returning null", id, errorMessage);
            return null;
        } catch (ClientException | IOException cause) {
            LOGGER.error("→ cancelMeeting: Error occurred - {}, returning null", cause.getMessage());
            return null;
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
        LOGGER.info("→ addAttendees: Adding attendees to meeting {} - {} required, {} optional",
                id, requiredAttendeeEmail != null ? requiredAttendeeEmail.size() : 0,
                optionalAttendeeEmail != null ? optionalAttendeeEmail.size() : 0);

        Validators.validateString(id, Constants.MEETING_ID);
        LOGGER.debug("→ Meeting ID validation passed");

        try {
            Event event = getCalendarRequestBuilder().events(id).buildRequest().get();
            LOGGER.debug("→ Retrieved existing event: {}", event.subject);

            List<Attendee> attendeesList = Objects.requireNonNull(event).attendees;
            assert attendeesList != null;
            int originalCount = attendeesList.size();
            LOGGER.debug("→ Original attendees count: {}", originalCount);

            attendeesList.addAll(MeetingDaoHelper.getAttendees(requiredAttendeeEmail, optionalAttendeeEmail));
            event.attendees = attendeesList;
            LOGGER.debug("→ Updated attendees count: {}", attendeesList.size());

            Event updatedEvent = getCalendarRequestBuilder().events(id).buildRequest().patch(event);
            LOGGER.debug("→ Patched event with new attendees via Graph API");

            LOGGER.info("→ addAttendees: Successfully added {} attendees to meeting {}",
                    attendeesList.size() - originalCount, id);
            return new MeetingImpl(updatedEvent);
        } catch (GraphServiceException | IOException cause) {
            LOGGER.error("→ addAttendees: Failed to add attendees to meeting {} - {}", id, cause.getMessage(), cause);
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
        LOGGER.info("→ removeAttendees: Removing {} attendees from meeting {}", attendees.size(), id);

        Validators.validateString(id, Constants.MEETING_ID);
        LOGGER.debug("→ Meeting ID validation passed");

        try {
            Event event = getCalendarRequestBuilder().events(id).buildRequest().get();
            LOGGER.debug("→ Retrieved existing event: {}", event.subject);

            List<Attendee> attendeesList = Objects.requireNonNull(event).attendees;
            int originalCount = attendeesList.size();
            LOGGER.debug("→ Original attendees count: {}", originalCount);

            MeetingDaoHelper.updateAttendeesList(Objects.requireNonNull(attendeesList), attendees);
            event.attendees = attendeesList;
            LOGGER.debug("→ Updated attendees count: {}", attendeesList.size());

            Event updatedEvent = getCalendarRequestBuilder().events(id).buildRequest().patch(event);
            LOGGER.debug("→ Patched event with removed attendees via Graph API");

            LOGGER.info("→ removeAttendees: Successfully removed {} attendees from meeting {}",
                    originalCount - attendeesList.size(), id);
            return new MeetingImpl(updatedEvent);
        } catch (GraphServiceException | IOException cause) {
            LOGGER.error("→ removeAttendees: Failed to remove attendees from meeting {} - {}", id, cause.getMessage(), cause);
            throw new IllegalStateException(Constants.FAILED_TO_REMOVE_ATTENDEE + cause.getMessage());
        }
    }

    private Event getEvent(String title, String location, Long dateFrom, Long dateTo, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String timeZone, String reminder, String recurrenceValue, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, String details, Boolean isCreateEvent, Boolean isTeamsMeeting) {
        LOGGER.debug("→ getEvent: Creating event object - title: '{}', isCreateEvent: {}, isTeamsMeeting: {}", title, isCreateEvent, isTeamsMeeting);

        try {
            Event event = new Event();
            LOGGER.debug("→ Created new Event object");

            CreateMeetingHelper.setEventSubject(title, event, isCreateEvent);
            LOGGER.debug("→ Set event subject");

            CreateMeetingHelper.setEventBody(details, event);
            LOGGER.debug("→ Set event body");

            CreateMeetingHelper.setEventDateTime(dateFrom, dateTo, isAllDay, timeZone, isCreateEvent, getEffectiveTimeZone(), event);
            LOGGER.debug("→ Set event date/time");

            CreateMeetingHelper.setEventLocation(location, event);
            LOGGER.debug("→ Set event location");

            CreateMeetingHelper.setEventAttendees(requiredAttendeeEmail, optionalAttendeeEmail, event);
            LOGGER.debug("→ Set event attendees");

            if (recurrenceValue != null && !recurrenceValue.isBlank()) {
                Validators.validateRecurrenceValues(recurrenceValue, daysOfWeek, recurrenceType, dayOfMonth, repeatDay);
                LOGGER.debug("→ Recurrence validation passed");
                CreateMeetingHelper.setEventRecurrence(dateFrom, recurrenceValue, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, event);
                LOGGER.debug("→ Set event recurrence");
            }

            CreateMeetingHelper.setEventReminder(reminder, event);
            LOGGER.debug("→ Set event reminder");

            CreateMeetingHelper.setIsTeamsMeeting(isTeamsMeeting, event);
            LOGGER.debug("→ Set Teams meeting setting");

            CreateMeetingHelper.setEventTransactionId(isCreateEvent, event);
            LOGGER.debug("→ Set event transaction ID");

            LOGGER.debug("→ getEvent: Successfully created event object");
            return event;
        } catch (ParseException cause) {
            LOGGER.error("→ getEvent: Failed to create event - {}", cause.getMessage(), cause);
            throw new IllegalStateException(Constants.FAILED_TO_SCHEDULE_MEETING + cause.getMessage());
        }
    }

    private CalendarRequestBuilder getCalendarRequestBuilder() throws IOException {
        LOGGER.debug("→ getCalendarRequestBuilder: Creating calendar request builder");
        CalendarRequestBuilder builder = clientProvider.getUserRequestBuilder(false, null).calendar();
        LOGGER.debug("→ getCalendarRequestBuilder: Calendar request builder created successfully");
        return builder;
    }

    /**
     * Creates a calendar request builder for delegated operations.
     * If delegateEmail is provided and not blank, creates a delegated request builder for that user.
     * Otherwise, falls back to the standard calendar request builder.
     *
     * @param delegateEmail the email of the user to delegate to, can be null or blank
     * @return CalendarRequestBuilder for the appropriate user context
     * @throws IOException if there's an error creating the request builder
     */
    private CalendarRequestBuilder getCalendarRequestBuilderDelegated(String delegateEmail) throws IOException {
        LOGGER.debug("→ getCalendarRequestBuilderDelegated: Creating delegated calendar request builder for: {}", delegateEmail);

        // Use the 3-parameter getUserRequestBuilder method to match reference implementation pattern
        CalendarRequestBuilder builder = clientProvider.getUserRequestBuilder(false, null, delegateEmail).calendar();
        LOGGER.debug("→ getCalendarRequestBuilderDelegated: Calendar request builder created successfully for delegate: {}", delegateEmail);
        return builder;
    }

    private long getDateInMilliSecond(DateTimeTimeZone dateToParse) {
        LOGGER.debug("→ getDateInMilliSecond: Parsing date: {} in timezone: {}", dateToParse.dateTime, dateToParse.timeZone);

        try {
            SimpleDateFormat sfd = new SimpleDateFormat(Constants.YYYY_MM_DD_T_HH_MM_SS);
            sfd.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sfd.parse(dateToParse.dateTime);
            LOGGER.debug("→ Parsed date in UTC: {}", date);

            SimpleDateFormat customFormat = new SimpleDateFormat(Constants.YYYY_MM_DD_T_HH_MM_SS);
            String userTimeZone = office365CalendarAttributes.getTimeZone();
            String effectiveTimeZone = (userTimeZone != null && !userTimeZone.trim().isEmpty()) ? userTimeZone : "UTC";
            customFormat.setTimeZone(TimeZone.getTimeZone(effectiveTimeZone));
            Date newDate = customFormat.parse(customFormat.format(date));
            long result = newDate.getTime();
            LOGGER.debug("→ getDateInMilliSecond: Converted to milliseconds: {}", result);

            return result;
        } catch (ParseException parseException) {
            LOGGER.error("→ getDateInMilliSecond: Failed to parse date: {} - {}", dateToParse.dateTime, parseException.getMessage(), parseException);
            throw new IllegalStateException("Unable to parse the date '" + dateToParse.dateTime + "'. Please ensure the date is in a valid format. " + parseException.getMessage(), parseException.getCause());
        }
    }

    private boolean isSendResponse(String eventId) throws IOException {
        LOGGER.debug("→ isSendResponse: Checking send response setting for event: {}", eventId);

        Event event = getCalendarRequestBuilder().events(eventId).buildRequest().get();
        LOGGER.debug("→ Retrieved event for response setting check");

        boolean sendResponse = true;
        if (event != null) {
            sendResponse = Boolean.TRUE.equals(event.responseRequested);
            LOGGER.debug("→ Event response requested: {}", event.responseRequested);
        }

        LOGGER.debug("→ isSendResponse: Returning sendResponse: {}", sendResponse);
        return sendResponse;
    }


    public ExtensionResponse getAttachmentsForAuthenticatedUserWithErrorHandling(String eventID, ValidationOrchestrator validationOrchestrator, CatalogTypes catalogTypes, ErrorHandlingStateManager errorHandlingStateManager) {
        String message = "The information provided is not valid. " + Constants.FAILED_TO_GET_EVENT_ATTACHMENTS;
        ExtensionResponse.Error error = new ExtensionResponse.Error(message, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "MeetingDaoImpl -> getAttachmentsForAuthenticatedUserWithValidation");
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.EVENT_ID, Validators.getNullOrEmptyDefault(eventID)),
                    app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.MeetingsArea.class,
                    "getAttachmentsForAuthenticatedUser");
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
        LOGGER.info("→ getAttachmentsForAuthenticatedUser: Retrieving attachments for event ID: {}", eventId);
        try {
            List<FileAttachment> attachments = fetchAttachments(null, eventId, true);
            LOGGER.info("→ getAttachmentsForAuthenticatedUser: Successfully retrieved {} attachments", attachments.size());
            return attachments;
        } catch (ClientException cause) {
            LOGGER.error("→ getAttachmentsForAuthenticatedUser: Failed to retrieve attachments for event ID: {} - {}", eventId, cause.getMessage(), cause);
            return Collections.emptyList();
        }
    }

    List<FileAttachment> fetchAttachments(@Nullable String userEmail, String eventId, boolean isAuthenticatedUser) {
        LOGGER.debug("→ fetchAttachments: Fetching attachments for event ID: {}, userEmail: {}, isAuthenticatedUser: {}",
                eventId, userEmail, isAuthenticatedUser);
        try {
            List<FileAttachment> attachments = getFileAttachments(userEmail, eventId, isAuthenticatedUser);
            LOGGER.debug("→ fetchAttachments: Successfully fetched {} attachments", attachments.size());
            return attachments;
        } catch (ClientException cause) {
            LOGGER.error("→ fetchAttachments: Failed to fetch attachments for event ID: {} - {}", eventId, cause.getMessage(), cause);
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
        LOGGER.debug("→ getFileAttachments: Processing attachments for event ID: {}", eventId);

        List<FileAttachment> fileAttachments = new ArrayList<>();
        AttachmentCollectionPage attachmentCollection = buildAttachmentCollectionRequest(userEmail, eventId, isAuthenticatedUser);
        LOGGER.debug("→ Built attachment collection request");

        while (attachmentCollection != null) {
            LOGGER.debug("→ Processing attachment page with {} items", attachmentCollection.getCurrentPage().size());
            for (Attachment attachment : attachmentCollection.getCurrentPage()) {
                if (attachment instanceof FileAttachment) {
                    fileAttachments.add((FileAttachment) attachment);
                    LOGGER.debug("→ Added file attachment: {}", attachment.name);
                }
            }
            attachmentCollection = attachmentCollection.getNextPage() != null ? attachmentCollection.getNextPage().buildRequest().get() : null;
            if (attachmentCollection != null) {
                LOGGER.debug("→ Retrieved next page of attachments");
            }
        }

        LOGGER.debug("→ getFileAttachments: Completed processing, found {} file attachments", fileAttachments.size());
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
        LOGGER.debug("→ buildAttachmentCollectionRequest: Building request for event: {}, userEmail: {}, isAuthenticatedUser: {}",
                eventId, userEmail, isAuthenticatedUser);

        if (isAuthenticatedUser) {
            LOGGER.debug("→ Using authenticated user context");
            return clientProvider.getGraphServiceClient().me().events(eventId).attachments().buildRequest().get();
        } else {
            LOGGER.debug("→ Using system context for user: {}", userEmail);
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
        LOGGER.info("→ getUnAcceptedMeetings: Retrieving unaccepted meetings for next 30 days");

        try {
            // Use ZonedDateTime for consistent date/time handling
            String userTimeZoneStr = office365CalendarAttributes.getTimeZone();
            String effectiveTimeZone = (userTimeZoneStr != null && !userTimeZoneStr.trim().isEmpty()) ? userTimeZoneStr : "UTC";
            ZoneId userTimeZone = ZoneId.of(effectiveTimeZone);
            ZonedDateTime now = ZonedDateTime.now(userTimeZone);
            ZonedDateTime endDate = now.plusDays(30);
            LOGGER.debug("→ Date range: {} to {} in timezone: {}", now, endDate, userTimeZone);

            // Format dates in ISO format for Graph API
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            String currentTime = now.format(formatter);
            String endDateTime = endDate.format(formatter);
            LOGGER.debug("→ Formatted date range: {} to {}", currentTime, endDateTime);

            // Define query parameters
            List<QueryOption> queryOptions = Arrays.asList(
                    new QueryOption("startDateTime", currentTime),
                    new QueryOption("endDateTime", endDateTime),
                    new QueryOption("$filter", "responseStatus/response ne 'accepted'"),
                    new QueryOption("$expand", "instances"),
                    new QueryOption("$orderby", "start/dateTime"),
                    new QueryOption("$top", "50")
            );
            LOGGER.debug("→ Created {} query options", queryOptions.size());

            // Fetch events with pagination handling
            List<Meeting> eventDetails = new ArrayList<>();
            EventCollectionPage eventCollection = getCalendarRequestBuilder()
                    .calendarView()
                    .buildRequest(queryOptions)
                    .get();
            LOGGER.debug("→ Retrieved event collection from Graph API");

            if (eventCollection != null) {
                User currentUser = clientProvider.getUserRequestBuilder(false, null).buildRequest().get();
                LOGGER.debug("→ Retrieved current user: {}", currentUser != null ? currentUser.mail : "null");
                processEventPages(eventCollection, currentUser, eventDetails);
            } else {
                LOGGER.debug("→ No event collection returned");
            }

            LOGGER.info("→ getUnAcceptedMeetings: Successfully retrieved {} unaccepted meetings", eventDetails.size());
            return eventDetails;
        } catch (GraphServiceException | IOException cause) {
            LOGGER.error("→ getUnAcceptedMeetings: Failed to retrieve unaccepted meetings - {}", cause.getMessage(), cause);
            throw new IllegalStateException(Constants.FAILED_TO_GET_ALL_UNACCEPTED_EVENTS + cause.getMessage());
        }
    }

    /**
     * Helper method to process event pages recursively with proper pagination handling
     *
     * @param eventCollection The current page of events
     * @param currentUser     The current user
     * @param eventDetails    The list to populate with meeting details
     */
    private void processEventPages(EventCollectionPage eventCollection, User currentUser, List<Meeting> eventDetails) {
        if (eventCollection == null) {
            LOGGER.debug("→ processEventPages: Event collection is null, returning");
            return;
        }

        // Process current page
        LOGGER.debug("→ processEventPages: Processing page with {} events", eventCollection.getCurrentPage().size());
        for (Event event : eventCollection.getCurrentPage()) {
            if (currentUser != null &&
                    MeetingDaoHelper.isUpcomingEvent(event) &&
                    MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(event, currentUser.mail)) {
                eventDetails.add(new MeetingImpl(event));
                LOGGER.debug("→ Added unaccepted event: {}", event.subject);
            }
        }

        // Process next page if available
        EventCollectionRequestBuilder nextPageRequest = eventCollection.getNextPage();
        if (nextPageRequest != null) {
            LOGGER.debug("→ processEventPages: Processing next page");
            try {
                EventCollectionPage nextPage = nextPageRequest.buildRequest().get();
                processEventPages(nextPage, currentUser, eventDetails);
            } catch (GraphServiceException e) {
                LOGGER.error("→ processEventPages: Failed to process next page - {}", e.getMessage(), e);
                throw new IllegalStateException("Unable to retrieve the next page of events. Please try again or contact support if the issue persists. " + e.getMessage(), e);
            }
        } else {
            LOGGER.debug("→ processEventPages: No more pages to process");
        }
    }

    public String loadPublicConfig() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
            return properties.getProperty("public.expiryDate");

        } catch (IOException cause) {
            throw new RuntimeException("Failed to load public configuration file.", cause);
        }
    }

}
