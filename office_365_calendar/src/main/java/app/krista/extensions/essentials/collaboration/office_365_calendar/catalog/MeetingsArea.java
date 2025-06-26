package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity.EventDetails;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.RemediationActionFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.model.base.File;
import com.microsoft.graph.http.GraphServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Inject
    public MeetingsArea(MeetingDaoImpl meetingDao, CatalogTypes catalogTypes, ValidationOrchestrator validationOrchestrator, ErrorHandlingStateManager errorHandlingStateManager, ExtensionResponseGenerator responseGenerator) {
        this.meetingDao = meetingDao;
        this.catalogTypes = catalogTypes;
        this.validationOrchestrator = validationOrchestrator;
        this.errorHandlingStateManager = errorHandlingStateManager;
        this.responseGenerator = responseGenerator;
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
            description = "Cancel event for given event id",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = Constants.EVENT_DETAILS, type = "Entity(Event Details)", required = false)
    public ExtensionResponse cancelEvent(
            @Field(name = "Event Id", type = "Text") String eventId) {
        return meetingDao.cancelMeetingWithErrorHandling(eventId, validationOrchestrator, catalogTypes, errorHandlingStateManager);
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
            description = "Get list of next upcoming meetings",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "[ Entity(Event Details) ]", required = false)
    public ExtensionResponse getNextUpcomingMeetings(
            @Field(name = "Number of meeting", type = "Number") Double numberOfMeeting) {
        return meetingDao.getMeetingsWithErrorHandling(numberOfMeeting, validationOrchestrator, catalogTypes, errorHandlingStateManager);
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
            description = "Decline invitation",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse declineInvitation(
            @Field(name = "Event Id", type = "Text") String eventId) {
        try {
            LOGGER.info("declineInvitation: eventId={}", eventId);

            // Validate input parameters
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.EVENT_ID, eventId));

            if (validationResults.isEmpty()) {
                // Validation passed, perform the decline invitation operation
                EventDetails eventDetails = CatalogTypes.fromEvent(meetingDao.declineMeetingInvitation(eventId));
                return new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                String stateId = UUID.randomUUID().toString();
                errorHandlingStateManager.put(stateId, Map.of(
                        Constants.VALIDATION_RESULTS, validationResults));

                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_DECLINE_INVITATION,
                        Map.of(Constants.STATE_ID, stateId,
                              Constants.EVENT_ID, eventId != null ? eventId : ""));
            }
        } catch (Exception cause) {
            LOGGER.error("Error occurred in declineInvitation: {}", cause.getMessage());
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in decline invitation: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Marks the invitation as tentative for the given event ID.
     *
     * @param eventId the ID of the event
     * @return the details of the event
     */
    @CatalogRequest(
            id = "localDomainRequest_5bf7458f-953f-40c4-b75c-17b1f76a08b1",
            name = "Tentative Invitation",
            description = "Tentative invitation",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails tentativeInvitation(
            @Field(name = "Event Id", type = "Text") String eventId) {
        return CatalogTypes.fromEvent(meetingDao.tentativeMeetingInvitation(eventId));
    }

    /**
     * Forwards the event to the specified attendees.
     *
     * @param eventId the ID of the event to forward
     * @param attendeeEmail the list of attendee emails
     * @return true if forwarding was successful
     * @throws IOException if forwarding fails
     */
    @CatalogRequest(
            id = "localDomainRequest_f2007cf4-d127-4a30-af0c-794b28f41de8",
            name = "Forward Event",
            description = "Forward Event",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "IsForwarded", type = "Switch", required = false)
    public Boolean forwardEvent(
            @Field(name = "Event Id", type = "Text") String eventId,
            @Field.Desc(name = "Attendee Email", type = "[ Email ]") List<String> attendeeEmail) throws IOException {
        try {
            meetingDao.forwardMeeting(eventId, attendeeEmail);
            return true;
        } catch (IOException cause) {
            throw new IOException("Failed to forward event " + cause.getMessage());
        }
    }

    /**
     * Schedules a new meeting with the provided details.
     *
     * @return the details of the scheduled event
     */
    @CatalogRequest(
            id = "localDomainRequest_f9cf27ee-ffe7-4b65-8e0a-ce0afa25f5dc",
            name = "Schedule Meeting",
            description = "Schedule meeting with given details",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails scheduleMeeting(
            @Field.Text(name = "Title", required = false, attributes = {}, options = {}) String title,
            @Field.Text(name = "Location", required = false, attributes = {}, options = {}) String location,
            @Field.Date(name = "Date From", required = true, includeTimeOfDay = true, allowToday = false, allowFuture = false, attributes = {}, options = {}) Long dateFrom,
            @Field.Date(name = "Date to", required = false, includeTimeOfDay = true, allowToday = false, allowFuture = false, attributes = {}, options = {}) Long dateTo,
            @Field.Desc(name = "Required Attendee Email", type = "[ Email ]", required = false) List<String> requiredAttendeeEmail,
            @Field.Desc(name = "Optional Attendee Email", type = "[ Email ]", required = false) List<String> optionalAttendeeEmail,
            @Field.Boolean(name = "Is All Day", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Boolean isAllDay,
            @Field.Text(name = "Time Zone", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String timeZone,
            @Field.PickOne(name = "Reminder", values = {"None", "Don't remind me", "At event start", "At time of event", "5 minutes before", "15 minutes before", "30 minutes before", "1 hour before", "2 hours before", "4 hours before", "12 hours before", "1 day before", "1 week before"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String reminder,
            @Field.PickOne(name = "Recurrence", values = {"Never", "Does Not Repeat", "Daily", "Weekly", "Monthly", "Yearly"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String recurrence,
            @Field(name = "Recurrence Interval", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Double recurrenceInterval,
            @Field.Desc(name = "Days Of Week", type = "[ Text ]", required = false) List<String> daysOfWeek,
            @Field.PickOne(name = "Recurrence Type", values = {"Absolute recurrence", "Relative recurrence"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String recurrenceType,
            @Field(name = "Day Of Month", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Double dayOfMonth,
            @Field.PickOne(name = "Repeat Index", values = {"First", "Second", "Third", "Fourth", "Last"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String repeatIndex,
            @Field.PickOne(name = "Repeat Day", values = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String repeatDay,
            @Field.PickOne(name = "Repeat Month", values = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String repeatMonth,
            @Field.PickOne(name = "End Recurrence", values = {"End date", "Numbered occurrences", "No end"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String endRecurrence,
            @Field.Date(name = "End Date", required = false, includeTimeOfDay = true, allowPast = false, allowToday = false, allowFuture = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Long endDate,
            @Field(name = "Numbered Occurrences", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Double numberedOccurrences,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) List<File> attachments,
            @Field(name = "Details", type = "RichText", required = false, attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String details,
            @Field.Boolean(name = "Is Teams Meeting", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Boolean isTeamsMeeting) {
        return CatalogTypes.fromEvent(meetingDao.addMeeting(title, location, dateFrom, dateTo, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, timeZone, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, catalogTypes.toAttachment(attachments), details, isTeamsMeeting));
    }

    /**
     * Retrieves all unaccepted events.
     *
     * @return a list of unaccepted event details
     */
    @CatalogRequest(
            id = "localDomainRequest_e0f748a2-17df-4e29-84e0-a1d0d06934d9",
            name = "Get All Unaccepted Events",
            description = "Get list of all unaccepted events",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "[ Entity(Event Details) ]", required = false)
    public List<EventDetails> getAllUnacceptedEvents() {
        return meetingDao.getUnAcceptedMeetings().stream().map(CatalogTypes::fromEvent).collect(Collectors.toList());
    }

    /**
     * Adds attendees to the specified event.
     *
     * @param eventId the ID of the event
     * @param requiredAttendeeEmail list of required attendee emails
     * @param optionalAttendeeEmail list of optional attendee emails
     * @return the updated event details
     */
    @CatalogRequest(
            id = "localDomainRequest_2e73b843-ace4-4b80-bfab-4f466706e5c5",
            name = "Add Attendees",
            description = "Add attendees for the given event",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails addAttendees(
            @Field(name = "Event Id", type = "Text") String eventId,
            @Field.Desc(name = "Attendee Email", type = "[ Email ]") List<String> requiredAttendeeEmail,
            @Field.Desc(name = "Optional Attendee Email", type = "[ Email ]", required = false) List<String> optionalAttendeeEmail) {
        return CatalogTypes.fromEvent(meetingDao.addAttendees(eventId, requiredAttendeeEmail, optionalAttendeeEmail));
    }

    /**
     * Updates the event with the given details.
     *
     * @return the updated event details
     */
    @CatalogRequest(
            id = "localDomainRequest_ff16001f-6709-44a8-9f00-5de09287e70e",
            name = "Update Event",
            description = "Update Event",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails updateEvent(
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

        return CatalogTypes.fromEvent(meetingDao.updateMeeting(eventId, title, details, location, dateFrom, dateTo, timeZone, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, recurrence, reminder, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, isTeamsMeeting));
    }

    /**
     * Accepts the invitation for the given event ID.
     *
     * @param eventId the ID of the event to accept
     * @return the details of the accepted event
     */
    @CatalogRequest(
            id = "localDomainRequest_d564897b-ad17-4289-af2c-bd79425c5341",
            name = "Accept Invitation",
            description = "Accept invitation for the given event id",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails acceptInvitation(
            @Field(name = "Event Id", type = "Text") String eventId) {
        return CatalogTypes.fromEvent(meetingDao.acceptMeetingInvitation(eventId));
    }

    /**
     * Removes attendees from the specified event.
     *
     * @param eventId the ID of the event
     * @param attendeeEmail list of attendee emails to remove
     * @return the updated event details
     */
    @CatalogRequest(
            id = "localDomainRequest_653a694e-2327-41d5-8a8c-48b30b07dc89",
            name = "Remove Attendee",
            description = "Remove attendee from a given event id",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails removeAttendee(
            @Field(name = "Event Id", type = "Text") String eventId,
            @Field.Desc(name = "Attendee Email", type = "[ Email ]") List<String> attendeeEmail) {
        return CatalogTypes.fromEvent(meetingDao.removeAttendees(eventId, attendeeEmail));
    }

    /**
     * Retrieves free meeting slots based on the provided criteria.
     *
     * @return a list of suggested free slots
     * @throws DatatypeConfigurationException if date configuration fails
     */
    @CatalogRequest(
            id = "localDomainRequest_7bbeb961-4ba2-4c89-8589-87a5226c33a4",
            name = "Get Free Slots",
            description = "Get list of meeting suggestions",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "List of Free Slots", type = "[ { Time Slot: DateTimeRange, Attendee Availability: [ { Attendee: Text, Availability: Text } ], Confidence: Percentage, Organizer Availability: Text, Suggestion Reason: Text } ]", required = false)
    public List<Map<String, Object>> getFreeSlots(
            @Field.Date(name = "Date From", required = true, showHowManyDaysInViewer = 60, allowPast = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) Long dateFrom,
            @Field.Date(name = "Date To", required = true, showHowManyDaysInViewer = 60, allowPast = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) Long dateTo,
            @Field.Boolean(name = "Is Organizer Optional", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean isOrganizerOptional,
            @Field.Desc(name = "Required Attendee", type = "[ Email ]") List<String> requiredAttendee,
            @Field.Desc(name = "Optional Attendee", type = "[ Email ]", required = false) List<String> optionalAttendee,
            @Field.Text(name = "Time Zone", attributes = {@Attribute(name = "visualWidth", value = "S")}) String timeZone,
            @Field.Text(name = "Meeting Duration", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) String meetingDuration)
            throws DatatypeConfigurationException {
        return meetingDao.getFreeSlot(dateFrom, dateTo, isOrganizerOptional, requiredAttendee, optionalAttendee, timeZone, meetingDuration);
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
        try {
            System.out.println("=== GET SCHEDULES START ===");
            System.out.println("Input parameters:");
            System.out.println("  dateFrom: " + dateFrom);
            System.out.println("  dateTo: " + dateTo);
            System.out.println("  attendees: " + attendees);
            System.out.println("  timeZone: " + timeZone);
            LOGGER.info("getSchedules: dateFrom={}, dateTo={}, attendees={}, timeZone={}", dateFrom, dateTo, attendees, timeZone);

            // Prepare validation parameters
            System.out.println("Preparing validation parameters...");
            Map<Validator.ValidationResource, String> validationParams = new HashMap<>();

            // Validate date range
            if (dateFrom != null && dateTo != null) {
                System.out.println("Adding date validation parameters:");
                System.out.println("  FROM_DATE: " + String.valueOf(dateFrom));
                System.out.println("  TO_DATE: " + String.valueOf(dateTo));
                validationParams.put(Validator.ValidationResource.FROM_DATE, String.valueOf(dateFrom));
                validationParams.put(Validator.ValidationResource.TO_DATE, String.valueOf(dateTo));
                validationParams.put(Validator.ValidationResource.DATE_RANGE, "dateRange");
            } else {
                System.out.println("Skipping date validation - dateFrom or dateTo is null");
            }

            if (dateFrom != null && dateTo != null) {
                System.out.println("Adding date validation parameters:");
                System.out.println("  FROM_DATE: " + String.valueOf(dateFrom));
                System.out.println("  TO_DATE: " + String.valueOf(dateTo));
                validationParams.put(Validator.ValidationResource.FROM_DATE, String.valueOf(dateFrom));
                validationParams.put(Validator.ValidationResource.TO_DATE, String.valueOf(dateTo));
                validationParams.put(Validator.ValidationResource.DATE_RANGE_TO, "dateRange");
            } else {
                System.out.println("Skipping date validation - dateFrom or dateTo is null");
            }

            // Validate attendee emails
            if (attendees != null && !attendees.isEmpty()) {
                System.out.println("Adding email validation parameters:");
                for (String attendee : attendees) {
                    if (attendee != null && !attendee.trim().isEmpty()) {
                        System.out.println("  EMAIL: " + attendee);
                        validationParams.put(Validator.ValidationResource.EMAIL, attendee);
                        break; // Validate at least one email for the pattern
                    }
                }
            } else {
                System.out.println("Skipping email validation - attendees is null or empty");
            }

            System.out.println("Final validation parameters: " + validationParams);

            // Validate input parameters
            System.out.println("Calling validation orchestrator...");
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(validationParams);

            System.out.println("Validation results count: " + validationResults.size());
            if (!validationResults.isEmpty()) {
                System.out.println("Validation results:");
                for (ValidationOrchestrator.ValidationResult result : validationResults) {
                    System.out.println("  - " + result);
                }
            }

            if (validationResults.isEmpty()) {
                // Validation passed, perform the get schedules operation
                System.out.println("Validation passed, calling meetingDao.getSchedules...");
                List<Map<String, Object>> schedules = meetingDao.getSchedules(dateFrom, dateTo, attendees, timeZone);
                System.out.println("Retrieved schedules count: " + (schedules != null ? schedules.size() : "null"));
                ExtensionResponse response = new ExtensionResponseBuilder().success(Map.of("List of Meetings", schedules)).build();
                System.out.println("=== GET SCHEDULES SUCCESS END ===");
                return response;
            } else {
                // Store validation results and call confirmation SubCatalogRequest
                System.out.println("Validation failed, preparing confirmation response...");
                String stateId = UUID.randomUUID().toString();
                System.out.println("Generated stateId: " + stateId);

                Map<String, Object> stateData = Map.of(Constants.VALIDATION_RESULTS, validationResults);
                System.out.println("Storing state data: " + stateData);
                errorHandlingStateManager.put(stateId, stateData);

                Map<String, Object> confirmationParams = Map.of(
                        Constants.STATE_ID, stateId,
                        Constants.FROM_DATE, dateFrom != null ? dateFrom : 0L,
                        Constants.TO_DATE, dateTo != null ? dateTo : 0L,
                        Constants.ATTENDEES, attendees != null ? attendees : List.of(),
                        Constants.TIME_ZONE, timeZone != null ? timeZone : "");
                System.out.println("Confirmation parameters: " + confirmationParams);

                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        Constants.CONFIRM_REENTER_GET_SCHEDULES, confirmationParams);
                System.out.println("=== GET SCHEDULES VALIDATION FAILED END ===");
                return response;
            }
        } catch (Exception cause) {
            System.out.println("=== GET SCHEDULES EXCEPTION ===");
            System.out.println("Exception: " + cause.getClass().getSimpleName());
            System.out.println("Message: " + cause.getMessage());
            cause.printStackTrace();
            LOGGER.error("Error occurred in getSchedules: {}", cause.getMessage());
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error occurred in get schedules: " + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    ""
            );
            ExtensionResponse response = new ExtensionResponseBuilder().failure(error).build();
            System.out.println("=== GET SCHEDULES EXCEPTION END ===");
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
        return meetingDao.getMeetingByIdWithErrorHandling(eventID, validationOrchestrator, catalogTypes, errorHandlingStateManager);
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
        return meetingDao.getQueryResults(searchTerms);
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
        return meetingDao.getEventsForUserWithErrorHandling(userEmail, from, to, validationOrchestrator, catalogTypes, errorHandlingStateManager);
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
        return meetingDao.getAttachmentsForGivenUserWithErrorHandling(userEmail, eventID, validationOrchestrator, catalogTypes, errorHandlingStateManager);
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
        return meetingDao.getAttachmentsForAuthenticatedUserWithErrorHandling(eventID, validationOrchestrator, catalogTypes, errorHandlingStateManager);
    }
}