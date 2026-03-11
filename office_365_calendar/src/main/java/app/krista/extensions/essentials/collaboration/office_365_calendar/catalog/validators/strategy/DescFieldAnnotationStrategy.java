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
        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        boolean canHandle = descAnnotation != null && fieldName.equals(descAnnotation.name());
        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        if (descAnnotation == null) {
            return Map.of();
        }

        if (!fieldName.equals(descAnnotation.name())) {
            return Map.of();
        }

        // Field.Desc typically doesn't have attributes, but we return empty map for consistency
        return Map.of();
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        if (descAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(descAnnotation.name())) {
            return null;
        }

        return descAnnotation.type();
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
        if (descAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(descAnnotation.name())) {
            return null;
        }

        Validator.ValidationResource resource = mapFieldTypeToValidationResource(descAnnotation.type(), fieldName);
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
        // Handle specific field names
        if ("Attendees".equals(fieldName)) {
            return Validator.ValidationResource.ATTENDEES;
        }

        // Default fallback
        return null;
    }
}
