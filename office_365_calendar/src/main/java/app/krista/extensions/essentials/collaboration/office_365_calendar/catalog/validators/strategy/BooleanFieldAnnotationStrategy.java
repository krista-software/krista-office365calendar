package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy;

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

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        System.out.println("        → BooleanFieldAnnotationStrategy.canHandle()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        boolean canHandle = booleanAnnotation != null && fieldName.equals(booleanAnnotation.name());

        if (booleanAnnotation != null) {
            System.out.println("        → Found @Field.Boolean annotation with name: " + booleanAnnotation.name());
            System.out.println("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(booleanAnnotation.name()) + ")");
        } else {
            System.out.println("        → No @Field.Boolean annotation found, cannot handle");
        }

        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        System.out.println("        → BooleanFieldAnnotationStrategy.extractAttributes()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        if (booleanAnnotation == null) {
            System.out.println("        → No @Field.Boolean annotation found, returning empty map");
            return Map.of();
        }

        if (!fieldName.equals(booleanAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + booleanAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        System.out.println("        → ✓ Field name matches! Extracting attributes from @Field.Boolean annotation");
        System.out.println("        → @Field.Boolean annotation details: name='" + booleanAnnotation.name() + "'");

        Map<String, Object> attributes = new HashMap<>();

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = booleanAnnotation.attributes();
        System.out.println("        → Processing " + customAttributes.length + " custom attributes");
        for (Attribute attribute : customAttributes) {
            System.out.println("        → Adding attribute: " + attribute.name() + "=" + attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }

        System.out.println("        → Final extracted attributes from BooleanFieldAnnotationStrategy: " + attributes);
        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        System.out.println("        → BooleanFieldAnnotationStrategy.extractFieldType()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        if (booleanAnnotation == null) {
            System.out.println("        → No @Field.Boolean annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(booleanAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + booleanAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Found @Field.Boolean annotation: name='" + fieldName + "' → type='Boolean'");
        System.out.println("        → Returning field type from BooleanFieldAnnotationStrategy: Boolean");
        return "Boolean";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        System.out.println("        → BooleanFieldAnnotationStrategy.mapToValidationResource()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        if (booleanAnnotation == null) {
            System.out.println("        → No @Field.Boolean annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(booleanAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + booleanAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Mapping @Field.Boolean annotation: name='" + fieldName + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource("Boolean", fieldName);
        System.out.println("        → BooleanFieldAnnotationStrategy mapped to validation resource: " + resource);
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
        System.out.println("          → BooleanFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        System.out.println("          → Mapping boolean field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Currently no specific boolean field mappings exist in the original code
        // This can be extended in the future if needed

        // Default fallback
        System.out.println("          → ⚠ No mapping found for boolean field '" + fieldName + "' → null");
        return null;
    }
}
