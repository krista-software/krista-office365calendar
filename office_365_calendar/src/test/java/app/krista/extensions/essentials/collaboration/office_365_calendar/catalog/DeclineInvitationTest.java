package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity.EventDetails;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

/**
 * Test class for the decline invitation functionality with SubCatalogRequest pattern.
 */
public class DeclineInvitationTest {

    @Mock
    private MeetingDaoImpl meetingDao;
    
    @Mock
    private CatalogTypes catalogTypes;
    
    @Mock
    private ValidationOrchestrator validationOrchestrator;
    
    @Mock
    private ErrorHandlingStateManager errorHandlingStateManager;
    
    @Mock
    private ExtensionResponseGenerator responseGenerator;
    
    private MeetingsArea meetingsArea;
    private MeetingsAreaSubCatalogRequestHelper subCatalogHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        meetingsArea = new MeetingsArea(meetingDao, catalogTypes, validationOrchestrator, 
                                       errorHandlingStateManager, responseGenerator);
        subCatalogHelper = new MeetingsAreaSubCatalogRequestHelper(meetingDao, catalogTypes, 
                                                                   errorHandlingStateManager, responseGenerator);
    }

    @Test
    public void testDeclineInvitation_ValidationPasses_Success() {
        // Arrange
        String eventId = "test-event-123";
        Meeting mockMeeting = mock(Meeting.class);
        EventDetails mockEventDetails = mock(EventDetails.class);
        
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(meetingDao.declineMeetingInvitation(eventId)).thenReturn(mockMeeting);
        when(CatalogTypes.fromEvent(mockMeeting)).thenReturn(mockEventDetails);

        // Act
        ExtensionResponse response = meetingsArea.declineInvitation(eventId);

        // Assert
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        assertNotNull(response.getResponseValue());
        assertTrue(response.getResponseValue().containsKey("Event Details"));
        verify(meetingDao).declineMeetingInvitation(eventId);
        verify(validationOrchestrator).validate(Map.of(Validator.ValidationResource.EVENT_ID, eventId));
    }

    @Test
    public void testDeclineInvitation_ValidationFails_ReturnsConfirmationResponse() {
        // Arrange
        String eventId = "invalid-event-123";
        ValidationOrchestrator.ValidationResult validationResult = 
            new ValidationOrchestrator.ValidationResult(
                "The provided event ID does not exist. Please check.",
                Constants.EVENT_ID,
                "Please enter valid event id.",
                "Invalid event ID: invalid-event-123"
            );
        List<ValidationOrchestrator.ValidationResult> validationResults = List.of(validationResult);
        
        ExtensionResponse mockConfirmationResponse = mock(ExtensionResponse.class);
        
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);
        when(responseGenerator.generateConfirmationResponse(any(), any(), any(), any()))
            .thenReturn(mockConfirmationResponse);

        // Act
        ExtensionResponse response = meetingsArea.declineInvitation(eventId);

        // Assert
        assertEquals(mockConfirmationResponse, response);
        verify(validationOrchestrator).validate(Map.of(Validator.ValidationResource.EVENT_ID, eventId));
        verify(errorHandlingStateManager).put(any(), anyMap());
        verify(responseGenerator).generateConfirmationResponse(
            eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
            eq(validationResults),
            eq(Constants.CONFIRM_REENTER_DECLINE_INVITATION),
            anyMap()
        );
        verify(meetingDao, never()).declineMeetingInvitation(any());
    }

    @Test
    public void testHandleReenterDeclineInvitation_Success() {
        // Arrange
        String eventId = "test-event-123";
        Map<String, Object> inputMap = Map.of(
            Constants.STATE_ID, "state-123",
            Constants.EVENT_ID, eventId
        );
        
        Meeting mockMeeting = mock(Meeting.class);
        EventDetails mockEventDetails = mock(EventDetails.class);
        
        when(meetingDao.declineMeetingInvitation(eventId)).thenReturn(mockMeeting);
        when(CatalogTypes.fromEvent(mockMeeting)).thenReturn(mockEventDetails);

        // Act
        ExtensionResponse response = subCatalogHelper.handleReenterDeclineInvitation(inputMap);

        // Assert
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        assertNotNull(response.getResponseValue());
        assertTrue(response.getResponseValue().containsKey("Event Details"));
        verify(meetingDao).declineMeetingInvitation(eventId);
    }

    @Test
    public void testConfirmReenterDeclineInvitation_UserDeclines_ReturnsDenyResponse() {
        // Arrange
        Map<String, Object> inputMap = Map.of(
            Constants.RE_ENTER, false,
            Constants.STATE_ID, "state-123",
            Constants.EVENT_ID, "test-event-123"
        );
        
        Map<String, Object> mockState = Map.of(
            Constants.VALIDATION_RESULTS, Collections.emptyList()
        );
        
        ExtensionResponse mockDenyResponse = mock(ExtensionResponse.class);
        
        when(errorHandlingStateManager.get("state-123")).thenReturn(mockState);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
            .thenReturn(mockDenyResponse);

        // Act
        ExtensionResponse response = subCatalogHelper.confirmReenterDeclineInvitation(inputMap);

        // Assert
        assertEquals(mockDenyResponse, response);
        verify(responseGenerator).generateFetchDenyResponse(
            eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
            any(),
            eq(null),
            eq(Map.of())
        );
    }

    @Test
    public void testConfirmReenterDeclineInvitation_UserAccepts_ReturnsFetchResponse() {
        // Arrange
        Map<String, Object> inputMap = Map.of(
            Constants.RE_ENTER, true,
            Constants.STATE_ID, "state-123",
            Constants.EVENT_ID, "test-event-123"
        );
        
        Map<String, Object> mockState = Map.of(
            Constants.VALIDATION_RESULTS, Collections.emptyList()
        );
        
        ExtensionResponse mockFetchResponse = mock(ExtensionResponse.class);
        
        when(errorHandlingStateManager.get("state-123")).thenReturn(mockState);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
            .thenReturn(mockFetchResponse);

        // Act
        ExtensionResponse response = subCatalogHelper.confirmReenterDeclineInvitation(inputMap);

        // Assert
        assertEquals(mockFetchResponse, response);
        verify(responseGenerator).generateFetchResponse(
            eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
            any(),
            eq(Constants.HANDLE_REENTER_DECLINE_INVITATION),
            anyMap()
        );
    }
}
