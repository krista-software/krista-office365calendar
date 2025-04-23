package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

public class ValidatorsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidString() {
        Validators.validateString("", Constants.MEETING_ID);
    }

    @Test()
    public void testValidString() {
        Validators.validateString("validString", "validString");
    }

    @Test(expected = IOException.class)
    public void testInValidTimezone() throws IOException {
        Validators.validateTimeZone("Asia");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAbsoluteRecurrence() {
        Validators.validateRecurrenceValues("Monthly", List.of(), "Absolute Recurrence", 39.0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRelativeRecurrence() {
        Validators.validateRecurrenceValues("Yearly", List.of(), "Relative Recurrence", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidWeeklyRecurrence() {
        Validators.validateRecurrenceValues("Weekly", List.of(), "Absolute Recurrence", null, null);
    }
    @Test
    public void testValidateAttributes_UsesLatestTimeZone() throws IOException {
        Office365CalendarAttributes attributes = mock(Office365CalendarAttributes.class);
        when(attributes.getTimeZone()).thenReturn("America/Phoenix");
        Validators.validateTimeZone("America/Phoenix");
    }

}
