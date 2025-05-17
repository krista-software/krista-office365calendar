package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ValidatorsTest {

    private ValidationOrchestrator orchestrator;

    private Validator mockEmailValidator;
    private Validator mockEventIdValidator;

    @Before
    public void setUp() throws Exception {
        MeetingDaoImpl mockMeetingDao = mock(MeetingDaoImpl.class);
        orchestrator = new ValidationOrchestrator(mockMeetingDao);

        mockEmailValidator = mock(Validator.class);
        mockEventIdValidator = mock(Validator.class);

        Field validatorsField = ValidationOrchestrator.class.getDeclaredField("validators");
        validatorsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Validator.ValidationResource, Validator> validatorMap = (Map<Validator.ValidationResource, Validator>) validatorsField.get(orchestrator);
        validatorMap.clear();
        validatorMap.put(Validator.ValidationResource.EMAIL, mockEmailValidator);
        validatorMap.put(Validator.ValidationResource.EVENT_ID, mockEventIdValidator);
    }

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

    @Test
    public void validate_shouldReturnValidationResult_whenEmailValidationFails() {
        Map<Validator.ValidationResource, String> input = Map.of(Validator.ValidationResource.EMAIL, "invalid-email");

        when(mockEmailValidator.validate(eq("invalid-email"), anyMap())).thenReturn(false);
        when(mockEmailValidator.getConfirmationStepMessage(any(), any())).thenReturn("Email is not valid.");
        when(mockEmailValidator.getFetchFieldName()).thenReturn("email");
        when(mockEmailValidator.getFetchStepMessage()).thenReturn("Please enter a valid email.");
        when(mockEmailValidator.getErrMessage(any())).thenReturn("Invalid email");

        List<ValidationOrchestrator.ValidationResult> result = orchestrator.validate(input);

        assertEquals(1, result.size());
        ValidationOrchestrator.ValidationResult vr = result.getFirst();
        assertEquals("Email is not valid.", vr.confirmStepMessage());
        assertEquals("email", vr.fetchFieldName());
        assertEquals("Please enter a valid email.", vr.fetchStepMessage());
        assertEquals("Invalid email", vr.errMessage());
    }

    @Test
    public void validate_shouldReturnEmptyList_whenAllValidationsPass() {
        Map<Validator.ValidationResource, String> input = Map.of(
                Validator.ValidationResource.EMAIL, "valid@email.com",
                Validator.ValidationResource.EVENT_ID, "event-123"
        );
        when(mockEmailValidator.validate(any(), any())).thenReturn(true);
        when(mockEventIdValidator.validate(any(), any())).thenReturn(true);

        List<ValidationOrchestrator.ValidationResult> result = orchestrator.validate(input);
        assertTrue(result.isEmpty());
    }

    @Test
    public void validate_shouldSkipValidator_whenValidatorNotPresentForKey() throws Exception {
        Field validatorsField = ValidationOrchestrator.class.getDeclaredField("validators");
        validatorsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Validator.ValidationResource, Validator> validatorMap = (Map<Validator.ValidationResource, Validator>) validatorsField.get(orchestrator);
        validatorMap.remove(Validator.ValidationResource.EVENT_ID);

        Map<Validator.ValidationResource, String> input = Map.of(Validator.ValidationResource.EVENT_ID, "someId");

        List<ValidationOrchestrator.ValidationResult> result = orchestrator.validate(input);
        assertTrue(result.isEmpty());
    }
}