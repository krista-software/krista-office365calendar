/*
 * Office365Calendar Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        boolean canHandle = fileAnnotation != null && fieldName.equals(fileAnnotation.name());
        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        if (fileAnnotation == null) {
            return Map.of();
        }

        if (!fieldName.equals(fileAnnotation.name())) {
            return Map.of();
        }

        Map<String, Object> attributes = new HashMap<>();

        // Add file-specific attributes
        if (fileAnnotation.multipleFileUpload()) {
            attributes.put("multipleFileUpload", "true");
        } else {
            attributes.put("multipleFileUpload", "false");
        }

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = fileAnnotation.attributes();
        for (Attribute attribute : customAttributes) {
            attributes.put(attribute.name(), attribute.value());
        }

        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        if (fileAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(fileAnnotation.name())) {
            return null;
        }

        return "File";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        Field.File fileAnnotation = parameter.getAnnotation(Field.File.class);
        if (fileAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(fileAnnotation.name())) {
            return null;
        }

        Validator.ValidationResource resource = mapFieldTypeToValidationResource("File", fieldName);
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
        // Handle specific field names for file fields
        if ("Attachments".equals(fieldName)) {
            return Validator.ValidationResource.ATTACHMENTS;
        }

        // Default fallback
        return null;
    }
}
