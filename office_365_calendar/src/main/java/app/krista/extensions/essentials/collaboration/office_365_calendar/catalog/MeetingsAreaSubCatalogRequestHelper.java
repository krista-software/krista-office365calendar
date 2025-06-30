package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.SubCatalogRequest;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity.EventDetails;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.model.base.File;
import com.microsoft.graph.models.FileAttachment;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class for handling sub-catalog requests related to meeting attachments and validation flows
 * in the Office 365 calendar integration.
 * <p>
 * Provides endpoints for confirming re-entry of details, handling user prompts, and retrieving attachments
 * for both authenticated and specified users. This class is typically used by the Krista platform's
 * catalog infrastructure to support remediation and error handling flows.
 * </p>
 */
@Service
public class MeetingsAreaSubCatalogRequestHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeetingsAreaSubCatalogRequestHelper.class);

    private final MeetingDaoImpl meetingDao;
    private final CatalogTypes catalogTypes;
    private final ErrorHandlingStateManager internalStateManager;
    private final ExtensionResponseGenerator responseGenerator;

    /**
     * Constructs a new MeetingsAreaSubCatalogRequestHelper with required dependencies.
     *
     * @param meetingDao the meeting DAO for event operations
     * @param catalogTypes the catalog types utility
     * @param internalStateManager the error handling state manager
     * @param responseGenerator the extension response generator
     */
    @Inject
    public MeetingsAreaSubCatalogRequestHelper(MeetingDaoImpl meetingDao, CatalogTypes catalogTypes, ErrorHandlingStateManager internalStateManager, ExtensionResponseGenerator responseGenerator) {
        this.meetingDao = meetingDao;
        this.catalogTypes = catalogTypes;
        this.internalStateManager = internalStateManager;
        this.responseGenerator = responseGenerator;
    }

    /**
     * Handles confirmation of whether the user wants to re-enter details for getting attachments for a given user.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, email, and eventId
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(name = "confirmReenterGetAttachmentsForAGivenUser", description = "Checks if user wants to re-enter details and if yes, sends prompt to do so", type = CatalogRequest.Type.QUERY_SYSTEM)
    public ExtensionResponse confirmReenterGetAttachmentsForAGivenUser(@Field.Desc(name = "inputMap", type = "{ re-enter: Boolean, stateId: Text, email: Text, eventId: Text}") Map<String, Object> map) {
        Boolean reEnter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);

        if (Boolean.FALSE.equals(reEnter)) {
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_GET_EVENT_ATTACHMENTS, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> getAttachmentsForAGivenUser ");

            return new ExtensionResponseBuilder().failure(error).build();
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults, Constants.HANDLE_RE_ENTER_GET_ATTACHMENT_FOR_GIVEN_USER, Map.of(Constants.EMAIL, map.get(Constants.EMAIL), Constants.EVENT_ID, map.get(Constants.EVENT_ID)));
    }

    /**
     * Handles the actual retrieval of attachments after the user has re-entered email and event ID.
     *
     * @param map input map containing email and eventId
     * @return an extension response containing the attachments or an error response
     */
    @SubCatalogRequest(name = Constants.HANDLE_RE_ENTER_GET_ATTACHMENT_FOR_GIVEN_USER, description = "Handle re-enter email and event id", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = Constants.ATTACHMENTS, type = "[ File ]", required = false)
    public ExtensionResponse handleReEnterGetAttachmentForGivenUser(@Field.Desc(name = "inputMap", type = "{ email: Text, eventId: Text}") Map<String, Object> map) {
        try {
            String userEmail = (String) map.get(Constants.EMAIL);
            String eventID = (String) map.get(Constants.EVENT_ID);
            List<FileAttachment> fileAttachments = meetingDao.getAttachmentsForGivenUser(userEmail, eventID);
            List<File> attachments = catalogTypes.toKristaFilList(fileAttachments);
            return new ExtensionResponseBuilder().success(Map.of(Constants.ATTACHMENTS, attachments)).build();
        } catch (Exception cause) {
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_GET_EVENT_ATTACHMENTS, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> getAttachmentsForAGivenUser ");
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Handles confirmation of whether the user wants to re-enter details for getting attachments for the authenticated user.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, and eventId
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(name = "confirmReenterGetAttachments", description = "Checks if user wants to re-enter details and if yes, sends prompt to do so", type = CatalogRequest.Type.QUERY_SYSTEM)
    public ExtensionResponse confirmReenterGetAttachments(@Field.Desc(name = "inputMap", type = "{ re-enter: Boolean, stateId: Text, eventId: Text}") Map<String, Object> map) {
        Boolean reEnter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);

        if (Boolean.FALSE.equals(reEnter)) {
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_GET_EVENT_ATTACHMENTS, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> getAttachmentsForAGivenUser ");

            return new ExtensionResponseBuilder().failure(error).build();
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults, Constants.HANDLE_RE_ENTER_GET_ATTACHMENT, Map.of(Constants.EVENT_ID, map.get(Constants.EVENT_ID)));
    }

    /**
     * Handles the actual retrieval of attachments after the user has re-entered the event ID for the authenticated user.
     *
     * @param map input map containing eventId
     * @return an extension response containing the attachments or an error response
     */
    @SubCatalogRequest(name = Constants.HANDLE_RE_ENTER_GET_ATTACHMENT, description = "Handle re-enter event id", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = Constants.ATTACHMENTS, type = "[ File ]", required = false)
    public ExtensionResponse handleReenterGetAttachments(@Field.Desc(name = "inputMap", type = "{eventId: Text}") Map<String, Object> map) {
        try {
            String eventID = (String) map.get(Constants.EVENT_ID);
            List<FileAttachment> fileAttachments = meetingDao.getAttachmentsForAuthenticatedUser(eventID);
            List<File> attachments = catalogTypes.toKristaFilList(fileAttachments);
            return new ExtensionResponseBuilder().success(Map.of(Constants.ATTACHMENTS, attachments)).build();
        } catch (Exception cause) {
            System.out.println(cause.getMessage() + " cause.getMessage() ");
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_GET_EVENT_ATTACHMENTS, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> getAttachmentsForAGivenUser ");
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Handles confirmation of whether the user wants to re-enter details for getting events for a given user.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, userEmail, fromDate, and toDate
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(name = "confirmReenterGetEventsForUser", description = "Checks if user wants to re-enter details and if yes, sends prompt to do so", type = CatalogRequest.Type.QUERY_SYSTEM)
    public ExtensionResponse confirmReenterGetEventsForUser(@Field.Desc(name = "inputMap", type = "{ re-enter: Boolean, stateId: Text, userEmail: Text, fromDate: Number, toDate: Number}") Map<String, Object> map) {
        Boolean reEnter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);

        if (Boolean.FALSE.equals(reEnter)) {
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_GET_EVENTS_FOR_USER, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> getEventsForAGivenUser ");
            return new ExtensionResponseBuilder().failure(error).build();
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults, Constants.HANDLE_RE_ENTER_GET_EVENTS_FOR_USER, map);
    }

    /**
     * Handles the actual retrieval of events after the user has re-entered user email and date range.
     *
     * @param map input map containing userEmail, fromDate, and toDate
     * @return an extension response containing the events or an error response
     */
    @SubCatalogRequest(name = Constants.HANDLE_RE_ENTER_GET_EVENTS_FOR_USER, description = "Handle re-enter user email and date range", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = Constants.EVENTS, type = "[ Entity(Event Details) ]", required = false)
    public ExtensionResponse handleReEnterGetEventsForUser(@Field.Desc(name = "inputMap", type = "{ userEmail: Text, fromDate: Number, toDate: Number}") Map<String, Object> map) {
        try {
            String userEmail = (String) map.get(Constants.USER_EMAIL);
            Object fromDateObj = map.get(Constants.FROM_DATE);
            Object toDateObj = map.get(Constants.TO_DATE);

            Long fromDate = null;
            Long toDate = null;

            if (fromDateObj instanceof Number) {
                fromDate = ((Number) fromDateObj).longValue();
            }
            if (toDateObj instanceof Number) {
                toDate = ((Number) toDateObj).longValue();
            }

            if (fromDate != null && toDate != null && fromDate > toDate) {
                throw new IllegalArgumentException("Failed to Get Events for a given User" + Constants.THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE);
            }

            List<Meeting> meetings = meetingDao.getEventsForUser(userEmail, fromDate, toDate);
            List<EventDetails> events = meetings.stream().map(CatalogTypes::fromEvent).collect(Collectors.toList());
            return new ExtensionResponseBuilder().success(Map.of(Constants.EVENTS, events)).build();
        } catch (Exception cause) {
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_GET_EVENTS_FOR_USER, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> getEventsForAGivenUser ");
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Handles confirmation of whether the user wants to re-enter details for cancelling an event.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, and eventId
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(name = "confirmReenterCancelEvent", description = "Checks if user wants to re-enter details and if yes, sends prompt to do so", type = CatalogRequest.Type.CHANGE_SYSTEM)
    public ExtensionResponse confirmReenterCancelEvent(@Field.Desc(name = "inputMap", type = "{ re-enter: Boolean, stateId: Text, eventId: Text}") Map<String, Object> map) {
        Boolean reEnter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);

        if (Boolean.FALSE.equals(reEnter)) {
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_CANCEL_EVENT_VALIDATION, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> cancelEvent ");
            return new ExtensionResponseBuilder().failure(error).build();
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults, Constants.HANDLE_RE_ENTER_CANCEL_EVENT, map);
    }

    /**
     * Handles the actual cancellation of an event after the user has re-entered the event ID.
     *
     * @param map input map containing eventId
     * @return an extension response containing the cancelled event details or an error response
     */
    @SubCatalogRequest(name = Constants.HANDLE_RE_ENTER_CANCEL_EVENT, description = "Handle re-enter event id for cancellation", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = Constants.EVENT_DETAILS, type = "Entity(Event Details)", required = false)
    public ExtensionResponse handleReEnterCancelEvent(@Field.Desc(name = "inputMap", type = "{eventId: Text}") Map<String, Object> map) {
        try {
            String eventId = (String) map.get(Constants.EVENT_ID);
            Meeting cancelledMeeting = meetingDao.cancelMeeting(eventId);
            EventDetails eventDetails = CatalogTypes.fromEvent(cancelledMeeting);
            return new ExtensionResponseBuilder().success(Map.of(Constants.EVENT_DETAILS, eventDetails)).build();
        } catch (Exception cause) {
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_CANCEL_EVENT_VALIDATION, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> cancelEvent ");
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Handles confirmation of whether the user wants to re-enter details for getting upcoming meetings.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, and numberOfMeetings
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(name = "confirmReenterGetUpcomingMeetings", description = "Checks if user wants to re-enter details and if yes, sends prompt to do so", type = CatalogRequest.Type.QUERY_SYSTEM)
    public ExtensionResponse confirmReenterGetUpcomingMeetings(@Field.Desc(name = "inputMap", type = "{ re-enter: Boolean, stateId: Text, numberOfMeetings: Number}") Map<String, Object> map) {
        Boolean reEnter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);

        if (Boolean.FALSE.equals(reEnter)) {
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_GET_UPCOMING_MEETINGS, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> getNextUpcomingMeetings ");
            return new ExtensionResponseBuilder().failure(error).build();
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults, Constants.HANDLE_RE_ENTER_GET_UPCOMING_MEETINGS, map);
    }

    /**
     * Handles the actual retrieval of upcoming meetings after the user has re-entered the number of meetings.
     *
     * @param map input map containing numberOfMeetings
     * @return an extension response containing the upcoming meetings or an error response
     */
    @SubCatalogRequest(name = Constants.HANDLE_RE_ENTER_GET_UPCOMING_MEETINGS, description = "Handle re-enter number of meetings", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "[ Entity(Event Details) ]", required = false)
    public ExtensionResponse handleReEnterGetUpcomingMeetings(@Field.Desc(name = "inputMap", type = "{numberOfMeetings: Number}") Map<String, Object> map) {
        try {
            Object numberOfMeetingsObj = map.get(Constants.NUMBER_OF_MEETINGS);
            Double numberOfMeetings = null;

            if (numberOfMeetingsObj instanceof Number) {
                numberOfMeetings = ((Number) numberOfMeetingsObj).doubleValue();
            }

            if (numberOfMeetings == null || numberOfMeetings <= 0) {
                throw new IllegalArgumentException(Constants.NUMBER_OF_MEETINGS_SHOULD_BE_GREATER_THAN_0);
            }

            List<Meeting> meetings = meetingDao.getMeetings(numberOfMeetings.intValue());
            List<EventDetails> events = meetings.stream().map(CatalogTypes::fromEvent).collect(Collectors.toList());
            return new ExtensionResponseBuilder().success(Map.of("Event Details", events)).build();
        } catch (Exception cause) {
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_GET_UPCOMING_MEETINGS, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> getNextUpcomingMeetings ");
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Handles confirmation of whether the user wants to re-enter details for getting an event.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, and eventId
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(name = "confirmReenterGetEvent", description = "Checks if user wants to re-enter details and if yes, sends prompt to do so", type = CatalogRequest.Type.QUERY_SYSTEM)
    public ExtensionResponse confirmReenterGetEvent(@Field.Desc(name = "inputMap", type = "{ re-enter: Boolean, stateId: Text, eventId: Text}") Map<String, Object> map) {
        Boolean reEnter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);

        if (Boolean.FALSE.equals(reEnter)) {
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_GET_EVENT, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> getEvent ");
            return new ExtensionResponseBuilder().failure(error).build();
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults, Constants.HANDLE_RE_ENTER_GET_EVENT, map);
    }

    /**
     * Handles the actual retrieval of an event after the user has re-entered the event ID.
     *
     * @param map input map containing eventId
     * @return an extension response containing the event details or an error response
     */
    @SubCatalogRequest(name = Constants.HANDLE_RE_ENTER_GET_EVENT, description = "Handle re-enter event id for retrieval", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse handleReEnterGetEvent(@Field.Desc(name = "inputMap", type = "{eventId: Text}") Map<String, Object> map) {
        try {
            String eventId = (String) map.get(Constants.EVENT_ID);
            Meeting meeting = meetingDao.getMeetingById(eventId);
            EventDetails eventDetails = CatalogTypes.fromEvent(meeting);
            return new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
        } catch (Exception cause) {
            ExtensionResponse.Error error = new ExtensionResponse.Error(Constants.FAILED_TO_GET_EVENT, System.currentTimeMillis(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR, " MeetingsArea -> getEvent ");
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Processes the provided state map and performs validations, returning a list of validation results.
     *
     * @param state a map representing the current state or context for validation, where keys are state names and values are their corresponding objects
     * @return a list of {@link ValidationOrchestrator.ValidationResult} objects representing the outcome of each validation performed on the state
     */
    /**
     * Handles confirmation of whether the user wants to re-enter details for declining an invitation.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, and eventId
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(
            name = Constants.CONFIRM_REENTER_DECLINE_INVITATION,
            description = "Checks if user wants to re-enter event ID for declining invitation and if yes, sends prompt to do so",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterDeclineInvitation(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, eventId: Text }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);

        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null, Map.of());
        }

        LOGGER.info("Reenter was true. Hence continuing.");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, Constants.HANDLE_REENTER_DECLINE_INVITATION,
                Map.of(Constants.STATE_ID, stateId,
                      Constants.EVENT_ID, map.get(Constants.EVENT_ID) != null ? map.get(Constants.EVENT_ID) : ""));
    }

    /**
     * Handles the actual decline of invitation after the user has re-entered the event ID.
     *
     * @param map input map containing eventId
     * @return an extension response containing the declined event details or an error response
     */
    @SubCatalogRequest(
            name = Constants.HANDLE_REENTER_DECLINE_INVITATION,
            description = "Handle re-entered event ID for declining invitation",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse handleReenterDeclineInvitation(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, eventId: Text }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterDeclineInvitation start: {}", map);

        // Extract parameters from the map
        String eventId = (String) map.get(Constants.EVENT_ID);

        try {
            // Perform the decline invitation operation
            Meeting declinedMeeting = meetingDao.declineMeetingInvitation(eventId);
            EventDetails eventDetails = CatalogTypes.fromEvent(declinedMeeting);
            return new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
        } catch (Exception cause) {
            LOGGER.error("Error occurred in handleReenterDeclineInvitation: {}", cause.getMessage());
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    Constants.FAILED_TO_DECLINE_INVITATION + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Handles confirmation of whether the user wants to re-enter details for getting schedules.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, dateFrom, dateTo, attendees, and timeZone
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(
            name = Constants.CONFIRM_REENTER_GET_SCHEDULES,
            description = "Checks if user wants to re-enter details for getting schedules and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterGetSchedules(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Date From: Number, Date To: Number, attendees: [ Text ], Time Zone: Text }", required = true) Map<String, Object> map) {
        System.out.println("=== CONFIRM REENTER GET SCHEDULES START ===");
        System.out.println("Input map: " + map);

        Boolean reenter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        System.out.println("Extracted values:");
        System.out.println("  reenter: " + reenter);
        System.out.println("  stateId: " + stateId);

        Map<String, Object> state = internalStateManager.get(stateId);
        System.out.println("Retrieved state: " + state);

        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        System.out.println("Validation results: " + validationResults);

        if (Boolean.FALSE.equals(reenter)) {
            System.out.println("User declined to reenter, generating deny response");
            ExtensionResponse response = responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null, Map.of());
            System.out.println("=== CONFIRM REENTER GET SCHEDULES DENY END ===");
            return response;
        }

        System.out.println("User accepted to reenter, preparing fetch response");
        LOGGER.info("Reenter was true. Hence continuing.");

        Object dateFromObj = map.get(Constants.FROM_DATE);
        Object dateToObj = map.get(Constants.TO_DATE);
        Object attendeesObj = map.get(Constants.ATTENDEES);
        Object timeZoneObj = map.get(Constants.TIME_ZONE);

        System.out.println("Raw values from map:");
        System.out.println("  FROM_DATE: " + dateFromObj + " (type: " + (dateFromObj != null ? dateFromObj.getClass().getSimpleName() : "null") + ")");
        System.out.println("  TO_DATE: " + dateToObj + " (type: " + (dateToObj != null ? dateToObj.getClass().getSimpleName() : "null") + ")");
        System.out.println("  ATTENDEES: " + attendeesObj + " (type: " + (attendeesObj != null ? attendeesObj.getClass().getSimpleName() : "null") + ")");
        System.out.println("  TIME_ZONE: " + timeZoneObj + " (type: " + (timeZoneObj != null ? timeZoneObj.getClass().getSimpleName() : "null") + ")");

        Map<String, Object> fetchParams = Map.of(
                Constants.STATE_ID, stateId,
                Constants.FROM_DATE, dateFromObj != null ? dateFromObj : 0L,
                Constants.TO_DATE, dateToObj != null ? dateToObj : 0L,
                Constants.ATTENDEES, attendeesObj != null ? attendeesObj : List.of(),
                Constants.TIME_ZONE, timeZoneObj != null ? timeZoneObj : "");

        System.out.println("Fetch parameters: " + fetchParams);

        ExtensionResponse response = responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, Constants.HANDLE_REENTER_GET_SCHEDULES, fetchParams);
        System.out.println("=== CONFIRM REENTER GET SCHEDULES ACCEPT END ===");
        return response;
    }

    /**
     * Handles the actual retrieval of schedules after the user has re-entered the details.
     *
     * @param map input map containing dateFrom, dateTo, attendees, and timeZone
     * @return an extension response containing the schedules or an error response
     */
    @SubCatalogRequest(
            name = Constants.HANDLE_REENTER_GET_SCHEDULES,
            description = "Handle re-entered details for getting schedules",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "List of Meetings", type = "[ { Attendee: Email, Schedules: [ { Scheduled Time: DateTimeRange, Status: Text } ] } ]", required = false)
    public ExtensionResponse handleReenterGetSchedules(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Date From: Number, Date To: Number, attendees: [ Text ], Time Zone: Text }") Map<String, Object> map) {
        System.out.println("=== HANDLE REENTER GET SCHEDULES START ===");
        System.out.println("Input map: " + map);
        LOGGER.info("SubCatalogRequest handleReenterGetSchedules start: {}", map);

        // Extract parameters from the map
        Object dateFromObj = map.get(Constants.FROM_DATE);
        Object dateToObj = map.get(Constants.TO_DATE);
        Object attendeesObj = map.get(Constants.ATTENDEES);
        String timeZone = (String) map.get(Constants.TIME_ZONE);

        System.out.println("Extracted raw objects:");
        System.out.println("  dateFromObj: " + dateFromObj + " (type: " + (dateFromObj != null ? dateFromObj.getClass().getSimpleName() : "null") + ")");
        System.out.println("  dateToObj: " + dateToObj + " (type: " + (dateToObj != null ? dateToObj.getClass().getSimpleName() : "null") + ")");
        System.out.println("  attendeesObj: " + attendeesObj + " (type: " + (attendeesObj != null ? attendeesObj.getClass().getSimpleName() : "null") + ")");
        System.out.println("  timeZone: " + timeZone);

        try {
            Long dateFrom = null;
            Long dateTo = null;
            List<String> attendees = null;

            // Handle dateFrom conversion
            if (dateFromObj instanceof Number) {
                dateFrom = ((Number) dateFromObj).longValue();
                System.out.println("Converted dateFrom: " + dateFrom);
            } else {
                System.out.println("dateFromObj is not a Number, keeping as null");
            }

            // Handle dateTo conversion
            if (dateToObj instanceof Number) {
                dateTo = ((Number) dateToObj).longValue();
                System.out.println("Converted dateTo: " + dateTo);
            } else {
                System.out.println("dateToObj is not a Number, keeping as null");
            }

            // Handle attendees conversion
            if (attendeesObj instanceof List) {
                attendees = (List<String>) attendeesObj;
                System.out.println("Converted attendees: " + attendees);
            } else {
                System.out.println("attendeesObj is not a List, keeping as null");
            }

            System.out.println("Final parameters for meetingDao.getSchedules:");
            System.out.println("  dateFrom: " + dateFrom);
            System.out.println("  dateTo: " + dateTo);
            System.out.println("  attendees: " + attendees);
            System.out.println("  timeZone: " + timeZone);

            // Perform the get schedules operation
            System.out.println("Calling meetingDao.getSchedules...");
            List<Map<String, Object>> schedules = meetingDao.getSchedules(dateFrom, dateTo, attendees, timeZone);
            System.out.println("Retrieved schedules: " + (schedules != null ? schedules.size() + " items" : "null"));
            if (schedules != null && !schedules.isEmpty()) {
                System.out.println("First schedule item: " + schedules.get(0));
            }

            ExtensionResponse response = new ExtensionResponseBuilder().success(Map.of("List of Meetings", schedules)).build();
            System.out.println("=== HANDLE REENTER GET SCHEDULES SUCCESS END ===");
            return response;
        } catch (Exception cause) {
            System.out.println("=== HANDLE REENTER GET SCHEDULES EXCEPTION ===");
            System.out.println("Exception: " + cause.getClass().getSimpleName());
            System.out.println("Message: " + cause.getMessage());
            cause.printStackTrace();
            LOGGER.error("Error occurred in handleReenterGetSchedules: {}", cause.getMessage());
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    Constants.FAILED_TO_GET_SCHEDULES_VALIDATION + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR,
                    ""
            );
            ExtensionResponse response = new ExtensionResponseBuilder().failure(error).build();
            System.out.println("=== HANDLE REENTER GET SCHEDULES EXCEPTION END ===");
            return response;
        }
    }

    @NotNull
    private static List<ValidationOrchestrator.ValidationResult> getValidationResults(Map<String, Object> state) {
        System.out.println("=== GET VALIDATION RESULTS START ===");
        System.out.println("State: " + state);

        List<ValidationOrchestrator.ValidationResult> validationResults = new ArrayList<>();
        List<?> results = (List<?>) state.get(Constants.VALIDATION_RESULTS);
        System.out.println("Raw validation results from state: " + results);

        if (results != null) {
            for (Object item : results) {
                System.out.println("Processing validation result item: " + item + " (type: " + (item != null ? item.getClass().getSimpleName() : "null") + ")");
                ValidationOrchestrator.ValidationResult validationResult = Constants.GSON.fromJson(Constants.GSON.toJson(item), ValidationOrchestrator.ValidationResult.class);
                System.out.println("Converted validation result: " + validationResult);
                validationResults.add(validationResult);
            }
        } else {
            System.out.println("No validation results found in state");
        }

        System.out.println("Final validation results: " + validationResults);
        System.out.println("=== GET VALIDATION RESULTS END ===");
        return validationResults;
    }

    /**
     * Handles confirmation of whether the user wants to re-enter details for getting free slots.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, dateFrom, dateTo, isOrganizerOptional, requiredAttendee, optionalAttendee, timeZone, and meetingDuration
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(
            name = Constants.CONFIRM_REENTER_GET_FREE_SLOTS,
            description = "Checks if user wants to re-enter details for getting free slots and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterGetFreeSlots(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Date From: Number, Date To: Number, Is Organizer Optional: Boolean, Required Attendee: [ Text ], Optional Attendee: [ Text ], Time Zone: Text, Meeting Duration: Text }", required = true) Map<String, Object> map) {
        System.out.println("=== CONFIRM REENTER GET FREE SLOTS START ===");
        System.out.println("Input map: " + map);

        Boolean reenter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        System.out.println("Extracted values:");
        System.out.println("  reenter: " + reenter);
        System.out.println("  stateId: " + stateId);

        Map<String, Object> state = internalStateManager.get(stateId);
        System.out.println("Retrieved state: " + state);

        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        System.out.println("Validation results: " + validationResults);

        if (Boolean.FALSE.equals(reenter)) {
            System.out.println("User declined to reenter, generating deny response");
            ExtensionResponse response = responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null, Map.of());
            System.out.println("=== CONFIRM REENTER GET FREE SLOTS DENY END ===");
            return response;
        }

        System.out.println("User accepted to reenter, generating fetch response");

        // Extract parameters for the fetch response
        Object dateFromObj = map.get(Constants.FROM_DATE);
        Object dateToObj = map.get(Constants.TO_DATE);
        Object isOrganizerOptionalObj = map.get(Constants.IS_ORGANIZER_OPTIONAL);
        Object requiredAttendeeObj = map.get(Constants.REQUIRED_ATTENDEE);
        Object optionalAttendeeObj = map.get(Constants.OPTIONAL_ATTENDEE);
        Object timeZoneObj = map.get(Constants.TIME_ZONE);
        Object meetingDurationObj = map.get(Constants.MEETING_DURATION);

        System.out.println("Extracted parameters:");
        System.out.println("  dateFromObj: " + dateFromObj);
        System.out.println("  dateToObj: " + dateToObj);
        System.out.println("  isOrganizerOptionalObj: " + isOrganizerOptionalObj);
        System.out.println("  requiredAttendeeObj: " + requiredAttendeeObj);
        System.out.println("  optionalAttendeeObj: " + optionalAttendeeObj);
        System.out.println("  timeZoneObj: " + timeZoneObj);
        System.out.println("  meetingDurationObj: " + meetingDurationObj);

        Map<String, Object> fetchParams = Map.of(
                Constants.STATE_ID, stateId,
                Constants.FROM_DATE, dateFromObj != null ? dateFromObj : 0L,
                Constants.TO_DATE, dateToObj != null ? dateToObj : 0L,
                Constants.IS_ORGANIZER_OPTIONAL, isOrganizerOptionalObj != null ? isOrganizerOptionalObj : false,
                Constants.REQUIRED_ATTENDEE, requiredAttendeeObj != null ? requiredAttendeeObj : List.of(),
                Constants.OPTIONAL_ATTENDEE, optionalAttendeeObj != null ? optionalAttendeeObj : List.of(),
                Constants.TIME_ZONE, timeZoneObj != null ? timeZoneObj : "",
                Constants.MEETING_DURATION, meetingDurationObj != null ? meetingDurationObj : "");

        System.out.println("Fetch parameters: " + fetchParams);

        ExtensionResponse response = responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, Constants.HANDLE_REENTER_GET_FREE_SLOTS, fetchParams);
        System.out.println("=== CONFIRM REENTER GET FREE SLOTS ACCEPT END ===");
        return response;
    }

    /**
     * Handles the actual retrieval of free slots after the user has re-entered the details.
     *
     * @param map input map containing dateFrom, dateTo, isOrganizerOptional, requiredAttendee, optionalAttendee, timeZone, and meetingDuration
     * @return an extension response containing the free slots or an error response
     */
    @SubCatalogRequest(
            name = Constants.HANDLE_REENTER_GET_FREE_SLOTS,
            description = "Handle re-entered details for getting free slots",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "List of Free Slots", type = "[ { Time Slot: DateTimeRange, Attendee Availability: [ { Attendee: Text, Availability: Text } ], Confidence: Percentage, Organizer Availability: Text, Suggestion Reason: Text } ]", required = false)
    public ExtensionResponse handleReenterGetFreeSlots(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Date From: Number, Date To: Number, Is Organizer Optional: Boolean, Required Attendee: [ Text ], Optional Attendee: [ Text ], Time Zone: Text, Meeting Duration: Text }") Map<String, Object> map) {
        System.out.println("=== HANDLE REENTER GET FREE SLOTS START ===");
        System.out.println("Input map: " + map);
        LOGGER.info("SubCatalogRequest handleReenterGetFreeSlots start: {}", map);

        try {
            // Extract parameters from the map
            Object dateFromObj = map.get(Constants.FROM_DATE);
            Object dateToObj = map.get(Constants.TO_DATE);
            Object isOrganizerOptionalObj = map.get(Constants.IS_ORGANIZER_OPTIONAL);
            Object requiredAttendeeObj = map.get(Constants.REQUIRED_ATTENDEE);
            Object optionalAttendeeObj = map.get(Constants.OPTIONAL_ATTENDEE);
            String timeZone = (String) map.get(Constants.TIME_ZONE);
            String meetingDuration = (String) map.get(Constants.MEETING_DURATION);

            System.out.println("Extracted raw parameters:");
            System.out.println("  dateFromObj: " + dateFromObj + " (type: " + (dateFromObj != null ? dateFromObj.getClass().getSimpleName() : "null") + ")");
            System.out.println("  dateToObj: " + dateToObj + " (type: " + (dateToObj != null ? dateToObj.getClass().getSimpleName() : "null") + ")");
            System.out.println("  isOrganizerOptionalObj: " + isOrganizerOptionalObj + " (type: " + (isOrganizerOptionalObj != null ? isOrganizerOptionalObj.getClass().getSimpleName() : "null") + ")");
            System.out.println("  requiredAttendeeObj: " + requiredAttendeeObj + " (type: " + (requiredAttendeeObj != null ? requiredAttendeeObj.getClass().getSimpleName() : "null") + ")");
            System.out.println("  optionalAttendeeObj: " + optionalAttendeeObj + " (type: " + (optionalAttendeeObj != null ? optionalAttendeeObj.getClass().getSimpleName() : "null") + ")");
            System.out.println("  timeZone: " + timeZone);
            System.out.println("  meetingDuration: " + meetingDuration);

            // Convert parameters to appropriate types
            Long dateFrom = null;
            Long dateTo = null;
            Boolean isOrganizerOptional = null;
            List<String> requiredAttendee = null;
            List<String> optionalAttendee = null;

            // Handle date conversion
            if (dateFromObj instanceof Number) {
                dateFrom = ((Number) dateFromObj).longValue();
                System.out.println("Converted dateFrom: " + dateFrom);
            } else {
                System.out.println("dateFromObj is not a Number, keeping as null");
            }

            if (dateToObj instanceof Number) {
                dateTo = ((Number) dateToObj).longValue();
                System.out.println("Converted dateTo: " + dateTo);
            } else {
                System.out.println("dateToObj is not a Number, keeping as null");
            }

            // Handle boolean conversion
            if (isOrganizerOptionalObj instanceof Boolean) {
                isOrganizerOptional = (Boolean) isOrganizerOptionalObj;
                System.out.println("Converted isOrganizerOptional: " + isOrganizerOptional);
            } else {
                System.out.println("isOrganizerOptionalObj is not a Boolean, keeping as null");
            }

            // Handle list conversion for required attendees
            if (requiredAttendeeObj instanceof List) {
                requiredAttendee = (List<String>) requiredAttendeeObj;
                System.out.println("Converted requiredAttendee: " + requiredAttendee);
            } else {
                System.out.println("requiredAttendeeObj is not a List, keeping as null");
            }

            // Handle list conversion for optional attendees
            if (optionalAttendeeObj instanceof List) {
                optionalAttendee = (List<String>) optionalAttendeeObj;
                System.out.println("Converted optionalAttendee: " + optionalAttendee);
            } else {
                System.out.println("optionalAttendeeObj is not a List, keeping as null");
            }

            System.out.println("Final parameters for meetingDao.getFreeSlot:");
            System.out.println("  dateFrom: " + dateFrom);
            System.out.println("  dateTo: " + dateTo);
            System.out.println("  isOrganizerOptional: " + isOrganizerOptional);
            System.out.println("  requiredAttendee: " + requiredAttendee);
            System.out.println("  optionalAttendee: " + optionalAttendee);
            System.out.println("  timeZone: " + timeZone);
            System.out.println("  meetingDuration: " + meetingDuration);

            // Perform the get free slots operation
            System.out.println("Calling meetingDao.getFreeSlot...");
            List<Map<String, Object>> freeSlots = meetingDao.getFreeSlot(dateFrom, dateTo, isOrganizerOptional, requiredAttendee, optionalAttendee, timeZone, meetingDuration);
            System.out.println("Retrieved free slots: " + (freeSlots != null ? freeSlots.size() + " items" : "null"));
            if (freeSlots != null && !freeSlots.isEmpty()) {
                System.out.println("First free slot item: " + freeSlots.get(0));
            }

            ExtensionResponse response = new ExtensionResponseBuilder().success(Map.of("List of Free Slots", freeSlots)).build();
            System.out.println("=== HANDLE REENTER GET FREE SLOTS SUCCESS END ===");
            return response;
        } catch (Exception cause) {
            System.out.println("=== HANDLE REENTER GET FREE SLOTS EXCEPTION ===");
            System.out.println("Exception: " + cause.getClass().getSimpleName());
            System.out.println("Message: " + cause.getMessage());
            cause.printStackTrace();
            LOGGER.error("Error occurred in handleReenterGetFreeSlots: {}", cause.getMessage());
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    Constants.FAILED_TO_GET_FREE_SLOTS_VALIDATION + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR,
                    ""
            );
            ExtensionResponse response = new ExtensionResponseBuilder().failure(error).build();
            System.out.println("=== HANDLE REENTER GET FREE SLOTS EXCEPTION END ===");
            return response;
        }
    }

    /**
     * Handles confirmation of whether the user wants to re-enter details for forwarding an event.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, eventId, and attendeeEmail
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(
            name = Constants.CONFIRM_REENTER_FORWARD_EVENT,
            description = "Checks if user wants to re-enter details for forwarding event and if yes, sends prompt to do so",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForwardEvent(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, eventId: Text, Attendee Email: [ Email ] }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);

        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null, Map.of());
        }

        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, Constants.HANDLE_REENTER_FORWARD_EVENT, map);
    }

    /**
     * Handles the actual forward event operation after the user has re-entered the event ID.
     *
     * @param map input map containing stateId, eventId, and attendeeEmail
     * @return an extension response indicating success or error information
     */
    @SubCatalogRequest(
            name = Constants.HANDLE_REENTER_FORWARD_EVENT,
            description = "Handle re-entered event ID and attendee emails for forwarding event",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "IsForwarded", type = "Switch", required = false)
    public ExtensionResponse handleReenterForwardEvent(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, eventId: Text, Attendee Email: [ Email ] }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterForwardEvent start: {}", map);

        try {
            String eventId = (String) map.get(Constants.EVENT_ID);
            @SuppressWarnings("unchecked")
            List<String> attendeeEmail = (List<String>) map.get(Constants.ATTENDEE_EMAIL);

            LOGGER.info("handleReenterForwardEvent: eventId={}, attendeeEmail={}", eventId, attendeeEmail);

            // Forward the event
            meetingDao.forwardMeeting(eventId, attendeeEmail);

            ExtensionResponse response = new ExtensionResponseBuilder().success(Map.of("IsForwarded", true)).build();
            LOGGER.info("SubCatalogRequest handleReenterForwardEvent success");
            return response;
        } catch (Exception cause) {
            LOGGER.error("Error occurred in handleReenterForwardEvent: {}", cause.getMessage());
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    Constants.FAILED_TO_FORWARD_EVENT_VALIDATION + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Handles confirmation of whether the user wants to re-enter details for accepting an invitation.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, and eventId
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(
            name = Constants.CONFIRM_REENTER_ACCEPT_INVITATION,
            description = "Checks if user wants to re-enter details for accepting invitation and if yes, sends prompt to do so",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterAcceptInvitation(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, eventId: Text }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);

        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null, Map.of());
        }

        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, Constants.HANDLE_REENTER_ACCEPT_INVITATION, map);
    }

    /**
     * Handles the actual accept invitation operation after the user has re-entered the event ID.
     *
     * @param map input map containing stateId and eventId
     * @return an extension response containing the accepted event details or error information
     */
    @SubCatalogRequest(
            name = Constants.HANDLE_REENTER_ACCEPT_INVITATION,
            description = "Handle re-entered event ID for accepting invitation",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse handleReenterAcceptInvitation(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, eventId: Text }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterAcceptInvitation start: {}", map);

        try {
            String eventId = (String) map.get(Constants.EVENT_ID);

            LOGGER.info("handleReenterAcceptInvitation: eventId={}", eventId);

            // Accept the invitation
            EventDetails eventDetails = CatalogTypes.fromEvent(meetingDao.acceptMeetingInvitation(eventId));

            ExtensionResponse response = new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
            LOGGER.info("SubCatalogRequest handleReenterAcceptInvitation success");
            return response;
        } catch (Exception cause) {
            LOGGER.error("Error occurred in handleReenterAcceptInvitation: {}", cause.getMessage());
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    Constants.FAILED_TO_ACCEPT_INVITATION_VALIDATION + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }

    /**
     * Handles confirmation of whether the user wants to re-enter details for tentative invitation.
     * If re-entry is declined, returns a failure response; otherwise, prompts for re-entry.
     *
     * @param map input map containing reEnter flag, stateId, and eventId
     * @return an extension response prompting for re-entry or indicating failure
     */
    @SubCatalogRequest(
            name = Constants.CONFIRM_REENTER_TENTATIVE_INVITATION,
            description = "Checks if user wants to re-enter details for tentative invitation and if yes, sends prompt to do so",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterTentativeInvitation(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, eventId: Text }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(Constants.RE_ENTER);
        String stateId = (String) map.get(Constants.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);

        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null, Map.of());
        }

        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, Constants.HANDLE_REENTER_TENTATIVE_INVITATION, map);
    }

    /**
     * Handles the actual tentative invitation operation after the user has re-entered the event ID.
     *
     * @param map input map containing stateId and eventId
     * @return an extension response containing the tentative event details or error information
     */
    @SubCatalogRequest(
            name = Constants.HANDLE_REENTER_TENTATIVE_INVITATION,
            description = "Handle re-entered event ID for tentative invitation",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public ExtensionResponse handleReenterTentativeInvitation(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, eventId: Text }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterTentativeInvitation start: {}", map);

        try {
            String eventId = (String) map.get(Constants.EVENT_ID);

            LOGGER.info("handleReenterTentativeInvitation: eventId={}", eventId);

            // Mark invitation as tentative
            EventDetails eventDetails = CatalogTypes.fromEvent(meetingDao.tentativeMeetingInvitation(eventId));

            ExtensionResponse response = new ExtensionResponseBuilder().success(Map.of("Event Details", eventDetails)).build();
            LOGGER.info("SubCatalogRequest handleReenterTentativeInvitation success");
            return response;
        } catch (Exception cause) {
            LOGGER.error("Error occurred in handleReenterTentativeInvitation: {}", cause.getMessage());
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    Constants.FAILED_TO_TENTATIVE_INVITATION_VALIDATION + cause.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR,
                    ""
            );
            return new ExtensionResponseBuilder().failure(error).build();
        }
    }
}
