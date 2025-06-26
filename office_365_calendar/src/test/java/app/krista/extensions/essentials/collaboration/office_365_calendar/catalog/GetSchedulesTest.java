package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
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
 * Test class for the get schedules functionality with SubCatalogRequest pattern.
 */
public class GetSchedulesTest {

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
    public void testGetSchedules_ValidationPasses_Success() {
        // Arrange
        Long dateFrom = 1640995200000L; // 2022-01-01
        Long dateTo = 1641081600000L;   // 2022-01-02
        List<String> attendees = List.of("test@example.com", "user@example.com");
        String timeZone = "UTC";
        
        List<Map<String, Object>> mockSchedules = List.of(
            Map.of("Attendee", "test@example.com", "Schedules", List.of())
        );
        
        try {
            when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
            when(meetingDao.getSchedules(dateFrom, dateTo, attendees, timeZone)).thenReturn(mockSchedules);

            // Act
            ExtensionResponse response = meetingsArea.getSchedules(dateFrom, dateTo, attendees, timeZone);

            // Assert
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
            assertNotNull(response.getResponseValue());
            assertTrue(response.getResponseValue().containsKey("List of Meetings"));
            verify(meetingDao).getSchedules(dateFrom, dateTo, attendees, timeZone);
            verify(validationOrchestrator).validate(anyMap());
        } catch (Exception e) {
            fail("Test should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testGetSchedules_ValidationFails_ReturnsConfirmationResponse() {
        // Arrange
        Long dateFrom = 1641081600000L; // Invalid: after dateTo
        Long dateTo = 1640995200000L;   // Invalid: before dateFrom
        List<String> attendees = List.of("invalid-email");
        String timeZone = "UTC";
        
        ValidationOrchestrator.ValidationResult validationResult =
            new ValidationOrchestrator.ValidationResult(
                "The provided date range is invalid. The 'From' date must be before the 'To' date. Please check your dates.",
                Constants.FROM_DATE,
                "Please enter valid date range where 'From' date is before 'To' date.",
                "Invalid date range: 'From' date must be before 'To' date.",
                "Date"
            );
        List<ValidationOrchestrator.ValidationResult> validationResults = List.of(validationResult);
        
        ExtensionResponse mockConfirmationResponse = mock(ExtensionResponse.class);
        
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);
        when(responseGenerator.generateConfirmationResponse(any(), any(), any(), any()))
            .thenReturn(mockConfirmationResponse);

        // Act
        ExtensionResponse response = meetingsArea.getSchedules(dateFrom, dateTo, attendees, timeZone);

        // Assert
        assertEquals(mockConfirmationResponse, response);
        verify(validationOrchestrator).validate(anyMap());
        verify(errorHandlingStateManager).put(any(), anyMap());
        verify(responseGenerator).generateConfirmationResponse(
            eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
            eq(validationResults),
            eq(Constants.CONFIRM_REENTER_GET_SCHEDULES),
            anyMap()
        );
        try {
            verify(meetingDao, never()).getSchedules(any(), any(), any(), any());
        } catch (Exception e) {
            // This should not be called in this test case
        }
    }

    @Test
    public void testHandleReenterGetSchedules_Success() {
        // Arrange
        Long dateFrom = 1640995200000L;
        Long dateTo = 1641081600000L;
        List<String> attendees = List.of("test@example.com");
        String timeZone = "UTC";
        
        Map<String, Object> inputMap = Map.of(
            Constants.STATE_ID, "state-123",
            Constants.FROM_DATE, dateFrom,
            Constants.TO_DATE, dateTo,
            Constants.ATTENDEES, attendees,
            Constants.TIME_ZONE, timeZone
        );
        
        List<Map<String, Object>> mockSchedules = List.of(
            Map.of("Attendee", "test@example.com", "Schedules", List.of())
        );
        
        try {
            when(meetingDao.getSchedules(dateFrom, dateTo, attendees, timeZone)).thenReturn(mockSchedules);

            // Act
            ExtensionResponse response = subCatalogHelper.handleReenterGetSchedules(inputMap);

            // Assert
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
            assertNotNull(response.getResponseValue());
            assertTrue(response.getResponseValue().containsKey("List of Meetings"));
            verify(meetingDao).getSchedules(dateFrom, dateTo, attendees, timeZone);
        } catch (Exception e) {
            fail("Test should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testConfirmReenterGetSchedules_UserDeclines_ReturnsDenyResponse() {
        // Arrange
        Map<String, Object> inputMap = Map.of(
            Constants.RE_ENTER, false,
            Constants.STATE_ID, "state-123",
            Constants.FROM_DATE, 1640995200000L,
            Constants.TO_DATE, 1641081600000L,
            Constants.ATTENDEES, List.of("test@example.com"),
            Constants.TIME_ZONE, "UTC"
        );
        
        Map<String, Object> mockState = Map.of(
            Constants.VALIDATION_RESULTS, Collections.emptyList()
        );
        
        ExtensionResponse mockDenyResponse = mock(ExtensionResponse.class);
        
        when(errorHandlingStateManager.get("state-123")).thenReturn(mockState);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
            .thenReturn(mockDenyResponse);

        // Act
        ExtensionResponse response = subCatalogHelper.confirmReenterGetSchedules(inputMap);

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
    public void testConfirmReenterGetSchedules_UserAccepts_ReturnsFetchResponse() {
        // Arrange
        Map<String, Object> inputMap = Map.of(
            Constants.RE_ENTER, true,
            Constants.STATE_ID, "state-123",
            Constants.FROM_DATE, 1640995200000L,
            Constants.TO_DATE, 1641081600000L,
            Constants.ATTENDEES, List.of("test@example.com"),
            Constants.TIME_ZONE, "UTC"
        );
        
        Map<String, Object> mockState = Map.of(
            Constants.VALIDATION_RESULTS, Collections.emptyList()
        );
        
        ExtensionResponse mockFetchResponse = mock(ExtensionResponse.class);
        
        when(errorHandlingStateManager.get("state-123")).thenReturn(mockState);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
            .thenReturn(mockFetchResponse);

        // Act
        ExtensionResponse response = subCatalogHelper.confirmReenterGetSchedules(inputMap);

        // Assert
        assertEquals(mockFetchResponse, response);
        verify(responseGenerator).generateFetchResponse(
            eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
            any(),
            eq(Constants.HANDLE_REENTER_GET_SCHEDULES),
            anyMap()
        );
    }

    @Test
    public void testGetSchedules_WithNullValues_HandlesGracefully() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        
        // Act
        ExtensionResponse response = meetingsArea.getSchedules(null, null, null, null);

        // Assert
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        verify(validationOrchestrator).validate(anyMap());
    }
}
