package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.EmailValidator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EmailValidatorTest {

    @Test
    public void test_extract_valid_email_address() {
        String input = "user.name123@example.com";
        String result = EmailValidator.emailExtractor(input);
        assertEquals("user.name123@example.com", result);
    }

    @Test
    public void test_handle_null_input() {
        String result = EmailValidator.emailExtractor(null);
        assertNull(result);
    }

    @Test
    public void testEmailWithTextAround() {
        String input = "Contact me at user@example.com for info.";
        String expected = "user@example.com";
        assertEquals(expected, EmailValidator.emailExtractor(input));
    }
}