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

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.NamedFieldFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.attributes.FieldAttributeProviderRegistry;
import app.krista.model.field.NamedField;

import java.util.Map;

/**
 * Test to demonstrate the new field attribute provider system.
 */
public class TestFieldAttributeProviders {

    public static void main(String[] args) {

        // Test 1: Direct attribute retrieval
        Map<String, Object> textAttrs = FieldAttributeProviderRegistry.getDefaultAttributes("Text");
        Map<String, Object> fileAttrs = FieldAttributeProviderRegistry.getDefaultAttributes("File");
        Map<String, Object> unknownAttrs = FieldAttributeProviderRegistry.getDefaultAttributes("Unknown");

        // Test 2: Using NamedFieldFactory public method
        Map<String, Object> textAttrs2 = NamedFieldFactory.getDefaultAttributesForFieldType("Text");
        Map<String, Object> fileAttrs2 = NamedFieldFactory.getDefaultAttributesForFieldType("File");

        // Test 3: Creating fields with default attributes
        NamedField textField = NamedFieldFactory.createTextFieldWithAttributes("testText", Map.of());
        NamedField fileField = NamedFieldFactory.createFileFieldWithAttributes("testFile", Map.of());

        // Test 4: Creating fields with custom attributes (overriding defaults)
        Map<String, Object> customTextAttrs = Map.of("maxLength", 100, "placeholder", "Enter text here");
        NamedField customTextField = NamedFieldFactory.createTextFieldWithAttributes("customText", customTextAttrs);

        Map<String, Object> customFileAttrs = Map.of("maxFileSizeUnit", "GB", "multipleFileUpload", true);
        NamedField customFileField = NamedFieldFactory.createFileFieldWithAttributes("customFile", customFileAttrs);

        // Test 5: Creating list fields
        NamedField textListField = NamedFieldFactory.createListField("textList", "Text");
        NamedField fileListField = NamedFieldFactory.createListField("fileList", "File");
    }
}
