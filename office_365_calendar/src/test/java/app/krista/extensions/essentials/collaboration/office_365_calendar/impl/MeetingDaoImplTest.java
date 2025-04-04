package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;


import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import com.microsoft.graph.options.Option;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class MeetingDaoImplTest {

    MeetingDaoImpl meetingDaoImpl;

    @InjectMocks
    GraphServiceClientProviderFactory graphServiceClientProviderFactory;

    @Mock
    Office365CalendarAttributes office365CalendarAttributes;

    @Before
    public void setUp() {
        meetingDaoImpl = new MeetingDaoImpl(graphServiceClientProviderFactory, office365CalendarAttributes);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetMeetingByInvalidId() throws IOException {
        meetingDaoImpl.getMeetingById("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet0Meetings() {
        meetingDaoImpl.getMeetings(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSchedulesForInvalidText() throws IOException {
        meetingDaoImpl.getSchedules(123224324522L, 12123123123L, List.of(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFreeSlot() throws DatatypeConfigurationException {
        Long dateFrom = 11111111111L;
        meetingDaoImpl.getFreeSlot(dateFrom, dateFrom, null, List.of(), List.of(), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMeeting() {
        Long dateFrom = 11111111111L;
        Long dateTo = 111111111L;
        meetingDaoImpl.addMeeting(null, null, dateFrom, dateTo, List.of(), List.of(), null, null, null, null, null, List.of(), null, null, null, null, null, null, null, null, List.of(), null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcceptMeetingInvitation() {
        meetingDaoImpl.acceptMeetingInvitation(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeclineMeetingInvitation() {
        meetingDaoImpl.declineMeetingInvitation(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTentativeMeetingInvitation() {
        meetingDaoImpl.tentativeMeetingInvitation(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCancelMeeting() {
        meetingDaoImpl.cancelMeeting(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttendees() {
        List<String> requiredAttendees = List.of();
        List<String> optionalAttendees = List.of();
        meetingDaoImpl.addAttendees(null, requiredAttendees, optionalAttendees);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttendees() {
        List<String> attendees = List.of();
        meetingDaoImpl.removeAttendees(null, attendees);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetQueryResults() {
        meetingDaoImpl.getQueryResults(null);
    }

    @Test
    public void testGetTimeRangeQueryOptions() {
        long startTimestamp = 1700000000000L;
        long endTimestamp = 1700000005000L;
        List<Option> options = MeetingDaoImpl.getTimeRangeQueryOptions(startTimestamp, endTimestamp);

        assertNotNull(options);
        assertEquals(4, options.size());

        ZonedDateTime startZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTimestamp), ZoneOffset.UTC);
        ZonedDateTime endZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(endTimestamp), ZoneOffset.UTC);

        String expectedStartTime = startZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String expectedEndTime = endZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        boolean hasStartTime = options.stream()
                .anyMatch(option -> option.getName().equals(Constants.START_DATE_TIME) && Objects.equals(option.getValue(), expectedStartTime));

        boolean hasEndTime = options.stream()
                .anyMatch(option -> option.getName().equals(Constants.END_DATE_TIME) && Objects.equals(option.getValue(), expectedEndTime));

        boolean hasTop = options.stream()
                .anyMatch(option -> option.getName().equals(Constants.TOP) && Objects.equals(option.getValue(), "1000"));

        boolean hasOrderBy = options.stream()
                .anyMatch(option -> option.getName().equals(Constants.ORDER_BY) && Objects.equals(option.getValue(), "start/dateTime asc"));

        assertTrue("startDateTime not found in options", hasStartTime);
        assertTrue("endDateTime not found in options", hasEndTime);
        assertTrue("$top not found in options", hasTop);
        assertTrue("$orderby not found in options", hasOrderBy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTimeRangeQueryOptions_NullFromValue() {
        MeetingDaoImpl.getTimeRangeQueryOptions(null, 1700000005000L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTimeRangeQueryOptions_NullToValue() {
        MeetingDaoImpl.getTimeRangeQueryOptions(1700000000000L, null);
    }
}
