package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.attributes;

import java.util.Map;

/**
 * Interface for providing default attributes for different field types.
 * This interface follows the Strategy pattern to allow different field types
 * to have their own attribute configuration logic.
 */
public interface FieldAttributeProvider {
    
    /**
     * Gets the default attributes for this field type.
     *
     * @return a map containing the default attributes for the field type
     */
    Map<String, Object> getDefaultAttributes();
    
    /**
     * Gets the field type that this provider handles.
     *
     * @return the field type string (e.g., "text", "file", "date")
     */
    String getFieldType();
}
