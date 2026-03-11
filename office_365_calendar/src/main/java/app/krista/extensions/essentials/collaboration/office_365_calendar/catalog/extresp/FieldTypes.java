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

/**
 * Utility class containing field type constants and helper methods for field type checking
 * in the Office 365 calendar extension.
 * <p>
 * This class provides logical name constants for all supported field types and utility methods
 * for checking field types based on their logical names.
 * </p>
 */
public class FieldTypes {
    private FieldTypes() {}

    // Basic field types
    public static final String TEXT_FIELD_LOGICAL_NAME = "com.krista.fields.Text";
    public static final String NUMBER_FIELD_LOGICAL_NAME = "com.krista.fields.Number";
    public static final String SWITCH_FIELD_LOGICAL_NAME = "com.krista.fields.Switch";
    public static final String BOOLEAN_FIELD_LOGICAL_NAME = "com.krista.fields.Boolean";
    public static final String DATE_FIELD_LOGICAL_NAME = "com.krista.fields.Date";
    public static final String RICH_TEXT_FIELD_LOGICAL_NAME = "com.krista.fields.RichText";
    public static final String FILE_FIELD_LOGICAL_NAME = "com.krista.fields.File";
    public static final String PICK_ONE_LOGICAL_NAME = "com.krista.fields.PickOne";

    // Additional field types from reference
    public static final String TABLE_FIELD_LOGICAL_NAME = "com.krista.fields.Table";
    public static final String ENTITY_FIELD_LOGICAL_NAME = "com.krista.fields.EntityReference";
    public static final String SELECTION_FIELD_LOGICAL_NAME = "com.krista.fields.Entity";
    public static final String SCHEDULE_FIELD_LOGICAL_NAME = "com.krista.fields.Schedule";
    public static final String LABEL_FIELD_LOGICAL_NAME = "com.krista.fields.Label";
    public static final String CALCULATION_FIELD_LOGICAL_NAME = "com.krista.fields.Calculation";
    public static final String CAMERA_FIELD_LOGICAL_NAME = "com.krista.fields.Camera";
    public static final String COMPOSITE_FIELD_LOGICAL_NAME = "com.krista.fields.Composite";
    public static final String LIST_FIELD_LOGICAL_NAME = "com.krista.fields.List";
    public static final String MULTI_FIELD_LOGICAL_NAME = "com.krista.fields.MultiField";
    public static final String PARAGRAPH_FIELD_LOGICAL_NAME = "com.krista.fields.Paragraph";
    public static final String CURRENCY_FIELD_LOGICAL_NAME = "com.krista.fields.Currency";
    public static final String PERCENTAGE_FIELD_LOGICAL_NAME = "com.krista.fields.Percentage";
    public static final String UNIT_FIELD_LOGICAL_NAME = "com.krista.fields.Unit";
    public static final String PICK_MANY_LOGICAL_NAME = "com.krista.fields.Checkboxes";
    public static final String ADDRESS_FIELD_LOGICAL_NAME = "com.krista.fields.Address";
    public static final String DATE_RANGE_LOGICAL_NAME = "com.krista.fields.DateRange";
    public static final String SCHEDULE_LOGICAL_NAME = "com.krista.fields.Schedule";
    public static final String DATE_TIME_RANGE_LOGICAL_NAME = "com.krista.fields.DateTimeRange";
    public static final String PHONE_FIELD_LOGICAL_NAME = "com.krista.fields.Phone";
    public static final String LOCATION_FIELD_LOGICAL_NAME = "com.krista.fields.Location";
    public static final String TIME_FIELD_LOGICAL_NAME = "com.krista.fields.Time";
    public static final String EMAIL_FIELD_LOGICAL_NAME = "com.krista.fields.Email";
    public static final String STREAM_FIELD_LOGICAL_NAME = "com.krista.fields.Stream";

    // Legacy field constants for backward compatibility
    public static final String TEXT_FIELD = TEXT_FIELD_LOGICAL_NAME;
    public static final String NUMBER_FIELD = NUMBER_FIELD_LOGICAL_NAME;
    public static final String SWITCH_FIELD = SWITCH_FIELD_LOGICAL_NAME;
    public static final String BOOLEAN_FIELD = BOOLEAN_FIELD_LOGICAL_NAME;
    public static final String DATE_FIELD = DATE_FIELD_LOGICAL_NAME;
    public static final String RICH_TEXT_FIELD = RICH_TEXT_FIELD_LOGICAL_NAME;
    public static final String FILE_FIELD = FILE_FIELD_LOGICAL_NAME;
    public static final String PICK_ONE_FIELD = PICK_ONE_LOGICAL_NAME;

    // Utility methods for field type checking

    /**
     * Checks if the given logical name represents a composite field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a composite field, false otherwise
     */
    public static boolean isCompositeField(String logicalName) {
        return COMPOSITE_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a table field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a table field, false otherwise
     */
    public static boolean isTableField(String logicalName) {
        return TABLE_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents an entity field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is an entity field, false otherwise
     */
    public static boolean isEntityField(String logicalName) {
        return ENTITY_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a selection field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a selection field, false otherwise
     */
    public static boolean isSelectionField(String logicalName) {
        return SELECTION_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a list field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a list field, false otherwise
     */
    public static boolean isListField(String logicalName) {
        return LIST_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a multi field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a multi field, false otherwise
     */
    public static boolean isMultiField(String logicalName) {
        return MULTI_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a number field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a number field, false otherwise
     */
    public static boolean isNumberField(String logicalName) {
        return NUMBER_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a file field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a file field, false otherwise
     */
    public static boolean isFileField(String logicalName) {
        return FILE_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a pick-one field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a pick-one field, false otherwise
     */
    public static boolean isPickOneField(String logicalName) {
        return PICK_ONE_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a pick-many field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a pick-many field, false otherwise
     */
    public static boolean isPickManyField(String logicalName) {
        return PICK_MANY_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a stream field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a stream field, false otherwise
     */
    public static boolean isStreamField(String logicalName) {
        return STREAM_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a text field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a text field, false otherwise
     */
    public static boolean isTextField(String logicalName) {
        return TEXT_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a date field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a date field, false otherwise
     */
    public static boolean isDateField(String logicalName) {
        return DATE_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a boolean field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a boolean field, false otherwise
     */
    public static boolean isBooleanField(String logicalName) {
        return BOOLEAN_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a switch field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a switch field, false otherwise
     */
    public static boolean isSwitchField(String logicalName) {
        return SWITCH_FIELD_LOGICAL_NAME.equals(logicalName);
    }

    /**
     * Checks if the given logical name represents a rich text field.
     *
     * @param logicalName the logical name of the field type
     * @return true if the field is a rich text field, false otherwise
     */
    public static boolean isRichTextField(String logicalName) {
        return RICH_TEXT_FIELD_LOGICAL_NAME.equals(logicalName);
    }
}
