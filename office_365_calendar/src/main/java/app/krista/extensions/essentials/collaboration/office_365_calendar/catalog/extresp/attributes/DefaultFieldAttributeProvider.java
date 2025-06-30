package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.attributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides default attributes for field types that don't have specific providers.
 * This class serves as a fallback for any field type that doesn't have its own
 * dedicated attribute provider.
 */
public class DefaultFieldAttributeProvider implements FieldAttributeProvider {
    
    @Override
    public Map<String, Object> getDefaultAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("visualWidth", "S");
        return attributes;
    }
    
    @Override
    public String getFieldType() {
        return "default";
    }
}
