package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy;

import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Strategy for handling @Field.Text annotations.
 * <p>
 * This strategy extracts attributes, field types, and validation resource mappings
 * from @Field.Text annotations.
 * </p>
 */
public class TextFieldAnnotationStrategy implements FieldAnnotationStrategy {

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        boolean canHandle = textAnnotation != null && fieldName.equals(textAnnotation.name());
        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        if (textAnnotation == null) {
            return Map.of();
        }

        if (!fieldName.equals(textAnnotation.name())) {
            return Map.of();
        }

        Map<String, Object> attributes = new HashMap<>();

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = textAnnotation.attributes();
        for (Attribute attribute : customAttributes) {
            attributes.put(attribute.name(), attribute.value());
        }

        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        if (textAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(textAnnotation.name())) {
            return null;
        }

        return "Text";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        if (textAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(textAnnotation.name())) {
            return null;
        }

        Validator.ValidationResource resource = mapFieldTypeToValidationResource("Text", fieldName);
        return resource;
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return Field.Text.class;
    }

    @Override
    public String getStrategyName() {
        return "TextField";
    }

    /**
     * Maps a field type and name to the appropriate ValidationResource for @Field.Text annotations.
     *
     * @param fieldType the type of the field (always "Text" for this strategy)
     * @param fieldName the name of the field
     * @return the corresponding ValidationResource
     */
    private Validator.ValidationResource mapFieldTypeToValidationResource(String fieldType, String fieldName) {
        // Handle specific field names
        if ("Time Zone".equals(fieldName)) {
            return Validator.ValidationResource.TIME_ZONE;
        }

        if ("Event ID".equals(fieldName) || "Event Id".equals(fieldName) || "EventId".equals(fieldName)) {
            return Validator.ValidationResource.EVENT_ID;
        }

        if("User Email".equals(fieldName)){
            return Validator.ValidationResource.EMAIL;
        }

        if("Meeting Duration".equals(fieldName)){
            return Validator.ValidationResource.MEETING_DURATION;
        }

        // Default fallback
        return null;
    }
}
