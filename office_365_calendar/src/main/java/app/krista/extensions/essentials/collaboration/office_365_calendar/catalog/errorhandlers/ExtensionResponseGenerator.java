package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers;


import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.ExtensionResponseFactory;
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

@Service
public class ExtensionResponseGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionResponseGenerator.class);
    public static final String ERROR_MESSAGE = "ErrorMessage";
    public static final String STEP_MESSAGE = "StepMessage";
    public static final String FIELD = "Field";

    public ExtensionResponse generateConfirmationResponse(
            ExtensionResponse.Error.ExceptionType exceptionType,
            List<ValidationOrchestrator.ValidationResult> validationResults,
            String subCatalogRequestName,
            Map<String, Object> state) {
        List<NamedField> fields = List.of(NamedFieldFactory.createSwitchField(Constants.RE_ENTER));
        Map<String, Object> validationResponse = generateResponse(validationResults, false);
        return ExtensionResponseFactory.create((String) validationResponse.get(ERROR_MESSAGE), exceptionType,
                List.of(RemediationActionFactory.createAskAction((String) validationResponse.get(STEP_MESSAGE), fields)),
                subCatalogRequestName, state);
    }

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

    public ExtensionResponse generateFetchResponse(
            ExtensionResponse.Error.ExceptionType exceptionType,
            List<ValidationOrchestrator.ValidationResult> validationResults,
            String subCatalogRequestName,
            Map<String, Object> state) {
        LOGGER.info("Validation failed for : {}", validationResults.size());
        LOGGER.info("Validation failed for : {}", validationResults.size());
        Map<String, Object> stringStringMap = generateResponse(validationResults, true);
        return ExtensionResponseFactory.create((String) stringStringMap.get(ERROR_MESSAGE), exceptionType,
                List.of(RemediationActionFactory.createAskAction((String) stringStringMap.get(STEP_MESSAGE), (List<NamedField>) stringStringMap.get(FIELD))),
                subCatalogRequestName, state);
    }

    public ExtensionResponse generateFetchDenyResponse(
            ExtensionResponse.Error.ExceptionType exceptionType,
            List<ValidationOrchestrator.ValidationResult> validationResults,
            String subCatalogRequestName,
            Map<String, Object> state) {
        StringJoiner stepMessage = new StringJoiner(", ", "Updated value for ", " were not provided.");
        StringBuilder errMessage = new StringBuilder();
        for (ValidationOrchestrator.ValidationResult validationResult : validationResults) {
            stepMessage.add("'" + validationResult.fetchFieldName() + "'");
            errMessage.append(validationResult.errMessage()).append("\n");
        }
        return ExtensionResponseFactory.create(errMessage.toString(), exceptionType,
                List.of(RemediationActionFactory.createInformActionALLParticipants(stepMessage.toString(), List.of())),
                subCatalogRequestName, state);
    }
}
