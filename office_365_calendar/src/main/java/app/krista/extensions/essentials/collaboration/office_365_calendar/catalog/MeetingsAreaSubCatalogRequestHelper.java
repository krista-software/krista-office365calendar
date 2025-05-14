package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.SubCatalogRequest;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.model.base.File;
import com.microsoft.graph.models.FileAttachment;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults, "handleReEnterGetAttachmentForGivenUser", Map.of(Constants.EMAIL, map.get(Constants.EMAIL), Constants.EVENT_ID, map.get(Constants.EVENT_ID)));
    }

    /**
     * Handles the actual retrieval of attachments after the user has re-entered email and event ID.
     *
     * @param map input map containing email and eventId
     * @return an extension response containing the attachments or an error response
     */
    @SubCatalogRequest(name = "handleReEnterGetAttachmentForGivenUser", description = "Handle re-enter email and event id", type = CatalogRequest.Type.QUERY_SYSTEM)
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
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults, "handleReEnterGetAttachments", Map.of(Constants.EVENT_ID, map.get(Constants.EVENT_ID)));
    }

    /**
     * Handles the actual retrieval of attachments after the user has re-entered the event ID for the authenticated user.
     *
     * @param map input map containing eventId
     * @return an extension response containing the attachments or an error response
     */
    @SubCatalogRequest(name = "handleReEnterGetAttachments", description = "Handle re-enter event id", type = CatalogRequest.Type.QUERY_SYSTEM)
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
     * Processes the provided state map and performs validations, returning a list of validation results.
     *
     * @param state a map representing the current state or context for validation, where keys are state names and values are their corresponding objects
     * @return a list of {@link ValidationOrchestrator.ValidationResult} objects representing the outcome of each validation performed on the state
     */
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
