package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy;

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

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        System.out.println("        → DateFieldAnnotationStrategy.canHandle()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        boolean canHandle = dateAnnotation != null && fieldName.equals(dateAnnotation.name());

        if (dateAnnotation != null) {
            System.out.println("        → Found @Field.Date annotation with name: " + dateAnnotation.name());
            System.out.println("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(dateAnnotation.name()) + ")");
        } else {
            System.out.println("        → No @Field.Date annotation found, cannot handle");
        }

        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        System.out.println("        → DateFieldAnnotationStrategy.extractAttributes()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        if (dateAnnotation == null) {
            System.out.println("        → No @Field.Date annotation found, returning empty map");
            return Map.of();
        }

        if (!fieldName.equals(dateAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + dateAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        System.out.println("        → ✓ Field name matches! Extracting attributes from @Field.Date annotation");
        System.out.println("        → @Field.Date annotation details: name='" + dateAnnotation.name() + "'");

        Map<String, Object> attributes = new HashMap<>();

        // Add date-specific attributes
        if (dateAnnotation.includeTimeOfDay()) {
            System.out.println("        → Adding includeTimeOfDay=true");
            attributes.put("includeTimeOfDay", "true");
        }

        if (dateAnnotation.showHowManyDaysInViewer() > 0) {
            System.out.println("        → Adding showHowManyDaysInViewer=" + dateAnnotation.showHowManyDaysInViewer());
            attributes.put("showHowManyDaysInViewer", String.valueOf(dateAnnotation.showHowManyDaysInViewer()));
        }

        if (!dateAnnotation.allowPast()) {
            System.out.println("        → Adding allowPast=false");
            attributes.put("allowPast", "false");
        }

        if (!dateAnnotation.allowToday()) {
            System.out.println("        → Adding allowToday=false");
            attributes.put("allowToday", "false");
        }

        if (!dateAnnotation.allowFuture()) {
            System.out.println("        → Adding allowFuture=false");
            attributes.put("allowFuture", "false");
        }

        if (dateAnnotation.defaultTimeSpan() > 0) {
            System.out.println("        → Adding defaultTimeSpan=" + dateAnnotation.defaultTimeSpan());
            attributes.put("defaultTimeSpan", String.valueOf(dateAnnotation.defaultTimeSpan()));
        }

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = dateAnnotation.attributes();
        System.out.println("        → Processing " + customAttributes.length + " custom attributes");
        for (Attribute attribute : customAttributes) {
            System.out.println("        → Adding custom attribute: " + attribute.name() + "=" + attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }

        System.out.println("        → Final extracted attributes from DateFieldAnnotationStrategy: " + attributes);
        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        System.out.println("        → DateFieldAnnotationStrategy.extractFieldType()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        if (dateAnnotation == null) {
            System.out.println("        → No @Field.Date annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(dateAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + dateAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Found @Field.Date annotation: name='" + fieldName + "' → type='Date'");
        System.out.println("        → Returning field type from DateFieldAnnotationStrategy: Date");
        return "Date";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        System.out.println("        → DateFieldAnnotationStrategy.mapToValidationResource()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        if (dateAnnotation == null) {
            System.out.println("        → No @Field.Date annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(dateAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + dateAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Mapping @Field.Date annotation: name='" + fieldName + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource("Date", fieldName);
        System.out.println("        → DateFieldAnnotationStrategy mapped to validation resource: " + resource);
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
        System.out.println("          → DateFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        System.out.println("          → Mapping date field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Handle specific field names
        if ("Date From".equals(fieldName) || "From".equals(fieldName)) {
            System.out.println("          → ✓ Matched specific field name 'Date From' → FROM_DATE");
            return Validator.ValidationResource.FROM_DATE;
        }
        if ("Date To".equals(fieldName) || "Date to".equals(fieldName) || "To".equals(fieldName)) {
            System.out.println("          → ✓ Matched specific field name 'Date To' → TO_DATE");
            return Validator.ValidationResource.TO_DATE;
        }

        // Default fallback
        System.out.println("          → ⚠ No mapping found for date field '" + fieldName + "' → null");
        return null;
    }
}
