package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class NumberValidatorTest {

    private final NumberValidator validator = new NumberValidator();

    @Test
    public void testValidateValidInteger() {
        Map<Validator.ValidationResource, String> context = new HashMap<>();
        assertTrue("Should validate integer 1", validator.validate("1", context));
        assertTrue("Should validate integer 5", validator.validate("5", context));
        assertTrue("Should validate integer 100", validator.validate("100", context));
        assertTrue("Should validate integer 1000", validator.validate("1000", context));
    }

    @Test
    public void testValidateValidDouble() {
        Map<Validator.ValidationResource, String> context = new HashMap<>();
        assertTrue("Should validate double 1.0", validator.validate("1.0", context));
        assertTrue("Should validate double 5.0", validator.validate("5.0", context));
        assertTrue("Should validate double 100.0", validator.validate("100.0", context));
    }

    @Test
    public void testValidateInvalidNumbers() {
        Map<Validator.ValidationResource, String> context = new HashMap<>();
        assertFalse("Should not validate 0", validator.validate("0", context));
        assertFalse("Should not validate negative number", validator.validate("-1", context));
        assertFalse("Should not validate decimal", validator.validate("1.5", context));
        assertFalse("Should not validate too large number", validator.validate("1001", context));
        assertFalse("Should not validate empty string", validator.validate("", context));
        assertFalse("Should not validate null", validator.validate(null, context));
        assertFalse("Should not validate non-number", validator.validate("abc", context));
    }

    @Test
    public void testGetFetchFieldName() {
        assertEquals("numberOfMeetings", validator.getFetchFieldName());
    }

    @Test
    public void testGetFetchStepMessage() {
        String message = validator.getFetchStepMessage();
        assertTrue("Should contain guidance", message.contains("valid positive number"));
        assertTrue("Should contain range", message.contains("1-1000"));
    }

    @Test
    public void testGetErrMessage() {
        String message = validator.getErrMessage("invalid");
        assertTrue("Should contain error info", message.contains("Invalid number"));
        assertTrue("Should contain the invalid value", message.contains("invalid"));
    }
}
