package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp;

import app.krista.model.field.NamedField;

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


}