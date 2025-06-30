package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy;

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

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        System.out.println("        → DescFieldAnnotationStrategy.canHandle()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        boolean canHandle = descAnnotation != null && fieldName.equals(descAnnotation.name());

        if (descAnnotation != null) {
            System.out.println("        → Found @Field.Desc annotation with name: " + descAnnotation.name());
            System.out.println("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(descAnnotation.name()) + ")");
        } else {
            System.out.println("        → No @Field.Desc annotation found, cannot handle");
        }

        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        System.out.println("        → DescFieldAnnotationStrategy.extractAttributes()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        if (descAnnotation == null) {
            System.out.println("        → No @Field.Desc annotation found, returning empty map");
            return Map.of();
        }

        if (!fieldName.equals(descAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + descAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        System.out.println("        → ✓ Field name matches! Found @Field.Desc annotation: name='" + fieldName + "', type='" + descAnnotation.type() + "'");
        System.out.println("        → @Field.Desc typically has no attributes, returning empty map");

        // Field.Desc typically doesn't have attributes, but we return empty map for consistency
        System.out.println("        → Final extracted attributes from DescFieldAnnotationStrategy: {}");
        return Map.of();
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        System.out.println("        → DescFieldAnnotationStrategy.extractFieldType()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        if (descAnnotation == null) {
            System.out.println("        → No @Field.Desc annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(descAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + descAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Found @Field.Desc annotation: name='" + fieldName + "', type='" + descAnnotation.type() + "'");
        System.out.println("        → Returning field type from DescFieldAnnotationStrategy: " + descAnnotation.type());
        return descAnnotation.type();
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        System.out.println("        → DescFieldAnnotationStrategy.mapToValidationResource()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        if (descAnnotation == null) {
            System.out.println("        → No @Field.Desc annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(descAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + descAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Mapping @Field.Desc annotation: name='" + fieldName + "', type='" + descAnnotation.type() + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource(descAnnotation.type(), fieldName);
        System.out.println("        → DescFieldAnnotationStrategy mapped to validation resource: " + resource);
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
        System.out.println("          → DescFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        System.out.println("          → Mapping desc field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Handle specific field names
        if ("Attendees".equals(fieldName)) {
            System.out.println("          → ✓ Matched specific field name 'Attendees' → ATTENDEES");
            return Validator.ValidationResource.ATTENDEES;
        }

        // Default fallback
        System.out.println("          → ⚠ No mapping found for desc field '" + fieldName + "' (type: '" + fieldType + "') → null");
        return null;
    }
}
