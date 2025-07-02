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
 * Strategy for handling @Field.Date annotations.
 * <p>
 * This strategy extracts date-specific attributes, field types, and validation resource mappings
 * from @Field.Date annotations.
 * </p>
 */
public class DateFieldAnnotationStrategy implements FieldAnnotationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateFieldAnnotationStrategy.class);

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        LOGGER.debug("        → DateFieldAnnotationStrategy.canHandle()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        boolean canHandle = dateAnnotation != null && fieldName.equals(dateAnnotation.name());

        if (dateAnnotation != null) {
            LOGGER.debug("        → Found @Field.Date annotation with name: {}", dateAnnotation.name());
            LOGGER.debug("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(dateAnnotation.name()) + ")");
        } else {
            LOGGER.debug("        → No @Field.Date annotation found, cannot handle");
        }

        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        LOGGER.debug("        → DateFieldAnnotationStrategy.extractAttributes()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        if (dateAnnotation == null) {
            LOGGER.debug("        → No @Field.Date annotation found, returning empty map");
            return Map.of();
        }

        if (!fieldName.equals(dateAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + dateAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        LOGGER.debug("        → ✓ Field name matches! Extracting attributes from @Field.Date annotation");
        LOGGER.debug("        → @Field.Date annotation details: name='" + dateAnnotation.name() + "'");

        Map<String, Object> attributes = new HashMap<>();

        // Add date-specific attributes
        if (dateAnnotation.includeTimeOfDay()) {
            LOGGER.debug("        → Adding includeTimeOfDay=true");
            attributes.put("includeTimeOfDay", "true");
        }

        if (dateAnnotation.showHowManyDaysInViewer() > 0) {
            LOGGER.debug("        → Adding showHowManyDaysInViewer=" + dateAnnotation.showHowManyDaysInViewer());
            attributes.put("showHowManyDaysInViewer", String.valueOf(dateAnnotation.showHowManyDaysInViewer()));
        }

        if (!dateAnnotation.allowPast()) {
            LOGGER.debug("        → Adding allowPast=false");
            attributes.put("allowPast", "false");
        }

        if (!dateAnnotation.allowToday()) {
            LOGGER.debug("        → Adding allowToday=false");
            attributes.put("allowToday", "false");
        }

        if (!dateAnnotation.allowFuture()) {
            LOGGER.debug("        → Adding allowFuture=false");
            attributes.put("allowFuture", "false");
        }

        if (dateAnnotation.defaultTimeSpan() > 0) {
            LOGGER.debug("        → Adding defaultTimeSpan=" + dateAnnotation.defaultTimeSpan());
            attributes.put("defaultTimeSpan", String.valueOf(dateAnnotation.defaultTimeSpan()));
        }

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = dateAnnotation.attributes();
        LOGGER.debug("        → Processing {} custom attributes", customAttributes.length);
        for (Attribute attribute : customAttributes) {
            LOGGER.debug("        → Adding custom attribute: " + attribute.name() + "=" + attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }

        LOGGER.debug("        → Final extracted attributes from DateFieldAnnotationStrategy: {}", attributes);
        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        LOGGER.debug("        → DateFieldAnnotationStrategy.extractFieldType()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        if (dateAnnotation == null) {
            LOGGER.debug("        → No @Field.Date annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(dateAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + dateAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Found @Field.Date annotation: name='" + fieldName + "' → type='Date'");
        LOGGER.debug("        → Returning field type from DateFieldAnnotationStrategy: Date");
        return "Date";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        LOGGER.debug("        → DateFieldAnnotationStrategy.mapToValidationResource()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);

        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        if (dateAnnotation == null) {
            LOGGER.debug("        → No @Field.Date annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(dateAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + dateAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Mapping @Field.Date annotation: name='" + fieldName + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource("Date", fieldName);
        LOGGER.debug("        → DateFieldAnnotationStrategy mapped to validation resource: {}", resource);
        return resource;
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return Field.Date.class;
    }

    @Override
    public String getStrategyName() {
        return "DateField";
    }

    /**
     * Maps a field type and name to the appropriate ValidationResource for @Field.Date annotations.
     *
     * @param fieldType the type of the field (always "Date" for this strategy)
     * @param fieldName the name of the field
     * @return the corresponding ValidationResource
     */
    private Validator.ValidationResource mapFieldTypeToValidationResource(String fieldType, String fieldName) {
        LOGGER.debug("          → DateFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        LOGGER.debug("          → Mapping date field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Handle specific field names
        if ("Date From".equals(fieldName) || "From".equals(fieldName)) {
            LOGGER.debug("          → ✓ Matched specific field name 'Date From' → FROM_DATE");
            return Validator.ValidationResource.FROM_DATE;
        }
        if ("Date To".equals(fieldName) || "Date to".equals(fieldName) || "To".equals(fieldName)) {
            LOGGER.debug("          → ✓ Matched specific field name 'Date To' → TO_DATE");
            return Validator.ValidationResource.TO_DATE;
        }

        // Default fallback
        LOGGER.debug("          → ⚠ No mapping found for date field '" + fieldName + "' → null");
        return null;
    }
}
