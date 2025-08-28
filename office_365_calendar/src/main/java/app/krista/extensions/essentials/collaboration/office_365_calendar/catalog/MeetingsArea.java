package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity.EventDetails;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.TelemetryHelper;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import app.krista.model.base.File;
import com.microsoft.graph.models.FileAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.TelemetryHelper.safeTagMap;

/**
 * Catalog area class exposing Office 365 meeting and calendar operations as catalog requests.
 * <p>
 * The {@code MeetingsArea} class defines a set of catalog requests for managing meetings and events
 * within the Office 365 calendar integration. It provides endpoints for scheduling, updating, canceling,
 * and querying meetings, as well as managing attendees, invitations, free slots, schedules, and attachments.
 * </p>
 */
@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "2329f537-825e-4037-8a7e-cdc1f223419c")
public final class MeetingsArea {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeetingsArea.class);

    private final MeetingDaoImpl meetingDao;
    private final CatalogTypes catalogTypes;
    private final ValidationOrchestrator validationOrchestrator;
    private final ErrorHandlingStateManager errorHandlingStateManager;
    private final ExtensionResponseGenerator responseGenerator;
    private final TelemetryHelper telemetryHelper;

    @Inject
    public MeetingsArea(MeetingDaoImpl meetingDao, CatalogTypes catalogTypes, ValidationOrchestrator validationOrchestrator, ErrorHandlingStateManager errorHandlingStateManager, ExtensionResponseGenerator responseGenerator, TelemetryHelper telemetryHelper) {

        this.meetingDao = meetingDao;

        this.catalogTypes = catalogTypes;

        this.validationOrchestrator = validationOrchestrator;

        this.errorHandlingStateManager = errorHandlingStateManager;

        this.responseGenerator = responseGenerator;

        this.telemetryHelper = telemetryHelper;
    }

    /**
     * Cancels the event with the given event ID.
     *
     * @param eventId the ID of the event to cancel
     * @return an extension response containing the cancelled event details or error information
     */
    @CatalogRequest(
            id = "localDomainRequest_44d8fe1f-3f2d-438d-bb9f-bb486b9a9e56",
            name = "Cancel Event",
            description = "Cancels an existing calendar event by providing the event ID. This will remove the event from all attendees' calendars and send cancellation notifications.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = Constants.EVENT_DETAILS, type = "Entity(Event Details)", required = false)
    public ExtensionResponse cancelEvent(
            @Field.Text(name = "Delegate Email", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String delegateEmail,
            @Field(name = "Event Id", type = "Text", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Enter the unique identifier of the event you want to cancel'")}) String eventId) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("→ cancelEvent: Starting event cancellation for eventId: {}", eventId);

        try {
            telemetryHelper.incrementCount("office365Calendar.cancelEvent");
            // Validate input parameters
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();
            LOGGER.debug("→ Created validation parameters map");

            // Add delegate email to validation context if provided
            if (delegateEmail != null && !delegateEmail.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.DELEGATE_EMAIL, delegateEmail);
                LOGGER.debug("→ Added DELEGATE_EMAIL to validation parameters");
            }

            // Validate Event ID (required)
            if (eventId != null && !eventId.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.EVENT_ID, eventId);
                LOGGER.debug("→ Added EVENT_ID to validation parameters");
            } else {
                LOGGER.warn("→ Event ID is null or empty");
            }

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "cancelEvent");
            LOGGER.debug("→ Validation completed with {} results", validationResults.size());

            if (validationResults.isEmpty()) {
                // Validation passed, perform the cancel event operation
                LOGGER.debug("→ Validation passed, proceeding with event cancellation");
                Meeting cancelledMeeting = meetingDao.cancelMeeting(eventId, delegateEmail);
                LOGGER.debug("→ Event cancelled successfully via DAO");

                EventDetails eventDetails = CatalogTypes.fromEvent(cancelledMeeting);
                LOGGER.debug("→ Converted cancelled meeting to EventDetails");

                LOGGER.info("→ cancelEvent: Successfully cancelled event: {}", eventId);
                telemetryHelper.recordSuccess("office365Calendar.cancelEvent", startTime, safeTagMap("event_id", eventId));
                return new ExtensionResponseBuilder().success(Map.of(Constants.EVENT_DETAILS, eventDetails)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                LOGGER.warn("→ Validation failed with {} errors, storing state for confirmation", validationResults.size());
                String stateId = UUID.randomUUID().toString();
                LOGGER.debug("→ Generated state ID: {}", stateId);

                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));
                LOGGER.debug("→ Stored validation results in error handling state manager");

                telemetryHelper.recordRetryPrompted("office365Calendar.cancelEvent", startTime,
                        safeTagMap("event_id", eventId, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_RE_ENTER_CANCEL_EVENT,
                        Map.of(Constants.STATE_ID, stateId,
                              "Delegate Email", delegateEmail != null ? delegateEmail : "",
                              Constants.EVENT_ID, eventId != null ? eventId : ""));
                LOGGER.debug("→ Generated confirmation response for validation errors");
                telemetryHelper.recordSuccess("office365Calendar.cancelEvent", startTime, safeTagMap("event_id", eventId));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("→ cancelEvent: Exception occurred - {}", cause.getMessage(), cause);
            telemetryHelper.recordError("office365Calendar.cancelEvent", startTime, cause, safeTagMap("event_id", eventId));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in cancel event: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            LOGGER.debug("→ Created error response for exception");
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Retrieves the next upcoming meetings.
     *
     * @param numberOfMeeting the number of meetings to retrieve
     * @return an extension response containing upcoming meetings or error information
     */
    @CatalogRequest(
            id = "localDomainRequest_771024d0-02e5-4163-869e-8f8860a10b62",
            name = "Get Next Upcoming Meetings",
            description = "Retrieves a specified number of upcoming meetings from your calendar, ordered chronologically by start time. Useful for getting a quick overview of your next scheduled events.",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "[ Entity(Event Details) ]", required = false)
    public ExtensionResponse getNextUpcomingMeetings(
            @Field(name = "Number of meeting", type = "Number", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Specify how many upcoming meetings to retrieve (e.g., 5 for next 5 meetings)'")}) Double numberOfMeeting) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("→ getNextUpcomingMeetings: Starting retrieval for numberOfMeeting: {}", numberOfMeeting);

        try {
            telemetryHelper.incrementCount("office365Calendar.getNextUpcomingMeetings");
            // Validate input parameters
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();
            LOGGER.debug("→ Created validation parameters map");

            // Validate Number of meetings (required)
            if (numberOfMeeting != null) {
                validationParams.put(Validator.ValidationResource.NUMBER_OF_MEETINGS, String.valueOf(numberOfMeeting));
                LOGGER.debug("→ Added NUMBER_OF_MEETINGS to validation parameters: {}", numberOfMeeting);
            } else {
                LOGGER.warn("→ Number of meetings is null");
            }

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "getNextUpcomingMeetings");
            LOGGER.debug("→ Validation completed with {} results", validationResults.size());

            if (validationResults.isEmpty()) {
                // Validation passed, perform the get next upcoming meetings operation
                LOGGER.debug("→ Validation passed, retrieving {} upcoming meetings", numberOfMeeting.intValue());
                List<Meeting> meetings = meetingDao.getMeetings(numberOfMeeting.intValue());
                LOGGER.debug("→ Retrieved {} meetings from DAO", meetings.size());

                List<EventDetails> events = meetings.stream().map(CatalogTypes::fromEvent).collect(Collectors.toList());
                LOGGER.debug("→ Converted {} meetings to EventDetails", events.size());

                LOGGER.info("→ getNextUpcomingMeetings: Successfully retrieved {} upcoming meetings", events.size());
                telemetryHelper.recordSuccess("office365Calendar.getNextUpcomingMeetings", startTime,
                        safeTagMap("meeting_count", String.valueOf(events.size()), "requested_count", String.valueOf(numberOfMeeting)));
                return new ExtensionResponseBuilder().success(Map.of("Event Details", events)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                LOGGER.warn("→ Validation failed with {} errors, storing state for confirmation", validationResults.size());
                String stateId = UUID.randomUUID().toString();
                LOGGER.debug("→ Generated state ID: {}", stateId);

                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));
                LOGGER.debug("→ Stored validation results in error handling state manager");

                telemetryHelper.recordRetryPrompted("office365Calendar.getNextUpcomingMeetings", startTime,
                        safeTagMap("requested_count", String.valueOf(numberOfMeeting), "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_GET_NEXT_UPCOMING_MEETINGS,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.NUMBER_OF_MEETING, numberOfMeeting != null ? numberOfMeeting : 0.0));
                LOGGER.debug("→ Generated confirmation response for validation errors");
                telemetryHelper.recordSuccess("office365Calendar.getNextUpcomingMeetings", startTime,
                        safeTagMap("meeting_count", String.valueOf(numberOfMeeting), "requested_count", String.valueOf(numberOfMeeting)));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("→ getNextUpcomingMeetings: Exception occurred - {}", cause.getMessage(), cause);
            telemetryHelper.recordError("office365Calendar.getNextUpcomingMeetings", startTime, cause,
                    safeTagMap("requested_count", String.valueOf(numberOfMeeting)));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in get next upcoming meetings: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            LOGGER.debug("→ Created error response for exception");
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Declines the invitation for the given event ID.
     *
     * @param eventId the ID of the event to decline
     * @return the details of the declined event or error response with validation prompts
     */
    @CatalogRequest(
            id = "localDomainRequest_9de59f5f-b77d-42e3-84da-63f587f65358",
            name = "Decline Invitation",
            description = "Declines a meeting invitation for a specific event. This will update your response status to 'Declined' and notify the meeting organizer of your decision.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse declineInvitation(
            @Field(name = "Event Id", type = "Text", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Enter the unique identifier of the event invitation you want to decline'")}) String eventId) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("→ declineInvitation: Starting invitation decline for eventId: {}", eventId);

        try {
            telemetryHelper.incrementCount("office365Calendar.declineInvitation");
            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.EVENT_ID, eventId),
                            MeetingsArea.class, "declineInvitation");
            LOGGER.debug("→ Validation completed with {} results", validationResults.size());

            if (validationResults.isEmpty()) {
                // Validation passed, perform the decline invitation operation
                LOGGER.debug("→ Validation passed, declining invitation for event: {}", eventId);
                Meeting declinedMeeting = meetingDao.declineMeetingInvitation(eventId);
                LOGGER.debug("→ Invitation declined successfully via DAO");

                EventDetails eventDetails = CatalogTypes.fromEvent(declinedMeeting);
                LOGGER.debug("→ Converted declined meeting to EventDetails");

                LOGGER.info("→ declineInvitation: Successfully declined invitation for event: {}", eventId);
                telemetryHelper.recordSuccess("office365Calendar.declineInvitation", startTime, safeTagMap("event_id", eventId));
                return new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                LOGGER.warn("→ Validation failed with {} errors, storing state for confirmation", validationResults.size());
                String stateId = UUID.randomUUID().toString();
                LOGGER.debug("→ Generated state ID: {}", stateId);

                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));
                LOGGER.debug("→ Stored validation results in error handling state manager");

                telemetryHelper.recordRetryPrompted("office365Calendar.declineInvitation", startTime,
                        safeTagMap("event_id", eventId, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_DECLINE_INVITATION,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.EVENT_ID, eventId != null ? eventId : ""));
                LOGGER.debug("→ Generated confirmation response for validation errors");
                telemetryHelper.recordSuccess("office365Calendar.declineInvitation", startTime, safeTagMap("event_id", eventId));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("→ declineInvitation: Exception occurred - {}", cause.getMessage(), cause);
            telemetryHelper.recordError("office365Calendar.declineInvitation", startTime, cause, safeTagMap("event_id", eventId));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in decline invitation: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            LOGGER.debug("→ Created error response for exception");
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Marks the invitation as tentative for the given event ID.
     *
     * @param eventId the ID of the event
     * @return an extension response containing the tentative event details or error information
     */
    @CatalogRequest(
            id = "localDomainRequest_5bf7458f-953f-40c4-b75c-17b1f76a08b1",
            name = "Tentative Invitation",
            description = "Marks your response to a meeting invitation as 'Tentative', indicating you might attend but are not certain. This notifies the organizer of your provisional acceptance.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse tentativeInvitation(
            @Field(name = "Event Id", type = "Text", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Enter the unique identifier of the event invitation you want to mark as tentative'")}) String eventId) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.tentativeInvitation");
            LOGGER.info("tentativeInvitation: eventId={}", eventId);

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.EVENT_ID, eventId),
                            MeetingsArea.class, "tentativeInvitation");

            if (validationResults.isEmpty()) {
                // Validation passed, perform the tentative invitation operation
                EventDetails eventDetails = CatalogTypes.fromEvent(meetingDao.tentativeMeetingInvitation(eventId));
                telemetryHelper.recordSuccess("office365Calendar.tentativeInvitation", startTime, safeTagMap("event_id", eventId));
                return new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));

                telemetryHelper.recordRetryPrompted("office365Calendar.tentativeInvitation", startTime,
                        safeTagMap("event_id", eventId, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_TENTATIVE_INVITATION,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.EVENT_ID, eventId != null ? eventId : ""));
                telemetryHelper.recordSuccess("office365Calendar.tentativeInvitation", startTime, safeTagMap("event_id", eventId));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("Error occurred in tentativeInvitation: {}", cause.getMessage());
            telemetryHelper.recordError("office365Calendar.tentativeInvitation", startTime, cause, safeTagMap("event_id", eventId));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in tentative invitation: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Forwards the event to the specified attendees.
     *
     * @param eventId the ID of the event to forward
     * @param attendeeEmail the list of attendee emails
     * @return an extension response indicating success or error information
     */
    @CatalogRequest(
            id = "localDomainRequest_f2007cf4-d127-4a30-af0c-794b28f41de8",
            name = "Forward Event",
            description = "Forwards an existing calendar event to additional attendees. The specified recipients will receive meeting invitations with all the original event details.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "IsForwarded", type = "Switch", required = false)
    public ExtensionResponse forwardEvent(
            @Field(name = "Event Id", type = "Text", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Enter the unique identifier of the event you want to forward to additional attendees'")}) String eventId,
            @Field.Desc(name = "Attendee Email", type = "[ Email ]") List<String> attendeeEmail) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.forwardEvent");
            LOGGER.info("forwardEvent: eventId={}, attendeeEmail={}", eventId, attendeeEmail);

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.EVENT_ID, eventId),
                            MeetingsArea.class, "forwardEvent");

            if (validationResults.isEmpty()) {
                // Validation passed, perform the forward event operation
                meetingDao.forwardMeeting(eventId, attendeeEmail);
                telemetryHelper.recordSuccess("office365Calendar.forwardEvent", startTime,
                        safeTagMap("event_id", eventId, "attendee_count", String.valueOf(attendeeEmail != null ? attendeeEmail.size() : 0)));
                return new ExtensionResponseBuilder().success(Map.of("IsForwarded", true)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));

                telemetryHelper.recordRetryPrompted("office365Calendar.forwardEvent", startTime,
                        safeTagMap("event_id", eventId, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_FORWARD_EVENT,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.EVENT_ID, eventId != null ? eventId : "",
                              Constants.ATTENDEE_EMAIL, attendeeEmail != null ? attendeeEmail : List.of()));
                telemetryHelper.recordSuccess("office365Calendar.forwardEvent", startTime,
                        safeTagMap("event_id", eventId, "attendee_count", String.valueOf(attendeeEmail != null ? attendeeEmail.size() : 0)));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("Error occurred in forwardEvent: {}", cause.getMessage());
            telemetryHelper.recordError("office365Calendar.forwardEvent", startTime, cause,
                    safeTagMap("event_id", eventId));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in forward event: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Schedules a new meeting with the provided details.
     *
     * @return an extension response containing the scheduled event details or error information
     */
    @CatalogRequest(
            id = "localDomainRequest_f9cf27ee-ffe7-4b65-8e0a-ce0afa25f5dc",
            name = "Schedule Meeting",
            description = "Creates a new calendar meeting with comprehensive details including title, location, date/time, attendees, recurrence patterns, reminders, and file attachments. Supports both regular and Teams meetings.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse scheduleMeeting(
            @Field.Text(name = "Organizer Email", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String organizerEmail,
            @Field.Text(name = "Title", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Enter a descriptive title for your meeting'")}, options = {}) String title,
            @Field.Text(name = "Location", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Specify the meeting location (room, address, or virtual meeting link)'")}, options = {}) String location,
            @Field.Date(name = "Date From", required = true, includeTimeOfDay = true, allowToday = true, allowFuture = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Select the start date and time for your meeting'")}, options = {})  Long dateFrom,
            @Field.Date(name = "Date to", required = true, includeTimeOfDay = true, allowToday = true, allowFuture = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Select the end date and time for your meeting'")}, options = {})  Long dateTo,
            @Field.Desc(name = "Required Attendee Email", type = "[ Email ]", required = false) List<String> requiredAttendeeEmail,
            @Field.Desc(name = "Optional Attendee Email", type = "[ Email ]", required = false) List<String> optionalAttendeeEmail,
            @Field.Boolean(name = "Is All Day", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Check this if the meeting lasts the entire day'")}, options = {}) Boolean isAllDay,
            @Field.Text(name = "Time Zone", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Specify the time zone for the meeting (e.g., EST, PST, UTC)'")}, options = {}) String timeZone,
            @Field.PickOne(name = "Reminder", values = {"None", "Don't remind me", "At event start", "At time of event", "5 minutes before", "15 minutes before", "30 minutes before", "1 hour before", "2 hours before", "4 hours before", "12 hours before", "1 day before", "1 week before"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Choose when you want to be reminded about this meeting'")}, options = {}) String reminder,
            @Field.PickOne(name = "Recurrence", values = {"Never", "Does Not Repeat", "Daily", "Weekly", "Monthly", "Yearly"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Select how often this meeting should repeat'")}, options = {}) String recurrence,
            @Field(name = "Recurrence Interval", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Specify the interval for recurrence (e.g., 2 for every 2 weeks)'")}, options = {}) Double recurrenceInterval,
            @Field.Desc(name = "Days Of Week", type = "[ Text ]", required = false) List<String> daysOfWeek,
            @Field.PickOne(name = "Recurrence Type", values = {"Absolute recurrence", "Relative recurrence"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Choose between absolute (specific date) or relative (e.g., first Monday) recurrence'")}, options = {}) String recurrenceType,
            @Field(name = "Day Of Month", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Specify the day of the month for monthly recurrence (1-31)'")}, options = {}) Double dayOfMonth,
            @Field.PickOne(name = "Repeat Index", values = {"First", "Second", "Third", "Fourth", "Last"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Select which occurrence in the month (e.g., first Monday, last Friday)'")}, options = {}) String repeatIndex,
            @Field.PickOne(name = "Repeat Day", values = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Choose the day of the week for weekly recurrence'")}, options = {}) String repeatDay,
            @Field.PickOne(name = "Repeat Month", values = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Select the month for yearly recurrence'")}, options = {}) String repeatMonth,
            @Field.PickOne(name = "End Recurrence", values = {"End date", "Numbered occurrences", "No end"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Choose how the recurring meeting should end'")}, options = {}) String endRecurrence,
            @Field.Date(name = "End Date", required = false, includeTimeOfDay = true, allowPast = false, allowToday = false, allowFuture = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Select the date when the recurring meeting should stop'")}, options = {}) Long endDate,
            @Field(name = "Numbered Occurrences", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Specify how many times the meeting should occur'")}, options = {}) Double numberedOccurrences,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Upload files to attach to the meeting invitation'")}, options = {}) List<File> attachments,
            @Field(name = "Details", type = "RichText", required = false, attributes = {@Attribute(name = "visualWidth", value = "L"), @Attribute(name = "toolTip", value = "'Add detailed description or agenda for the meeting'")}, options = {}) String details,
            @Field.Boolean(name = "Is Teams Meeting", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Check this to create a Microsoft Teams meeting with video conferencing'")}, options = {}) Boolean isTeamsMeeting) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("→ scheduleMeeting: Starting meeting scheduling - title: '{}', location: '{}', dateFrom: {}, dateTo: {}",
                title, location, dateFrom, dateTo);
        LOGGER.debug("→ Additional parameters - timeZone: {}, recurrenceInterval: {}, daysOfWeek: {}, numberedOccurrences: {}, isTeamsMeeting: {}",
                timeZone, recurrenceInterval, daysOfWeek, numberedOccurrences, isTeamsMeeting);

        try {
            telemetryHelper.incrementCount("office365Calendar.scheduleMeeting");
            // Prepare validation parameters
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();
            LOGGER.debug("→ Created validation parameters map");

            // Validate date range
            if (dateFrom != null && dateTo != null) {
                validationParams.put(Validator.ValidationResource.FROM_DATE, String.valueOf(dateFrom));
                validationParams.put(Validator.ValidationResource.TO_DATE, String.valueOf(dateTo));
                LOGGER.debug("→ Added date range to validation parameters: {} to {}", dateFrom, dateTo);
            } else {
                LOGGER.warn("→ Date range is incomplete - dateFrom: {}, dateTo: {}", dateFrom, dateTo);
            }

            // Validate time zone
            if (timeZone != null && !timeZone.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.TIME_ZONE, timeZone);
                LOGGER.debug("→ Added TIME_ZONE to validation parameters: {}", timeZone);
            }

            // Validate recurrence interval
            if (recurrenceInterval != null) {
                validationParams.put(Validator.ValidationResource.RECURRENCE_INTERVAL, String.valueOf(recurrenceInterval));
                LOGGER.debug("→ Added RECURRENCE_INTERVAL to validation parameters: {}", recurrenceInterval);
            }

            // Validate days of week
            if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
                validationParams.put(Validator.ValidationResource.DAYS_OF_WEEK, Constants.GSON.toJson(daysOfWeek));
                LOGGER.debug("→ Added DAYS_OF_WEEK to validation parameters: {}", daysOfWeek);
            }

            // Validate numbered occurrences
            if (numberedOccurrences != null) {
                validationParams.put(Validator.ValidationResource.NUMBERED_OCCURRENCES, String.valueOf(numberedOccurrences));
                LOGGER.debug("→ Added NUMBERED_OCCURRENCES to validation parameters: {}", numberedOccurrences);
            }

            // Validate attachments (only PDF files allowed)
            if (attachments != null && !attachments.isEmpty()) {
                validationParams.put(Validator.ValidationResource.ATTACHMENTS, Constants.GSON.toJson(attachments));
                LOGGER.debug("→ Added ATTACHMENTS to validation parameters: {} files", attachments.size());
            }

            // Validate input parameters using dynamic field attribute extraction
            LOGGER.debug("→ Starting validation with {} parameters", validationParams.size());
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "scheduleMeeting");
            LOGGER.debug("→ Validation completed with {} results", validationResults.size());

            if (validationResults.isEmpty()) {
                // Validation passed, perform the schedule meeting operation
                LOGGER.debug("→ Validation passed, creating meeting via DAO");
                LOGGER.debug("→ Converting {} attachments to Attachment objects", attachments != null ? attachments.size() : 0);

                Meeting createdMeeting = meetingDao.addMeeting(organizerEmail,title, location, dateFrom, dateTo, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, timeZone, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, catalogTypes.toAttachment(attachments), details, isTeamsMeeting);
                LOGGER.debug("→ Meeting created successfully via DAO");

                EventDetails eventDetails = CatalogTypes.fromEvent(createdMeeting);
                LOGGER.debug("→ Converted created meeting to EventDetails");

                LOGGER.info("→ scheduleMeeting: Successfully scheduled meeting: '{}'", title);
                telemetryHelper.recordSuccess("office365Calendar.scheduleMeeting", startTime,
                        safeTagMap("title", title, "is_teams_meeting", String.valueOf(isTeamsMeeting),
                                  "has_recurrence", String.valueOf(recurrence != null && !recurrence.equals("Never") && !recurrence.equals("Does Not Repeat"))));
                return new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                LOGGER.warn("→ Validation failed with {} errors, storing state for confirmation", validationResults.size());
                String stateId = UUID.randomUUID().toString();
                LOGGER.debug("→ Generated state ID: {}", stateId);

                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));
                LOGGER.debug("→ Stored validation results in error handling state manager");

                Map<String, Object> confirmationParams = createScheduleMeetingConfirmationParams(stateId, organizerEmail, title, location, dateFrom, dateTo, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, timeZone, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, attachments, details, isTeamsMeeting);
                LOGGER.debug("→ Created confirmation parameters with {} entries", confirmationParams.size());

                telemetryHelper.recordRetryPrompted("office365Calendar.scheduleMeeting", startTime,
                        safeTagMap("title", title, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_SCHEDULE_MEETING,
                        confirmationParams);
                LOGGER.debug("→ Generated confirmation response for validation errors");
                telemetryHelper.recordSuccess("office365Calendar.scheduleMeeting", startTime,
                        safeTagMap("title", title, "is_teams_meeting", String.valueOf(isTeamsMeeting),
                                  "has_recurrence", String.valueOf(recurrence != null && !recurrence.equals("Never") && !recurrence.equals("Does Not Repeat"))));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("→ scheduleMeeting: Exception occurred - {}", cause.getMessage(), cause);
            telemetryHelper.recordError("office365Calendar.scheduleMeeting", startTime, cause,
                    safeTagMap("title", title));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in schedule meeting: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            LOGGER.debug("→ Created error response for exception");
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Helper method to create confirmation parameters for schedule meeting error handling.
     */
    private Map<String, Object> createScheduleMeetingConfirmationParams(String stateId, String organizerEmail, String title, String location, Long dateFrom, Long dateTo, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String timeZone, String reminder, String recurrence, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, List<File> attachments, String details, Boolean isTeamsMeeting) {
        LOGGER.debug("→ createScheduleMeetingConfirmationParams: Creating confirmation parameters for stateId: {}", stateId);

        Map<String, Object> params = new HashMap<>();
        params.put(Constants.STATE_ID, stateId);
        params.put("Organizer Email", organizerEmail != null ? organizerEmail : "");
        params.put(Constants.TITLE, title != null ? title : "");
        params.put(Constants.LOCATION, location != null ? location : "");
        params.put(Constants.FROM_DATE, dateFrom != null ? dateFrom : 0L);
        params.put(Constants.DATE_TO, dateTo != null ? dateTo : 0L);
        LOGGER.debug("→ Added basic parameters: title='{}', location='{}', dateFrom={}, dateTo={}", title, location, dateFrom, dateTo);

        params.put(Constants.REQUIRED_ATTENDEE_EMAIL, requiredAttendeeEmail != null ? requiredAttendeeEmail : List.of());
        params.put(Constants.OPTIONAL_ATTENDEE_EMAIL, optionalAttendeeEmail != null ? optionalAttendeeEmail : List.of());
        LOGGER.debug("→ Added attendee parameters: {} required, {} optional",
                requiredAttendeeEmail != null ? requiredAttendeeEmail.size() : 0,
                optionalAttendeeEmail != null ? optionalAttendeeEmail.size() : 0);

        params.put(Constants.IS_ALL_DAY, isAllDay != null ? isAllDay : false);
        params.put(Constants.TIME_ZONE, timeZone != null ? timeZone : "");
        LOGGER.debug("→ Added time parameters: isAllDay={}, timeZone='{}'", isAllDay, timeZone);
        params.put(Constants.REMINDER, reminder != null ? reminder : "");
        params.put(Constants.RECURRENCE, recurrence != null ? recurrence : "");
        params.put(Constants.RECURRENCE_INTERVAL, recurrenceInterval != null ? recurrenceInterval : 0.0);
        params.put(Constants.DAYS_OF_WEEK, daysOfWeek != null ? daysOfWeek : List.of());
        params.put(Constants.RECURRENCE_TYPE, recurrenceType != null ? recurrenceType : "");
        params.put(Constants.DAY_OF_MONTH, dayOfMonth != null ? dayOfMonth : 0.0);
        params.put(Constants.REPEAT_INDEX, repeatIndex != null ? repeatIndex : "");
        params.put(Constants.REPEAT_DAY, repeatDay != null ? repeatDay : "");
        params.put(Constants.REPEAT_MONTH, repeatMonth != null ? repeatMonth : "");
        params.put(Constants.END_RECURRENCE, endRecurrence != null ? endRecurrence : "");
        params.put(Constants.END_DATE, endDate != null ? endDate : 0L);
        params.put(Constants.NUMBERED_OCCURRENCES, numberedOccurrences != null ? numberedOccurrences : 0.0);
        params.put(Constants.ATTACHMENTS_FIELD, attachments != null ? attachments : List.of());
        params.put(Constants.DETAILS, details != null ? details : "");
        params.put(Constants.IS_TEAMS_MEETING, isTeamsMeeting != null ? isTeamsMeeting : false);
        LOGGER.debug("Schedule meeting confirmation parameters: {}", params);
        return params;
    }

    /**
     * Retrieves all unaccepted events.
     *
     * @return a list of unaccepted event details
     */
    @CatalogRequest(
            id = "localDomainRequest_e0f748a2-17df-4e29-84e0-a1d0d06934d9",
            name = "Get All Unaccepted Events",
            description = "Retrieves all meeting invitations that you have not yet responded to (neither accepted, declined, nor marked as tentative). Helps you manage pending meeting requests.",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "[ Entity(Event Details) ]", required = false)
    public List<EventDetails> getAllUnacceptedEvents() {
        long startTime = System.currentTimeMillis();
        LOGGER.info("getAllUnacceptedEvents: Retrieving all unaccepted events");
        try {
            telemetryHelper.incrementCount("office365Calendar.getAllUnacceptedEvents");
            List<Meeting> unacceptedMeetings = meetingDao.getUnAcceptedMeetings();
            LOGGER.info("getAllUnacceptedEvents: Found {} unaccepted meetings", unacceptedMeetings.size());
            List<EventDetails> eventDetails = unacceptedMeetings.stream().map(CatalogTypes::fromEvent).collect(Collectors.toList());
            LOGGER.info("getAllUnacceptedEvents: Successfully converted to {} event details", eventDetails.size());
            telemetryHelper.recordSuccess("office365Calendar.getAllUnacceptedEvents", startTime,
                    safeTagMap("unaccepted_count", String.valueOf(eventDetails.size())));
            return eventDetails;
        } catch (Exception cause) {
            LOGGER.error("Error occurred in getAllUnacceptedEvents: {}", cause.getMessage(), cause);
            telemetryHelper.recordError("office365Calendar.getAllUnacceptedEvents", startTime, cause, safeTagMap());
            throw cause;
        }
    }

    /**
     * Adds attendees to the specified event.
     *
     * @param eventId the ID of the event
     * @param requiredAttendeeEmail list of required attendee emails
     * @param optionalAttendeeEmail list of optional attendee emails
     * @return an extension response containing the updated event details or error information
     */
    @CatalogRequest(
            id = "localDomainRequest_2e73b843-ace4-4b80-bfab-4f466706e5c5",
            name = "Add Attendees",
            description = "Adds new attendees to an existing calendar event. You can specify both required and optional attendees who will receive meeting invitations for the event.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse addAttendees(
            @Field(name = "Event Id", type = "Text", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Enter the unique identifier of the event to add attendees to'")}) String eventId,
            @Field.Desc(name = "Attendee Email", type = "[ Email ]") List<String> requiredAttendeeEmail,
            @Field.Desc(name = "Optional Attendee Email", type = "[ Email ]", required = false) List<String> optionalAttendeeEmail) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.addAttendees");
            LOGGER.info("addAttendees: eventId={}, requiredAttendeeEmail={}, optionalAttendeeEmail={}", eventId, requiredAttendeeEmail, optionalAttendeeEmail);

            // Prepare validation parameters
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();

            // Validate Event ID
            if (eventId != null && !eventId.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.EVENT_ID, eventId);
            }

            // Validate required attendee emails
            if (requiredAttendeeEmail != null && !requiredAttendeeEmail.isEmpty()) {
                for (String email : requiredAttendeeEmail) {
                    if (email != null && !email.trim().isEmpty()) {
                        validationParams.put(Validator.ValidationResource.EMAIL, email);
                        break; // Just validate one email as representative
                    }
                }
            }

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "addAttendees");

            if (validationResults.isEmpty()) {
                // Validation passed, perform the add attendees operation
                EventDetails eventDetails = CatalogTypes.fromEvent(meetingDao.addAttendees(eventId, requiredAttendeeEmail, optionalAttendeeEmail));
                telemetryHelper.recordSuccess("office365Calendar.addAttendees", startTime,
                        safeTagMap("event_id", eventId, "required_count", String.valueOf(requiredAttendeeEmail != null ? requiredAttendeeEmail.size() : 0),
                                  "optional_count", String.valueOf(optionalAttendeeEmail != null ? optionalAttendeeEmail.size() : 0)));
                return new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));

                telemetryHelper.recordRetryPrompted("office365Calendar.addAttendees", startTime,
                        safeTagMap("event_id", eventId, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_ADD_ATTENDEES,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.EVENT_ID, eventId != null ? eventId : "",
                              Constants.REQUIRED_ATTENDEE_EMAIL, requiredAttendeeEmail != null ? requiredAttendeeEmail : List.of(),
                              Constants.OPTIONAL_ATTENDEE_EMAIL, optionalAttendeeEmail != null ? optionalAttendeeEmail : List.of()));
                telemetryHelper.recordSuccess("office365Calendar.addAttendees", startTime,
                        safeTagMap("event_id", eventId, "required_count", String.valueOf(requiredAttendeeEmail != null ? requiredAttendeeEmail.size() : 0),
                                  "optional_count", String.valueOf(optionalAttendeeEmail != null ? optionalAttendeeEmail.size() : 0)));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("Error occurred in addAttendees: {}", cause.getMessage());
            telemetryHelper.recordError("office365Calendar.addAttendees", startTime, cause,
                    safeTagMap("event_id", eventId));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in add attendees: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Updates the event with the given details.
     *
     * @return an extension response containing the updated event details or error information
     */
    @CatalogRequest(
            id = "localDomainRequest_ff16001f-6709-44a8-9f00-5de09287e70e",
            name = "Update Event",
            description = "Modifies an existing calendar event with new details such as title, location, date/time, attendees, recurrence settings, and other meeting properties. All attendees will be notified of the changes.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse updateEvent(
            @Field.Text(name = "Organizer Email", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String organizerEmail,
            @Field(name = "Event Id", type = "Text") String eventId,
            @Field(name = "Title", type = "Text", required = false) String title,
            @Field(name = "Location", type = "Text", required = false) String location,
            @Field(name = "Date From", type = "Date", attributes = @Attribute(name = "includeTimeOfDay", value = "true"), required = false) Long dateFrom,
            @Field(name = "Date to", type = "Date", attributes = @Attribute(name = "includeTimeOfDay", value = "true"), required = false) Long dateTo,
            @Field(name = "Details", type = "RichText", required = false) String details,
            @Field(name = "Time Zone", type = "Text", required = false) String timeZone,
            @Field.Desc(name = "Required Attendee Email", type = "[ Email ]", required = false) List<String> requiredAttendeeEmail,
            @Field.Desc(name = "Optional Attendee Email", type = "[ Email ]", required = false) List<String> optionalAttendeeEmail,
            @Field(name = "Is All Day", type = "Switch", required = false) Boolean isAllDay,
            @Field.Desc(name = "Reminder", type = "PickOne(None|At event start|5 minutes before|15 minutes before|30 minutes before|1 hour before|2 hours before|4 hours before|1 day before|1 week before)", required = false) String reminder,
            @Field.Desc(name = "Recurrence", type = "PickOne(Does Not Repeat|Daily|Weekly|Monthly|Yearly)", required = false) String recurrence,
            @Field(name = "Recurrence Interval", type = "Number", required = false) Double recurrenceInterval,
            @Field.Desc(name = "Days Of Week", type = "[ Text ]", required = false) List<String> daysOfWeek,
            @Field.Desc(name = "Recurrence Type", type = "PickOne(Absolute recurrence|Relative recurrence)", required = false) String recurrenceType,
            @Field(name = "Day Of Month", type = "Number", required = false) Double dayOfMonth,
            @Field.Desc(name = "Repeat Index", type = "PickOne(First|Second|Third|Fourth|Last)", required = false) String repeatIndex,
            @Field.Desc(name = "Repeat Day", type = "PickOne(Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday)", required = false) String repeatDay,
            @Field.Desc(name = "Repeat Month", type = "PickOne(January|February|March|April|May|June|July|August|September|October|November|December)", required = false) String repeatMonth,
            @Field.Desc(name = "End Recurrence", type = "PickOne(End date|Numbered occurrences|No end)", required = false) String endRecurrence,
            @Field(name = "End Date", type = "Date", attributes = @Attribute(name = "includeTimeOfDay", value = "true"), required = false) Long endDate,
            @Field(name = "Numbered Occurrences", type = "Number", required = false) Double numberedOccurrences,
            @Field(name = "Is Teams Meeting", type = "Switch", required = false) Boolean isTeamsMeeting) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.updateEvent");
            LOGGER.info("updateEvent: eventId={}, title={}, location={}, dateFrom={}, dateTo={}, timeZone={}, recurrenceInterval={}, daysOfWeek={}, numberedOccurrences={}",
                    eventId, title, location, dateFrom, dateTo, timeZone, recurrenceInterval, daysOfWeek, numberedOccurrences);

            // Prepare validation parameters
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();


            // Add delegate email to validation context if provided
            if (organizerEmail != null && !organizerEmail.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.DELEGATE_EMAIL, organizerEmail);
                LOGGER.debug("→ Added DELEGATE_EMAIL to validation parameters");
            }
            // Validate Organizer Email (optional)
            if (organizerEmail != null && !organizerEmail.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.EMAIL, organizerEmail);
            }

            // Validate Event ID (required)
            if (eventId != null && !eventId.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.EVENT_ID, eventId);
            }

            // Validate date range (if provided)
            if (dateFrom != null && dateTo != null) {
                validationParams.put(Validator.ValidationResource.FROM_DATE, String.valueOf(dateFrom));
                validationParams.put(Validator.ValidationResource.TO_DATE, String.valueOf(dateTo));
            }

            // Validate time zone (if provided)
            if (timeZone != null && !timeZone.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.TIME_ZONE, timeZone);
            }

            // Validate recurrence interval (if provided)
            if (recurrenceInterval != null) {
                validationParams.put(Validator.ValidationResource.RECURRENCE_INTERVAL, String.valueOf(recurrenceInterval));
            }

            // Validate days of week (if provided)
            if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
                validationParams.put(Validator.ValidationResource.DAYS_OF_WEEK, Constants.GSON.toJson(daysOfWeek));
            }

            // Validate numbered occurrences (if provided)
            if (numberedOccurrences != null) {
                validationParams.put(Validator.ValidationResource.NUMBERED_OCCURRENCES, String.valueOf(numberedOccurrences));
            }

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "updateEvent");

            if (validationResults.isEmpty()) {
                // Validation passed, perform the update event operation
                EventDetails eventDetails = CatalogTypes.fromEvent(meetingDao.updateMeeting(organizerEmail, eventId, title, details, location, dateFrom, dateTo, timeZone, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, recurrence, reminder, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, isTeamsMeeting));
                telemetryHelper.recordSuccess("office365Calendar.updateEvent", startTime,
                        safeTagMap("event_id", eventId, "title", title));
                return new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));

                telemetryHelper.recordRetryPrompted("office365Calendar.updateEvent", startTime,
                        safeTagMap("event_id", eventId, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_UPDATE_EVENT,
                        createUpdateEventConfirmationParams(stateId, organizerEmail, eventId, title, location, dateFrom, dateTo, details, timeZone, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, isTeamsMeeting));
                telemetryHelper.recordSuccess("office365Calendar.updateEvent", startTime, safeTagMap("event_id", eventId));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("Error occurred in updateEvent: {}", cause.getMessage());
            telemetryHelper.recordError("office365Calendar.updateEvent", startTime, cause,
                    safeTagMap("event_id", eventId));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in update event: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Helper method to create confirmation parameters for update event error handling.
     */
    private Map<String, Object> createUpdateEventConfirmationParams(String stateId, String organizerEmail, String eventId, String title, String location, Long dateFrom, Long dateTo, String details, String timeZone, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String reminder, String recurrence, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, Boolean isTeamsMeeting) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.STATE_ID, stateId);
        params.put("Organizer Email", organizerEmail != null ? organizerEmail : "");
        params.put(Constants.EVENT_ID, eventId != null ? eventId : "");
        params.put(Constants.TITLE, title != null ? title : "");
        params.put(Constants.LOCATION, location != null ? location : "");
        params.put(Constants.FROM_DATE, dateFrom != null ? dateFrom : 0L);
        params.put(Constants.DATE_TO, dateTo != null ? dateTo : 0L);
        params.put(Constants.DETAILS, details != null ? details : "");
        params.put(Constants.TIME_ZONE, timeZone != null ? timeZone : "");
        params.put(Constants.REQUIRED_ATTENDEE_EMAIL, requiredAttendeeEmail != null ? requiredAttendeeEmail : List.of());
        params.put(Constants.OPTIONAL_ATTENDEE_EMAIL, optionalAttendeeEmail != null ? optionalAttendeeEmail : List.of());
        params.put(Constants.IS_ALL_DAY, isAllDay != null ? isAllDay : false);
        params.put(Constants.REMINDER, reminder != null ? reminder : "");
        params.put(Constants.RECURRENCE, recurrence != null ? recurrence : "");
        params.put(Constants.RECURRENCE_INTERVAL, recurrenceInterval != null ? recurrenceInterval : 0.0);
        params.put(Constants.DAYS_OF_WEEK, daysOfWeek != null ? daysOfWeek : List.of());
        params.put(Constants.RECURRENCE_TYPE, recurrenceType != null ? recurrenceType : "");
        params.put(Constants.DAY_OF_MONTH, dayOfMonth != null ? dayOfMonth : 0.0);
        params.put(Constants.REPEAT_INDEX, repeatIndex != null ? repeatIndex : "");
        params.put(Constants.REPEAT_DAY, repeatDay != null ? repeatDay : "");
        params.put(Constants.REPEAT_MONTH, repeatMonth != null ? repeatMonth : "");
        params.put(Constants.END_RECURRENCE, endRecurrence != null ? endRecurrence : "");
        params.put(Constants.END_DATE, endDate != null ? endDate : 0L);
        params.put(Constants.NUMBERED_OCCURRENCES, numberedOccurrences != null ? numberedOccurrences : 0.0);
        params.put(Constants.IS_TEAMS_MEETING, isTeamsMeeting != null ? isTeamsMeeting : false);
        return params;
    }

    /**
     * Accepts the invitation for the given event ID.
     *
     * @param eventId the ID of the event to accept
     * @return an extension response containing the accepted event details or error information
     */
    @CatalogRequest(
            id = "localDomainRequest_d564897b-ad17-4289-af2c-bd79425c5341",
            name = "Accept Invitation",
            description = "Accepts a meeting invitation for a specific event. This will update your response status to 'Accepted' and add the event to your calendar, notifying the organizer of your acceptance.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse acceptInvitation(
            @Field(name = "Event Id", type = "Text", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Enter the unique identifier of the event invitation you want to accept'")}) String eventId) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.acceptInvitation");
            LOGGER.info("acceptInvitation: eventId={}", eventId);

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.EVENT_ID, eventId),
                            MeetingsArea.class, "acceptInvitation");

            if (validationResults.isEmpty()) {
                // Validation passed, perform the accept invitation operation
                EventDetails eventDetails = CatalogTypes.fromEvent(meetingDao.acceptMeetingInvitation(eventId));
                telemetryHelper.recordSuccess("office365Calendar.acceptInvitation", startTime, safeTagMap("event_id", eventId));
                return new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));

                telemetryHelper.recordRetryPrompted("office365Calendar.acceptInvitation", startTime,
                        safeTagMap("event_id", eventId, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_ACCEPT_INVITATION,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.EVENT_ID, eventId != null ? eventId : ""));
                telemetryHelper.recordSuccess("office365Calendar.acceptInvitation", startTime, safeTagMap("event_id", eventId));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("Error occurred in acceptInvitation: {}", cause.getMessage());
            telemetryHelper.recordError("office365Calendar.acceptInvitation", startTime, cause, safeTagMap("event_id", eventId));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in accept invitation: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Removes attendees from the specified event.
     *
     * @param eventId the ID of the event
     * @param attendeeEmail list of attendee emails to remove
     * @return an extension response containing the updated event details or error information
     */
    @CatalogRequest(
            id = "localDomainRequest_653a694e-2327-41d5-8a8c-48b30b07dc89",
            name = "Remove Attendee",
            description = "Removes specified attendees from an existing calendar event. The removed attendees will be notified that they are no longer invited to the meeting.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse removeAttendee(
            @Field(name = "Event Id", type = "Text", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Enter the unique identifier of the event to remove attendees from'")}) String eventId,
            @Field.Desc(name = "Attendee Email", type = "[ Email ]") List<String> attendeeEmail) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.removeAttendee");
            LOGGER.info("removeAttendee: eventId={}, attendeeEmail={}", eventId, attendeeEmail);

            // Prepare validation parameters
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();

            // Validate Event ID
            if (eventId != null && !eventId.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.EVENT_ID, eventId);
            }

            // Validate attendee emails
            if (attendeeEmail != null && !attendeeEmail.isEmpty()) {
                for (String email : attendeeEmail) {
                    if (email != null && !email.trim().isEmpty()) {
                        validationParams.put(Validator.ValidationResource.EMAIL, email);
                        break; // Just validate one email as representative
                    }
                }
            }

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "removeAttendee");

            if (validationResults.isEmpty()) {
                // Validation passed, perform the remove attendee operation
                EventDetails eventDetails = CatalogTypes.fromEvent(meetingDao.removeAttendees(eventId, attendeeEmail));
                telemetryHelper.recordSuccess("office365Calendar.removeAttendee", startTime,
                        safeTagMap("event_id", eventId, "attendee_count", String.valueOf(attendeeEmail != null ? attendeeEmail.size() : 0)));
                return new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));

                telemetryHelper.recordRetryPrompted("office365Calendar.removeAttendee", startTime,
                        safeTagMap("event_id", eventId, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_REMOVE_ATTENDEE,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.EVENT_ID, eventId != null ? eventId : "",
                              Constants.ATTENDEE_EMAIL, attendeeEmail != null ? attendeeEmail : List.of()));
                telemetryHelper.recordSuccess("office365Calendar.removeAttendee", startTime,
                        safeTagMap("event_id", eventId, "attendee_count", String.valueOf(attendeeEmail != null ? attendeeEmail.size() : 0)));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("Error occurred in removeAttendee: {}", cause.getMessage());
            telemetryHelper.recordError("office365Calendar.removeAttendee", startTime, cause,
                    safeTagMap("event_id", eventId));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in remove attendee: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Retrieves free meeting slots based on the provided criteria.
     *
     * @param dateFrom start date (epoch millis)
     * @param dateTo end date (epoch millis)
     * @param isOrganizerOptional whether organizer is optional
     * @param requiredAttendee list of required attendee emails
     * @param optionalAttendee list of optional attendee emails
     * @param timeZone time zone (optional)
     * @param meetingDuration meeting duration (optional)
     * @return an extension response containing free slots or error response with validation prompts
     */
    @CatalogRequest(
            id = "localDomainRequest_7bbeb961-4ba2-4c89-8589-87a5226c33a4",
            name = "Get Free Slots",
            description = "Analyzes calendars of specified attendees to find available time slots for scheduling a meeting. Returns suggested meeting times based on everyone's availability within the given date range.",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "List of Free Slots", type = "[ { Time Slot: DateTimeRange, Attendee Availability: [ { Attendee: Text, Availability: Text } ], Confidence: Percentage, Organizer Availability: Text, Suggestion Reason: Text } ]", required = false)
    public ExtensionResponse getFreeSlots(
            @Field.Date(name = "Date From", required = true, showHowManyDaysInViewer = 60, allowPast = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) Long dateFrom,
            @Field.Date(name = "Date To", required = true, showHowManyDaysInViewer = 60, allowPast = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) Long dateTo,
            @Field.Boolean(name = "Is Organizer Optional", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean isOrganizerOptional,
            @Field.Desc(name = "Required Attendee", type = "[ Email ]") List<String> requiredAttendee,
            @Field.Desc(name = "Optional Attendee", type = "[ Email ]", required = false) List<String> optionalAttendee,
            @Field.Text(name = "Time Zone", attributes = {@Attribute(name = "visualWidth", value = "S")}) String timeZone,
            @Field.Text(name = "Meeting Duration", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) String meetingDuration) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.getFreeSlots");
            LOGGER.info("→ GET FREE SLOTS START");
            LOGGER.info("→ Input parameters: dateFrom={}, dateTo={}, isOrganizerOptional={}, requiredAttendee={}, optionalAttendee={}, timeZone={}, meetingDuration={}",
                dateFrom, dateTo, isOrganizerOptional, requiredAttendee, optionalAttendee, timeZone, meetingDuration);

            // Prepare validation parameters
            LOGGER.debug("→ Preparing validation parameters...");
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();

            // Validate date range
            if (dateFrom != null && dateTo != null) {
                LOGGER.debug("→ Adding date validation parameters: FROM_DATE={}, TO_DATE={}", dateFrom, dateTo);
                validationParams.put(Validator.ValidationResource.FROM_DATE, String.valueOf(dateFrom));
                validationParams.put(Validator.ValidationResource.TO_DATE, String.valueOf(dateTo));
            } else {
                LOGGER.debug("→ Skipping date validation - dateFrom or dateTo is null");
            }
            // Validate time zone
            if (timeZone != null && !timeZone.trim().isEmpty()) {
                LOGGER.debug("→ Adding time zone validation parameter: TIME_ZONE={}", timeZone);
                validationParams.put(Validator.ValidationResource.TIME_ZONE, timeZone);
            } else {
                LOGGER.debug("→ Skipping time zone validation - timeZone is null or empty");
            }

            // Validate meeting duration
            if (meetingDuration != null && !meetingDuration.trim().isEmpty()) {
                LOGGER.debug("→ Adding meeting duration validation parameter: MEETING_DURATION={}", meetingDuration);
                validationParams.put(Validator.ValidationResource.MEETING_DURATION, meetingDuration);
            } else {
                LOGGER.debug("→ Skipping meeting duration validation - meetingDuration is null or empty");
            }

            LOGGER.debug("→ Final validation parameters: {}", validationParams);

            // Validate input parameters using dynamic field attribute extraction
            LOGGER.debug("→ Calling validation orchestrator with dynamic attribute extraction...");
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "getFreeSlots");

            LOGGER.debug("→ Validation results count: {}", validationResults.size());
            if (!validationResults.isEmpty()) {
                LOGGER.debug("→ Validation results: {}", validationResults);
            }

            if (validationResults.isEmpty()) {
                // Validation passed, perform the get free slots operation
                LOGGER.debug("→ Validation passed, calling meetingDao.getFreeSlot...");
                List<Map<String, Object>> freeSlots = meetingDao.getFreeSlot(dateFrom, dateTo, isOrganizerOptional, requiredAttendee, optionalAttendee, timeZone, meetingDuration);
                LOGGER.info("→ Retrieved free slots count: {}", (freeSlots != null ? freeSlots.size() : "null"));
                ExtensionResponse response = new ExtensionResponseBuilder().success(Map.of("List of Free Slots", freeSlots)).build();
                LOGGER.info("→ GET FREE SLOTS SUCCESS END");
                telemetryHelper.recordSuccess("office365Calendar.getFreeSlots", startTime,
                        safeTagMap("slots_found", String.valueOf(freeSlots != null ? freeSlots.size() : 0),
                                  "required_attendees", String.valueOf(requiredAttendee != null ? requiredAttendee.size() : 0),
                                  "optional_attendees", String.valueOf(optionalAttendee != null ? optionalAttendee.size() : 0)));
                return response;
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                LOGGER.debug("→ Validation failed, preparing confirmation response...");
                String stateId = UUID.randomUUID().toString();
                LOGGER.debug("→ Generated stateId: {}", stateId);

                Map<String, Object> stateData = Map.of(Constants.VALIDATION_RESULTS, validationResults);
                LOGGER.debug("→ Storing state data: {}", stateData);
                errorHandlingStateManager.put(stateId, stateData);

                Map<String, Object> confirmationParams = Map.of(
                        Constants.STATE_ID, stateId,
                        Constants.FROM_DATE, dateFrom != null ? dateFrom : 0L,
                        Constants.TO_DATE, dateTo != null ? dateTo : 0L,
                        Constants.IS_ORGANIZER_OPTIONAL, isOrganizerOptional != null ? isOrganizerOptional : false,
                        Constants.REQUIRED_ATTENDEE, requiredAttendee != null ? requiredAttendee : List.of(),
                        Constants.OPTIONAL_ATTENDEE, optionalAttendee != null ? optionalAttendee : List.of(),
                        Constants.TIME_ZONE, timeZone != null ? timeZone : "",
                        Constants.MEETING_DURATION, meetingDuration != null ? meetingDuration : "");
                LOGGER.debug("→ Confirmation parameters: {}", confirmationParams);

                telemetryHelper.recordRetryPrompted("office365Calendar.getFreeSlots", startTime,
                        safeTagMap("validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_GET_FREE_SLOTS, confirmationParams);
                LOGGER.info("→ GET FREE SLOTS VALIDATION FAILED END");
                telemetryHelper.recordSuccess("office365Calendar.getFreeSlots", startTime,
                        safeTagMap("required_attendee_count", String.valueOf(requiredAttendee != null ? requiredAttendee.size() : 0),
                                  "optional_attendee_count", String.valueOf(optionalAttendee != null ? optionalAttendee.size() : 0)));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("→ GET FREE SLOTS EXCEPTION: {} - {}", cause.getClass().getSimpleName(), cause.getMessage(), cause);
            telemetryHelper.recordError("office365Calendar.getFreeSlots", startTime, cause, safeTagMap());
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in get free slots: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            ExtensionResponse response = new ExtensionResponseBuilder().failure(error).build();
            LOGGER.error("→ GET FREE SLOTS EXCEPTION END");
            return response;
        }
    }

    /**
     * Retrieves scheduled meetings for the given date range and attendees.
     *
     * @param dateFrom start date (epoch millis)
     * @param dateTo end date (epoch millis)
     * @param attendees list of attendee emails
     * @param timeZone time zone (optional)
     * @return an extension response containing scheduled meetings or error response with validation prompts
     */
    @CatalogRequest(
            id = "localDomainRequest_6c9fb027-ab27-48ef-b966-d0f07a62fc32",
            name = "Get Schedules",
            description = "Get list of scheduled meetings for the given date and attendees",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "List of Meetings", type = "[ { Attendee: Email, Schedules: [ { Scheduled Time: DateTimeRange, Status: Text } ] } ]", required = false)
    public ExtensionResponse getSchedules(
            @Field(name = "Date From", type = "Date", attributes = @Attribute(name = "includeTimeOfDay", value = "true")) Long dateFrom,
            @Field(name = "Date To", type = "Date", attributes = @Attribute(name = "includeTimeOfDay", value = "true")) Long dateTo,
            @Field.Desc(name = "Attendees", type = "[ Email ]") List<String> attendees,
            @Field(name = "Time Zone", type = "Text", required = false) String timeZone) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.getSchedules");
            LOGGER.info("→ GET SCHEDULES START");
            LOGGER.info("→ Input parameters: dateFrom={}, dateTo={}, attendees={}, timeZone={}", dateFrom, dateTo, attendees, timeZone);

            // Prepare validation parameters
            LOGGER.debug("→ Preparing validation parameters...");
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();

            // Validate date range
            if (dateFrom != null && dateTo != null) {
                LOGGER.debug("→ Adding date validation parameters: FROM_DATE={}, TO_DATE={}", dateFrom, dateTo);
                validationParams.put(Validator.ValidationResource.FROM_DATE, String.valueOf(dateFrom));
                validationParams.put(Validator.ValidationResource.TO_DATE, String.valueOf(dateTo));
            } else {
                LOGGER.debug("→ Skipping date validation - dateFrom or dateTo is null");
            }

            // Validate time zone
            if (timeZone != null && !timeZone.trim().isEmpty()) {
                LOGGER.debug("→ Adding time zone validation parameter: TIME_ZONE={}", timeZone);
                validationParams.put(Validator.ValidationResource.TIME_ZONE, timeZone);
            } else {
                LOGGER.debug("→ Skipping time zone validation - timeZone is null or empty");
            }

            LOGGER.debug("→ Final validation parameters: {}", validationParams);

            // Validate input parameters using dynamic field attribute extraction
            LOGGER.debug("→ Calling validation orchestrator with dynamic attribute extraction...");
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "getSchedules");

            LOGGER.debug("→ Final Validation Results: {}", validationResults);

            LOGGER.debug("→ Validation results count: {}", validationResults.size());
            if (!validationResults.isEmpty()) {
                LOGGER.debug("→ Validation results: {}", validationResults);
            }

            if (validationResults.isEmpty()) {
                // Validation passed, perform the get schedules operation
                LOGGER.debug("→ Validation passed, calling meetingDao.getSchedules...");
                List<Map<String, Object>> schedules = meetingDao.getSchedules(dateFrom, dateTo, attendees, timeZone);
                LOGGER.info("→ Retrieved schedules count: {}", (schedules != null ? schedules.size() : "null"));
                ExtensionResponse response = new ExtensionResponseBuilder().success(Map.of("List of Meetings", schedules)).build();
                LOGGER.info("→ GET SCHEDULES SUCCESS END");
                telemetryHelper.recordSuccess("office365Calendar.getSchedules", startTime,
                        safeTagMap("schedules_count", String.valueOf(schedules != null ? schedules.size() : 0),
                                  "attendees_count", String.valueOf(attendees != null ? attendees.size() : 0)));
                return response;
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                LOGGER.debug("→ Validation failed, preparing confirmation response...");
                String stateId = UUID.randomUUID().toString();
                LOGGER.debug("→ Generated stateId: {}", stateId);

                Map<String, Object> stateData = Map.of(Constants.VALIDATION_RESULTS, validationResults);
                LOGGER.debug("→ Storing state data: {}", stateData);
                errorHandlingStateManager.put(stateId, stateData);

                Map<String, Object> confirmationParams = Map.of(
                        Constants.STATE_ID, stateId,
                        Constants.FROM_DATE, dateFrom != null ? dateFrom : 0L,
                        Constants.TO_DATE, dateTo != null ? dateTo : 0L,
                        Constants.ATTENDEES, attendees != null ? attendees : List.of(),
                        Constants.TIME_ZONE, timeZone != null ? timeZone : "");
                LOGGER.debug("→ Confirmation parameters: {}", confirmationParams);

                telemetryHelper.recordRetryPrompted("office365Calendar.getSchedules", startTime,
                        safeTagMap("validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_GET_SCHEDULES, confirmationParams);
                LOGGER.info("→ GET SCHEDULES VALIDATION FAILED END");
                telemetryHelper.recordSuccess("office365Calendar.getSchedules", startTime,
                        safeTagMap("attendee_count", String.valueOf(attendees != null ? attendees.size() : 0)));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("→ GET SCHEDULES EXCEPTION: {} - {}", cause.getClass().getSimpleName(), cause.getMessage(), cause);
            telemetryHelper.recordError("office365Calendar.getSchedules", startTime, cause, safeTagMap());
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in get schedules: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            ExtensionResponse response = new ExtensionResponseBuilder().failure(error).build();
            LOGGER.error("→ GET SCHEDULES EXCEPTION END");
            return response;
        }
    }

    /**
     * Retrieves event details for the given event ID.
     *
     * @param eventID the ID of the event
     * @return the event details, or null if not found
     */
    @CatalogRequest(
            id = "localDomainRequest_48062a11-b237-4307-9394-a6c3496bff14",
            name = "Get Event",
            description = "Get Event details for the given Event ID",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse getEvent(
            @Field(name = "Event ID", type = "Text") String eventID) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.getEvent");
            LOGGER.info("getEvent: eventID={}", eventID);

            // Validate input parameters
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();

            // Validate Event ID (required)
            if (eventID != null && !eventID.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.EVENT_ID, eventID);
            }

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "getEvent");

            LOGGER.debug("First Validation Result: {}", validationResults);

            if (validationResults.isEmpty()) {
                // Validation passed, perform the get event operation
                Meeting meeting = meetingDao.getMeetingById(eventID);
                EventDetails eventDetails = CatalogTypes.fromEvent(meeting);
                telemetryHelper.recordSuccess("office365Calendar.getEvent", startTime,
                        safeTagMap("event_id", eventID));
                return new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));

                telemetryHelper.recordRetryPrompted("office365Calendar.getEvent", startTime,
                        safeTagMap("event_id", eventID, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_GET_EVENT,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.EVENT_ID_FIELD, eventID != null ? eventID : ""));
                telemetryHelper.recordSuccess("office365Calendar.getEvent", startTime, safeTagMap("event_id", eventID));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("Error occurred in getEvent: {}", cause.getMessage());
            telemetryHelper.recordError("office365Calendar.getEvent", startTime, cause,
                    safeTagMap("event_id", eventID));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in get event: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Searches for events matching the given search terms.
     *
     * @param searchTerms the search keywords
     * @return a list of search result maps
     */
    @CatalogRequest(
            id = "localDomainRequest_c5aabea7-b876-471a-964c-37c154a1a5fe",
            name = "Search Events",
            description = "Get all the events that matches with given search keys",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Search Results", type = "[ { Event ID: Text, Event Title: Text, Scheduled Time: DateTimeRange } ]", required = false)
    public List<Map<String, Object>> searchEvents(
            @Field(name = "Search Terms", type = "Text", required = false) String searchTerms) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.searchEvents");
            List<Map<String, Object>> results = meetingDao.getQueryResults(searchTerms);
            telemetryHelper.recordSuccess("office365Calendar.searchEvents", startTime,
                    safeTagMap("search_terms", searchTerms, "results_count", String.valueOf(results != null ? results.size() : 0)));
            return results;
        } catch (Exception cause) {
            telemetryHelper.recordError("office365Calendar.searchEvents", startTime, cause,
                    safeTagMap("search_terms", searchTerms));
            throw cause;
        }
    }

    /**
     * Retrieves events for a given user within a date range.
     *
     * @param userEmail the user's email
     * @param from start date (epoch millis)
     * @param to end date (epoch millis)
     * @return an extension response containing events or error information
     */
    @CatalogRequest(
            id = "localDomainRequest_74cb78ed-7c37-4234-850b-29069186c9a8",
            name = "Get Events for a given User",
            description = "This request will return the events scheduled for a given user given that the setup is done using a system account.",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = Constants.EVENTS, type = "[ Entity(Event Details) ]", required = false)
    public ExtensionResponse getEventsForAGivenUser(
            @Field(name = "User Email", type = "Email", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String userEmail,
            @Field.Date(name = "From", required = true, includeTimeOfDay = true, showHowManyDaysInViewer = 60, allowPast = true, allowToday = true, allowFuture = true, defaultTimeSpan = 1, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Long from,
            @Field.Date(name = "To", required = true, includeTimeOfDay = true, showHowManyDaysInViewer = 60, allowPast = true, allowToday = true, allowFuture = true, defaultTimeSpan = 1, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Long to) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.getEventsForUser");
            LOGGER.info("getEventsForAGivenUser: userEmail={}, from={}, to={}", userEmail, from, to);

            // Validate input parameters
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();

            // Validate date range (required)
            if (from != null && to != null) {
                validationParams.put(Validator.ValidationResource.FROM_DATE, String.valueOf(from));
                validationParams.put(Validator.ValidationResource.TO_DATE, String.valueOf(to));
            }

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "getEventsForAGivenUser");

            if (validationResults.isEmpty()) {
                // Validation passed, perform the get events operation
                List<Meeting> meetings = meetingDao.getEventsForUser(userEmail, from, to);
                List<EventDetails> events = meetings.stream().map(CatalogTypes::fromEvent).collect(Collectors.toList());
                telemetryHelper.recordSuccess("office365Calendar.getEventsForUser", startTime,
                        safeTagMap("user_email", userEmail, "events_count", String.valueOf(events.size())));
                return new ExtensionResponseBuilder().success(Map.of(Constants.EVENTS, events)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));

                telemetryHelper.recordRetryPrompted("office365Calendar.getEventsForUser", startTime,
                        safeTagMap("user_email", userEmail, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_GET_EVENTS_FOR_USER,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.USER_EMAIL, userEmail != null ? userEmail : "",
                              Constants.FROM, from != null ? from : 0L,
                              Constants.TO, to != null ? to : 0L));
                telemetryHelper.recordSuccess("office365Calendar.getEventsForUser", startTime,
                        safeTagMap("user_email", userEmail));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("Error occurred in getEventsForAGivenUser: {}", cause.getMessage());
            telemetryHelper.recordError("office365Calendar.getEventsForUser", startTime, cause,
                    safeTagMap("user_email", userEmail));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in get events for user: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Retrieves all attachments for a specific meeting for a given user.
     *
     * @param userEmail the user's email
     * @param eventID the event ID
     * @return an extension response containing attachments
     */
    @CatalogRequest(
            id = "localDomainRequest_1be1993a-8abe-4993-96cb-231025d92f13",
            name = "Get Attachments for a given User",
            description = "This request will return all attachments for a specific meeting for a given user given that the setup is done using a system account.",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = Constants.ATTACHMENTS, type = "[ File ]", required = false)
    public ExtensionResponse getAttachmentsForAGivenUser(
            @Field.Text(name = "User Email", required = true, attributes = {
                    @Attribute(name = "visualWidth", value = "S")}, options = {}) String userEmail,
            @Field.Text(name = "Event ID", required = true, attributes = {
                    @Attribute(name = "visualWidth", value = "S")}, options = {}) String eventID) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.getAttachmentsForUser");
            LOGGER.info("getAttachmentsForAGivenUser: userEmail={}, eventID={}", userEmail, eventID);

            // Validate input parameters
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();

            // Validate User Email (required)
            if (userEmail != null && !userEmail.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.EMAIL, userEmail);
            }

            // Validate Event ID (required)
            if (eventID != null && !eventID.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.EVENT_ID, eventID);
            }

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "getAttachmentsForAGivenUser");

            if (validationResults.isEmpty()) {
                // Validation passed, perform the get attachments operation
                List<FileAttachment> fileAttachments = meetingDao.getAttachmentsForAuthenticatedUser(eventID);
                List<File> attachments = catalogTypes.toKristaFilList(fileAttachments);
                telemetryHelper.recordSuccess("office365Calendar.getAttachmentsForUser", startTime,
                        safeTagMap("user_email", userEmail, "event_id", eventID, "attachments_count", String.valueOf(attachments.size())));
                return new ExtensionResponseBuilder().success(Map.of(Constants.ATTACHMENTS, attachments)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));

                telemetryHelper.recordRetryPrompted("office365Calendar.getAttachmentsForUser", startTime,
                        safeTagMap("user_email", userEmail, "event_id", eventID, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_GET_ATTACHMENTS_FOR_USER,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.USER_EMAIL, userEmail != null ? userEmail : "",
                              Constants.EVENT_ID_FIELD, eventID != null ? eventID : ""));
                telemetryHelper.recordSuccess("office365Calendar.getAttachmentsForUser", startTime,
                        safeTagMap("user_email", userEmail, "event_id", eventID));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("Error occurred in getAttachmentsForAGivenUser: {}", cause.getMessage());
            telemetryHelper.recordError("office365Calendar.getAttachmentsForUser", startTime, cause,
                    safeTagMap("user_email", userEmail, "event_id", eventID));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in get attachments for user: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Retrieves all attachments for a specific meeting for the current logged-in user.
     *
     * @param eventID the event ID
     * @return an extension response containing attachments
     */
    @CatalogRequest(
            id = "localDomainRequest_a9483149-0a01-4f0f-b7a8-5ed475dc5871",
            name = "Get Attachments",
            description = "This request will return all attachments for a specific meeting for a current logged in user given that the setup is done using a system account.",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Attachments", type = "[ File ]", required = false)
    public ExtensionResponse getAttachments(
            @Field.Text(name = "Event ID", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}) String eventID) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("office365Calendar.getAttachments");
            LOGGER.info("getAttachments: eventID={}", eventID);

            // Validate input parameters
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();

            // Validate Event ID (required)
            if (eventID != null && !eventID.trim().isEmpty()) {
                validationParams.put(Validator.ValidationResource.EVENT_ID, eventID);
            }

            // Validate input parameters using dynamic field attribute extraction
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams, MeetingsArea.class, "getAttachments");

            if (validationResults.isEmpty()) {
                // Validation passed, perform the get attachments operation
                List<FileAttachment> fileAttachments = meetingDao.getAttachmentsForAuthenticatedUser(eventID);
                 LOGGER.info("  → Attachments retrieved successfully");
                LOGGER.info("  → Number of attachments found: {}", fileAttachments.size());
                List<File> attachments = catalogTypes.toKristaFilList(fileAttachments);
                LOGGER.info("  → Attachments converted to Krista File objects");
                LOGGER.info("  → Number of Krista File objects: {}", attachments.size());
                telemetryHelper.recordSuccess("office365Calendar.getAttachments", startTime,
                        safeTagMap("event_id", eventID, "attachments_count", String.valueOf(attachments.size())));
                return new ExtensionResponseBuilder().success(Map.of(Constants.ATTACHMENTS, attachments)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));

                telemetryHelper.recordRetryPrompted("office365Calendar.getAttachments", startTime,
                        safeTagMap("event_id", eventID, "validation_count", String.valueOf(validationResults.size())));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_GET_ATTACHMENTS,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.EVENT_ID_FIELD, eventID != null ? eventID : ""));
                telemetryHelper.recordSuccess("office365Calendar.getAttachments", startTime, safeTagMap("event_id", eventID));
                return response;
            }
        } catch (Exception cause) {
            LOGGER.error("Error occurred in getAttachments: {}", cause.getMessage());
            telemetryHelper.recordError("office365Calendar.getAttachments", startTime, cause,
                    safeTagMap("event_id", eventID));
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in get attachments: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }
}