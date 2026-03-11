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
 * Strategy for handling @Field.Date annotations.
 * <p>
 * This strategy extracts date-specific attributes, field types, and validation resource mappings
 * from @Field.Date annotations.
 * </p>
 */
public class DateFieldAnnotationStrategy implements FieldAnnotationStrategy {

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        boolean canHandle = dateAnnotation != null && fieldName.equals(dateAnnotation.name());
        return canHandle;
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        if (dateAnnotation == null) {
            return Map.of();
        }

        if (!fieldName.equals(dateAnnotation.name())) {
            return Map.of();
        }

        Map<String, Object> attributes = new HashMap<>();

        // Add date-specific attributes
        if (dateAnnotation.includeTimeOfDay()) {
            attributes.put("includeTimeOfDay", "true");
        }

        if (dateAnnotation.showHowManyDaysInViewer() > 0) {
            attributes.put("showHowManyDaysInViewer", String.valueOf(dateAnnotation.showHowManyDaysInViewer()));
        }

        if (!dateAnnotation.allowPast()) {
            attributes.put("allowPast", "false");
        }

        if (!dateAnnotation.allowToday()) {
            attributes.put("allowToday", "false");
        }

        if (!dateAnnotation.allowFuture()) {
            attributes.put("allowFuture", "false");
        }

        if (dateAnnotation.defaultTimeSpan() > 0) {
            attributes.put("defaultTimeSpan", String.valueOf(dateAnnotation.defaultTimeSpan()));
        }

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = dateAnnotation.attributes();
        for (Attribute attribute : customAttributes) {
            attributes.put(attribute.name(), attribute.value());
        }

        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        if (dateAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(dateAnnotation.name())) {
            return null;
        }

        return "Date";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
        if (dateAnnotation == null) {
            return null;
        }

        if (!fieldName.equals(dateAnnotation.name())) {
            return null;
        }

        Validator.ValidationResource resource = mapFieldTypeToValidationResource("Date", fieldName);
        return resource;
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return Field.Date.class;
    }

    @Override
    public String getStrategyName() {
        return "DateField";
    }

    /**
     * Maps a field type and name to the appropriate ValidationResource for @Field.Date annotations.
     *
     * @param fieldType the type of the field (always "Date" for this strategy)
     * @param fieldName the name of the field
     * @return the corresponding ValidationResource
     */
    private Validator.ValidationResource mapFieldTypeToValidationResource(String fieldType, String fieldName) {
        // Handle specific field names
        if ("Date From".equals(fieldName) || "From".equals(fieldName)) {
            return Validator.ValidationResource.FROM_DATE;
        }
        if ("Date To".equals(fieldName) || "Date to".equals(fieldName) || "To".equals(fieldName)) {
            return Validator.ValidationResource.TO_DATE;
        }

        // Default fallback
        return null;
    }
}
