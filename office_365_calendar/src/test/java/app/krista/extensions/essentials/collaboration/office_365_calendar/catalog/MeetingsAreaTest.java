/*
 * Office365Calendar Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity.EventDetails;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.audit.Office365CalendarAuditService;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.TelemetryHelper;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import app.krista.model.base.File;
import com.microsoft.graph.models.FileAttachment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;

@RunWith(MockitoJUnitRunner.class)
public class MeetingsAreaTest {

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

    @Mock
    private TelemetryHelper telemetryHelper;

    @Mock
    private Office365CalendarAuditService auditService;

    @Mock
    private app.krista.extension.executor.Invoker invoker;

    @InjectMocks
    private MeetingsArea meetingsArea;

    @Before
    public void setup() {
        // Setup default mocks if needed
    }

    // ========================================================================
    // cancelEvent
    // ========================================================================

    @Test
    public void testCancelEvent_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("cancelEvent")))
                    .thenReturn(Collections.emptyList());

            Meeting meeting = mock(Meeting.class);
            when(meetingDao.cancelMeeting("event-123", "delegate@example.com")).thenReturn(meeting);
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.cancelEvent("delegate@example.com", "event-123", true);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testCancelEvent_Success_NullMeeting() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("cancelEvent")))
                .thenReturn(Collections.emptyList());

        when(meetingDao.cancelMeeting("event-123", "delegate@example.com")).thenReturn(null);
        
        ExtensionResponse response = meetingsArea.cancelEvent("delegate@example.com", "event-123", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testCancelEvent_Success_AllowRetryFalse() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            Meeting meeting = mock(Meeting.class);
            when(meetingDao.cancelMeeting("event-123", "delegate@example.com")).thenReturn(meeting);
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.cancelEvent("delegate@example.com", "event-123", false);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testCancelEvent_ValidationError() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("cancelEvent")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.cancelEvent("delegate@example.com", "event-123", true);

        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testCancelEvent_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("cancelEvent")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.cancelMeeting(anyString(), anyString())).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.cancelEvent("delegate@example.com", "event-123", true);
    }

    @Test
    public void testCancelEvent_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("cancelEvent")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.cancelMeeting(anyString(), anyString())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.cancelEvent("delegate@example.com", "event-123", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getNextUpcomingMeetings
    // ========================================================================

    @Test
    public void testGetNextUpcomingMeetings_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getNextUpcomingMeetings")))
                    .thenReturn(Collections.emptyList());

            Meeting meeting = mock(Meeting.class);
            when(meetingDao.getMeetings(5)).thenReturn(List.of(meeting));
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.getNextUpcomingMeetings(5.0, true);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testGetNextUpcomingMeetings_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getNextUpcomingMeetings")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getNextUpcomingMeetings(5.0, true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGetNextUpcomingMeetings_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getNextUpcomingMeetings")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getNextUpcomingMeetings(5.0, false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetNextUpcomingMeetings_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getNextUpcomingMeetings")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getMeetings(anyInt())).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.getNextUpcomingMeetings(5.0, true);
    }

    @Test
    public void testGetNextUpcomingMeetings_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getNextUpcomingMeetings")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getMeetings(anyInt())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.getNextUpcomingMeetings(5.0, true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // declineInvitation
    // ========================================================================

    @Test
    public void testDeclineInvitation_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("declineInvitation")))
                    .thenReturn(Collections.emptyList());

            Meeting meeting = mock(Meeting.class);
            when(meetingDao.declineMeetingInvitation("event-123")).thenReturn(meeting);
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.declineInvitation("event-123", true);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testDeclineInvitation_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("declineInvitation")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.declineInvitation("event-123", true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testDeclineInvitation_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("declineInvitation")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.declineInvitation("event-123", false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testDeclineInvitation_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("declineInvitation")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.declineMeetingInvitation(anyString())).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.declineInvitation("event-123", true);
    }

    @Test
    public void testDeclineInvitation_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("declineInvitation")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.declineMeetingInvitation(anyString())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.declineInvitation("event-123", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // tentativeInvitation
    // ========================================================================

    @Test
    public void testTentativeInvitation_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("tentativeInvitation")))
                    .thenReturn(Collections.emptyList());

            Meeting meeting = mock(Meeting.class);
            when(meetingDao.tentativeMeetingInvitation("event-123")).thenReturn(meeting);
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.tentativeInvitation("event-123", true);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testTentativeInvitation_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("tentativeInvitation")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.tentativeInvitation("event-123", true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testTentativeInvitation_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("tentativeInvitation")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.tentativeInvitation("event-123", false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testTentativeInvitation_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("tentativeInvitation")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.tentativeMeetingInvitation(anyString())).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.tentativeInvitation("event-123", true);
    }

    @Test
    public void testTentativeInvitation_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("tentativeInvitation")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.tentativeMeetingInvitation(anyString())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.tentativeInvitation("event-123", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // forwardEvent
    // ========================================================================

    @Test
    public void testForwardEvent_Success() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("forwardEvent")))
                .thenReturn(Collections.emptyList());

        ExtensionResponse response = meetingsArea.forwardEvent("event-123", List.of("attendee@example.com"), true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testForwardEvent_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("forwardEvent")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.forwardEvent("event-123", List.of("attendee@example.com"), true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testForwardEvent_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("forwardEvent")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.forwardEvent("event-123", List.of(), false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testForwardEvent_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("forwardEvent")))
                .thenReturn(Collections.emptyList());
        doThrow(new MustAuthorizeException("auth")).when(meetingDao).forwardMeeting(anyString(), anyList());

        meetingsArea.forwardEvent("event-123", List.of("attendee@example.com"), true);
    }

    @Test
    public void testForwardEvent_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("forwardEvent")))
                .thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("error")).when(meetingDao).forwardMeeting(anyString(), anyList());

        ExtensionResponse response = meetingsArea.forwardEvent("event-123", List.of("attendee@example.com"), true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // scheduleMeeting
    // ========================================================================

    @Test
    public void testScheduleMeeting_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("scheduleMeeting")))
                    .thenReturn(Collections.emptyList());

            Meeting meeting = mock(Meeting.class);
            when(meetingDao.addMeeting(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(meeting);
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.scheduleMeeting("org@example.com", "Title", "Loc", 1L, 2L, List.of("req@example.com"), List.of("opt@example.com"), false, "UTC", "None", "Never", 1.0, List.of("Monday"), "Absolute recurrence", 1.0, "First", "Monday", "January", "No end", 3L, 5.0, List.of(mock(File.class)), "Details", false, true);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testScheduleMeeting_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("scheduleMeeting")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.scheduleMeeting("org@example.com", "Title", "Loc", 1L, 2L, List.of("req@example.com"), List.of("opt@example.com"), false, "UTC", "None", "Never", 1.0, List.of("Monday"), "Absolute recurrence", 1.0, "First", "Monday", "January", "No end", 3L, 5.0, List.of(mock(File.class)), "Details", false, true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testScheduleMeeting_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("scheduleMeeting")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.scheduleMeeting(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testScheduleMeeting_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("scheduleMeeting")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.addMeeting(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.scheduleMeeting(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, true);
    }

    @Test
    public void testScheduleMeeting_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("scheduleMeeting")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.addMeeting(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.scheduleMeeting(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getAllUnacceptedEvents
    // ========================================================================

    @Test
    public void testGetAllUnacceptedEvents_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            Meeting meeting = mock(Meeting.class);
            when(meetingDao.getUnAcceptedMeetings()).thenReturn(List.of(meeting));
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            List<EventDetails> response = meetingsArea.getAllUnacceptedEvents();

            assertNotNull(response);
            assertEquals(1, response.size());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetAllUnacceptedEvents_MustAuthorizeException() throws Exception {
        when(meetingDao.getUnAcceptedMeetings()).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.getAllUnacceptedEvents();
    }

    @Test(expected = Exception.class)
    public void testGetAllUnacceptedEvents_GeneralException() throws Exception {
        when(meetingDao.getUnAcceptedMeetings()).thenThrow(new RuntimeException("error"));

        meetingsArea.getAllUnacceptedEvents();
    }

    // ========================================================================
    // addAttendees
    // ========================================================================

    @Test
    public void testAddAttendees_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("addAttendees")))
                    .thenReturn(Collections.emptyList());

            Meeting meeting = mock(Meeting.class);
            when(meetingDao.addAttendees("event-123", List.of("req@example.com"), List.of("opt@example.com"))).thenReturn(meeting);
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.addAttendees("event-123", List.of("req@example.com"), List.of("opt@example.com"), true);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testAddAttendees_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("addAttendees")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.addAttendees("event-123", List.of("req@example.com"), List.of("opt@example.com"), true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testAddAttendees_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("addAttendees")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.addAttendees("event-123", List.of(), null, false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testAddAttendees_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("addAttendees")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.addAttendees(anyString(), anyList(), anyList())).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.addAttendees("event-123", List.of("req@example.com"), List.of("opt@example.com"), true);
    }

    @Test
    public void testAddAttendees_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("addAttendees")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.addAttendees(anyString(), anyList(), anyList())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.addAttendees("event-123", List.of("req@example.com"), List.of("opt@example.com"), true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // updateEvent
    // ========================================================================

    @Test
    public void testUpdateEvent_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("updateEvent")))
                    .thenReturn(Collections.emptyList());

            Meeting meeting = mock(Meeting.class);
            when(meetingDao.updateMeeting(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(meeting);
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.updateEvent("org@ex.com", "event-123", "Title", "Loc", 1L, 2L, "Details", "UTC", List.of("req@example.com"), List.of("opt@example.com"), false, "15 minutes before", "Weekly", 1.0, List.of("Monday"), "Absolute recurrence", 1.0, "First", "Monday", "January", "No end", 3L, 5.0, false, true);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testUpdateEvent_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("updateEvent")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new java.util.HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.updateEvent("org@ex.com", "event-123", "Title", "Loc", 1L, 2L, "Details", "UTC", List.of(), List.of(), false, "None", "Never", 1.0, List.of(), "Absolute", 1.0, "First", "Monday", "Jan", "No end", 3L, 5.0, false, true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testUpdateEvent_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("updateEvent")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.updateEvent(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testUpdateEvent_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("updateEvent")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.updateMeeting(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.updateEvent("org@ex.com", "event-123", "Title", "Loc", 1L, 2L, "Details", "UTC", List.of(), List.of(), false, "None", "Never", 1.0, List.of(), "Absolute", 1.0, "First", "Monday", "Jan", "No end", 3L, 5.0, false, true);
    }

    @Test
    public void testUpdateEvent_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("updateEvent")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.updateMeeting(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.updateEvent("org@ex.com", "event-123", "Title", "Loc", 1L, 2L, "Details", "UTC", List.of(), List.of(), false, "None", "Never", 1.0, List.of(), "Absolute", 1.0, "First", "Monday", "Jan", "No end", 3L, 5.0, false, true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // acceptInvitation
    // ========================================================================

    @Test
    public void testAcceptInvitation_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("acceptInvitation")))
                    .thenReturn(Collections.emptyList());

            Meeting meeting = mock(Meeting.class);
            when(meetingDao.acceptMeetingInvitation("event-123")).thenReturn(meeting);
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.acceptInvitation("event-123", true);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testAcceptInvitation_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("acceptInvitation")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new java.util.HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.acceptInvitation("event-123", true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testAcceptInvitation_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("acceptInvitation")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.acceptInvitation("event-123", false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testAcceptInvitation_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("acceptInvitation")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.acceptMeetingInvitation(anyString())).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.acceptInvitation("event-123", true);
    }

    @Test
    public void testAcceptInvitation_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("acceptInvitation")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.acceptMeetingInvitation(anyString())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.acceptInvitation("event-123", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // removeAttendee
    // ========================================================================

    @Test
    public void testRemoveAttendee_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("removeAttendee")))
                    .thenReturn(Collections.emptyList());

            Meeting meeting = mock(Meeting.class);
            when(meetingDao.removeAttendees("event-123", List.of("attendee@example.com"))).thenReturn(meeting);
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.removeAttendee("event-123", List.of("attendee@example.com"), true);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testRemoveAttendee_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("removeAttendee")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new java.util.HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.removeAttendee("event-123", List.of("attendee@example.com"), true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testRemoveAttendee_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("removeAttendee")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.removeAttendee(null, null, false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testRemoveAttendee_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("removeAttendee")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.removeAttendees(anyString(), anyList())).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.removeAttendee("event-123", List.of("attendee@example.com"), true);
    }

    @Test
    public void testRemoveAttendee_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("removeAttendee")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.removeAttendees(anyString(), anyList())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.removeAttendee("event-123", List.of("attendee@example.com"), true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getFreeSlots
    // ========================================================================

    @Test
    public void testGetFreeSlots_Success() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getFreeSlots")))
                .thenReturn(Collections.emptyList());

        when(meetingDao.getFreeSlot(anyLong(), anyLong(), anyBoolean(), anyList(), anyList(), anyString(), anyString()))
                .thenReturn(java.util.Collections.emptyList());

        ExtensionResponse response = meetingsArea.getFreeSlots(1L, 2L, false, List.of("req@example.com"), List.of(), "UTC", "60.0", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testGetFreeSlots_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getFreeSlots")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new java.util.HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getFreeSlots(1L, 2L, false, List.of("req@example.com"), List.of(), "UTC", "60.0", true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGetFreeSlots_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getFreeSlots")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getFreeSlots(null, null, null, null, null, null, null, false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetFreeSlots_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getFreeSlots")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getFreeSlot(anyLong(), anyLong(), anyBoolean(), anyList(), anyList(), anyString(), anyString()))
                .thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.getFreeSlots(1L, 2L, false, List.of("req@example.com"), List.of(), "UTC", "60.0", true);
    }

    @Test
    public void testGetFreeSlots_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getFreeSlots")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getFreeSlot(anyLong(), anyLong(), anyBoolean(), anyList(), anyList(), anyString(), anyString()))
                .thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.getFreeSlots(1L, 2L, false, List.of("req@example.com"), List.of(), "UTC", "60.0", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getSchedules
    // ========================================================================

    @Test
    public void testGetSchedules_Success() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getSchedules")))
                .thenReturn(Collections.emptyList());

        when(meetingDao.getSchedules(anyLong(), anyLong(), anyList(), anyString()))
                .thenReturn(java.util.Collections.emptyList());

        ExtensionResponse response = meetingsArea.getSchedules(1L, 2L, List.of("req@example.com"), "UTC", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testGetSchedules_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getSchedules")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new java.util.HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getSchedules(1L, 2L, List.of("req@example.com"), "UTC", true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGetSchedules_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getSchedules")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getSchedules(null, null, null, null, false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetSchedules_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getSchedules")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getSchedules(anyLong(), anyLong(), anyList(), anyString()))
                .thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.getSchedules(1L, 2L, List.of("req@example.com"), "UTC", true);
    }

    @Test
    public void testGetSchedules_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getSchedules")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getSchedules(anyLong(), anyLong(), anyList(), anyString()))
                .thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.getSchedules(1L, 2L, List.of("req@example.com"), "UTC", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getEvent
    // ========================================================================

    @Test
    public void testGetEvent_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getEvent")))
                    .thenReturn(Collections.emptyList());

            Meeting meeting = mock(Meeting.class);
            when(meetingDao.getMeetingById("event-123")).thenReturn(meeting);
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.getEvent("event-123", true);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testGetEvent_Success_NullMeeting() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getEvent")))
                .thenReturn(Collections.emptyList());

        when(meetingDao.getMeetingById("event-123")).thenReturn(null);
        ExtensionResponse response = meetingsArea.getEvent("event-123", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testGetEvent_ValidationError() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getEvent")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new java.util.HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getEvent("event-123", true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGetEvent_AllowRetryFalse() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            Meeting meeting = mock(Meeting.class);
            when(meetingDao.getMeetingById("event-123")).thenReturn(meeting);
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.getEvent("event-123", false);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetEvent_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getEvent")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getMeetingById(anyString())).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.getEvent("event-123", true);
    }

    @Test
    public void testGetEvent_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getEvent")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getMeetingById(anyString())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.getEvent("event-123", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // searchEvents
    // ========================================================================

    @Test
    public void testSearchEvents_Success() throws Exception {
        when(meetingDao.getQueryResults(anyString(), any(), any())).thenReturn(java.util.Collections.emptyList());
        List<Map<String, Object>> response = meetingsArea.searchEvents("query", null, null);
        assertNotNull(response);
    }

    @Test
    public void testSearchEvents_WithDateRange_Success() throws Exception {
        when(meetingDao.getQueryResults(anyString(), any(), any())).thenReturn(java.util.Collections.emptyList());
        long from = System.currentTimeMillis();
        long to = from + 86_400_000L;
        List<Map<String, Object>> response = meetingsArea.searchEvents("MBL", from, to);
        assertNotNull(response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testSearchEvents_MustAuthorizeException() throws Exception {
        when(meetingDao.getQueryResults(anyString(), any(), any())).thenThrow(new MustAuthorizeException("auth"));
        meetingsArea.searchEvents("query", null, null);
    }

    @Test(expected = Exception.class)
    public void testSearchEvents_GeneralException() throws Exception {
        when(meetingDao.getQueryResults(anyString(), any(), any())).thenThrow(new RuntimeException("error"));
        meetingsArea.searchEvents("query", null, null);
    }

    // ========================================================================
    // getEventsForAGivenUser
    // ========================================================================

    @Test
    public void testGetEventsForAGivenUser_Success() throws Exception {
        try (MockedStatic<CatalogTypes> catalogTypesMockedStatic = mockStatic(CatalogTypes.class)) {
            when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getEventsForAGivenUser")))
                    .thenReturn(Collections.emptyList());

            Meeting meeting = mock(Meeting.class);
            when(meetingDao.getEventsForUser("user@example.com", 1L, 2L)).thenReturn(List.of(meeting));
            
            EventDetails details = new EventDetails();
            catalogTypesMockedStatic.when(() -> CatalogTypes.fromEvent(meeting)).thenReturn(details);

            ExtensionResponse response = meetingsArea.getEventsForAGivenUser("user@example.com", 1L, 2L, true);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testGetEventsForAGivenUser_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getEventsForAGivenUser")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new java.util.HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getEventsForAGivenUser("user@example.com", 1L, 2L, true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGetEventsForAGivenUser_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getEventsForAGivenUser")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getEventsForAGivenUser(null, null, null, false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetEventsForAGivenUser_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getEventsForAGivenUser")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getEventsForUser(anyString(), anyLong(), anyLong())).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.getEventsForAGivenUser("user@example.com", 1L, 2L, true);
    }

    @Test
    public void testGetEventsForAGivenUser_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getEventsForAGivenUser")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getEventsForUser(anyString(), anyLong(), anyLong())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.getEventsForAGivenUser("user@example.com", 1L, 2L, true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getAttachmentsForAGivenUser
    // ========================================================================

    @Test
    public void testGetAttachmentsForAGivenUser_Success() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getAttachmentsForAGivenUser")))
                .thenReturn(Collections.emptyList());

        when(meetingDao.getAttachmentsForAuthenticatedUser("event-123")).thenReturn(java.util.Collections.emptyList());
        when(catalogTypes.toKristaFilList(anyList())).thenReturn(java.util.Collections.emptyList());

        ExtensionResponse response = meetingsArea.getAttachmentsForAGivenUser("user@example.com", "event-123", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testGetAttachmentsForAGivenUser_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getAttachmentsForAGivenUser")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new java.util.HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getAttachmentsForAGivenUser("user@example.com", "event-123", true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGetAttachmentsForAGivenUser_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getAttachmentsForAGivenUser")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getAttachmentsForAGivenUser(null, null, false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetAttachmentsForAGivenUser_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getAttachmentsForAGivenUser")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getAttachmentsForAuthenticatedUser(anyString())).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.getAttachmentsForAGivenUser("user@example.com", "event-123", true);
    }

    @Test
    public void testGetAttachmentsForAGivenUser_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getAttachmentsForAGivenUser")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getAttachmentsForAuthenticatedUser(anyString())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.getAttachmentsForAGivenUser("user@example.com", "event-123", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getAttachments
    // ========================================================================

    @Test
    public void testGetAttachments_Success() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getAttachments")))
                .thenReturn(Collections.emptyList());

        when(meetingDao.getAttachmentsForAuthenticatedUser("event-123")).thenReturn(java.util.Collections.emptyList());
        when(catalogTypes.toKristaFilList(anyList())).thenReturn(java.util.Collections.emptyList());

        ExtensionResponse response = meetingsArea.getAttachments("event-123", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testGetAttachments_ValidationError_AllowRetryTrue() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getAttachments")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new java.util.HashMap<>()).build();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getAttachments("event-123", true);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGetAttachments_ValidationError_AllowRetryFalse() throws Exception {
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "code", "err", new java.util.HashMap<>());
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getAttachments")))
                .thenReturn(List.of(vr));

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateValidationErrorResponse(anyList())).thenReturn(expectedResponse);

        ExtensionResponse response = meetingsArea.getAttachments(null, false);
        assertEquals(expectedResponse, response);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetAttachments_MustAuthorizeException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getAttachments")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getAttachmentsForAuthenticatedUser(anyString())).thenThrow(new MustAuthorizeException("auth"));

        meetingsArea.getAttachments("event-123", true);
    }

    @Test
    public void testGetAttachments_GeneralException() throws Exception {
        when(validationOrchestrator.validate(anyMap(), eq(MeetingsArea.class), eq("getAttachments")))
                .thenReturn(Collections.emptyList());
        when(meetingDao.getAttachmentsForAuthenticatedUser(anyString())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.getAttachments("event-123", true);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // checkPublicAuthExpiry
    // ========================================================================

    @Test
    public void testCheckPublicAuthExpiry_NotConfigured() throws Exception {
        when(meetingDao.loadPublicConfig()).thenReturn(null);

        ExtensionResponse response = meetingsArea.checkPublicAuthExpiry();

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testCheckPublicAuthExpiry_ValidForMoreThanMonth() throws Exception {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MONTH, 2);
        String dateStr = new java.text.SimpleDateFormat("M/d/yyyy").format(cal.getTime());

        when(meetingDao.loadPublicConfig()).thenReturn(dateStr);

        ExtensionResponse response = meetingsArea.checkPublicAuthExpiry();

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testCheckPublicAuthExpiry_ExpiresWithinMonth() throws Exception {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, 5);
        String dateStr = new java.text.SimpleDateFormat("M/d/yyyy").format(cal.getTime());

        when(meetingDao.loadPublicConfig()).thenReturn(dateStr);

        ExtensionResponse response = meetingsArea.checkPublicAuthExpiry();

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testCheckPublicAuthExpiry_Exception() throws Exception {
        when(meetingDao.loadPublicConfig()).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = meetingsArea.checkPublicAuthExpiry();

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

}
