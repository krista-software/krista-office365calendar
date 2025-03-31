package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;


import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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


//    @Test
//    public void testGetOptions_ValidTimestamps() {
//        long from = 1700000000000L;
//        long to = 1700000005000L;
//        List<Option> options = MeetingDaoImpl.getOptions(from, to);
//
//        assertNotNull(options);
//        assertEquals(4, options.size());
//
//        ZonedDateTime startZoned = Instant.ofEpochMilli(from).atZone(ZoneId.of("UTC"));
//        ZonedDateTime endZoned = Instant.ofEpochMilli(to).atZone(ZoneId.of("UTC"));
//        String expectedStartTime = startZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//        String expectedEndTime = endZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//
//        // Validate individual options
//        assertTrue(options.contains(new QueryOption("startDateTime", expectedStartTime)));
//        assertTrue(options.contains(new QueryOption("endDateTime", expectedEndTime)));
//        assertTrue(options.contains(new QueryOption("$top", "100")));
//        assertTrue(options.contains(new QueryOption("$orderby", "start/dateTime asc")));
//    }

    @Test
    public void testGetOptions_ValidTimestamps() {
        Long from = 1700000000000L;
        Long to = 1700000005000L;
        List<Option> options = MeetingDaoImpl.getOptions(from, to);

        assertNotNull(options);
        assertEquals(4, options.size());

        ZonedDateTime startZoned = Instant.ofEpochMilli(from).atZone(ZoneId.of("UTC"));
        ZonedDateTime endZoned = Instant.ofEpochMilli(to).atZone(ZoneId.of("UTC"));
        String expectedStartTime = startZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String expectedEndTime = endZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        boolean hasStartTime = options.stream()
                .anyMatch(option -> option.getName().equals("startDateTime") && option.getValue().equals(expectedStartTime));

        boolean hasEndTime = options.stream()
                .anyMatch(option -> option.getName().equals("endDateTime") && option.getValue().equals(expectedEndTime));

        boolean hasTop = options.stream()
                .anyMatch(option -> option.getName().equals("$top") && option.getValue().equals("100"));

        boolean hasOrderBy = options.stream()
                .anyMatch(option -> option.getName().equals("$orderby") && option.getValue().equals("start/dateTime asc"));

        assertTrue("startDateTime not found in options", hasStartTime);
        assertTrue("endDateTime not found in options", hasEndTime);
        assertTrue("$top not found in options", hasTop);
        assertTrue("$orderby not found in options", hasOrderBy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetOptions_NullFromValue() {
        MeetingDaoImpl.getOptions(null, 1700000005000L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetOptions_NullToValue() {
        MeetingDaoImpl.getOptions(1700000000000L, null);
    }
}
