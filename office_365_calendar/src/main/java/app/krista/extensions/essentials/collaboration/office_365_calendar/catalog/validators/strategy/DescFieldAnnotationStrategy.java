package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Strategy for handling @Field.Desc annotations.
 * <p>
 * This strategy extracts attributes, field types, and validation resource mappings
 * from @Field.Desc annotations. Note that @Field.Desc typically doesn't have attributes.
 * </p>
 */
public class DescFieldAnnotationStrategy implements FieldAnnotationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescFieldAnnotationStrategy.class);

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        LOGGER.debug("        → DescFieldAnnotationStrategy.canHandle()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        boolean canHandle = descAnnotation != null && fieldName.equals(descAnnotation.name());

        if (descAnnotation != null) {
            LOGGER.debug("        → Found @Field.Desc annotation with name: {}", descAnnotation.name());
            LOGGER.debug("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(descAnnotation.name()) + ")");
        } else {
            LOGGER.debug("        → No @Field.Desc annotation found, cannot handle");
        }

        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        LOGGER.debug("        → DescFieldAnnotationStrategy.extractAttributes()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        if (descAnnotation == null) {
            LOGGER.debug("        → No @Field.Desc annotation found, returning empty map");
            return Map.of();
        }

        if (!fieldName.equals(descAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + descAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        LOGGER.debug("        → ✓ Field name matches! Found @Field.Desc annotation: name='" + fieldName + "', type='" + descAnnotation.type() + "'");
        LOGGER.debug("        → @Field.Desc typically has no attributes, returning empty map");

        // Field.Desc typically doesn't have attributes, but we return empty map for consistency
        LOGGER.debug("        → Final extracted attributes from DescFieldAnnotationStrategy: {}");
        return Map.of();
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        LOGGER.debug("        → DescFieldAnnotationStrategy.extractFieldType()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        if (descAnnotation == null) {
            LOGGER.debug("        → No @Field.Desc annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(descAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + descAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Found @Field.Desc annotation: name='" + fieldName + "', type='" + descAnnotation.type() + "'");
        LOGGER.debug("        → Returning field type from DescFieldAnnotationStrategy: " + descAnnotation.type());
        return descAnnotation.type();
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        LOGGER.debug("        → DescFieldAnnotationStrategy.mapToValidationResource()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        if (descAnnotation == null) {
            LOGGER.debug("        → No @Field.Desc annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(descAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + descAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Mapping @Field.Desc annotation: name='" + fieldName + "', type='" + descAnnotation.type() + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource(descAnnotation.type(), fieldName);
        LOGGER.debug("        → DescFieldAnnotationStrategy mapped to validation resource: {}", resource);
        return resource;
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return Field.Desc.class;
    }

    @Override
    public String getStrategyName() {
        return "DescField";
    }

    /**
     * Maps a field type and name to the appropriate ValidationResource for @Field.Desc annotations.
     *
     * @param fieldType the type of the field from the annotation
     * @param fieldName the name of the field
     * @return the corresponding ValidationResource
     */
    private Validator.ValidationResource mapFieldTypeToValidationResource(String fieldType, String fieldName) {
        LOGGER.debug("          → DescFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        LOGGER.debug("          → Mapping desc field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Handle specific field names
        if ("Attendees".equals(fieldName)) {
            LOGGER.debug("          → ✓ Matched specific field name 'Attendees' → ATTENDEES");
            return Validator.ValidationResource.ATTENDEES;
        }

        // Default fallback
        LOGGER.debug("          → ⚠ No mapping found for desc field '" + fieldName + "' (type: '" + fieldType + "') → null");
        return null;
    }
}
