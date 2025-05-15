package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.EventIdValidator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EventIdValidatorTest {

    private MeetingDaoImpl meetingDao;
    private EventIdValidator validator;

    @Before
    public void setup() {
        meetingDao = mock(MeetingDaoImpl.class);
        validator = new EventIdValidator(meetingDao);
    }

    @Test
    public void validate_shouldReturnTrue_whenMeetingExists() throws IOException {
        String eventId = "12345";
        Meeting mockMeeting = mock(Meeting.class);
        when(meetingDao.getMeetingById(eventId)).thenReturn(mockMeeting);

        boolean result = validator.validate(eventId, Collections.emptyMap());

        assertTrue(result);
        verify(meetingDao, times(1)).getMeetingById(eventId);
    }

    @Test
    public void validate_shouldReturnFalse_whenMeetingDoesNotExist() throws IOException {
        String eventId = "nonexistent";
        when(meetingDao.getMeetingById(eventId)).thenReturn(null);

        boolean result = validator.validate(eventId, Collections.emptyMap());

        assertFalse(result);
        verify(meetingDao, times(1)).getMeetingById(eventId);
    }

    @Test
    public void validate_shouldThrowMustAuthorizeException_whenAuthorizationFails() throws IOException {
        String eventId = "unauthorized";
        when(meetingDao.getMeetingById(eventId)).thenThrow(new MustAuthorizeException("Authorization failed"));

        MustAuthorizeException exception = assertThrows(MustAuthorizeException.class,
                () -> validator.validate(eventId, Collections.emptyMap()));

        assertEquals("Authorization failed", exception.getMessage());
    }

    @Test
    public void validate_shouldReturnFalse_whenGenericExceptionOccurs() throws IOException {
        String eventId = "error";
        when(meetingDao.getMeetingById(eventId)).thenThrow(new RuntimeException("DB error"));

        boolean result = validator.validate(eventId, Collections.emptyMap());

        assertFalse(result);
    }

    @Test
    public void getFetchFieldName_shouldReturnEventId() {
        assertEquals("eventId", validator.getFetchFieldName());
    }

    @Test
    public void getFetchStepMessage_shouldReturnProperMessage() {
        assertEquals("Please enter valid event id.", validator.getFetchStepMessage());
    }

    @Test
    public void getConfirmationStepMessage_shouldIncludeEventId() {
        String message = validator.getConfirmationStepMessage("123", Collections.emptyMap());
        assertEquals("The provided event id: 123 does not exist.", message);
    }

    @Test
    public void getErrMessage_shouldIncludeEventId() {
        String message = validator.getErrMessage("123");
        assertEquals("Invalid event id: 123", message);
    }
}
