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
