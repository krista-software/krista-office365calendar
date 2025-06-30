import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.NamedFieldFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.attributes.FieldAttributeProviderRegistry;
import app.krista.model.field.NamedField;

import java.util.Map;

/**
 * Test to demonstrate the new field attribute provider system.
 */
public class TestFieldAttributeProviders {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Field Attribute Provider System ===\n");
        
        // Test 1: Direct attribute retrieval
        System.out.println("1. Testing direct attribute retrieval:\n");
        
        Map<String, Object> textAttrs = FieldAttributeProviderRegistry.getDefaultAttributes("Text");
        System.out.println("Text field default attributes: " + textAttrs);
        
        Map<String, Object> fileAttrs = FieldAttributeProviderRegistry.getDefaultAttributes("File");
        System.out.println("File field default attributes: " + fileAttrs);
        
        Map<String, Object> unknownAttrs = FieldAttributeProviderRegistry.getDefaultAttributes("Unknown");
        System.out.println("Unknown field default attributes: " + unknownAttrs);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Test 2: Using NamedFieldFactory public method
        System.out.println("2. Testing NamedFieldFactory.getDefaultAttributesForFieldType():\n");
        
        Map<String, Object> textAttrs2 = NamedFieldFactory.getDefaultAttributesForFieldType("Text");
        System.out.println("Text field attributes via NamedFieldFactory: " + textAttrs2);
        
        Map<String, Object> fileAttrs2 = NamedFieldFactory.getDefaultAttributesForFieldType("File");
        System.out.println("File field attributes via NamedFieldFactory: " + fileAttrs2);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Test 3: Creating fields with default attributes
        System.out.println("3. Testing field creation with default attributes:\n");
        
        NamedField textField = NamedFieldFactory.createTextFieldWithAttributes("testText", Map.of());
        System.out.println("Text field created: " + textField.getFieldType());
        System.out.println("Text field attributes: " + textField.getAttributes());
        
        NamedField fileField = NamedFieldFactory.createFileFieldWithAttributes("testFile", Map.of());
        System.out.println("File field created: " + fileField.getFieldType());
        System.out.println("File field attributes: " + fileField.getAttributes());
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Test 4: Creating fields with custom attributes (overriding defaults)
        System.out.println("4. Testing field creation with custom attributes:\n");
        
        Map<String, Object> customTextAttrs = Map.of("maxLength", 100, "placeholder", "Enter text here");
        NamedField customTextField = NamedFieldFactory.createTextFieldWithAttributes("customText", customTextAttrs);
        System.out.println("Custom text field attributes: " + customTextField.getAttributes());
        
        Map<String, Object> customFileAttrs = Map.of("maxFileSizeUnit", "GB", "multipleFileUpload", true);
        NamedField customFileField = NamedFieldFactory.createFileFieldWithAttributes("customFile", customFileAttrs);
        System.out.println("Custom file field attributes: " + customFileField.getAttributes());
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Test 5: Creating list fields
        System.out.println("5. Testing list field creation:\n");
        
        NamedField textListField = NamedFieldFactory.createListField("textList", "Text");
        System.out.println("Text list field created: " + textListField.getFieldType());
        System.out.println("Text list field attributes: " + textListField.getAttributes());
        
        NamedField fileListField = NamedFieldFactory.createListField("fileList", "File");
        System.out.println("File list field created: " + fileListField.getFieldType());
        System.out.println("File list field attributes: " + fileListField.getAttributes());
        
        System.out.println("\n=== Test Complete ===");
    }
}
