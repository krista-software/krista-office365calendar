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
        System.out.println("        → TextFieldAnnotationStrategy.canHandle()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        boolean canHandle = textAnnotation != null && fieldName.equals(textAnnotation.name());

        if (textAnnotation != null) {
            System.out.println("        → Found @Field.Text annotation with name: " + textAnnotation.name());
            System.out.println("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(textAnnotation.name()) + ")");
        } else {
            System.out.println("        → No @Field.Text annotation found, cannot handle");
        }

        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        System.out.println("        → TextFieldAnnotationStrategy.extractAttributes()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        if (textAnnotation == null) {
            System.out.println("        → No @Field.Text annotation found, returning empty map");
            return Map.of();
        }

        if (!fieldName.equals(textAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + textAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        System.out.println("        → ✓ Field name matches! Extracting attributes from @Field.Text annotation");
        System.out.println("        → @Field.Text annotation details: name='" + textAnnotation.name() + "'");

        Map<String, Object> attributes = new HashMap<>();

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = textAnnotation.attributes();
        System.out.println("        → Processing " + customAttributes.length + " custom attributes");
        for (Attribute attribute : customAttributes) {
            System.out.println("        → Adding attribute: " + attribute.name() + "=" + attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }

        System.out.println("        → Final extracted attributes from TextFieldAnnotationStrategy: " + attributes);
        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        System.out.println("        → TextFieldAnnotationStrategy.extractFieldType()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        if (textAnnotation == null) {
            System.out.println("        → No @Field.Text annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(textAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + textAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Found @Field.Text annotation: name='" + fieldName + "' → type='Text'");
        System.out.println("        → Returning field type from TextFieldAnnotationStrategy: Text");
        return "Text";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        System.out.println("        → TextFieldAnnotationStrategy.mapToValidationResource()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
        if (textAnnotation == null) {
            System.out.println("        → No @Field.Text annotation found, returning null");
            return null;
        }

        if (!fieldName.equals(textAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + textAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Mapping @Field.Text annotation: name='" + fieldName + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource("Text", fieldName);
        System.out.println("        → TextFieldAnnotationStrategy mapped to validation resource: " + resource);
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
        System.out.println("          → TextFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        System.out.println("          → Mapping text field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Handle specific field names
        if ("Time Zone".equals(fieldName)) {
            System.out.println("          → ✓ Matched specific field name 'Time Zone' → TIME_ZONE");
            return Validator.ValidationResource.TIME_ZONE;
        }

        // Default fallback
        System.out.println("          → ⚠ No mapping found for text field '" + fieldName + "' → null");
        return null;
    }
}
