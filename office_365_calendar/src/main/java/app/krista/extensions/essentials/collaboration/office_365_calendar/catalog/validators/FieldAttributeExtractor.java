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

    // So in the Future if we add a new annotation type, we only need to add it here.
    public static Map<String, Object> extractFieldAttributes(Method method, String fieldName) {
        System.out.println("    → FieldAttributeExtractor.extractFieldAttributes()");
        System.out.println("    → Method: " + (method != null ? method.getName() : "null"));
        System.out.println("    → Field Name: " + fieldName);

        if (method == null || fieldName == null) {
            System.out.println("    → Null method or field name, returning empty map");
            return Map.of();
        }

        Parameter[] parameters = method.getParameters();
        System.out.println("    → Examining " + parameters.length + " parameters...");

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            System.out.println("    → Parameter " + i + ": " + parameter.getType().getSimpleName());

            // Check for @Field annotation
            Field fieldAnnotation = parameter.getAnnotation(Field.class);
            if (fieldAnnotation != null) {
                System.out.println("    → Found @Field annotation with name: " + fieldAnnotation.name());
                if (fieldName.equals(fieldAnnotation.name())) {
                    System.out.println("    → ✓ Field name matches! Extracting attributes from @Field");
                    return extractAttributesFromFieldAnnotation(fieldAnnotation);
                }
            }

            // Check for @Field.Date annotation
            Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
            if (dateAnnotation != null) {
                System.out.println("    → Found @Field.Date annotation with name: " + dateAnnotation.name());
                if (fieldName.equals(dateAnnotation.name())) {
                    System.out.println("    → ✓ Field name matches! Extracting attributes from @Field.Date");
                    return extractAttributesFromDateAnnotation(dateAnnotation);
                }
            }

            // Check for @Field.Text annotation
            Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
            if (textAnnotation != null) {
                System.out.println("    → Found @Field.Text annotation with name: " + textAnnotation.name());
                if (fieldName.equals(textAnnotation.name())) {
                    System.out.println("    → ✓ Field name matches! Extracting attributes from @Field.Text");
                    return extractAttributesFromTextAnnotation(textAnnotation);
                }
            }

            // Check for @Field.Boolean annotation
            Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
            if (booleanAnnotation != null) {
                System.out.println("    → Found @Field.Boolean annotation with name: " + booleanAnnotation.name());
                if (fieldName.equals(booleanAnnotation.name())) {
                    System.out.println("    → ✓ Field name matches! Extracting attributes from @Field.Boolean");
                    return extractAttributesFromBooleanAnnotation(booleanAnnotation);
                }
            }

            // Note: Field.Number annotation doesn't exist, number fields use @Field with type="Number"

            // Check for @Field.Desc annotation (for list types like attendees)
            Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
            if (descAnnotation != null) {
                System.out.println("    → Found @Field.Desc annotation with name: " + descAnnotation.name());
                if (fieldName.equals(descAnnotation.name())) {
                    System.out.println("    → ✓ Field name matches! @Field.Desc typically has no attributes");
                    // Field.Desc typically doesn't have attributes, but we return empty map for consistency
                    return Map.of();
                }
            }
        }

        System.out.println("    → No matching field annotation found for: " + fieldName);
        return Map.of();
    }

    /**
     * Extracts the field type for a specific field name from a given method.
     * <p>
     * This method uses reflection to examine the method parameters and their annotations
     * to extract the field type associated with a field that matches the given field name.
     * </p>
     *
     * @param method the method to examine for field annotations
     * @param fieldName the name of the field to extract the type for
     * @return the field type string, or null if no matching field is found
     */
    public static String extractFieldType(Method method, String fieldName) {
        System.out.println("    → FieldAttributeExtractor.extractFieldType()");
        System.out.println("    → Method: " + (method != null ? method.getName() : "null"));
        System.out.println("    → Field Name: " + fieldName);

        if (method == null || fieldName == null) {
            System.out.println("    → Null method or field name, returning null");
            return null;
        }

        Parameter[] parameters = method.getParameters();
        System.out.println("    → Examining " + parameters.length + " parameters for field type...");

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            System.out.println("    → Parameter " + i + ": " + parameter.getType().getSimpleName());

            // Check for @Field annotation
            Field fieldAnnotation = parameter.getAnnotation(Field.class);
            if (fieldAnnotation != null) {
                System.out.println("    → Found @Field annotation with name: " + fieldAnnotation.name());
                if (fieldName.equals(fieldAnnotation.name())) {
                    System.out.println("    → ✓ Field name matches! Field type: " + fieldAnnotation.type());
                    return fieldAnnotation.type();
                }
            }

            // Check for @Field.Date annotation
            Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
            if (dateAnnotation != null) {
                System.out.println("    → Found @Field.Date annotation with name: " + dateAnnotation.name());
                if (fieldName.equals(dateAnnotation.name())) {
                    System.out.println("    → ✓ Field name matches! Field type: Date");
                    return "Date";
                }
            }

            // Check for @Field.Text annotation
            Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
            if (textAnnotation != null) {
                System.out.println("    → Found @Field.Text annotation with name: " + textAnnotation.name());
                if (fieldName.equals(textAnnotation.name())) {
                    System.out.println("    → ✓ Field name matches! Field type: Text");
                    return "Text";
                }
            }

            // Check for @Field.Boolean annotation
            Field.Boolean booleanAnnotation = parameter.getAnnotation(Field.Boolean.class);
            if (booleanAnnotation != null) {
                System.out.println("    → Found @Field.Boolean annotation with name: " + booleanAnnotation.name());
                if (fieldName.equals(booleanAnnotation.name())) {
                    System.out.println("    → ✓ Field name matches! Field type: Boolean");
                    return "Boolean";
                }
            }

            // Check for @Field.Desc annotation (for list types like attendees)
            Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
            if (descAnnotation != null) {
                System.out.println("    → Found @Field.Desc annotation with name: " + descAnnotation.name());
                if (fieldName.equals(descAnnotation.name())) {
                    System.out.println("    → ✓ Field name matches! Field type: " + descAnnotation.type());
                    return descAnnotation.type();
                }
            }
        }

        System.out.println("    → No matching field annotation found for field type extraction: " + fieldName);
        return null;
    }

    /**
     * Extracts attributes from a @Field annotation.
     *
     * @param fieldAnnotation the @Field annotation
     * @return a map containing the extracted attributes
     */
    private static Map<String, Object> extractAttributesFromFieldAnnotation(Field fieldAnnotation) {
        System.out.println("      → Extracting attributes from @Field annotation");
        Map<String, Object> attributes = new HashMap<>();

        Attribute[] customAttributes = fieldAnnotation.attributes();
        System.out.println("      → Processing " + customAttributes.length + " custom attributes");
        for (Attribute attribute : customAttributes) {
            System.out.println("      → Adding attribute: " + attribute.name() + "=" + attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }

        System.out.println("      → Final extracted attributes: " + attributes);
        return attributes;
    }

    /**
     * Extracts attributes from a @Field.Date annotation.
     *
     * @param dateAnnotation the @Field.Date annotation
     * @return a map containing the extracted attributes
     */
    private static Map<String, Object> extractAttributesFromDateAnnotation(Field.Date dateAnnotation) {
        System.out.println("      → Extracting attributes from @Field.Date annotation");
        Map<String, Object> attributes = new HashMap<>();

        // Add date-specific attributes
        if (dateAnnotation.includeTimeOfDay()) {
            System.out.println("      → Adding includeTimeOfDay=true");
            attributes.put("includeTimeOfDay", "true");
        }

        if (dateAnnotation.showHowManyDaysInViewer() > 0) {
            System.out.println("      → Adding showHowManyDaysInViewer=" + dateAnnotation.showHowManyDaysInViewer());
            attributes.put("showHowManyDaysInViewer", String.valueOf(dateAnnotation.showHowManyDaysInViewer()));
        }

        if (!dateAnnotation.allowPast()) {
            System.out.println("      → Adding allowPast=false");
            attributes.put("allowPast", "false");
        }

        if (!dateAnnotation.allowToday()) {
            System.out.println("      → Adding allowToday=false");
            attributes.put("allowToday", "false");
        }

        if (!dateAnnotation.allowFuture()) {
            System.out.println("      → Adding allowFuture=false");
            attributes.put("allowFuture", "false");
        }

        if (dateAnnotation.defaultTimeSpan() > 0) {
            System.out.println("      → Adding defaultTimeSpan=" + dateAnnotation.defaultTimeSpan());
            attributes.put("defaultTimeSpan", String.valueOf(dateAnnotation.defaultTimeSpan()));
        }

        // Add custom attributes from the attributes array
        Attribute[] customAttributes = dateAnnotation.attributes();
        System.out.println("      → Processing " + customAttributes.length + " custom attributes");
        for (Attribute attribute : customAttributes) {
            System.out.println("      → Adding custom attribute: " + attribute.name() + "=" + attribute.value());
            attributes.put(attribute.name(), attribute.value());
        }

        System.out.println("      → Final extracted attributes: " + attributes);
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
        System.out.println("    → FieldAttributeExtractor.createFieldNameToResourceMapping()");
        System.out.println("    → Method: " + (method != null ? method.getName() : "null"));

        Map<String, Validator.ValidationResource> mapping = new HashMap<>();

        if (method == null) {
            System.out.println("    → Method is null, returning empty mapping");
            return mapping;
        }

        Parameter[] parameters = method.getParameters();
        System.out.println("    → Processing " + parameters.length + " parameters to create field mappings...");

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            System.out.println("    → Parameter " + i + ": " + parameter.getType().getSimpleName());

            String fieldName = null;
            Validator.ValidationResource resource = null;

            // Check different field annotation types
            // So In the Future I only need to add the new annotation type here
            Field fieldAnnotation = parameter.getAnnotation(Field.class);
            if (fieldAnnotation != null) {
                fieldName = fieldAnnotation.name();
                System.out.println("    → Found @Field annotation: name='" + fieldName + "', type='" + fieldAnnotation.type() + "'");
                resource = mapFieldTypeToValidationResource(fieldAnnotation.type(), fieldName);
                System.out.println("    → Mapped to validation resource: " + resource);
            }

            Field.Date dateAnnotation = parameter.getAnnotation(Field.Date.class);
            if (dateAnnotation != null) {
                fieldName = dateAnnotation.name();
                System.out.println("    → Found @Field.Date annotation: name='" + fieldName + "'");
                resource = mapFieldTypeToValidationResource("Date", fieldName);
                System.out.println("    → Mapped to validation resource: " + resource);
            }

            Field.Text textAnnotation = parameter.getAnnotation(Field.Text.class);
            if (textAnnotation != null) {
                fieldName = textAnnotation.name();
                System.out.println("    → Found @Field.Text annotation: name='" + fieldName + "'");
                resource = mapFieldTypeToValidationResource("Text", fieldName);
                System.out.println("    → Mapped to validation resource: " + resource);
            }

            Field.Desc descAnnotation = parameter.getAnnotation(Field.Desc.class);
            if (descAnnotation != null) {
                fieldName = descAnnotation.name();
                System.out.println("    → Found @Field.Desc annotation: name='" + fieldName + "', type='" + descAnnotation.type() + "'");
                resource = mapFieldTypeToValidationResource(descAnnotation.type(), fieldName);
                System.out.println("    → Mapped to validation resource: " + resource);
            }

            if (fieldName != null && resource != null) {
                System.out.println("    → ✓ Adding mapping: '" + fieldName + "' → " + resource);
                mapping.put(fieldName, resource);
            } else if (fieldName != null) {
                System.out.println("    → ⚠ Field '" + fieldName + "' could not be mapped to a validation resource");
            } else {
                System.out.println("    → No field annotation found for parameter " + i);
            }
        }

        System.out.println("    → Final field name to resource mapping: " + mapping);
        return mapping;
    }

    /**
     * Maps a field type and name to the appropriate ValidationResource.
     *
     * @param fieldType the type of the field
     * @param fieldName the name of the field
     * @return the corresponding ValidationResource
     */


    // So in the future Only need to add the field name to validation resource mapping here
    private static Validator.ValidationResource mapFieldTypeToValidationResource(String fieldType, String fieldName) {
        System.out.println("      → Mapping field '" + fieldName + "' (type: '" + fieldType + "') to validation resource");

        // Handle specific field names first
        if ("Date From".equals(fieldName)) {
            System.out.println("      → Matched specific field name 'Date From' → FROM_DATE");
            return Validator.ValidationResource.FROM_DATE;
        }
        if ("Date To".equals(fieldName)) {
            System.out.println("      → Matched specific field name 'Date To' → TO_DATE");
            return Validator.ValidationResource.TO_DATE;
        }
        if ("Attendees".equals(fieldName)) {
            System.out.println("      → Matched specific field name 'Attendees' → ATTENDEES");
            return Validator.ValidationResource.ATTENDEES;
        }
        if ("Event ID".equals(fieldName)) {
            System.out.println("      → Matched specific field name 'Event ID' → EVENT_ID");
            return Validator.ValidationResource.EVENT_ID;
        }
        if ("Number of meeting".equals(fieldName) || "Number of meetings".equals(fieldName)) {
            System.out.println("      → Matched specific field name '" + fieldName + "' → NUMBER_OF_MEETINGS");
            return Validator.ValidationResource.NUMBER_OF_MEETINGS;
        }

        // Default fallback
        System.out.println("      → No mapping found for field '" + fieldName + "' (type: '" + fieldType + "') → null");
        return null;
    }
}
