package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(TextFieldAnnotationStrategy.class);

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        LOGGER.debug("        → TextFieldAnnotationStrategy.canHandle()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        boolean canHandle = textAnnotation != null && fieldName.equals(textAnnotation.name());

        if (textAnnotation != null) {
            LOGGER.debug("        → Found @Field.Text annotation with name: {}", textAnnotation.name());
            LOGGER.debug("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(textAnnotation.name()) + ")");
        } else {
            LOGGER.debug("        → No @Field.Text annotation found, cannot handle");
        }

        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        LOGGER.debug("        → TextFieldAnnotationStrategy.extractAttributes()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        if (textAnnotation == null) {
            LOGGER.debug("        → No @Field.Text annotation found, returning empty map");
            return Map.of();
        }

        if (!fieldName.equals(textAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + textAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        LOGGER.debug("        → ✓ Field name matches! Extracting attributes from @Field.Text annotation");
        LOGGER.debug("        → @Field.Text annotation details: name='" + textAnnotation.name() + "'");

        Map<String, Object> attributes = new HashMap<>();

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = textAnnotation.attributes();
        LOGGER.debug("        → Processing {} custom attributes", customAttributes.length);
        for (Attribute attribute : customAttributes) {
            LOGGER.debug("        → Adding attribute: {}={}", attribute.name(), attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }

        LOGGER.debug("        → Final extracted attributes from TextFieldAnnotationStrategy: {}", attributes);
        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        LOGGER.debug("        → TextFieldAnnotationStrategy.extractFieldType()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        if (textAnnotation == null) {
            LOGGER.debug("        → No @Field.Text annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(textAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + textAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Found @Field.Text annotation: name='" + fieldName + "' → type='Text'");
        LOGGER.debug("        → Returning field type from TextFieldAnnotationStrategy: Text");
        return "Text";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        LOGGER.debug("        → TextFieldAnnotationStrategy.mapToValidationResource()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        if (textAnnotation == null) {
            LOGGER.debug("        → No @Field.Text annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(textAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + textAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Mapping @Field.Text annotation: name='" + fieldName + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource("Text", fieldName);
        LOGGER.debug("        → TextFieldAnnotationStrategy mapped to validation resource: {}", resource);
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
        LOGGER.debug("          → TextFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        LOGGER.debug("          → Mapping text field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Handle specific field names
        if ("Time Zone".equals(fieldName)) {
            LOGGER.debug("          → ✓ Matched specific field name 'Time Zone' → TIME_ZONE");
            return Validator.ValidationResource.TIME_ZONE;
        }

        if ("Event ID".equals(fieldName) || "Event Id".equals(fieldName) || "EventId".equals(fieldName)) {
            LOGGER.debug("          → ✓ Matched specific field name 'Event ID' → EVENT_ID");
            return Validator.ValidationResource.EVENT_ID;
        }

        if("User Email".equals(fieldName)){
            LOGGER.debug("          → ✓ Matched specific field name 'User Email' → EMAIL");
            return Validator.ValidationResource.EMAIL;
        }

        if("Meeting Duration".equals(fieldName)){
            LOGGER.debug("          → ✓ Matched specific field name 'Meeting Duration' → MEETING_DURATION");
            return Validator.ValidationResource.MEETING_DURATION;
        }
        //

        // Default fallback
        LOGGER.debug("          → ⚠ No mapping found for text field '" + fieldName + "' → null");
        return null;
    }
}
