package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers;


import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.ExtensionResponseBuilder;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.NamedFieldFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.RemediationActionFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.model.field.NamedField;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Utility class responsible for generating standardized {@link app.krista.extension.executor.ExtensionResponse}
 * objects for the Office 365 Calendar extension.
 * <p>
 * This class centralizes the logic for constructing success and error responses, including
 * the creation of named fields, remediation actions, and error details. It ensures consistency
 * in how responses are built and returned throughout the extension, particularly in scenarios
 * involving validation failures, user prompts, and error handling.
 * </p>
 * <p>
 * ExtensionResponseGenerator is typically used by service and handler classes to encapsulate
 * response-building logic, making it easier to maintain and extend response formats as the
 * extension evolves.
 * </p>
 */
@Service
public class ExtensionResponseGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionResponseGenerator.class);
    public static final String ERROR_MESSAGE = "ErrorMessage";
    public static final String STEP_MESSAGE = "StepMessage";
    public static final String FIELD = "Field";

    /**
     * Generates an {@link ExtensionResponse} for confirmation scenarios based on validation results.
     * <p>
     * This response typically prompts the user to confirm or re-enter details after a validation failure.
     * It includes a remediation action with a switch field for re-entry.
     * </p>
     *
     * @param exceptionType         the type of exception to be included in the response
     * @param validationResults     a list of validation results indicating what failed
     * @param subCatalogRequestName the name of the sub-catalog request for context
     * @param state                 the current state map to be included in the response
     * @return an {@link ExtensionResponse} prompting the user for confirmation or correction
     */
    public ExtensionResponse generateConfirmationResponse(ExtensionResponse.Error.ExceptionType exceptionType, List<ValidationOrchestrator.ValidationResult> validationResults, String subCatalogRequestName, Map<String, Object> state) {
        List<NamedField> fields = List.of(NamedFieldFactory.createSwitchField(Constants.RE_ENTER));
        Map<String, Object> validationResponse = generateResponse(validationResults, false);
        return new ExtensionResponseBuilder().result(ExtensionResponse.Result.FAILURE).error((String) validationResponse.get(ERROR_MESSAGE), exceptionType).remediationActions(List.of(RemediationActionFactory.createAskAction((String) validationResponse.get(STEP_MESSAGE), fields)), subCatalogRequestName).state(state).build();
    }

    /**
     * Helper method to construct a response map containing step messages, error messages,
     * and fields based on the provided validation results.
     *
     * @param validationResults the list of validation results to process
     * @param fetchResponse     if true, includes fields for user input; if false, only messages are included
     * @return a map containing step message, error message, and optionally fields for further input
     */
    private Map<String, Object> generateResponse(List<ValidationOrchestrator.ValidationResult> validationResults, boolean fetchResponse) {
        StringBuilder stepMessage = new StringBuilder();
        StringBuilder errMessage = new StringBuilder();
        List<NamedField> fields = new ArrayList<>();
        for (ValidationOrchestrator.ValidationResult validationResult : validationResults) {
            stepMessage.append(!fetchResponse ? validationResult.confirmStepMessage() : validationResult.fetchStepMessage()).append("\n");
            errMessage.append(validationResult.errMessage()).append("\n");
            if (fetchResponse) {
                fields.add(NamedFieldFactory.createTextField(validationResult.fetchFieldName()));
            }
        }
        return Map.of(STEP_MESSAGE, stepMessage.toString(), ERROR_MESSAGE, errMessage.toString(), FIELD, fields);
    }

    /**
     * Generates an {@link ExtensionResponse} for fetch scenarios where additional user input is required.
     * <p>
     * This response is typically used when validation fails and the user must provide or correct specific fields.
     * </p>
     *
     * @param exceptionType         the type of exception to be included in the response
     * @param validationResults     a list of validation results indicating what needs to be fetched or corrected
     * @param subCatalogRequestName the name of the sub-catalog request for context
     * @param state                 the current state map to be included in the response
     * @return an {@link ExtensionResponse} prompting the user to provide missing or invalid information
     */
    @SuppressWarnings("unchecked")
    public ExtensionResponse generateFetchResponse(ExtensionResponse.Error.ExceptionType exceptionType, List<ValidationOrchestrator.ValidationResult> validationResults, String subCatalogRequestName, Map<String, Object> state) {
        LOGGER.info("Validation failed for : {}", validationResults.size());
        Map<String, Object> stringStringMap = generateResponse(validationResults, true);
        return new ExtensionResponseBuilder().result(ExtensionResponse.Result.FAILURE).error((String) stringStringMap.get(ERROR_MESSAGE), exceptionType).remediationActions(List.of(RemediationActionFactory.createAskAction((String) stringStringMap.get(STEP_MESSAGE), (List<NamedField>) stringStringMap.get(FIELD))), subCatalogRequestName).state(state).build();
    }

    /**
     * Generates an {@link ExtensionResponse} for scenarios where required fields were not provided by the user.
     * <p>
     * This response informs all participants that updated values for certain fields were not supplied.
     * </p>
     *
     * @param exceptionType         the type of exception to be included in the response
     * @param validationResults     a list of validation results indicating which fields were missing
     * @param subCatalogRequestName the name of the sub-catalog request for context
     * @param state                 the current state map to be included in the response
     * @return an {@link ExtensionResponse} informing all participants about the missing fields
     */
    public ExtensionResponse generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType exceptionType, List<ValidationOrchestrator.ValidationResult> validationResults, String subCatalogRequestName, Map<String, Object> state) {
        StringJoiner stepMessage = new StringJoiner(", ", "Updated value for ", " were not provided.");
        StringBuilder errMessage = new StringBuilder();
        for (ValidationOrchestrator.ValidationResult validationResult : validationResults) {
            stepMessage.add("'" + validationResult.fetchFieldName() + "'");
            errMessage.append(validationResult.errMessage()).append("\n");
        }
        return new ExtensionResponseBuilder().result(ExtensionResponse.Result.FAILURE).error(errMessage.toString(), exceptionType).remediationActions(List.of(RemediationActionFactory.createInformActionALLParticipants(stepMessage.toString(), List.of())), subCatalogRequestName).state(state).build();
    }
}
