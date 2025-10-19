package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for Validators static methods.
 * Tests the utility methods for validation and attribute handling.
 */
public class ValidatorsTest {

    private Map<String, Object> testAttributeMap;

    @Before
    public void setUp() {
        testAttributeMap = new HashMap<>();
    }

    // Tests for addAttributeIfNotNull method

    @Test
    public void shouldAddAttributeWhenValueIsNotNull() {
        // Arrange
        String attributeName = "testAttribute";
        String attributeValue = "testValue";

        // Act
        Validators.addAttributeIfNotNull(testAttributeMap, attributeName, attributeValue);

        // Assert
        assertTrue(testAttributeMap.containsKey(attributeName));
        assertEquals(attributeValue, testAttributeMap.get(attributeName));
    }

    @Test
    public void shouldNotAddAttributeWhenValueIsNull() {
        // Arrange
        String attributeName = "testAttribute";
        Object attributeValue = null;

        // Act
        Validators.addAttributeIfNotNull(testAttributeMap, attributeName, attributeValue);

        // Assert
        assertFalse(testAttributeMap.containsKey(attributeName));
        assertTrue(testAttributeMap.isEmpty());
    }

    @Test
    public void shouldAddAttributeWhenValueIsEmptyString() {
        // Arrange
        String attributeName = "testAttribute";
        String attributeValue = "";

        // Act
        Validators.addAttributeIfNotNull(testAttributeMap, attributeName, attributeValue);

        // Assert
        assertTrue(testAttributeMap.containsKey(attributeName));
        assertEquals("", testAttributeMap.get(attributeName));
    }

    @Test
    public void shouldAddAttributeWhenValueIsZero() {
        // Arrange
        String attributeName = "testAttribute";
        Integer attributeValue = 0;

        // Act
        Validators.addAttributeIfNotNull(testAttributeMap, attributeName, attributeValue);

        // Assert
        assertTrue(testAttributeMap.containsKey(attributeName));
        assertEquals(Integer.valueOf(0), testAttributeMap.get(attributeName));
    }

    @Test
    public void shouldAddAttributeWhenValueIsFalse() {
        // Arrange
        String attributeName = "testAttribute";
        Boolean attributeValue = false;

        // Act
        Validators.addAttributeIfNotNull(testAttributeMap, attributeName, attributeValue);

        // Assert
        assertTrue(testAttributeMap.containsKey(attributeName));
        assertEquals(Boolean.FALSE, testAttributeMap.get(attributeName));
    }

    @Test
    public void shouldHandleMultipleAttributes() {
        // Arrange
        String attr1Name = "attribute1";
        String attr1Value = "value1";
        String attr2Name = "attribute2";
        Integer attr2Value = 42;
        String attr3Name = "attribute3";
        Object attr3Value = null;

        // Act
        Validators.addAttributeIfNotNull(testAttributeMap, attr1Name, attr1Value);
        Validators.addAttributeIfNotNull(testAttributeMap, attr2Name, attr2Value);
        Validators.addAttributeIfNotNull(testAttributeMap, attr3Name, attr3Value);

        // Assert
        assertEquals(2, testAttributeMap.size());
        assertTrue(testAttributeMap.containsKey(attr1Name));
        assertTrue(testAttributeMap.containsKey(attr2Name));
        assertFalse(testAttributeMap.containsKey(attr3Name));
        assertEquals(attr1Value, testAttributeMap.get(attr1Name));
        assertEquals(attr2Value, testAttributeMap.get(attr2Name));
    }

    @Test
    public void shouldHandleNullAttributeName() {
        // Arrange
        String attributeName = null;
        String attributeValue = "testValue";

        // Act
        Validators.addAttributeIfNotNull(testAttributeMap, attributeName, attributeValue);

        // Assert
        assertTrue(testAttributeMap.containsKey(null));
        assertEquals(attributeValue, testAttributeMap.get(null));
    }

    @Test(expected = NullPointerException.class)
    public void shouldHandleNullAttributeMap() {
        // Arrange
        Map<String, Object> nullMap = null;
        String attributeName = "testAttribute";
        String attributeValue = "testValue";

        // Act
        Validators.addAttributeIfNotNull(nullMap, attributeName, attributeValue);
    }

    @Test
    public void shouldOverwriteExistingAttribute() {
        // Arrange
        String attributeName = "testAttribute";
        String originalValue = "originalValue";
        String newValue = "newValue";
        
        testAttributeMap.put(attributeName, originalValue);

        // Act
        Validators.addAttributeIfNotNull(testAttributeMap, attributeName, newValue);

        // Assert
        assertEquals(1, testAttributeMap.size());
        assertEquals(newValue, testAttributeMap.get(attributeName));
    }

    // Tests for validateString method

    @Test
    public void shouldNotThrowExceptionForValidString() {
        // Arrange
        String validString = "valid string";
        String errorMsg = "test field";

        // Act & Assert - should not throw exception
        Validators.validateString(validString, errorMsg);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionForNullString() {
        // Arrange
        String nullString = null;
        String errorMsg = "test field";

        // Act
        Validators.validateString(nullString, errorMsg);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionForEmptyString() {
        // Arrange
        String emptyString = "";
        String errorMsg = "test field";

        // Act
        Validators.validateString(emptyString, errorMsg);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionForBlankString() {
        // Arrange
        String blankString = "   ";
        String errorMsg = "test field";

        // Act
        Validators.validateString(blankString, errorMsg);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionForStringWithOnlyTabs() {
        // Arrange
        String tabString = "\t\t\t";
        String errorMsg = "test field";

        // Act
        Validators.validateString(tabString, errorMsg);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionForStringWithOnlyNewlines() {
        // Arrange
        String newlineString = "\n\n\n";
        String errorMsg = "test field";

        // Act
        Validators.validateString(newlineString, errorMsg);
    }

    @Test
    public void shouldNotThrowExceptionForStringWithLeadingTrailingSpacesButContent() {
        // Arrange
        String stringWithSpaces = "  valid content  ";
        String errorMsg = "test field";

        // Act & Assert - should not throw exception
        Validators.validateString(stringWithSpaces, errorMsg);
    }

    @Test
    public void shouldHandleNullErrorMessage() {
        // Arrange
        String nullString = null;
        String errorMsg = null;

        // Act & Assert
        try {
            Validators.validateString(nullString, errorMsg);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Given null is null or blank."));
        }
    }

    @Test
    public void shouldHandleEmptyErrorMessage() {
        // Arrange
        String nullString = null;
        String errorMsg = "";

        // Act & Assert
        try {
            Validators.validateString(nullString, errorMsg);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Given  is null or blank."));
        }
    }

    @Test
    public void shouldValidateStringErrorMessage() {
        // Arrange
        String nullString = null;
        String errorMsg = "test field";

        // Act & Assert
        try {
            Validators.validateString(nullString, errorMsg);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Given test field is null or blank."));
        }
    }
}
