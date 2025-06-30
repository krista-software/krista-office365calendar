package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy;

import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Strategy for handling @Field.File annotations.
 * <p>
 * This strategy extracts attributes, field types, and validation resource mappings
 * from @Field.File annotations.
 * </p>
 */
public class FileFieldAnnotationStrategy implements FieldAnnotationStrategy {

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        System.out.println("        → FileFieldAnnotationStrategy.canHandle()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);
        
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        boolean canHandle = fileAnnotation != null && fieldName.equals(fileAnnotation.name());
        
        if (fileAnnotation != null) {
            System.out.println("        → Found @Field.File annotation with name: " + fileAnnotation.name());
            System.out.println("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(fileAnnotation.name()) + ")");
        } else {
            System.out.println("        → No @Field.File annotation found, cannot handle");
        }
        
        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        System.out.println("        → FileFieldAnnotationStrategy.extractAttributes()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);
        
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        if (fileAnnotation == null) {
            System.out.println("        → No @Field.File annotation found, returning empty map");
            return Map.of();
        }
        
        if (!fieldName.equals(fileAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + fileAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        System.out.println("        → ✓ Field name matches! Extracting attributes from @Field.File annotation");
        System.out.println("        → @Field.File annotation details: name='" + fileAnnotation.name() + "'");
        
        Map<String, Object> attributes = new HashMap<>();

        // Add file-specific attributes
        if (fileAnnotation.multipleFileUpload()) {
            System.out.println("        → Adding multipleFileUpload=true");
            attributes.put("multipleFileUpload", "true");
        } else {
            System.out.println("        → Adding multipleFileUpload=false");
            attributes.put("multipleFileUpload", "false");
        }

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = fileAnnotation.attributes();
        System.out.println("        → Processing " + customAttributes.length + " custom attributes");
        for (Attribute attribute : customAttributes) {
            System.out.println("        → Adding attribute: " + attribute.name() + "=" + attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }
        
        System.out.println("        → Final extracted attributes from FileFieldAnnotationStrategy: " + attributes);
        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        System.out.println("        → FileFieldAnnotationStrategy.extractFieldType()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);
        
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        if (fileAnnotation == null) {
            System.out.println("        → No @Field.File annotation found, returning null");
            return null;
        }
        
        if (!fieldName.equals(fileAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + fileAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Found @Field.File annotation: name='" + fieldName + "' → type='File'");
        System.out.println("        → Returning field type from FileFieldAnnotationStrategy: File");
        return "File";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        System.out.println("        → FileFieldAnnotationStrategy.mapToValidationResource()");
        System.out.println("        → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);
        
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        if (fileAnnotation == null) {
            System.out.println("        → No @Field.File annotation found, returning null");
            return null;
        }
        
        if (!fieldName.equals(fileAnnotation.name())) {
            System.out.println("        → Field name mismatch: expected '" + fieldName + "', found '" + fileAnnotation.name() + "', returning null");
            return null;
        }

        System.out.println("        → ✓ Field name matches! Mapping @Field.File annotation: name='" + fieldName + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource("File", fieldName);
        System.out.println("        → FileFieldAnnotationStrategy mapped to validation resource: " + resource);
        return resource;
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return Field.File.class;
    }

    @Override
    public String getStrategyName() {
        return "FileField";
    }

    /**
     * Maps a field type and name to the appropriate ValidationResource for @Field.File annotations.
     *
     * @param fieldType the type of the field (always "File" for this strategy)
     * @param fieldName the name of the field
     * @return the corresponding ValidationResource
     */
    private Validator.ValidationResource mapFieldTypeToValidationResource(String fieldType, String fieldName) {
        System.out.println("          → FileFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        System.out.println("          → Mapping file field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Handle specific field names for file fields
        if ("Attachments".equals(fieldName)) {
            System.out.println("          → ✓ Matched specific field name 'Attachments' → ATTACHMENTS");
            return Validator.ValidationResource.ATTACHMENTS;
        }

        // Default fallback
        System.out.println("          → ⚠ No mapping found for file field '" + fieldName + "' → null");
        return null;
    }
}
