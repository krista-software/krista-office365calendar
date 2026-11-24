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

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp;

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.attributes.FieldAttributeProviderRegistry;
import app.krista.model.field.NamedField;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Utility class providing static factory methods for creating {@link NamedField} instances
 * with predefined field types for use in Office 365 calendar extension responses.
 * <p>
 * This class simplifies the creation of commonly used field types such as text fields, switch fields,
 * number fields, boolean fields, date fields, rich text fields, file fields, and pick-one fields,
 * ensuring consistency and reducing boilerplate code throughout the extension.
 * </p>
 */
public class NamedFieldFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NamedFieldFactory.class);

    /**
     * Creates a text field with the specified name.
     *
     * @param fieldName the name of the field
     * @return a new NamedField instance configured as a text field
     */
    public static NamedField createTextField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.TEXT_FIELD, Map.of(), Map.of());
    }

    /**
     * Creates a switch field with the specified name.
     *
     * @param fieldName the name of the field
     * @return a new NamedField instance configured as a switch field
     */
    public static NamedField createSwitchField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.SWITCH_FIELD, Map.of(), Map.of());
    }

    /**
     * Creates a number field with the specified name.
     *
     * @param fieldName the name of the field
     * @return a new NamedField instance configured as a number field
     */
    public static NamedField createNumberField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.NUMBER_FIELD, Map.of(), Map.of());
    }

    /**
     * Creates a boolean field with the specified name.
     *
     * @param fieldName the name of the field
     * @return a new NamedField instance configured as a boolean field
     */
    public static NamedField createBooleanField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.BOOLEAN_FIELD, Map.of(), Map.of());
    }

    /**
     * Creates a date field with the specified name.
     *
     * @param fieldName the name of the field
     * @return a new NamedField instance configured as a date field
     */
    public static NamedField createDateField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.DATE_FIELD, Map.of(), Map.of());
    }

    /**
     * Creates a rich text field with the specified name.
     *
     * @param fieldName the name of the field
     * @return a new NamedField instance configured as a rich text field
     */
    public static NamedField createRichTextField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.RICH_TEXT_FIELD, Map.of(), Map.of());
    }

    /**
     * Creates a file field with the specified name.
     *
     * @param fieldName the name of the field
     * @return a new NamedField instance configured as a file field
     */
    public static NamedField createFileField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.FILE_FIELD, Map.of(), Map.of());
    }

    /**
     * Creates a pick-one field with the specified name.
     *
     * @param fieldName the name of the field
     * @return a new NamedField instance configured as a pick-one field
     */
    public static NamedField createPickOneField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.PICK_ONE_FIELD, Map.of(), Map.of());
    }

    /**
     * Creates an email field with the specified name.
     *
     * @param fieldName the name of the field
     * @return a new NamedField instance configured as an email field
     */
    public static NamedField createEmailField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.EMAIL_FIELD_LOGICAL_NAME, Map.of(), Map.of());
    }

    /**
     * Creates a password field with the specified name.
     *
     * @param fieldName the name of the field
     * @return a new NamedField instance configured as a password field (text field with password attributes)
     */
    public static NamedField createPasswordField(String fieldName) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("password", true);
        return new NamedField(fieldName, FieldTypes.TEXT_FIELD, attributes, Map.of());
    }

    /**
     * Creates a list field containing elements of the specified inner field type.
     *
     * @param fieldName the name of the list field
     * @param innerFieldType the type of fields that the list will contain (Text, Date, Email, Number, etc.)
     * @return a new NamedField instance configured as a list field
     */
    public static NamedField createListField(String fieldName, String innerFieldType) {
        return createListField(fieldName, innerFieldType, Map.of());
    }

    /**
     * Creates a list field containing elements of the specified inner field type with custom attributes.
     *
     * @param fieldName the name of the list field
     * @param innerFieldType the type of fields that the list will contain (Text, Date, Email, Number, etc.)
     * @param innerAttributes custom attributes for the inner field type
     * @return a new NamedField instance configured as a list field
     */
    public static NamedField createListField(String fieldName, String innerFieldType, Map<String, Object> innerAttributes) {
        // Map the inner field type to the appropriate logical name
        String innerFieldLogicalName = getLogicalNameForFieldType(innerFieldType);

        // Get default attributes for the inner field type and merge with provided attributes
        Map<String, Object> finalAttributes = getDefaultAttributesForFieldType(innerFieldType);
        if (innerAttributes != null && !innerAttributes.isEmpty()) {
            finalAttributes.putAll(innerAttributes); // Override defaults with provided attributes
        }

        // Create the inner field configuration
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("type", "Field");
        fieldMap.put("fieldType", innerFieldLogicalName);
        fieldMap.put("attributes", finalAttributes);

        // Convert the field configuration to JSON
        String fieldMapJson = convertMapToJson(fieldMap);

        // Create the list field attributes
        Map<String, Object> listAttributes = new HashMap<>();
        listAttributes.put("field", fieldMapJson);

        return new NamedField(fieldName, FieldTypes.LIST_FIELD_LOGICAL_NAME, listAttributes, Map.of());
    }

    /**
     * Creates a field with the specified name, type, and attributes.
     * This method is used when validation results include specific attributes that need to be applied to the field.
     *
     * @param fieldName the name of the field
     * @param fieldType the type of field to create (Date, Email, Text, Number, Boolean, File, List, etc.)
     * @param attributes the attributes to apply to the field
     * @return a new NamedField instance configured with the specified type and attributes
     */
    public static NamedField createFieldWithAttributes(String fieldName, String fieldType, Map<String, Object> attributes) {
        // Handle catalog request list format first (e.g., "[ Email ]", "[ Text ]", "[ File ]")
        if (fieldType.startsWith("[") && fieldType.endsWith("]")) {
            String innerType = fieldType.substring(1, fieldType.length() - 1).trim();
            return createListFieldWithAttributes(fieldName, innerType, attributes);
        }

        // Handle list types with inner type specification (e.g., "List<Email>", "List[Date]", etc.)
        if (fieldType.startsWith("List<") && fieldType.endsWith(">")) {
            String innerType = fieldType.substring(5, fieldType.length() - 1);
            return createListFieldWithAttributes(fieldName, innerType, attributes);
        }

        if (fieldType.startsWith("List[") && fieldType.endsWith("]")) {
            String innerType = fieldType.substring(5, fieldType.length() - 1);
            return createListFieldWithAttributes(fieldName, innerType, attributes);
        }

        // Handle simple list case
        if (fieldType.equals(FieldTypes.LIST_FIELD_LOGICAL_NAME) || fieldType.equalsIgnoreCase("List")) {
            // Default to Text list if no inner type specified
            return createListFieldWithAttributes(fieldName, "Text", attributes);
        }

        // Handle password field special case
        if (fieldType.equals(FieldTypes.TEXT_FIELD_LOGICAL_NAME) || fieldType.equalsIgnoreCase("Text")) {
            if (fieldName.equalsIgnoreCase("Password")) {
                // Merge password attributes with provided attributes
                Map<String, Object> mergedAttributes = new HashMap<>(attributes);
                mergedAttributes.put("password", true);
                return new NamedField(fieldName, FieldTypes.TEXT_FIELD, mergedAttributes, Map.of());
            }
        }

        // Use the existing createFieldByTypeWithAttributes for standard types
        return createFieldByTypeWithAttributes(fieldName, fieldType, attributes);
    }

    /**
     * Creates a comprehensive field based on the specified field type, handling both simple and complex types.
     * This method supports list fields, password fields, and all standard field types.
     *
     * @param fieldName the name of the field
     * @param fieldType the type of field to create (Date, Email, Text, Number, Boolean, File, List, etc.)
     * @return a new NamedField instance configured with the specified type
     */
    public static NamedField createField(String fieldName, String fieldType) {
        // Handle catalog request list format first (e.g., "[ Email ]", "[ Text ]", "[ File ]")
        if (fieldType.startsWith("[") && fieldType.endsWith("]")) {
            String innerType = fieldType.substring(1, fieldType.length() - 1).trim();
            return createListField(fieldName, innerType);
        }

        // Handle list types with inner type specification (e.g., "List<Email>", "List[Date]", etc.)
        if (fieldType.startsWith("List<") && fieldType.endsWith(">")) {
            String innerType = fieldType.substring(5, fieldType.length() - 1);
            return createListField(fieldName, innerType);
        }

        if (fieldType.startsWith("List[") && fieldType.endsWith("]")) {
            String innerType = fieldType.substring(5, fieldType.length() - 1);
            return createListField(fieldName, innerType);
        }

        // Handle simple list case
        if (fieldType.equals(FieldTypes.LIST_FIELD_LOGICAL_NAME) || fieldType.equalsIgnoreCase("List")) {
            // Default to Text list if no inner type specified
            return createListField(fieldName, "Text");
        }

        // Handle password field special case
        if (fieldType.equals(FieldTypes.TEXT_FIELD_LOGICAL_NAME) || fieldType.equalsIgnoreCase("Text")) {
            if (fieldName.equalsIgnoreCase("Password")) {
                return createPasswordField(fieldName);
            }
        }

        // Use the existing createFieldByType for standard types
        return createFieldByType(fieldName, fieldType);
    }

    /**
     * Creates a field based on the specified field type with custom attributes.
     * This method acts as a factory that delegates to the appropriate specific creation method.
     *
     * @param fieldName the name of the field
     * @param fieldType the type of field to create (Date, Email, Text, Number, Boolean, File, etc.)
     * @param attributes the attributes to apply to the field
     * @return a new NamedField instance configured with the specified type and attributes
     */
    public static NamedField createFieldByTypeWithAttributes(String fieldName, String fieldType, Map<String, Object> attributes) {
        return switch (fieldType.toLowerCase()) {
            case "date" -> createDateFieldWithAttributes(fieldName, attributes);
            case "email" -> createEmailFieldWithAttributes(fieldName, attributes);
            case "number" -> createNumberFieldWithAttributes(fieldName, attributes);
            case "boolean" -> createBooleanFieldWithAttributes(fieldName, attributes);
            case "file" -> createFileFieldWithAttributes(fieldName, attributes);
            case "switch" -> createSwitchFieldWithAttributes(fieldName, attributes);
            case "richtext" -> createRichTextFieldWithAttributes(fieldName, attributes);
            case "pickone" -> createPickOneFieldWithAttributes(fieldName, attributes);
            case "text" -> createTextFieldWithAttributes(fieldName, attributes);
            case "password" -> {
                // Merge password attributes with provided attributes
                Map<String, Object> mergedAttributes = new HashMap<>(attributes);
                mergedAttributes.put("password", true);
                yield new NamedField(fieldName, FieldTypes.TEXT_FIELD, mergedAttributes, Map.of());
            }
            case "list" -> createListFieldWithAttributes(fieldName, "Text", attributes); // Default list of text
            default -> createTextFieldWithAttributes(fieldName, attributes); // Default fallback to text field
        };
    }

    /**
     * Creates a field based on the specified field type.
     * This method acts as a factory that delegates to the appropriate specific creation method.
     *
     * @param fieldName the name of the field
     * @param fieldType the type of field to create (Date, Email, Text, Number, Boolean, File, etc.)
     * @return a new NamedField instance configured with the specified type
     */
    public static NamedField createFieldByType(String fieldName, String fieldType) {
        return switch (fieldType.toLowerCase()) {
            case "date" -> createDateField(fieldName);
            case "email" -> createEmailField(fieldName);
            case "number" -> createNumberField(fieldName);
            case "boolean" -> createBooleanField(fieldName);
            case "file" -> createFileField(fieldName);
            case "switch" -> createSwitchField(fieldName);
            case "richtext" -> createRichTextField(fieldName);
            case "pickone" -> createPickOneField(fieldName);
            case "text" -> createTextField(fieldName);
            case "password" -> createPasswordField(fieldName);
            case "list" -> createListField(fieldName, "Text"); // Default list of text
            default -> createTextField(fieldName); // Default fallback to text field
        };
    }

    // ========== Field Creation Methods with Attributes ==========

    /**
     * Creates a text field with the specified name and attributes.
     * Default text field attributes are automatically applied and can be overridden by provided attributes.
     *
     * @param fieldName the name of the field
     * @param attributes the attributes to apply to the field (will override defaults)
     * @return a new NamedField instance configured as a text field with attributes
     */
    public static NamedField createTextFieldWithAttributes(String fieldName, Map<String, Object> attributes) {
        // Get default attributes for text fields and merge with provided attributes
        Map<String, Object> finalAttributes = getDefaultAttributesForFieldType("Text");
        if (attributes != null && !attributes.isEmpty()) {
            finalAttributes.putAll(attributes); // Override defaults with provided attributes
        }

        return new NamedField(fieldName, FieldTypes.TEXT_FIELD, finalAttributes, Map.of());
    }

    /**
     * Creates a switch field with the specified name and attributes.
     *
     * @param fieldName the name of the field
     * @param attributes the attributes to apply to the field
     * @return a new NamedField instance configured as a switch field with attributes
     */
    public static NamedField createSwitchFieldWithAttributes(String fieldName, Map<String, Object> attributes) {
        return new NamedField(fieldName, FieldTypes.SWITCH_FIELD, attributes, Map.of());
    }

    /**
     * Creates a number field with the specified name and attributes.
     *
     * @param fieldName the name of the field
     * @param attributes the attributes to apply to the field
     * @return a new NamedField instance configured as a number field with attributes
     */
    public static NamedField createNumberFieldWithAttributes(String fieldName, Map<String, Object> attributes) {
        return new NamedField(fieldName, FieldTypes.NUMBER_FIELD, attributes, Map.of());
    }

    /**
     * Creates a boolean field with the specified name and attributes.
     *
     * @param fieldName the name of the field
     * @param attributes the attributes to apply to the field
     * @return a new NamedField instance configured as a boolean field with attributes
     */
    public static NamedField createBooleanFieldWithAttributes(String fieldName, Map<String, Object> attributes) {
        return new NamedField(fieldName, FieldTypes.BOOLEAN_FIELD, attributes, Map.of());
    }

    /**
     * Creates a date field with the specified name and attributes.
     *
     * @param fieldName the name of the field
     * @param attributes the attributes to apply to the field
     * @return a new NamedField instance configured as a date field with attributes
     */
    public static NamedField createDateFieldWithAttributes(String fieldName, Map<String, Object> attributes) {
        return new NamedField(fieldName, FieldTypes.DATE_FIELD, attributes, Map.of());
    }

    /**
     * Creates a rich text field with the specified name and attributes.
     *
     * @param fieldName the name of the field
     * @param attributes the attributes to apply to the field
     * @return a new NamedField instance configured as a rich text field with attributes
     */
    public static NamedField createRichTextFieldWithAttributes(String fieldName, Map<String, Object> attributes) {
        return new NamedField(fieldName, FieldTypes.RICH_TEXT_FIELD, attributes, Map.of());
    }

    /**
     * Creates a file field with the specified name and attributes.
     * Default file field attributes are automatically applied and can be overridden by provided attributes.
     *
     * @param fieldName the name of the field
     * @param attributes the attributes to apply to the field (will override defaults)
     * @return a new NamedField instance configured as a file field with attributes
     */
    public static NamedField createFileFieldWithAttributes(String fieldName, Map<String, Object> attributes) {
        Map<String, Object> fileUploadConfig = new HashMap<>();

        fileUploadConfig.put("visualWidth", "S");
        fileUploadConfig.put("multipleFileUpload", attributes.getOrDefault("multipleFileUpload", false));
        fileUploadConfig.put("minFileSizeUnit", "KB");
        fileUploadConfig.put("maxFileSizeUnit", "MB");
        fileUploadConfig.put("fileTypes", "[\"Document\",\"Image gallery\",\"Camera\",\"Contact\",\"Audio\"]");


        return new NamedField(fieldName, FieldTypes.FILE_FIELD, fileUploadConfig, Map.of());
    }

    /**
     * Creates a pick-one field with the specified name and attributes.
     *
     * @param fieldName the name of the field
     * @param attributes the attributes to apply to the field
     * @return a new NamedField instance configured as a pick-one field with attributes
     */
    public static NamedField createPickOneFieldWithAttributes(String fieldName, Map<String, Object> attributes) {
        return new NamedField(fieldName, FieldTypes.PICK_ONE_FIELD, attributes, Map.of());
    }

    /**
     * Creates an email field with the specified name and attributes.
     *
     * @param fieldName the name of the field
     * @param attributes the attributes to apply to the field
     * @return a new NamedField instance configured as an email field with attributes
     */
    public static NamedField createEmailFieldWithAttributes(String fieldName, Map<String, Object> attributes) {
        return new NamedField(fieldName, FieldTypes.EMAIL_FIELD_LOGICAL_NAME, attributes, Map.of());
    }

    /**
     * Creates a list field containing elements of the specified inner field type with custom attributes.
     * This method follows the predefined configuration format for list fields where the inner field type
     * is defined in a fieldMap with type 'Field', fieldType set to the logical name, and the fieldMap
     * is converted to JSON and stored in the 'field' attribute of the list's attributes.
     *
     * @param fieldName the name of the list field
     * @param innerFieldType the type of fields that the list will contain (Text, Date, Email, Number, etc.)
     * @param attributes custom attributes for the inner field type (optional config)
     * @return a new NamedField instance configured as a list field with nested field structure
     */

    // In the Future need to make Support other field in the list
    public static NamedField createListFieldWithAttributes(String fieldName, String innerFieldType, Map<String, Object> attributes) {
        // Map the inner field type to the appropriate logical name
        String innerFieldLogicalName = getLogicalNameForFieldType(innerFieldType);

        // Get default attributes for the inner field type and merge with provided attributes
        Map<String, Object> finalAttributes = getDefaultAttributesForFieldType(innerFieldType);

        // Create the inner field configuration
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("type", "Field");
        fieldMap.put("fieldType", innerFieldLogicalName);
        fieldMap.put("attributes", finalAttributes);

        // This defines that the list holds fields of the specified type
        String fieldMapJson = convertMapToJson(fieldMap);

        // Create the list field attributes
        Map<String, Object> listAttributes = new HashMap<>();
        listAttributes.put("field", fieldMapJson);
        LOGGER.debug("List Attributes:");

        return new NamedField(fieldName, FieldTypes.LIST_FIELD_LOGICAL_NAME, listAttributes, Map.of());
    }



    // ========== Private Helper Methods ==========

    /**
     * Maps a field type string to its corresponding logical name.
     *
     * @param fieldType the field type string (e.g., "Text", "Date", "Email")
     * @return the logical name for the field type
     */
    private static String getLogicalNameForFieldType(String fieldType) {
        return switch (fieldType.toLowerCase()) {
            case "text" -> FieldTypes.TEXT_FIELD_LOGICAL_NAME;
            case "date" -> FieldTypes.DATE_FIELD_LOGICAL_NAME;
            case "email" -> FieldTypes.EMAIL_FIELD_LOGICAL_NAME;
            case "number" -> FieldTypes.NUMBER_FIELD_LOGICAL_NAME;
            case "boolean" -> FieldTypes.BOOLEAN_FIELD_LOGICAL_NAME;
            case "file" -> FieldTypes.FILE_FIELD_LOGICAL_NAME;
            case "switch" -> FieldTypes.SWITCH_FIELD_LOGICAL_NAME;
            case "richtext" -> FieldTypes.RICH_TEXT_FIELD_LOGICAL_NAME;
            case "pickone" -> FieldTypes.PICK_ONE_LOGICAL_NAME;
            case "pickmany" -> FieldTypes.PICK_MANY_LOGICAL_NAME;
            case "phone" -> FieldTypes.PHONE_FIELD_LOGICAL_NAME;
            case "address" -> FieldTypes.ADDRESS_FIELD_LOGICAL_NAME;
            case "currency" -> FieldTypes.CURRENCY_FIELD_LOGICAL_NAME;
            case "percentage" -> FieldTypes.PERCENTAGE_FIELD_LOGICAL_NAME;
            case "time" -> FieldTypes.TIME_FIELD_LOGICAL_NAME;
            case "daterange" -> FieldTypes.DATE_RANGE_LOGICAL_NAME;
            case "datetimerange" -> FieldTypes.DATE_TIME_RANGE_LOGICAL_NAME;
            case "location" -> FieldTypes.LOCATION_FIELD_LOGICAL_NAME;
            default -> FieldTypes.TEXT_FIELD_LOGICAL_NAME; // Default fallback
        };
    }

    /**
     * Gets default attributes for the specified field type.
     * This method can be used by any field creation method to get appropriate default attributes.
     *
     * @param fieldType the field type string (e.g., "Text", "File", "Date")
     * @return a map containing default attributes for the field type
     */
    public static Map<String, Object> getDefaultAttributesForFieldType(String fieldType) {
        LOGGER.debug("→ NamedFieldFactory: Getting default attributes for field type: {}", fieldType);
        return FieldAttributeProviderRegistry.getDefaultAttributes(fieldType);
    }

    /**
     * Converts a Map to JSON string using Gson.
     *
     * @param map the map to convert
     * @return JSON string representation of the map
     */
    private static String convertMapToJson(Map<String, Object> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }

}