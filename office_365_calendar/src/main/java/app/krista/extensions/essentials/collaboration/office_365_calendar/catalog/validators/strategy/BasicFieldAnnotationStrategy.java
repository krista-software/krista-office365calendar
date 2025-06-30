package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy;

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

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        System.out.println("        → BasicFieldAnnotationStrategy.canHandle()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        boolean canHandle = fieldAnnotation != null && fieldName.equals(fieldAnnotation.name());

        if (fieldAnnotation != null) {
            System.out.println("        → Found @Field annotation with name: " + fieldAnnotation.name());
            System.out.println("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(fieldAnnotation.name()) + ")");
        } else {
            System.out.println("        → No @Field annotation found, cannot handle");
        }

        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        System.out.println("        → BasicFieldAnnotationStrategy.extractAttributes()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        if (fieldAnnotation == null) {
            System.out.println("        → No @Field annotation found, returning empty map");
            return Map.of();
        }

        if (!fieldName.equals(fieldAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + fieldAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        System.out.println("        → ✓ Field name matches! Extracting attributes from @Field annotation");
        System.out.println("        → @Field annotation details: name='" + fieldAnnotation.name() + "', type='" + fieldAnnotation.type() + "'");

        Map<String, Object> attributes = new HashMap<>();

        Attribute[] customAttributes = fieldAnnotation.attributes();
        System.out.println("        → Processing " + customAttributes.length + " custom attributes");
        for (Attribute attribute : customAttributes) {
            System.out.println("        → Adding attribute: " + attribute.name() + "=" + attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }

        System.out.println("        → Final extracted attributes from BasicFieldAnnotationStrategy: " + attributes);
        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        System.out.println("        → BasicFieldAnnotationStrategy.extractFieldType()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        if (fieldAnnotation == null) {
            System.out.println("        → No @Field annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(fieldAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + fieldAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Found @Field annotation: name='" + fieldName + "', type='" + fieldAnnotation.type() + "'");
        System.out.println("        → Returning field type from BasicFieldAnnotationStrategy: " + fieldAnnotation.type());
        return fieldAnnotation.type();
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        System.out.println("        → BasicFieldAnnotationStrategy.mapToValidationResource()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        if (fieldAnnotation == null) {
            System.out.println("        → No @Field annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(fieldAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + fieldAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Mapping @Field annotation: name='" + fieldName + "', type='" + fieldAnnotation.type() + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource(fieldAnnotation.type(), fieldName);
        System.out.println("        → BasicFieldAnnotationStrategy mapped to validation resource: " + resource);
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
        System.out.println("          → BasicFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        System.out.println("          → Mapping field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Handle specific field names first
        if ("Date From".equals(fieldName)) {
            System.out.println("          → ✓ Matched specific field name 'Date From' → FROM_DATE");
            return Validator.ValidationResource.FROM_DATE;
        }
        if ("Date To".equals(fieldName)) {
            System.out.println("          → ✓ Matched specific field name 'Date To' → TO_DATE");
            return Validator.ValidationResource.TO_DATE;
        }
        if ("Event ID".equals(fieldName)) {
            System.out.println("          → ✓ Matched specific field name 'Event ID' → EVENT_ID");
            return Validator.ValidationResource.EVENT_ID;
        }
        if ("Number of meeting".equals(fieldName) || "Number of meetings".equals(fieldName)) {
            System.out.println("          → ✓ Matched specific field name '" + fieldName + "' → NUMBER_OF_MEETINGS");
            return Validator.ValidationResource.NUMBER_OF_MEETINGS;
        }
        if ("Time Zone".equals(fieldName)) {
            System.out.println("          → ✓ Matched specific field name 'Time Zone' → TIME_ZONE");
            return Validator.ValidationResource.TIME_ZONE;
        }

        // Default fallback
        System.out.println("          → ⚠ No mapping found for field '" + fieldName + "' (type: '" + fieldType + "') → null");
        return null;
    }
}
