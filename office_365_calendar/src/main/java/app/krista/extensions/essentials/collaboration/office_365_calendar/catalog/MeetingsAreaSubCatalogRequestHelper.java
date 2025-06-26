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
                      Constants.EVENT_ID, map.get(Constants.EVENT_ID)));
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

    @NotNull
    private static List<ValidationOrchestrator.ValidationResult> getValidationResults(Map<String, Object> state) {
        List<ValidationOrchestrator.ValidationResult> validationResults = new ArrayList<>();
        List<?> results = (List<?>) state.get(Constants.VALIDATION_RESULTS);
        for (Object item : results) {
            validationResults.add(Constants.GSON.fromJson(Constants.GSON.toJson(item), ValidationOrchestrator.ValidationResult.class));
        }
        return validationResults;
    }
}
