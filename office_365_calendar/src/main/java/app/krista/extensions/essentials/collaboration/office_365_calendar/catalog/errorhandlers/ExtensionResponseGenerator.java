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
        LOGGER.info("→ Generating confirmation response for {} validation results", validationResults.size());
        LOGGER.debug("→ Exception type: {}, SubCatalogRequest: {}", exceptionType, subCatalogRequestName);

        List<NamedField> fields = List.of(NamedFieldFactory.createSwitchField(Constants.RE_ENTER));
        LOGGER.debug("→ Created switch field for re-entry confirmation");

        Map<String, Object> validationResponse = generateResponse(validationResults, false);
        LOGGER.debug("→ Generated validation response with confirmation messages");

        ExtensionResponse response = new ExtensionResponseBuilder()
                .result(ExtensionResponse.Result.FAILURE)
                .error((String) validationResponse.get(ERROR_MESSAGE), exceptionType)
                .remediationActions(List.of(RemediationActionFactory.createAskAction((String) validationResponse.get(STEP_MESSAGE), fields)), subCatalogRequestName)
                .state(state)
                .build();

        LOGGER.info("→ Confirmation response generated successfully");
        return response;
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
        LOGGER.debug("→ Generating response for {} validation results, fetchResponse={}", validationResults.size(), fetchResponse);

        StringBuilder stepMessage = new StringBuilder();
        StringBuilder errMessage = new StringBuilder();
        List<NamedField> fields = new ArrayList<>();

        for (ValidationOrchestrator.ValidationResult validationResult : validationResults) {
            String currentStepMessage = !fetchResponse ? validationResult.confirmStepMessage() : validationResult.fetchStepMessage();
            stepMessage.append(currentStepMessage).append("\n");
            errMessage.append(validationResult.errMessage()).append("\n");

            LOGGER.debug("→ Processing validation result: fieldName={}, stepMessage={}, errorMessage={}",
                    validationResult.fetchFieldName(), currentStepMessage, validationResult.errMessage());

            if (fetchResponse) {
                // Use the field type and attributes from ValidationResult to create the appropriate field
                String fieldType = validationResult.fieldType();
                Map<String, Object> fieldAttributes = validationResult.fieldAttributes();
                LOGGER.debug("→ Creating field: name={}, type={}, attributes={}", validationResult.fetchFieldName(), fieldType, fieldAttributes);
                fields.add(NamedFieldFactory.createFieldWithAttributes(validationResult.fetchFieldName(), fieldType, fieldAttributes));
            }
        }

        LOGGER.debug("→ Generated response: stepMessage length={}, errorMessage length={}, fields count={}",
                stepMessage.length(), errMessage.length(), fields.size());

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
        LOGGER.info("→ Generating fetch response for {} validation failures", validationResults.size());
        LOGGER.debug("→ Exception type: {}, SubCatalogRequest: {}", exceptionType, subCatalogRequestName);

        Map<String, Object> stringStringMap = generateResponse(validationResults, true);
        LOGGER.debug("→ Generated validation response with fetch fields");

        ExtensionResponse response = new ExtensionResponseBuilder()
                .result(ExtensionResponse.Result.FAILURE)
                .error((String) stringStringMap.get(ERROR_MESSAGE), exceptionType)
                .remediationActions(List.of(RemediationActionFactory.createAskAction((String) stringStringMap.get(STEP_MESSAGE), (List<NamedField>) stringStringMap.get(FIELD))), subCatalogRequestName)
                .state(state)
                .build();

        LOGGER.info("→ Fetch response generated successfully");
        return response;
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
        StringJoiner stepMessage = new StringJoiner(", ", "The following required fields were not provided: ", ". Please provide these values to continue.");
        StringBuilder errMessage = new StringBuilder();
        for (ValidationOrchestrator.ValidationResult validationResult : validationResults) {
            stepMessage.add("'" + validationResult.fetchFieldName() + "'");
            errMessage.append(validationResult.errMessage()).append("\n");
        }
        return new ExtensionResponseBuilder().result(ExtensionResponse.Result.FAILURE).error(errMessage.toString(), exceptionType).remediationActions(List.of(RemediationActionFactory.createInformActionALLParticipants(stepMessage.toString(), List.of())), subCatalogRequestName).state(state).build();
    }

    /**
     * Generates a direct validation error response without retry option.
     * This method is used when the "Allow Retry" feature is disabled. It returns a simple
     * error message without remediation actions, preventing the interactive retry flow.
     *
     * @param validationResults a list of validation results indicating what failed
     * @return an {@link ExtensionResponse} with a direct error message without retry prompts
     */
    public ExtensionResponse generateValidationErrorResponse(List<ValidationOrchestrator.ValidationResult> validationResults) {
        LOGGER.info("→ Generating direct validation error response (no retry) for {} validation results", validationResults.size());

        // Build error message from validation results
        StringBuilder errMessage = new StringBuilder();
        for (ValidationOrchestrator.ValidationResult result : validationResults) {
            errMessage.append(result.errMessage()).append("\n");
            LOGGER.debug("→ Validation error: {}", result.errMessage());
        }
        // Return simple failure response without remediation
        ExtensionResponse response = new ExtensionResponseBuilder()
                .result(ExtensionResponse.Result.FAILURE)
                .error(errMessage.toString().trim(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR)
                .build();
        LOGGER.info("→ Direct validation error response generated successfully");
        return response;
    }
}