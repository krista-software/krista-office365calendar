package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.attributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides default attributes for File field types.
 * This class encapsulates all the default configuration for file fields.
 */
public class FileFieldAttributeProvider implements FieldAttributeProvider {
    
    @Override
    public Map<String, Object> getDefaultAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("visualWidth", "S");
        attributes.put("multipleFileUpload", false);
        attributes.put("minFileSizeUnit", "KB");
        attributes.put("maxFileSizeUnit", "MB");
        attributes.put("fileTypes", java.util.List.of(
            "Document",
            "Image gallery",
            "Camera",
            "Contact",
            "Audio"
        ));
        return attributes;
    }
    
    @Override
    public String getFieldType() {
        return "file";
    }
}
