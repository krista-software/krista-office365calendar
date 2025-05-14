package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp;

import app.krista.model.field.NamedField;

import java.util.Map;


/**
 * Utility class providing static factory methods for creating {@link NamedField} instances
 * with predefined field types for use in Office 365 calendar extension responses.
 * <p>
 * This class simplifies the creation of commonly used field types such as text fields and switch fields,
 * ensuring consistency and reducing boilerplate code throughout the extension.
 * </p>
 */
public class NamedFieldFactory {

    public static NamedField createTextField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.TEXT_FIELD, Map.of(), Map.of());
    }

    public static NamedField createSwitchField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.SWITCH_FIELD, Map.of(), Map.of());
    }
}