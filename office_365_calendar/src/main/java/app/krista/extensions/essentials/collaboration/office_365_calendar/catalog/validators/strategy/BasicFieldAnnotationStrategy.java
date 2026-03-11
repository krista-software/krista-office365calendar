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
 * Strategy for handling @Field annotations.
 * <p>
 * This strategy extracts attributes, field types, and validation resource mappings
 * from basic @Field annotations.
 * </p>
 */
public class BasicFieldAnnotationStrategy implements FieldAnnotationStrategy {

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        boolean canHandle = fieldAnnotation != null && fieldName.equals(fieldAnnotation.name());
        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        if (fieldAnnotation == null) {
            return Map.of();
        }

        if (!fieldName.equals(fieldAnnotation.name())) {
            return Map.of();
        }

        Map<String, Object> attributes = new HashMap<>();

        Attribute[] customAttributes = fieldAnnotation.attributes();
        for (Attribute attribute : customAttributes) {
            attributes.put(attribute.name(), attribute.value());
        }

        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        if (fieldAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(fieldAnnotation.name())) {
            return null;
        }

        return fieldAnnotation.type();
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        Field fieldAnnotation = parameter.getAnnotation(Field.class);
        if (fieldAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(fieldAnnotation.name())) {
            return null;
        }

        Validator.ValidationResource resource = mapFieldTypeToValidationResource(fieldAnnotation.type(), fieldName);
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
        // Handle specific field names first
        if ("Date From".equals(fieldName)) {
            return Validator.ValidationResource.FROM_DATE;
        }
        if ("Date To".equals(fieldName)) {
            return Validator.ValidationResource.TO_DATE;
        }
        if ("Event ID".equals(fieldName)) {
            return Validator.ValidationResource.EVENT_ID;
        }
        if ("Number of meeting".equals(fieldName) || "Number of meetings".equals(fieldName)) {
            return Validator.ValidationResource.NUMBER_OF_MEETINGS;
        }
        if ("Time Zone".equals(fieldName)) {
            return Validator.ValidationResource.TIME_ZONE;
        }

        // Default fallback
        return null;
    }
}
