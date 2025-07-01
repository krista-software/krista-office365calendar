package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.lang.reflect.Method;
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
        validators.put(Validator.ValidationResource.FROM_DATE, new DateValidator());
        validators.put(Validator.ValidationResource.TO_DATE, new ToDateValidator());
        validators.put(Validator.ValidationResource.NUMBER_OF_MEETINGS, new NumberValidator());
        validators.put(Validator.ValidationResource.TIME_ZONE, new TimeZoneValidator());
        validators.put(Validator.ValidationResource.RECURRENCE_INTERVAL, new RecurrenceIntervalValidator());
        validators.put(Validator.ValidationResource.DAYS_OF_WEEK, new DaysOfWeekValidator());
        validators.put(Validator.ValidationResource.NUMBERED_OCCURRENCES, new NumberedOccurrencesValidator());
        validators.put(Validator.ValidationResource.ATTACHMENTS, new AttachmentsValidator());
        validators.put(Validator.ValidationResource.MEETING_DURATION, new MeetingDurationValidator());
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
        return validate(resources, null, null);
    }

    /**
     * Validates the provided resources using the registered validators for each {@link Validator.ValidationResource}.
     * <p>
     * This overloaded method allows for dynamic field attribute extraction from the specified method,
     * eliminating the need for hardcoded field attributes.
     * </p>
     *
     * @param resources a map where the key is a {@link Validator.ValidationResource} and the value is the resource string to validate
     * @param sourceClass the class containing the catalog request method (can be null for fallback to hardcoded attributes)
     * @param methodName the name of the catalog request method (can be null for fallback to hardcoded attributes)
     * @return a list of {@link ValidationResult} objects representing the outcome of failed validations;
     * the list is empty if all resources are valid
     */
    public List<ValidationResult> validate(Map<Validator.ValidationResource, String> resources, Class<?> sourceClass, String methodName) {
        System.out.println("=== ValidationOrchestrator.validate() with Dynamic Attribute Extraction ===");
        System.out.println("Source Class: " + (sourceClass != null ? sourceClass.getSimpleName() : "null"));
        System.out.println("Method Name: " + (methodName != null ? methodName : "null"));
        System.out.println("Resources to validate: " + resources.keySet());

        List<ValidationResult> results = new ArrayList<>();

        // Extract field attributes dynamically if method information is provided
        Method sourceMethod = null;
        Map<String, Validator.ValidationResource> fieldNameToResourceMapping = new HashMap<>();

        if (sourceClass != null && methodName != null) {
            System.out.println("Attempting to extract method and field mappings...");
            sourceMethod = FieldAttributeExtractor.getMethodByName(sourceClass, methodName);
            if (sourceMethod != null) {
                System.out.println("✓ Found method: " + sourceMethod.getName());
                fieldNameToResourceMapping = FieldAttributeExtractor.createFieldNameToResourceMapping(sourceMethod);
                System.out.println("✓ Created field name to resource mapping: " + fieldNameToResourceMapping);
            } else {
                System.out.println("✗ Method not found, falling back to hardcoded attributes");
            }
        } else {
            System.out.println("No source class/method provided, using hardcoded attributes");
        }

        System.out.println("Starting validation of " + resources.size() + " resources...");
        for (Map.Entry<Validator.ValidationResource, String> entry : resources.entrySet()) {
            System.out.println("Validating resource: " + entry.getKey() + " = " + entry.getValue());

            Validator validator = validators.get(entry.getKey());
            if (validator != null && !validator.validate(entry.getValue(), resources)) {
                System.out.println("✗ Validation failed for: " + entry.getKey());

                Map<String, Object> fieldAttributes = getFieldAttributesForResource(entry.getKey(), sourceMethod, fieldNameToResourceMapping);
                System.out.println("Extracted field attributes: " + fieldAttributes);

                String fieldType = getFieldTypeForResource(entry.getKey(), sourceMethod, fieldNameToResourceMapping);
                System.out.println("Extracted field type: " + fieldType);

                String fieldName = getFieldNameForResource(entry.getKey(), sourceMethod, fieldNameToResourceMapping);
                System.out.println("Extracted field name: " + fieldName);

                results.add(new ValidationResult(validator.getConfirmationStepMessage(entry.getValue(), resources),
                        fieldName, validator.getFetchStepMessage(),
                        validator.getErrMessage(entry.getValue()), fieldType,
                        fieldAttributes));
            } else {
                System.out.println("✓ Validation passed for: " + entry.getKey());
            }
        }

        System.out.println("Validation complete. Failed validations: " + results.size());
        System.out.println("=== End ValidationOrchestrator.validate() ===");
        return results;
    }

    /**
     * Maps a ValidationResource to its corresponding field type for proper field creation.
     * This ensures that the correct field type is used when generating fetch responses.
     * <p>
     * This method now supports dynamic field type extraction from method annotations
     * when method information is provided, falling back to hardcoded types when necessary.
     * </p>
     *
     * @param resource the ValidationResource to map
     * @param sourceMethod the method containing field annotations (can be null)
     * @param fieldNameToResourceMapping mapping of field names to validation resources (can be null)
     * @return the field type string corresponding to the resource
     */
    private String getFieldTypeForResource(Validator.ValidationResource resource,
                                          Method sourceMethod,
                                          Map<String, Validator.ValidationResource> fieldNameToResourceMapping) {

        System.out.println("  → Getting field type for resource: " + resource);

        // Try to extract field type dynamically if method information is available
        if (sourceMethod != null && fieldNameToResourceMapping != null) {
            System.out.println("  → Attempting dynamic field type extraction...");

            // Find the field name that corresponds to this validation resource
            String fieldName = findFieldNameForResource(resource, fieldNameToResourceMapping);
            System.out.println("  → Found field name for resource " + resource + ": " + fieldName);

            if (fieldName != null) {
                String extractedFieldType = FieldAttributeExtractor.extractFieldType(sourceMethod, fieldName);
                System.out.println("  → Extracted field type for field '" + fieldName + "': " + extractedFieldType);

                if (extractedFieldType != null) {
                    System.out.println("  → Using dynamically extracted field type: " + extractedFieldType);
                    return extractedFieldType;
                } else {
                    System.out.println("  → No field type found, falling back to hardcoded");
                }
            } else {
                System.out.println("  → No field name found for resource, falling back to hardcoded");
            }
        } else {
            System.out.println("  → No method information available, using hardcoded field types");
        }

        // Fallback to hardcoded field types for backward compatibility
        String fallbackFieldType = getFieldTypeForResourceFallback(resource);
        System.out.println("  → Using fallback field type: " + fallbackFieldType);
        return fallbackFieldType;
    }

    /**
     * Fallback method for hardcoded field types.
     * This method maintains backward compatibility when dynamic extraction is not available.
     *
     * @param resource the ValidationResource to map
     * @return the field type string corresponding to the resource
     */
    private String getFieldTypeForResourceFallback(Validator.ValidationResource resource) {
        return switch (resource) {
            case FROM_DATE, TO_DATE, DATE_RANGE, DATE_RANGE_TO -> "Date";
            case EMAIL -> "Email";
            case EVENT_ID -> "Text";
            case NUMBER_OF_MEETINGS, RECURRENCE_INTERVAL, NUMBERED_OCCURRENCES -> "Number";
            case ATTENDEES -> "[ Email ]"; // For list of emails, we'll use the list format
            case TIME_ZONE -> "Text"; // Time zone is a text field
            case DAYS_OF_WEEK -> "[ Text ]"; // For list of day names
            case MEETING_DURATION -> "Text"; // Meeting duration is a text field with specific format
            case ATTACHMENTS -> "[ File ]"; // For list of file attachments
            default -> "Text"; // Default fallback
        };
    }

    /**
     * Maps a ValidationResource to its corresponding field name for proper field creation.
     * This ensures that the correct field name is used when generating fetch responses.
     * <p>
     * This method supports dynamic field name extraction from method annotations
     * when method information is provided, falling back to validator field names when necessary.
     * </p>
     *
     * @param resource the ValidationResource to map
     * @param sourceMethod the method containing field annotations (can be null)
     * @param fieldNameToResourceMapping mapping of field names to validation resources (can be null)
     * @return the field name string corresponding to the resource
     */
    private String getFieldNameForResource(Validator.ValidationResource resource,
                                          Method sourceMethod,
                                          Map<String, Validator.ValidationResource> fieldNameToResourceMapping) {

        System.out.println("  → Getting field name for resource: " + resource);

        // Try to extract field name dynamically if method information is available
        if (sourceMethod != null && fieldNameToResourceMapping != null) {
            System.out.println("  → Attempting dynamic field name extraction...");

            // Find the field name that corresponds to this validation resource
            String fieldName = findFieldNameForResource(resource, fieldNameToResourceMapping);
            System.out.println("  → Found field name for resource " + resource + ": " + fieldName);

            if (fieldName != null) {
                System.out.println("  → Using dynamically extracted field name: " + fieldName);
                return fieldName;
            } else {
                System.out.println("  → No field name found for resource, falling back to validator field name");
            }
        } else {
            System.out.println("  → No method information available, using validator field name");
        }

        // Fallback to validator field name for backward compatibility
        Validator validator = validators.get(resource);
        String fallbackFieldName = validator != null ? validator.getFetchFieldName() : "Unknown Field";
        System.out.println("  → Using fallback field name: " + fallbackFieldName);
        return fallbackFieldName;
    }

    /**
     * Maps a ValidationResource to its corresponding field type for proper field creation.
     * This ensures that the correct field type is used when generating fetch responses.
     * <p>
     * This is the legacy method that uses hardcoded field types for backward compatibility.
     * </p>
     *
     * @param resource the ValidationResource to map
     * @return the field type string corresponding to the resource
     * @deprecated Use {@link #getFieldTypeForResource(Validator.ValidationResource, Method, Map)} instead
     */
    @Deprecated
    private String getFieldTypeForResource(Validator.ValidationResource resource) {
        return getFieldTypeForResourceFallback(resource);
    }

    /**
     * Maps a ValidationResource to its corresponding field attributes for proper field creation.
     * This ensures that the correct field attributes are used when generating fetch responses.
     * <p>
     * This method now supports dynamic field attribute extraction from method annotations
     * when method information is provided, falling back to hardcoded attributes when necessary.
     * </p>
     *
     * @param resource the ValidationResource to map
     * @param sourceMethod the method containing field annotations (can be null)
     * @param fieldNameToResourceMapping mapping of field names to validation resources (can be null)
     * @return the field attributes map corresponding to the resource
     */
    private Map<String, Object> getFieldAttributesForResource(Validator.ValidationResource resource,
                                                              Method sourceMethod,
                                                              Map<String, Validator.ValidationResource> fieldNameToResourceMapping) {

        System.out.println("  → Getting field attributes for resource: " + resource);

        // Try to extract attributes dynamically if method information is available
        if (sourceMethod != null && fieldNameToResourceMapping != null) {
            System.out.println("  → Attempting dynamic attribute extraction...");

            // Find the field name that corresponds to this validation resource
            String fieldName = findFieldNameForResource(resource, fieldNameToResourceMapping);
            System.out.println("  → Found field name for resource " + resource + ": " + fieldName);

            if (fieldName != null) {
                Map<String, Object> extractedAttributes = FieldAttributeExtractor.extractFieldAttributes(sourceMethod, fieldName);
                System.out.println("  → Extracted attributes for field '" + fieldName + "': " + extractedAttributes);

                if (!extractedAttributes.isEmpty()) {
                    System.out.println("  → Using dynamically extracted attributes");
                    return extractedAttributes;
                } else {
                    System.out.println("  → No attributes found, falling back to hardcoded");
                }
            } else {
                System.out.println("  → No field name found for resource, falling back to hardcoded");
            }
        } else {
            System.out.println("  → No method information available, using hardcoded attributes");
        }

        // Fallback to hardcoded attributes for backward compatibility
        Map<String, Object> fallbackAttributes = getFieldAttributesForResourceFallback(resource);
        System.out.println("  → Using fallback attributes: " + fallbackAttributes);
        return fallbackAttributes;
    }

    /**
     * Finds the field name that corresponds to a given validation resource.
     *
     * @param resource the validation resource to find the field name for
     * @param fieldNameToResourceMapping the mapping of field names to validation resources
     * @return the field name if found, null otherwise
     */
    private String findFieldNameForResource(Validator.ValidationResource resource,
                                           Map<String, Validator.ValidationResource> fieldNameToResourceMapping) {
        for (Map.Entry<String, Validator.ValidationResource> entry : fieldNameToResourceMapping.entrySet()) {
            if (resource.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Fallback method for hardcoded field attributes.
     * This method maintains backward compatibility when dynamic extraction is not available.
     *
     * @param resource the ValidationResource to map
     * @return the field attributes map corresponding to the resource
     */
    private Map<String, Object> getFieldAttributesForResourceFallback(Validator.ValidationResource resource) {
        return switch (resource) {
            case FROM_DATE, TO_DATE, DATE_RANGE, DATE_RANGE_TO -> Map.of("includeTimeOfDay", "true");
            case EMAIL -> Map.of(); // No special attributes for email fields
            case EVENT_ID -> Map.of(); // No special attributes for text fields
            case NUMBER_OF_MEETINGS, RECURRENCE_INTERVAL, NUMBERED_OCCURRENCES -> Map.of(); // No special attributes for number fields
            case MEETING_DURATION -> Map.of("visualWidth", "S"); // Small visual width for meeting duration
            case ATTENDEES -> Map.of(); // No special attributes for attendee email fields
            case TIME_ZONE -> Map.of(); // No special attributes for time zone fields
            case DAYS_OF_WEEK -> Map.of(); // No special attributes for days of week fields
            case ATTACHMENTS -> Map.of(); // No special attributes for attachment fields
            default -> Map.of(); // Default empty attributes
        };
    }

    /**
     * Maps a ValidationResource to its corresponding field attributes for proper field creation.
     * This ensures that the correct field attributes are used when generating fetch responses.
     * <p>
     * This is the legacy method that uses hardcoded attributes for backward compatibility.
     * </p>
     *
     * @param resource the ValidationResource to map
     * @return the field attributes map corresponding to the resource
     * @deprecated Use {@link #getFieldAttributesForResource(Validator.ValidationResource, Method, Map)} instead
     */
    @Deprecated
    private Map<String, Object> getFieldAttributesForResource(Validator.ValidationResource resource) {
        return getFieldAttributesForResourceFallback(resource);
    }
}
