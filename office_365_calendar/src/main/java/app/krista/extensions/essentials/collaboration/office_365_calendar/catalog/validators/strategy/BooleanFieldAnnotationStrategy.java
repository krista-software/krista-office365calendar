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
 * Strategy for handling @Field.Boolean annotations.
 * <p>
 * This strategy extracts attributes, field types, and validation resource mappings
 * from @Field.Boolean annotations.
 * </p>
 */
public class BooleanFieldAnnotationStrategy implements FieldAnnotationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooleanFieldAnnotationStrategy.class);

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        LOGGER.debug("        → BooleanFieldAnnotationStrategy.canHandle()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        boolean canHandle = booleanAnnotation != null && fieldName.equals(booleanAnnotation.name());

        if (booleanAnnotation != null) {
            LOGGER.debug("        → Found @Field.Boolean annotation with name: {}", booleanAnnotation.name());
            LOGGER.debug("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(booleanAnnotation.name()) + ")");
        } else {
            LOGGER.debug("        → No @Field.Boolean annotation found, cannot handle");
        }

        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        LOGGER.debug("        → BooleanFieldAnnotationStrategy.extractAttributes()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        if (booleanAnnotation == null) {
            LOGGER.debug("        → No @Field.Boolean annotation found, returning empty map");
            return Map.of();
        }

        if (!fieldName.equals(booleanAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + booleanAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        LOGGER.debug("        → ✓ Field name matches! Extracting attributes from @Field.Boolean annotation");
        LOGGER.debug("        → @Field.Boolean annotation details: name='" + booleanAnnotation.name() + "'");

        Map<String, Object> attributes = new HashMap<>();

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = booleanAnnotation.attributes();
        LOGGER.debug("        → Processing {} custom attributes", customAttributes.length);
        for (Attribute attribute : customAttributes) {
            LOGGER.debug("        → Adding attribute: {}={}", attribute.name(), attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }

        LOGGER.debug("        → Final extracted attributes from BooleanFieldAnnotationStrategy: {}", attributes);
        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        LOGGER.debug("        → BooleanFieldAnnotationStrategy.extractFieldType()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        if (booleanAnnotation == null) {
            LOGGER.debug("        → No @Field.Boolean annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(booleanAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + booleanAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Found @Field.Boolean annotation: name='" + fieldName + "' → type='Boolean'");
        LOGGER.debug("        → Returning field type from BooleanFieldAnnotationStrategy: Boolean");
        return "Boolean";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        LOGGER.debug("        → BooleanFieldAnnotationStrategy.mapToValidationResource()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        if (booleanAnnotation == null) {
            LOGGER.debug("        → No @Field.Boolean annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(booleanAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + booleanAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Mapping @Field.Boolean annotation: name='" + fieldName + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource("Boolean", fieldName);
        LOGGER.debug("        → BooleanFieldAnnotationStrategy mapped to validation resource: {}", resource);
        return resource;
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return Field.Boolean.class;
    }

    @Override
    public String getStrategyName() {
        return "BooleanField";
    }

    /**
     * Maps a field type and name to the appropriate ValidationResource for @Field.Boolean annotations.
     *
     * @param fieldType the type of the field (always "Boolean" for this strategy)
     * @param fieldName the name of the field
     * @return the corresponding ValidationResource
     */
    private Validator.ValidationResource mapFieldTypeToValidationResource(String fieldType, String fieldName) {
        LOGGER.debug("          → BooleanFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        LOGGER.debug("          → Mapping boolean field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Currently no specific boolean field mappings exist in the original code
        // This can be extended in the future if needed

        // Default fallback
        LOGGER.debug("          → ⚠ No mapping found for boolean field '" + fieldName + "' → null");
        return null;
    }
}
