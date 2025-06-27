package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.Field;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for extracting field attributes from method parameters using reflection.
 * <p>
 * This class provides methods to dynamically extract field attributes from catalog request methods,
 * eliminating the need for hardcoded field attributes in validation orchestrators.
 * </p>
 */
public class FieldAttributeExtractor {

    /**
     * Extracts field attributes for a specific field name from a given method.
     * <p>
     * This method uses reflection to examine the method parameters and their annotations
     * to extract the attributes associated with a field that matches the given field name.
     * </p>
     *
     * @param method the method to examine for field annotations
     * @param fieldName the name of the field to extract attributes for
     * @return a map containing the field attributes, or an empty map if no attributes are found
     */
    public static Map<String, Object> extractFieldAttributes(Method method, String fieldName) {
        if (method == null || fieldName == null) {
            return Map.of();
        }

        Parameter[] parameters = method.getParameters();
        
        for (Parameter parameter : parameters) {
            // Check for @Field annotation
            Field fieldAnnotation = parameter.getAnnotation(Field.class);
            if (fieldAnnotation != null && fieldName.equals(fieldAnnotation.name())) {
                return extractAttributesFromFieldAnnotation(fieldAnnotation);
            }

            // Check for @Field.Date annotation
            Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
            if (dateAnnotation != null && fieldName.equals(dateAnnotation.name())) {
                return extractAttributesFromDateAnnotation(dateAnnotation);
            }

            // Check for @Field.Text annotation
            Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
            if (textAnnotation != null && fieldName.equals(textAnnotation.name())) {
                return extractAttributesFromTextAnnotation(textAnnotation);
            }

            // Check for @Field.Boolean annotation
            Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
            if (booleanAnnotation != null && fieldName.equals(booleanAnnotation.name())) {
                return extractAttributesFromBooleanAnnotation(booleanAnnotation);
            }

            // Note: Field.Number annotation doesn't exist, number fields use @Field with type="Number"

            // Check for @Field.Desc annotation (for list types like attendees)
            Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
            if (descAnnotation != null && fieldName.equals(descAnnotation.name())) {
                // Field.Desc typically doesn't have attributes, but we return empty map for consistency
                return Map.of();
            }
        }

        return Map.of();
    }

    /**
     * Extracts attributes from a @Field annotation.
     *
     * @param fieldAnnotation the @Field annotation
     * @return a map containing the extracted attributes
     */
    private static Map<String, Object> extractAttributesFromFieldAnnotation(Field fieldAnnotation) {
        Map<String, Object> attributes = new HashMap<>();
        
        for (Attribute attribute : fieldAnnotation.attributes()) {
            attributes.put(attribute.name(), attribute.value());
        }
        
        return attributes;
    }

    /**
     * Extracts attributes from a @Field.Date annotation.
     *
     * @param dateAnnotation the @Field.Date annotation
     * @return a map containing the extracted attributes
     */
    private static Map<String, Object> extractAttributesFromDateAnnotation(Field.Date dateAnnotation) {
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
        for (Attribute attribute : dateAnnotation.attributes()) {
            attributes.put(attribute.name(), attribute.value());
        }
        
        return attributes;
    }

    /**
     * Extracts attributes from a @Field.Text annotation.
     *
     * @param textAnnotation the @Field.Text annotation
     * @return a map containing the extracted attributes
     */
    private static Map<String, Object> extractAttributesFromTextAnnotation(Field.Text textAnnotation) {
        Map<String, Object> attributes = new HashMap<>();
        
        // Add custom attributes from the attributes array
        for (Attribute attribute : textAnnotation.attributes()) {
            attributes.put(attribute.name(), attribute.value());
        }
        
        return attributes;
    }

    /**
     * Extracts attributes from a @Field.Boolean annotation.
     *
     * @param booleanAnnotation the @Field.Boolean annotation
     * @return a map containing the extracted attributes
     */
    private static Map<String, Object> extractAttributesFromBooleanAnnotation(Field.Boolean booleanAnnotation) {
        Map<String, Object> attributes = new HashMap<>();
        
        // Add custom attributes from the attributes array
        for (Attribute attribute : booleanAnnotation.attributes()) {
            attributes.put(attribute.name(), attribute.value());
        }
        
        return attributes;
    }



    /**
     * Gets the method by name from a given class.
     * <p>
     * This is a utility method to help find the specific catalog request method
     * that contains the field definitions we want to extract attributes from.
     * </p>
     *
     * @param clazz the class to search for the method
     * @param methodName the name of the method to find
     * @return the Method object if found, null otherwise
     */
    public static Method getMethodByName(Class<?> clazz, String methodName) {
        if (clazz == null || methodName == null) {
            return null;
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        
        return null;
    }

    /**
     * Creates a mapping of field names to their validation resources for a given method.
     * <p>
     * This method helps map field names from the catalog request method to their
     * corresponding validation resource types.
     * </p>
     *
     * @param method the method to examine
     * @return a map of field names to validation resource types
     */
    public static Map<String, Validator.ValidationResource> createFieldNameToResourceMapping(Method method) {
        Map<String, Validator.ValidationResource> mapping = new HashMap<>();
        
        if (method == null) {
            return mapping;
        }

        Parameter[] parameters = method.getParameters();
        
        for (Parameter parameter : parameters) {
            String fieldName = null;
            Validator.ValidationResource resource = null;

            // Check different field annotation types
            Field fieldAnnotation = parameter.getAnnotation(Field.class);
            if (fieldAnnotation != null) {
                fieldName = fieldAnnotation.name();
                resource = mapFieldTypeToValidationResource(fieldAnnotation.type(), fieldName);
            }

            Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
            if (dateAnnotation != null) {
                fieldName = dateAnnotation.name();
                resource = mapFieldTypeToValidationResource("Date", fieldName);
            }

            Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
            if (textAnnotation != null) {
                fieldName = textAnnotation.name();
                resource = mapFieldTypeToValidationResource("Text", fieldName);
            }

            Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
            if (descAnnotation != null) {
                fieldName = descAnnotation.name();
                resource = mapFieldTypeToValidationResource(descAnnotation.type(), fieldName);
            }

            if (fieldName != null && resource != null) {
                mapping.put(fieldName, resource);
            }
        }
        
        return mapping;
    }

    /**
     * Maps a field type and name to the appropriate ValidationResource.
     *
     * @param fieldType the type of the field
     * @param fieldName the name of the field
     * @return the corresponding ValidationResource
     */
    private static Validator.ValidationResource mapFieldTypeToValidationResource(String fieldType, String fieldName) {
        // Handle specific field names first
        if ("Date From".equals(fieldName)) {
            return Validator.ValidationResource.FROM_DATE;
        }
        if ("Date To".equals(fieldName)) {
            return Validator.ValidationResource.TO_DATE;
        }
        if ("Attendees".equals(fieldName)) {
            return Validator.ValidationResource.ATTENDEES;
        }
        if ("Event ID".equals(fieldName)) {
            return Validator.ValidationResource.EVENT_ID;
        }
        if ("Number of meeting".equals(fieldName) || "Number of meetings".equals(fieldName)) {
            return Validator.ValidationResource.NUMBER_OF_MEETINGS;
        }

        // Handle field types
        if ("Date".equals(fieldType)) {
            return Validator.ValidationResource.DATE_RANGE;
        }
        if ("Email".equals(fieldType) || "[ Email ]".equals(fieldType)) {
            return Validator.ValidationResource.EMAIL;
        }
        if ("Text".equals(fieldType)) {
            return Validator.ValidationResource.EVENT_ID; // Default text to event ID validation
        }
        if ("Number".equals(fieldType)) {
            return Validator.ValidationResource.NUMBER_OF_MEETINGS;
        }

        // Default fallback
        return null;
    }
}
