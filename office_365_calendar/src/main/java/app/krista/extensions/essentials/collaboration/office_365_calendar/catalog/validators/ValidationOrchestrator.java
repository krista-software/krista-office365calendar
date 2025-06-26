package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the validation of input resources using a collection of {@link Validator} instances.
 * <p>
 * The {@code ValidationOrchestrator} coordinates multiple validators to check the validity of various
 * input resources required for Office 365 calendar operations.
 * It aggregates the results of each validator into a list of {@link ValidationResult} objects, which
 * can then be used for error handling, user feedback, or remediation flows.
 * </p>
 * This class is typically used in service or handler layers to ensure that all required
 * inputs are valid before proceeding with business logic.
 */
@Service
public class ValidationOrchestrator {

    private final Map<Validator.ValidationResource, Validator> validators = new HashMap<>();

    @Inject
    public ValidationOrchestrator(MeetingDaoImpl meetingDao) {
        validators.put(Validator.ValidationResource.EMAIL, new EmailValidator());
        validators.put(Validator.ValidationResource.EVENT_ID, new EventIdValidator(meetingDao));
        validators.put(Validator.ValidationResource.DATE_RANGE, new DateValidator());
        validators.put(Validator.ValidationResource.NUMBER_OF_MEETINGS, new NumberValidator());
    }

    public record ValidationResult(String confirmStepMessage, String fetchFieldName, String fetchStepMessage,
                                   String errMessage, String fieldType, Map<String, Object> fieldAttributes) {
    }

    /**
     * Validates the provided resources using the registered validators for each {@link Validator.ValidationResource}.
     * <p>
     * For each resource entry, the corresponding validator is invoked. If validation fails,
     * a {@link ValidationResult} is created containing messages and field information relevant to the failure.
     * </p>
     *
     * @param resources a map where the key is a {@link Validator.ValidationResource} and the value is the resource string to validate
     * @return a list of {@link ValidationResult} objects representing the outcome of failed validations;
     * the list is empty if all resources are valid
     */
    public List<ValidationResult> validate(Map<Validator.ValidationResource, String> resources) {
        List<ValidationResult> results = new ArrayList<>();
        for (Map.Entry<Validator.ValidationResource, String> entry : resources.entrySet()) {
            Validator validator = validators.get(entry.getKey());
            if (validator != null && !validator.validate(entry.getValue(), resources)) {
                results.add(new ValidationResult(validator.getConfirmationStepMessage(entry.getValue(), resources),
                        validator.getFetchFieldName(), validator.getFetchStepMessage(),
                        validator.getErrMessage(entry.getValue()), getFieldTypeForResource(entry.getKey()),
                        getFieldAttributesForResource(entry.getKey())));
            }
        }
        return results;
    }

    /**
     * Maps a ValidationResource to its corresponding field type for proper field creation.
     * This ensures that the correct field type is used when generating fetch responses.
     *
     * @param resource the ValidationResource to map
     * @return the field type string corresponding to the resource
     */
    private String getFieldTypeForResource(Validator.ValidationResource resource) {
        return switch (resource) {
            case FROM_DATE, TO_DATE, DATE_RANGE -> "Date";
            case EMAIL -> "Email";
            case EVENT_ID -> "Text";
            case NUMBER_OF_MEETINGS -> "Number";
            case ATTENDEES -> "Email"; // For list of emails, we'll use Email type
            default -> "Text"; // Default fallback
        };
    }

    /**
     * Maps a ValidationResource to its corresponding field attributes for proper field creation.
     * This ensures that the correct field attributes are used when generating fetch responses.
     *
     * @param resource the ValidationResource to map
     * @return the field attributes map corresponding to the resource
     */
    private Map<String, Object> getFieldAttributesForResource(Validator.ValidationResource resource) {
        return switch (resource) {
            case FROM_DATE, TO_DATE, DATE_RANGE -> Map.of("includeTimeOfDay", "true");
            case EMAIL -> Map.of(); // No special attributes for email fields
            case EVENT_ID -> Map.of(); // No special attributes for text fields
            case NUMBER_OF_MEETINGS -> Map.of(); // No special attributes for number fields
            case ATTENDEES -> Map.of(); // No special attributes for attendee email fields
            default -> Map.of(); // Default empty attributes
        };
    }
}
