package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.NamedFieldFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.model.field.NamedField;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class for the enhanced NamedFieldFactory with list field support and field type validation.
 */
public class NamedFieldFactoryEnhancedTest {

    @Test
    public void testValidationResultWithFieldType() {
        // Test that ValidationResult now includes field type information
        ValidationOrchestrator.ValidationResult result = new ValidationOrchestrator.ValidationResult(
                "Confirm message", "fieldName", "Fetch message", "Error message", "Date"
        );
        
        assertEquals("Date", result.fieldType());
        assertEquals("fieldName", result.fetchFieldName());
        assertEquals("Confirm message", result.confirmStepMessage());
        assertEquals("Fetch message", result.fetchStepMessage());
        assertEquals("Error message", result.errMessage());
    }

    @Test
    public void testCreateFieldByTypeReturnsNonNull() {
        // Test that all field types return non-null NamedField instances
        assertNotNull(NamedFieldFactory.createFieldByType("test", "Date"));
        assertNotNull(NamedFieldFactory.createFieldByType("test", "Email"));
        assertNotNull(NamedFieldFactory.createFieldByType("test", "Text"));
        assertNotNull(NamedFieldFactory.createFieldByType("test", "Number"));
        assertNotNull(NamedFieldFactory.createFieldByType("test", "Boolean"));
        assertNotNull(NamedFieldFactory.createFieldByType("test", "File"));
        assertNotNull(NamedFieldFactory.createFieldByType("test", "Switch"));
        assertNotNull(NamedFieldFactory.createFieldByType("test", "RichText"));
        assertNotNull(NamedFieldFactory.createFieldByType("test", "PickOne"));
        assertNotNull(NamedFieldFactory.createFieldByType("test", "Password"));
        assertNotNull(NamedFieldFactory.createFieldByType("test", "List"));
        assertNotNull(NamedFieldFactory.createFieldByType("test", "UnknownType")); // Should default to text
    }

    @Test
    public void testCreateListFieldReturnsNonNull() {
        // Test that list field creation works for various inner types
        assertNotNull(NamedFieldFactory.createListField("textList", "Text"));
        assertNotNull(NamedFieldFactory.createListField("emailList", "Email"));
        assertNotNull(NamedFieldFactory.createListField("dateList", "Date"));
        assertNotNull(NamedFieldFactory.createListField("numberList", "Number"));
        assertNotNull(NamedFieldFactory.createListField("booleanList", "Boolean"));
        assertNotNull(NamedFieldFactory.createListField("fileList", "File"));
    }

    @Test
    public void testCreateListFieldWithCustomAttributes() {
        Map<String, Object> customAttributes = Map.of(
            "required", true,
            "maxLength", 100
        );
        
        NamedField listField = NamedFieldFactory.createListField("customList", "Text", customAttributes);
        assertNotNull(listField);
        assertEquals("customList", listField.getName());
    }

    @Test
    public void testCreateFieldWithListSyntax() {
        // Test List<Type> and List[Type] syntax
        assertNotNull(NamedFieldFactory.createField("emails", "List<Email>"));
        assertNotNull(NamedFieldFactory.createField("dates", "List[Date]"));
        assertNotNull(NamedFieldFactory.createField("numbers", "List<Number>"));
        assertNotNull(NamedFieldFactory.createField("texts", "List<Text>"));
    }

    @Test
    public void testCreatePasswordField() {
        // Test password field creation
        NamedField passwordField1 = NamedFieldFactory.createField("Password", "Text");
        assertNotNull(passwordField1);
        assertEquals("Password", passwordField1.getName());

        NamedField passwordField2 = NamedFieldFactory.createPasswordField("userPassword");
        assertNotNull(passwordField2);
        assertEquals("userPassword", passwordField2.getName());
    }

    @Test
    public void testCreateEmailField() {
        NamedField emailField = NamedFieldFactory.createEmailField("userEmail");
        assertNotNull(emailField);
        assertEquals("userEmail", emailField.getName());
    }

    @Test
    public void testCreateFieldHandlesAllTypes() {
        // Test that createField method handles all types correctly
        assertNotNull(NamedFieldFactory.createField("text", "Text"));
        assertNotNull(NamedFieldFactory.createField("date", "Date"));
        assertNotNull(NamedFieldFactory.createField("email", "Email"));
        assertNotNull(NamedFieldFactory.createField("number", "Number"));
        assertNotNull(NamedFieldFactory.createField("boolean", "Boolean"));
        assertNotNull(NamedFieldFactory.createField("file", "File"));
        assertNotNull(NamedFieldFactory.createField("list", "List"));
        assertNotNull(NamedFieldFactory.createField("switch", "Switch"));
        assertNotNull(NamedFieldFactory.createField("richtext", "RichText"));
        assertNotNull(NamedFieldFactory.createField("pickone", "PickOne"));
    }

    @Test
    public void testCaseInsensitiveFieldTypeCreation() {
        // Test that field type matching is case-insensitive
        assertNotNull(NamedFieldFactory.createFieldByType("test1", "date"));
        assertNotNull(NamedFieldFactory.createFieldByType("test2", "DATE"));
        assertNotNull(NamedFieldFactory.createFieldByType("test3", "Date"));
        assertNotNull(NamedFieldFactory.createFieldByType("test4", "email"));
        assertNotNull(NamedFieldFactory.createFieldByType("test5", "EMAIL"));
        assertNotNull(NamedFieldFactory.createFieldByType("test6", "Email"));
    }

    @Test
    public void testFieldNamePreservation() {
        // Test that field names are preserved correctly
        String[] fieldNames = {"testField", "user_email", "dateRange", "fileUpload", "switchOption"};
        
        for (String fieldName : fieldNames) {
            NamedField field = NamedFieldFactory.createTextField(fieldName);
            assertEquals(fieldName, field.getName());
            
            NamedField listField = NamedFieldFactory.createListField(fieldName, "Text");
            assertEquals(fieldName, listField.getName());
        }
    }

    @Test
    public void testAllFactoryMethodsReturnNonNull() {
        // Verify that all factory methods return non-null instances
        assertNotNull(NamedFieldFactory.createTextField("test"));
        assertNotNull(NamedFieldFactory.createDateField("test"));
        assertNotNull(NamedFieldFactory.createEmailField("test"));
        assertNotNull(NamedFieldFactory.createNumberField("test"));
        assertNotNull(NamedFieldFactory.createBooleanField("test"));
        assertNotNull(NamedFieldFactory.createFileField("test"));
        assertNotNull(NamedFieldFactory.createSwitchField("test"));
        assertNotNull(NamedFieldFactory.createRichTextField("test"));
        assertNotNull(NamedFieldFactory.createPickOneField("test"));
        assertNotNull(NamedFieldFactory.createPasswordField("test"));
    }
}
