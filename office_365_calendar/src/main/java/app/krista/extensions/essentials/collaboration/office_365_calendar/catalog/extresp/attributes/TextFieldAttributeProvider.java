package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.attributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides default attributes for Text field types.
 * This class encapsulates all the default configuration for text fields.
 */
public class TextFieldAttributeProvider implements FieldAttributeProvider {
    
    @Override
    public Map<String, Object> getDefaultAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("visualWidth", "S");
        attributes.put("trim", true);
        return attributes;
    }
    
    @Override
    public String getFieldType() {
        return "text";
    }
}
