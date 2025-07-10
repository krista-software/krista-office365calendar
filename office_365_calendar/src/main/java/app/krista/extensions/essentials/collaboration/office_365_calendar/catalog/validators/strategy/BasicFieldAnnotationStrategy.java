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
 * Strategy for handling @Field annotations.
 * <p>
 * This strategy extracts attributes, field types, and validation resource mappings
 * from basic @Field annotations.
 * </p>
 */
public class BasicFieldAnnotationStrategy implements FieldAnnotationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicFieldAnnotationStrategy.class);

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        LOGGER.debug("        → BasicFieldAnnotationStrategy.canHandle()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        boolean canHandle = fieldAnnotation != null && fieldName.equals(fieldAnnotation.name());

        if (fieldAnnotation != null) {
            LOGGER.debug("        → Found @Field annotation with name: {}", fieldAnnotation.name());
            LOGGER.debug("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(fieldAnnotation.name()) + ")");
        } else {
            LOGGER.debug("        → No @Field annotation found, cannot handle");
        }

        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        LOGGER.debug("        → BasicFieldAnnotationStrategy.extractAttributes()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        if (fieldAnnotation == null) {
            LOGGER.debug("        → No @Field annotation found, returning empty map");
            return Map.of();
        }

        if (!fieldName.equals(fieldAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + fieldAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        LOGGER.debug("        → ✓ Field name matches! Extracting attributes from @Field annotation");
        LOGGER.debug("        → @Field annotation details: name='" + fieldAnnotation.name() + "', type='" + fieldAnnotation.type() + "'");

        Map<String, Object> attributes = new HashMap<>();

        Attribute[] customAttributes = fieldAnnotation.attributes();
        LOGGER.debug("        → Processing {} custom attributes", customAttributes.length);
        for (Attribute attribute : customAttributes) {
            LOGGER.debug("        → Adding attribute: {}={}", attribute.name(), attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }

        LOGGER.debug("        → Final extracted attributes from BasicFieldAnnotationStrategy: {}", attributes);
        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        LOGGER.debug("        → BasicFieldAnnotationStrategy.extractFieldType()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        if (fieldAnnotation == null) {
            LOGGER.debug("        → No @Field annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(fieldAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + fieldAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Found @Field annotation: name='" + fieldName + "', type='" + fieldAnnotation.type() + "'");
        LOGGER.debug("        → Returning field type from BasicFieldAnnotationStrategy: " + fieldAnnotation.type());
        return fieldAnnotation.type();
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        LOGGER.debug("        → BasicFieldAnnotationStrategy.mapToValidationResource()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        if (fieldAnnotation == null) {
            LOGGER.debug("        → No @Field annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(fieldAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + fieldAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Mapping @Field annotation: name='" + fieldName + "', type='" + fieldAnnotation.type() + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource(fieldAnnotation.type(), fieldName);
        LOGGER.debug("        → BasicFieldAnnotationStrategy mapped to validation resource: {}", resource);
        return resource;
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return Field.class;
    }

    @Override
    public String getStrategyName() {
        return "BasicField";
    }

    /**
     * Maps a field type and name to the appropriate ValidationResource for @Field annotations.
     *
     * @param fieldType the type of the field
     * @param fieldName the name of the field
     * @return the corresponding ValidationResource
     */
    private Validator.ValidationResource mapFieldTypeToValidationResource(String fieldType, String fieldName) {
        LOGGER.debug("          → BasicFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        LOGGER.debug("          → Mapping field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Handle specific field names first
        if ("Date From".equals(fieldName)) {
            LOGGER.debug("          → ✓ Matched specific field name 'Date From' → FROM_DATE");
            return Validator.ValidationResource.FROM_DATE;
        }
        if ("Date To".equals(fieldName)) {
            LOGGER.debug("          → ✓ Matched specific field name 'Date To' → TO_DATE");
            return Validator.ValidationResource.TO_DATE;
        }
        if ("Event ID".equals(fieldName)) {
            LOGGER.debug("          → ✓ Matched specific field name 'Event ID' → EVENT_ID");
            return Validator.ValidationResource.EVENT_ID;
        }
        if ("Number of meeting".equals(fieldName) || "Number of meetings".equals(fieldName)) {
            LOGGER.debug("          → ✓ Matched specific field name '" + fieldName + "' → NUMBER_OF_MEETINGS");
            return Validator.ValidationResource.NUMBER_OF_MEETINGS;
        }
        if ("Time Zone".equals(fieldName)) {
            LOGGER.debug("          → ✓ Matched specific field name 'Time Zone' → TIME_ZONE");
            return Validator.ValidationResource.TIME_ZONE;
        }

        // Default fallback
        LOGGER.debug("          → ⚠ No mapping found for field '" + fieldName + "' (type: '" + fieldType + "') → null");
        return null;
    }
}
