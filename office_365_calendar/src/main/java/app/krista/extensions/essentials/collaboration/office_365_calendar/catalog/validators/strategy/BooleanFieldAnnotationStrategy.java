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
 * Strategy for handling @Field.Boolean annotations.
 * <p>
 * This strategy extracts attributes, field types, and validation resource mappings
 * from @Field.Boolean annotations.
 * </p>
 */
public class BooleanFieldAnnotationStrategy implements FieldAnnotationStrategy {

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        boolean canHandle = booleanAnnotation != null && fieldName.equals(booleanAnnotation.name());
        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        if (booleanAnnotation == null) {
            return Map.of();
        }

        if (!fieldName.equals(booleanAnnotation.name())) {
            return Map.of();
        }

        Map<String, Object> attributes = new HashMap<>();

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = booleanAnnotation.attributes();
        for (Attribute attribute : customAttributes) {
            attributes.put(attribute.name(), attribute.value());
        }

        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        if (booleanAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(booleanAnnotation.name())) {
            return null;
        }

        return "Boolean";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
        if (booleanAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(booleanAnnotation.name())) {
            return null;
        }

        Validator.ValidationResource resource = mapFieldTypeToValidationResource("Boolean", fieldName);
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
        // Currently no specific boolean field mappings exist in the original code
        // This can be extended in the future if needed

        // Default fallback
        return null;
    }
}
