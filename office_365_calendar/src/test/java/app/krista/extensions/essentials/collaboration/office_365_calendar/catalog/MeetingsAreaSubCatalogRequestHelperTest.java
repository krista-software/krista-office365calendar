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
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MeetingsAreaSubCatalogRequestHelperTest {

    @Mock private MeetingDaoImpl meetingDao;
    @Mock private CatalogTypes catalogTypes;
    @Mock private ErrorHandlingStateManager internalStateManager;
    @Mock private ExtensionResponseGenerator responseGenerator;

    private MeetingsAreaSubCatalogRequestHelper helper;

    @Before
    public void setUp() {
        helper = new MeetingsAreaSubCatalogRequestHelper(meetingDao, catalogTypes, internalStateManager, responseGenerator);
    }

    private Meeting createMockMeeting() {
        return mock(Meeting.class);
    }

    // ========================================================================
    // handleReEnterGetUpcomingMeetings
    // ========================================================================

    @Test
    public void testHandleReEnterGetUpcomingMeetings_Success() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.NUMBER_OF_MEETINGS, 3);

        List<Meeting> meetings = List.of(createMockMeeting(), createMockMeeting(), createMockMeeting());
        when(meetingDao.getMeetings(3)).thenReturn(meetings);

        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReEnterGetUpcomingMeetings(map);

            assertNotNull("Response should not be null", response);
            assertEquals("Response should be successful", ExtensionResponse.Result.SUCCESS, response.getResult());

            Map<String, Object> data = response.getResponseValue();
            assertNotNull("Response data should not be null", data);
            assertTrue("Response should contain 'Event Details'", data.containsKey("Event Details"));

            @SuppressWarnings("unchecked")
            List<EventDetails> events = (List<EventDetails>) data.get("Event Details");
            assertEquals("Should have 3 events", 3, events.size());
        }
    }

    @Test
    public void testHandleReEnterGetUpcomingMeetings_SuccessWithDouble() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.NUMBER_OF_MEETINGS, 2.0);

        List<Meeting> meetings = List.of(createMockMeeting(), createMockMeeting());
        when(meetingDao.getMeetings(2)).thenReturn(meetings);

        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReEnterGetUpcomingMeetings(map);

            assertNotNull("Response should not be null", response);
            assertEquals("Response should be successful", ExtensionResponse.Result.SUCCESS, response.getResult());

            Map<String, Object> data = response.getResponseValue();
            @SuppressWarnings("unchecked")
            List<EventDetails> events = (List<EventDetails>) data.get("Event Details");
            assertEquals("Should have 2 events", 2, events.size());
        }
    }

    @Test
    public void testHandleReEnterGetUpcomingMeetings_EmptyMeetingsList() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.NUMBER_OF_MEETINGS, 5);

        when(meetingDao.getMeetings(5)).thenReturn(Collections.emptyList());

        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            ExtensionResponse response = helper.handleReEnterGetUpcomingMeetings(map);

            assertNotNull("Response should not be null", response);
            assertEquals("Response should be successful", ExtensionResponse.Result.SUCCESS, response.getResult());

            Map<String, Object> data = response.getResponseValue();
            @SuppressWarnings("unchecked")
            List<EventDetails> events = (List<EventDetails>) data.get("Event Details");
            assertNotNull("Event Details list should not be null", events);
            assertTrue("Event Details list should be empty", events.isEmpty());
        }
    }

    @Test
    public void testHandleReEnterGetUpcomingMeetings_NullNumberOfMeetings() {
        Map<String, Object> map = new HashMap<>();
        // key missing — map.get(Constants.NUMBER_OF_MEETINGS) returns null

        ExtensionResponse response = helper.handleReEnterGetUpcomingMeetings(map);

        assertNotNull("Response should not be null", response);
        assertEquals("Response should be failure", ExtensionResponse.Result.FAILURE, response.getResult());
    }

    @Test
    public void testHandleReEnterGetUpcomingMeetings_ZeroNumberOfMeetings() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.NUMBER_OF_MEETINGS, 0);

        ExtensionResponse response = helper.handleReEnterGetUpcomingMeetings(map);

        assertNotNull("Response should not be null", response);
        assertEquals("Response should be failure", ExtensionResponse.Result.FAILURE, response.getResult());
    }

    @Test
    public void testHandleReEnterGetUpcomingMeetings_NegativeNumberOfMeetings() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.NUMBER_OF_MEETINGS, -1);

        ExtensionResponse response = helper.handleReEnterGetUpcomingMeetings(map);

        assertNotNull("Response should not be null", response);
        assertEquals("Response should be failure", ExtensionResponse.Result.FAILURE, response.getResult());
    }

    @Test
    public void testHandleReEnterGetUpcomingMeetings_NonNumberValue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.NUMBER_OF_MEETINGS, "abc");

        ExtensionResponse response = helper.handleReEnterGetUpcomingMeetings(map);

        assertNotNull("Response should not be null", response);
        assertEquals("Response should be failure", ExtensionResponse.Result.FAILURE, response.getResult());
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReEnterGetUpcomingMeetings_MustAuthorizeException() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.NUMBER_OF_MEETINGS, 3);

        when(meetingDao.getMeetings(3)).thenThrow(new MustAuthorizeException("auth required"));

        helper.handleReEnterGetUpcomingMeetings(map);
    }

    @Test
    public void testHandleReEnterGetUpcomingMeetings_DaoThrowsRuntimeException() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.NUMBER_OF_MEETINGS, 3);

        when(meetingDao.getMeetings(3)).thenThrow(new RuntimeException("connection failed"));

        ExtensionResponse response = helper.handleReEnterGetUpcomingMeetings(map);

        assertNotNull("Response should not be null", response);
        assertEquals("Response should be failure", ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // confirmReenterGetUpcomingMeetings
    // ========================================================================

    @Test
    public void testConfirmReenterGetUpcomingMeetings_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());

        ExtensionResponse response = helper.confirmReenterGetUpcomingMeetings(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    @Test
    public void testConfirmReenterGetUpcomingMeetings_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_RE_ENTER_GET_UPCOMING_MEETINGS), eq(map))).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetUpcomingMeetings(map);

        assertEquals(expectedResponse, response);
    }

    // ========================================================================
    // getAttachmentsForAGivenUser
    // ========================================================================

    @Test
    public void testConfirmReenterGetAttachmentsForAGivenUser_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());

        ExtensionResponse response = helper.confirmReenterGetAttachmentsForAGivenUser(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
        assertNotNull(response.getError());
    }

    @Test
    public void testConfirmReenterGetAttachmentsForAGivenUser_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");
        map.put(Constants.EMAIL, "test@test.com");
        map.put(Constants.EVENT_ID, "event-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_RE_ENTER_GET_ATTACHMENT_FOR_GIVEN_USER), any())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetAttachmentsForAGivenUser(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReEnterGetAttachmentForGivenUser_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EMAIL, "test@test.com");
        map.put(Constants.EVENT_ID, "event-123");

        List<com.microsoft.graph.models.FileAttachment> mockAttachments = new ArrayList<>();
        when(meetingDao.getAttachmentsForGivenUser("test@test.com", "event-123")).thenReturn(mockAttachments);
        when(catalogTypes.toKristaFilList(mockAttachments)).thenReturn(new ArrayList<>());

        ExtensionResponse response = helper.handleReEnterGetAttachmentForGivenUser(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReEnterGetAttachmentForGivenUser_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EMAIL, "test@test.com");
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.getAttachmentsForGivenUser("test@test.com", "event-123")).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReEnterGetAttachmentForGivenUser(map);
    }

    @Test
    public void testHandleReEnterGetAttachmentForGivenUser_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EMAIL, "test@test.com");
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.getAttachmentsForGivenUser("test@test.com", "event-123")).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReEnterGetAttachmentForGivenUser(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // cancelEvent
    // ========================================================================

    @Test
    public void testConfirmReenterCancelEvent_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());

        ExtensionResponse response = helper.confirmReenterCancelEvent(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    @Test
    public void testConfirmReenterCancelEvent_ReEnterTrueWithValidationResults() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        List<Object> validationResultsList = new ArrayList<>();
        Map<String, Object> validationObj = new HashMap<>();
        validationObj.put("field", "someField");
        validationObj.put("message", "someMessage");
        validationObj.put("isSuccess", true);
        validationResultsList.add(validationObj);
        validationResultsList.add(null);
        state.put(Constants.VALIDATION_RESULTS, validationResultsList);
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_RE_ENTER_CANCEL_EVENT), eq(map))).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterCancelEvent(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterCancelEvent_SuccessWithDelegate() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.DELEGATE_EMAIL, "delegate@test.com");
        when(internalStateManager.get("state-123")).thenReturn(state);

        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.cancelMeeting("event-123", "delegate@test.com")).thenReturn(mockMeeting);

        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterCancelEvent(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test
    public void testHandleReenterCancelEvent_SuccessWithoutDelegate() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.cancelMeeting("event-123")).thenReturn(mockMeeting);

        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterCancelEvent(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterCancelEvent_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.cancelMeeting("event-123")).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterCancelEvent(map);
    }

    @Test
    public void testHandleReenterCancelEvent_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.cancelMeeting("event-123")).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterCancelEvent(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getEvent
    // ========================================================================

    @Test
    public void testConfirmReenterGetEvent_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());

        ExtensionResponse response = helper.confirmReenterGetEvent(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    @Test
    public void testConfirmReenterGetEvent_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_GET_EVENT), eq(map))).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetEvent(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterGetEvent_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID_FIELD, "event-123");

        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.getMeetingById("event-123")).thenReturn(mockMeeting);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterGetEvent(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterGetEvent_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID_FIELD, "event-123");

        when(meetingDao.getMeetingById("event-123")).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterGetEvent(map);
    }

    @Test
    public void testHandleReenterGetEvent_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID_FIELD, "event-123");

        when(meetingDao.getMeetingById("event-123")).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterGetEvent(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // declineInvitation
    // ========================================================================

    @Test
    public void testConfirmReenterDeclineInvitation_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterDeclineInvitation(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterDeclineInvitation_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");
        map.put(Constants.EVENT_ID, "event-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_DECLINE_INVITATION), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterDeclineInvitation(map);

        assertEquals(expectedResponse, response);
    }
    
    @Test
    public void testConfirmReenterDeclineInvitation_ReEnterTrue_NullEventId() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");
        map.put(Constants.EVENT_ID, null);

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_DECLINE_INVITATION), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterDeclineInvitation(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterDeclineInvitation_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.declineMeetingInvitation("event-123")).thenReturn(mockMeeting);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterDeclineInvitation(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterDeclineInvitation_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.declineMeetingInvitation("event-123")).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterDeclineInvitation(map);
    }

    @Test
    public void testHandleReenterDeclineInvitation_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.declineMeetingInvitation("event-123")).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterDeclineInvitation(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getSchedules
    // ========================================================================

    @Test
    public void testConfirmReenterGetSchedules_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetSchedules(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterGetSchedules_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");
        map.put(Constants.FROM_DATE, 12345L);
        map.put(Constants.TO_DATE, 67890L);
        map.put(Constants.ATTENDEES, List.of("test@test.com"));
        map.put(Constants.TIME_ZONE, "UTC");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_GET_SCHEDULES), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetSchedules(map);

        assertEquals(expectedResponse, response);
    }
    
    @Test
    public void testConfirmReenterGetSchedules_ReEnterTrue_Nulls() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_GET_SCHEDULES), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetSchedules(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterGetSchedules_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.FROM_DATE, 12345L);
        map.put(Constants.TO_DATE, 67890L);
        map.put(Constants.ATTENDEES, List.of("test@test.com"));
        map.put(Constants.TIME_ZONE, "UTC");

        List<Map<String, Object>> mockSchedules = new ArrayList<>();
        Map<String, Object> scheduleMap = new HashMap<>();
        scheduleMap.put("someKey", "someValue");
        mockSchedules.add(scheduleMap);
        when(meetingDao.getSchedules(12345L, 67890L, List.of("test@test.com"), "UTC")).thenReturn(mockSchedules);

        ExtensionResponse response = helper.handleReenterGetSchedules(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }
    
    @Test
    public void testHandleReenterGetSchedules_Success_DifferentTypes() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.FROM_DATE, "not a number");
        map.put(Constants.TO_DATE, "not a number");
        map.put(Constants.ATTENDEES, "not a list");
        map.put(Constants.TIME_ZONE, "UTC");

        List<Map<String, Object>> mockSchedules = new ArrayList<>();
        when(meetingDao.getSchedules(null, null, null, "UTC")).thenReturn(mockSchedules);

        ExtensionResponse response = helper.handleReenterGetSchedules(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterGetSchedules_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.FROM_DATE, 12345L);

        when(meetingDao.getSchedules(12345L, null, null, null)).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterGetSchedules(map);
    }

    @Test
    public void testHandleReenterGetSchedules_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.FROM_DATE, 12345L);

        when(meetingDao.getSchedules(12345L, null, null, null)).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterGetSchedules(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getFreeSlots
    // ========================================================================

    @Test
    public void testConfirmReenterGetFreeSlots_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetFreeSlots(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterGetFreeSlots_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");
        map.put(Constants.FROM_DATE, 12345L);
        map.put(Constants.TO_DATE, 67890L);
        map.put(Constants.IS_ORGANIZER_OPTIONAL, true);
        map.put(Constants.REQUIRED_ATTENDEE, List.of("req@test.com"));
        map.put(Constants.OPTIONAL_ATTENDEE, List.of("opt@test.com"));
        map.put(Constants.TIME_ZONE, "UTC");
        map.put(Constants.MEETING_DURATION, "PT1H");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_GET_FREE_SLOTS), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetFreeSlots(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterGetFreeSlots_ReEnterTrue_Nulls() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_GET_FREE_SLOTS), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetFreeSlots(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterGetFreeSlots_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.FROM_DATE, 12345L);
        map.put(Constants.TO_DATE, 67890L);
        map.put(Constants.IS_ORGANIZER_OPTIONAL, true);
        map.put(Constants.REQUIRED_ATTENDEE, List.of("req@test.com"));
        map.put(Constants.OPTIONAL_ATTENDEE, List.of("opt@test.com"));
        map.put(Constants.TIME_ZONE, "UTC");
        map.put(Constants.MEETING_DURATION, "PT1H");

        List<Map<String, Object>> mockSlots = new ArrayList<>();
        Map<String, Object> slotMap = new HashMap<>();
        slotMap.put("someKey", "someValue");
        mockSlots.add(slotMap);
        when(meetingDao.getFreeSlot(12345L, 67890L, true, List.of("req@test.com"), List.of("opt@test.com"), "UTC", "PT1H")).thenReturn(mockSlots);

        ExtensionResponse response = helper.handleReenterGetFreeSlots(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testHandleReenterGetFreeSlots_Success_DifferentTypes() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.FROM_DATE, "not number");
        map.put(Constants.TO_DATE, "not number");
        map.put(Constants.IS_ORGANIZER_OPTIONAL, "not boolean");
        map.put(Constants.REQUIRED_ATTENDEE, "not list");
        map.put(Constants.OPTIONAL_ATTENDEE, "not list");
        map.put(Constants.TIME_ZONE, "UTC");
        map.put(Constants.MEETING_DURATION, "PT1H");

        List<Map<String, Object>> mockSlots = new ArrayList<>();
        when(meetingDao.getFreeSlot(null, null, null, null, null, "UTC", "PT1H")).thenReturn(mockSlots);

        ExtensionResponse response = helper.handleReenterGetFreeSlots(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterGetFreeSlots_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.FROM_DATE, 12345L);

        when(meetingDao.getFreeSlot(12345L, null, null, null, null, null, null)).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterGetFreeSlots(map);
    }

    @Test
    public void testHandleReenterGetFreeSlots_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.FROM_DATE, 12345L);

        when(meetingDao.getFreeSlot(12345L, null, null, null, null, null, null)).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterGetFreeSlots(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // forwardEvent
    // ========================================================================

    @Test
    public void testConfirmReenterForwardEvent_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterForwardEvent(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterForwardEvent_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");
        map.put(Constants.EVENT_ID, "event-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_FORWARD_EVENT), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterForwardEvent(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterForwardEvent_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");
        map.put(Constants.ATTENDEE_EMAIL, List.of("test@test.com"));

        ExtensionResponse response = helper.handleReenterForwardEvent(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        verify(meetingDao).forwardMeeting("event-123", List.of("test@test.com"));
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterForwardEvent_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");
        map.put(Constants.ATTENDEE_EMAIL, List.of("test@test.com"));

        doThrow(new MustAuthorizeException("auth")).when(meetingDao).forwardMeeting("event-123", List.of("test@test.com"));

        helper.handleReenterForwardEvent(map);
    }

    @Test
    public void testHandleReenterForwardEvent_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");
        map.put(Constants.ATTENDEE_EMAIL, List.of("test@test.com"));

        doThrow(new RuntimeException("error")).when(meetingDao).forwardMeeting("event-123", List.of("test@test.com"));

        ExtensionResponse response = helper.handleReenterForwardEvent(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // acceptInvitation
    // ========================================================================

    @Test
    public void testConfirmReenterAcceptInvitation_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterAcceptInvitation(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterAcceptInvitation_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_ACCEPT_INVITATION), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterAcceptInvitation(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterAcceptInvitation_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.acceptMeetingInvitation("event-123")).thenReturn(mockMeeting);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterAcceptInvitation(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterAcceptInvitation_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.acceptMeetingInvitation("event-123")).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterAcceptInvitation(map);
    }

    @Test
    public void testHandleReenterAcceptInvitation_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.acceptMeetingInvitation("event-123")).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterAcceptInvitation(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // tentativeInvitation
    // ========================================================================

    @Test
    public void testConfirmReenterTentativeInvitation_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterTentativeInvitation(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterTentativeInvitation_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_TENTATIVE_INVITATION), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterTentativeInvitation(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterTentativeInvitation_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.tentativeMeetingInvitation("event-123")).thenReturn(mockMeeting);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterTentativeInvitation(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterTentativeInvitation_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.tentativeMeetingInvitation("event-123")).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterTentativeInvitation(map);
    }

    @Test
    public void testHandleReenterTentativeInvitation_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.tentativeMeetingInvitation("event-123")).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterTentativeInvitation(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // scheduleMeeting
    // ========================================================================

    @Test
    public void testConfirmReenterScheduleMeeting_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterScheduleMeeting(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterScheduleMeeting_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_SCHEDULE_MEETING), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterScheduleMeeting(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterScheduleMeeting_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.TITLE, "Meeting");
        map.put(Constants.LOCATION, "Room 1");
        map.put(Constants.IS_ALL_DAY, false);
        map.put(Constants.TIME_ZONE, "UTC");
        
        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.addMeeting(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(mockMeeting);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterScheduleMeeting(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }
    
    @Test
    public void testHandleReenterScheduleMeeting_DelegatedSuccess() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.STATE_ID, "state-123");
        map.put(Constants.TITLE, "Meeting");
        
        Map<String, Object> state = new HashMap<>();
        state.put(Constants.ORGANIZER_EMAIL, "org@test.com");
        when(internalStateManager.get("state-123")).thenReturn(state);

        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.addMeeting(
                anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(mockMeeting);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterScheduleMeeting(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterScheduleMeeting_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.TITLE, "Meeting");

        when(meetingDao.addMeeting(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterScheduleMeeting(map);
    }

    @Test
    public void testHandleReenterScheduleMeeting_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.TITLE, "Meeting");

        when(meetingDao.addMeeting(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterScheduleMeeting(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // addAttendees
    // ========================================================================

    @Test
    public void testConfirmReenterAddAttendees_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterAddAttendees(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterAddAttendees_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_ADD_ATTENDEES), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterAddAttendees(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterAddAttendees_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");
        map.put(Constants.REQUIRED_ATTENDEE_EMAIL, List.of("req@test.com"));
        map.put(Constants.OPTIONAL_ATTENDEE_EMAIL, List.of("opt@test.com"));

        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.addAttendees("event-123", List.of("req@test.com"), List.of("opt@test.com"))).thenReturn(mockMeeting);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterAddAttendees(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterAddAttendees_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.addAttendees(any(), any(), any())).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterAddAttendees(map);
    }

    @Test
    public void testHandleReenterAddAttendees_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.addAttendees(any(), any(), any())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterAddAttendees(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // removeAttendee
    // ========================================================================

    @Test
    public void testConfirmReenterRemoveAttendee_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterRemoveAttendee(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterRemoveAttendee_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_REMOVE_ATTENDEE), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterRemoveAttendee(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterRemoveAttendee_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");
        map.put(Constants.ATTENDEE_EMAIL, List.of("test@test.com"));

        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.removeAttendees("event-123", List.of("test@test.com"))).thenReturn(mockMeeting);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterRemoveAttendee(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterRemoveAttendee_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.removeAttendees(any(), any())).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterRemoveAttendee(map);
    }

    @Test
    public void testHandleReenterRemoveAttendee_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.removeAttendees(any(), any())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterRemoveAttendee(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // updateEvent
    // ========================================================================

    @Test
    public void testConfirmReenterUpdateEvent_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterUpdateEvent(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterUpdateEvent_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_UPDATE_EVENT), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterUpdateEvent(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterUpdateEvent_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");
        map.put(Constants.TITLE, "New Title");

        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.updateMeeting(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(mockMeeting);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterUpdateEvent(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }
    
    @Test
    public void testHandleReenterUpdateEvent_DelegatedSuccess() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.STATE_ID, "state-123");
        map.put(Constants.EVENT_ID, "event-123");
        map.put(Constants.TITLE, "New Title");
        
        Map<String, Object> state = new HashMap<>();
        state.put(Constants.ORGANIZER_EMAIL, "org@test.com");
        when(internalStateManager.get("state-123")).thenReturn(state);

        Meeting mockMeeting = createMockMeeting();
        when(meetingDao.updateMeeting(
                anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(mockMeeting);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterUpdateEvent(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterUpdateEvent_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.updateMeeting(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterUpdateEvent(map);
    }

    @Test
    public void testHandleReenterUpdateEvent_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID, "event-123");

        when(meetingDao.updateMeeting(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterUpdateEvent(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getNextUpcomingMeetings
    // ========================================================================

    @Test
    public void testConfirmReenterGetNextUpcomingMeetings_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetNextUpcomingMeetings(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterGetNextUpcomingMeetings_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_GET_NEXT_UPCOMING_MEETINGS), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetNextUpcomingMeetings(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterGetNextUpcomingMeetings_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.NUMBER_OF_MEETING, 5);

        List<Meeting> mockMeetings = List.of(createMockMeeting());
        when(meetingDao.getMeetings(5)).thenReturn(mockMeetings);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterGetNextUpcomingMeetings(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterGetNextUpcomingMeetings_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.NUMBER_OF_MEETING, 5);

        when(meetingDao.getMeetings(5)).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterGetNextUpcomingMeetings(map);
    }

    @Test
    public void testHandleReenterGetNextUpcomingMeetings_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.NUMBER_OF_MEETING, 5);

        when(meetingDao.getMeetings(5)).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterGetNextUpcomingMeetings(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getEventsForUser
    // ========================================================================

    @Test
    public void testConfirmReenterGetEventsForUser_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetEventsForUser(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterGetEventsForUser_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_GET_EVENTS_FOR_USER), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetEventsForUser(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterGetEventsForUser_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.USER_EMAIL, "test@test.com");
        map.put(Constants.FROM, 12345L);
        map.put(Constants.TO, 67890L);

        List<Meeting> mockMeetings = List.of(createMockMeeting());
        when(meetingDao.getEventsForUser("test@test.com", 12345L, 67890L)).thenReturn(mockMeetings);
        
        try (MockedStatic<CatalogTypes> mockedStatic = mockStatic(CatalogTypes.class)) {
            mockedStatic.when(() -> CatalogTypes.fromEvent(any(Meeting.class))).thenReturn(new EventDetails());

            ExtensionResponse response = helper.handleReenterGetEventsForUser(map);

            assertNotNull(response);
            assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterGetEventsForUser_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.USER_EMAIL, "test@test.com");

        when(meetingDao.getEventsForUser(any(), any(), any())).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterGetEventsForUser(map);
    }

    @Test
    public void testHandleReenterGetEventsForUser_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.USER_EMAIL, "test@test.com");

        when(meetingDao.getEventsForUser(any(), any(), any())).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterGetEventsForUser(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getAttachmentsForUser
    // ========================================================================

    @Test
    public void testConfirmReenterGetAttachmentsForUser_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetAttachmentsForUser(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterGetAttachmentsForUser_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_GET_ATTACHMENTS_FOR_USER), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetAttachmentsForUser(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterGetAttachmentsForUser_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID_FIELD, "event-123");

        List<com.microsoft.graph.models.FileAttachment> mockAttachments = new ArrayList<>();
        when(meetingDao.getAttachmentsForAuthenticatedUser("event-123")).thenReturn(mockAttachments);
        when(catalogTypes.toKristaFilList(mockAttachments)).thenReturn(new ArrayList<>());

        ExtensionResponse response = helper.handleReenterGetAttachmentsForUser(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterGetAttachmentsForUser_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID_FIELD, "event-123");

        when(meetingDao.getAttachmentsForAuthenticatedUser("event-123")).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterGetAttachmentsForUser(map);
    }

    @Test
    public void testHandleReenterGetAttachmentsForUser_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID_FIELD, "event-123");

        when(meetingDao.getAttachmentsForAuthenticatedUser("event-123")).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterGetAttachmentsForUser(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    // ========================================================================
    // getAttachments
    // ========================================================================

    @Test
    public void testConfirmReenterGetAttachments_ReEnterFalse() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, false);
        map.put(Constants.STATE_ID, "state-123");

        when(internalStateManager.get("state-123")).thenReturn(new HashMap<>());
        
        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().failure(new ExtensionResponse.Error("err", 1L, ExtensionResponse.Error.ExceptionType.INPUT_ERROR, "")).build();
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetAttachments(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testConfirmReenterGetAttachments_ReEnterTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.RE_ENTER, true);
        map.put(Constants.STATE_ID, "state-123");

        Map<String, Object> state = new HashMap<>();
        state.put(Constants.VALIDATION_RESULTS, Collections.emptyList());
        when(internalStateManager.get("state-123")).thenReturn(state);

        ExtensionResponse expectedResponse = new ExtensionResponseBuilder().success(new HashMap<>()).build();
        when(responseGenerator.generateFetchResponse(any(), any(), eq(Constants.HANDLE_REENTER_GET_ATTACHMENTS), anyMap())).thenReturn(expectedResponse);

        ExtensionResponse response = helper.confirmReenterGetAttachments(map);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleReenterGetAttachments_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID_FIELD, "event-123");

        List<com.microsoft.graph.models.FileAttachment> mockAttachments = new ArrayList<>();
        when(meetingDao.getAttachmentsForAuthenticatedUser("event-123")).thenReturn(mockAttachments);
        when(catalogTypes.toKristaFilList(mockAttachments)).thenReturn(new ArrayList<>());

        ExtensionResponse response = helper.handleReenterGetAttachments(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test(expected = MustAuthorizeException.class)
    public void testHandleReenterGetAttachments_MustAuthorizeException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID_FIELD, "event-123");

        when(meetingDao.getAttachmentsForAuthenticatedUser("event-123")).thenThrow(new MustAuthorizeException("auth"));

        helper.handleReenterGetAttachments(map);
    }

    @Test
    public void testHandleReenterGetAttachments_GeneralException() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.EVENT_ID_FIELD, "event-123");

        when(meetingDao.getAttachmentsForAuthenticatedUser("event-123")).thenThrow(new RuntimeException("error"));

        ExtensionResponse response = helper.handleReenterGetAttachments(map);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }
}

