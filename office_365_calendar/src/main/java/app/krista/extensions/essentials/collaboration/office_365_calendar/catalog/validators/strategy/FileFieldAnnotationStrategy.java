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
 * Strategy for handling @Field.File annotations.
 * <p>
 * This strategy extracts attributes, field types, and validation resource mappings
 * from @Field.File annotations.
 * </p>
 */
public class FileFieldAnnotationStrategy implements FieldAnnotationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileFieldAnnotationStrategy.class);

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        LOGGER.debug("        → FileFieldAnnotationStrategy.canHandle()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);
        
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        boolean canHandle = fileAnnotation != null && fieldName.equals(fileAnnotation.name());
        
        if (fileAnnotation != null) {
            LOGGER.debug("        → Found @Field.File annotation with name: {}", fileAnnotation.name());
            LOGGER.debug("        → Can handle: " + canHandle + " (field name match: " + fieldName.equals(fileAnnotation.name()) + ")");
        } else {
            LOGGER.debug("        → No @Field.File annotation found, cannot handle");
        }
        
        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        LOGGER.debug("        → FileFieldAnnotationStrategy.extractAttributes()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);
        
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        if (fileAnnotation == null) {
            LOGGER.debug("        → No @Field.File annotation found, returning empty map");
            return Map.of();
        }
        
        if (!fieldName.equals(fileAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + fileAnnotation.name() + "', returning empty map");
            return Map.of();
        }

        LOGGER.debug("        → ✓ Field name matches! Extracting attributes from @Field.File annotation");
        LOGGER.debug("        → @Field.File annotation details: name='" + fileAnnotation.name() + "'");
        
        Map<String, Object> attributes = new HashMap<>();

        // Add file-specific attributes
        if (fileAnnotation.multipleFileUpload()) {
            LOGGER.debug("        → Adding multipleFileUpload=true");
            attributes.put("multipleFileUpload", "true");
        } else {
            LOGGER.debug("        → Adding multipleFileUpload=false");
            attributes.put("multipleFileUpload", "false");
        }

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = fileAnnotation.attributes();
        LOGGER.debug("        → Processing {} custom attributes", customAttributes.length);
        for (Attribute attribute : customAttributes) {
            LOGGER.debug("        → Adding attribute: {}={}", attribute.name(), attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }
        
        LOGGER.debug("        → Final extracted attributes from FileFieldAnnotationStrategy: {}", attributes);
        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        LOGGER.debug("        → FileFieldAnnotationStrategy.extractFieldType()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);
        
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        if (fileAnnotation == null) {
            LOGGER.debug("        → No @Field.File annotation found, returning null");
            return null;
        }
        
        if (!fieldName.equals(fileAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + fileAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Found @Field.File annotation: name='" + fieldName + "' → type='File'");
        LOGGER.debug("        → Returning field type from FileFieldAnnotationStrategy: File");
        return "File";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        LOGGER.debug("        → FileFieldAnnotationStrategy.mapToValidationResource()");
        LOGGER.debug("        → Parameter: {}, Field Name: {}", parameter.getType().getSimpleName(), fieldName);
        
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        if (fileAnnotation == null) {
            LOGGER.debug("        → No @Field.File annotation found, returning null");
            return null;
        }
        
        if (!fieldName.equals(fileAnnotation.name())) {
            LOGGER.debug("        → Field name mismatch: expected '" + fieldName + "', found '" + fileAnnotation.name() + "', returning null");
            return null;
        }

        LOGGER.debug("        → ✓ Field name matches! Mapping @Field.File annotation: name='" + fieldName + "'");
        Validator.ValidationResource resource = mapFieldTypeToValidationResource("File", fieldName);
        LOGGER.debug("        → FileFieldAnnotationStrategy mapped to validation resource: {}", resource);
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
        LOGGER.debug("          → FileFieldAnnotationStrategy.mapFieldTypeToValidationResource()");
        LOGGER.debug("          → Mapping file field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Handle specific field names for file fields
        if ("Attachments".equals(fieldName)) {
            LOGGER.debug("          → ✓ Matched specific field name 'Attachments' → ATTACHMENTS");
            return Validator.ValidationResource.ATTACHMENTS;
        }

        // Default fallback
        LOGGER.debug("          → ⚠ No mapping found for file field '" + fieldName + "' → null");
        return null;
    }
}
