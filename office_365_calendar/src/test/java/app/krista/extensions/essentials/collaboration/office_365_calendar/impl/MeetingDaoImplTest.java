package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;


import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.xml.datatype.DatatypeConfigurationException;
import java.beans.BeanProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MeetingDaoImplTest  {

    MeetingDaoImpl meetingDao = Mockito.mock(MeetingDaoImpl.class);

    @Test()
    public void testGetMeetingById() throws IOException {
        Assert.assertNull(meetingDao.getMeetingById(null));
        String meetingId = "";
        Assert.assertNull(meetingDao.getMeetingById(meetingId));
        meetingId = "   ";
        Assert.assertNull(meetingDao.getMeetingById(meetingId));
    }

    @Test()
    public void testGetMeetings() {
        Assert.assertEquals(meetingDao.getMeetings(0), List.of());
    }

    @Test
    public void testGetSchedules() throws IOException {
        Long dateFrom = null, dateTo = null;
        Assert.assertEquals(meetingDao.getSchedules(dateFrom, dateTo, List.of(), null), List.of());
        dateFrom = 11111111111L;
        dateTo = 11111111111L;
        Assert.assertEquals(meetingDao.getSchedules(dateFrom, dateTo, List.of(), null), List.of());
        dateFrom = 1111111L;
        dateTo = 1111111111L;
        Assert.assertEquals(meetingDao.getSchedules(dateFrom, dateTo, List.of(), null), List.of());
    }

    @Test()
    public void testGetFreeSlot() throws DatatypeConfigurationException, IOException {
        Long dateFrom = null, dateTo = null;
        Assert.assertEquals(meetingDao.getFreeSlot(dateFrom, dateTo, null, List.of(),List.of(),null, null), List.of());
        dateFrom = 11111111111L;
        dateTo = 11111111111L;
        Assert.assertEquals(meetingDao.getFreeSlot(dateFrom, dateTo, null, List.of(), List.of(), null, null), List.of());
        dateFrom = 1111111L;
        dateTo = 1111111111L;
        Assert.assertEquals(meetingDao.getFreeSlot(dateFrom, dateTo, null, List.of(), List.of(), null, null), List.of());

    }

    @Test()
    public void testAddMeeting() {
        Long dateFrom = null;
        Assert.assertNull(meetingDao.addMeeting(null, null, dateFrom, null, List.of(), List.of(), null, null, null, null, null, List.of(), null, null, null, null, null, null, null, null, List.of(), null, false));
        dateFrom = 11111111111L;
        Long dateTo = 11111111001L;
        Assert.assertNull(meetingDao.addMeeting(null, null, dateFrom, dateTo, List.of(), List.of(), null, null, null, null, null, List.of(), null, null, null, null, null, null, null, null, List.of(), null, false));
        Double recurrenceInterval = 999d;
        Assert.assertNull(meetingDao.addMeeting(null, null, dateFrom, null, List.of(), List.of(), null, null, null, null, recurrenceInterval, List.of(), null, null, null, null, null, null, null, null, List.of(), null, false));
        Double dayOfMonth = 999d;
        Assert.assertNull(meetingDao.addMeeting(null, null, dateFrom, null, List.of(), List.of(), null, null, null, "Monthly", null, List.of(), "Absolute Recurrence", dayOfMonth, null, null, null, null, null, null, List.of(), null, false));
        List<String> daysOfWeek = List.of();
        Assert.assertNull(meetingDao.addMeeting(null, null, dateFrom, null, List.of(), List.of(), null, null, null, "Weekly", null, daysOfWeek, "Absolute Recurrence", null, null, null, null, null, null, null, List.of(), null, false));
    }

    @Test
    public void testUpdateMeeting() {
        Assert.assertNull(meetingDao.updateMeeting(null, null, null, null, null, null, null, List.of(), List.of(), null,  null, null, null, List.of(), null, null, null, null, null, null, null, null, false));
        String meetingId = "";
        Assert.assertNull(meetingDao.updateMeeting(meetingId, null, null, null, null, null, null, List.of(), List.of(), null,  null, null, null, List.of(), null, null, null, null, null, null, null, null, false));
        meetingId = "   ";
        Assert.assertNull(meetingDao.updateMeeting(meetingId, null, null, null, null, null, null, List.of(), List.of(), null,  null, null, null, List.of(), null, null, null, null, null, null, null, null, false));
    }

    @Test
    public void testAcceptMeetingInvitation() {
        Assert.assertNull(meetingDao.acceptMeetingInvitation(null));
        String meetingId = "";
        Assert.assertNull(meetingDao.acceptMeetingInvitation(meetingId));
        meetingId = "   ";
        Assert.assertNull(meetingDao.acceptMeetingInvitation(meetingId));
    }

    @Test
    public void testDeclineMeetingInvitation() {
        Assert.assertNull(meetingDao.declineMeetingInvitation(null));
        String meetingId = "";
        Assert.assertNull(meetingDao.declineMeetingInvitation(meetingId));
        meetingId = "   ";
        Assert.assertNull(meetingDao.declineMeetingInvitation(meetingId));
    }

    @Test()
    public void testTentativeMeetingInvitation() {
        Assert.assertNull(meetingDao.tentativeMeetingInvitation(null));
        String meetingId = "";
        Assert.assertNull(meetingDao.tentativeMeetingInvitation(meetingId));
        meetingId = "   ";
        Assert.assertNull(meetingDao.tentativeMeetingInvitation(meetingId));
    }

    @Test()
    public void testCancelMeeting() {
        String meetingId = "";
        Assert.assertNull(meetingDao.cancelMeeting(null));
        Assert.assertNull(meetingDao.cancelMeeting(meetingId));
        meetingId = "   ";
        Assert.assertNull(meetingDao.cancelMeeting(meetingId));
    }

    @Test()
    public void testAddAttendees() {
        List<String> requiredAttendees = List.of();
        List<String> optionalAttendees = List.of();
        Assert.assertNull(meetingDao.addAttendees(null, requiredAttendees, optionalAttendees));
        String meetingId = "";
        Assert.assertNull(meetingDao.addAttendees(meetingId, requiredAttendees, optionalAttendees));
        meetingId = "   ";
        Assert.assertNull(meetingDao.addAttendees(meetingId, requiredAttendees, optionalAttendees));
    }

    @Test()
    public void testRemoveAttendees() {
        List<String> attendees = List.of();
        Assert.assertNull(meetingDao.removeAttendees(null, attendees));
        String meetingId = "";
        Assert.assertNull(meetingDao.removeAttendees(meetingId, attendees));
        meetingId = "   ";
        Assert.assertNull(meetingDao.removeAttendees(meetingId, attendees));

    }

    @Test()
    public void testGetQueryResults() throws IOException {
        Assert.assertEquals(meetingDao.getQueryResults(null), List.of());
        String query = "";
        Assert.assertEquals(meetingDao.getQueryResults(query), List.of());
        query = "    ";
        Assert.assertEquals(meetingDao.getQueryResults(query), List.of());
    }
}
