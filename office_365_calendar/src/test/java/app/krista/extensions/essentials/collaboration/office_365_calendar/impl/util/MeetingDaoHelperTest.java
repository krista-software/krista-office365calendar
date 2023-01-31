package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import com.microsoft.graph.models.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.microsoft.graph.models.RecurrencePatternType.ABSOLUTE_MONTHLY;

public class MeetingDaoHelperTest {

    public static final String ABSOLUTE_RECURRENCE = "Absolute Recurrence";
    public static final String NUMBERED_OCCURRENCES = "Numbered occurrences";
    public static final String MONTHLY = "Monthly";
    public static String EXAMPLE_EMAIL_COM;
    public static List<String> attendeesEmailList;
    public static long dateFrom;
    public static String timeZone;
    public static List<String> emptyList;
    public static List<Attendee> attendeeLinkedList;

    @BeforeClass
    public static void setupParameters() {
        EXAMPLE_EMAIL_COM = "example@email.com";
        attendeesEmailList = List.of(EXAMPLE_EMAIL_COM);
        dateFrom = 1674578375946L;
        timeZone = "Asia/Kolkata";
        emptyList = List.of();
        attendeeLinkedList = new LinkedList<>();
    }

    @Test
    public void testDateTimeTimeZone() throws IOException, ParseException {
        DateTimeTimeZone dateTimeTimeZone = MeetingDaoHelper.getDateTimeTimeZone(dateFrom, timeZone, timeZone);
        Assert.assertEquals("2023-01-24T22:09:35", dateTimeTimeZone.dateTime);
        Assert.assertEquals(timeZone, dateTimeTimeZone.timeZone);
    }

    @Test
    public void testRequiredAttendees() {
        List<AttendeeBase> attendees = MeetingDaoHelper.getRequiredAttendee(attendeesEmailList, emptyList);
        Assert.assertNotNull(attendees.get(0));
        Assert.assertEquals(EXAMPLE_EMAIL_COM, (Objects.requireNonNull(attendees.get(0).emailAddress).address));
    }

    @Test
    public void testOptionalAttendees() {
        List<AttendeeBase> attendees = MeetingDaoHelper.getRequiredAttendee(emptyList, attendeesEmailList);
        Assert.assertEquals(EXAMPLE_EMAIL_COM, (Objects.requireNonNull(attendees.get(0).emailAddress).address));
        Assert.assertEquals(AttendeeType.OPTIONAL, (Objects.requireNonNull(attendees.get(0).type)));
    }

    @Test
    public void testRecipients() {
        List<Recipient> toRecipientsList = MeetingDaoHelper.getRecipient(attendeesEmailList);
        Assert.assertEquals(EXAMPLE_EMAIL_COM, Objects.requireNonNull(toRecipientsList.get(0).emailAddress).address);
    }

    @Test
    public void testGetReminder() {
        Assert.assertNull(MeetingDaoHelper.getReminder("None"));
        Assert.assertEquals((Integer) 0, MeetingDaoHelper.getReminder("At time of event"));
        Assert.assertEquals((Integer) 30, MeetingDaoHelper.getReminder("30 minutes before"));
        Assert.assertEquals((Integer) 720, MeetingDaoHelper.getReminder("12 hours before"));
    }

    @Test
    public void testGetRecurrence() {
        PatternedRecurrence recurrence = MeetingDaoHelper.getRecurrence(MONTHLY, 3.0, dateFrom,
                NUMBERED_OCCURRENCES, null, 5.0, List.of(), ABSOLUTE_RECURRENCE,
                25.0,null, null, null);
        Assert.assertNotNull(recurrence);
        Assert.assertEquals(ABSOLUTE_MONTHLY, Objects.requireNonNull(recurrence.pattern).type);
    }

    @Test
    public void testGetAttendees() {
        attendeeLinkedList = MeetingDaoHelper.getAttendees(attendeesEmailList, emptyList);
        Assert.assertEquals(Objects.requireNonNull(attendeeLinkedList.get(0).emailAddress).address, EXAMPLE_EMAIL_COM);
    }

    @Test
    public void testUpdateAttendeesList() {
        attendeeLinkedList = MeetingDaoHelper.getAttendees(attendeesEmailList, emptyList);
        System.out.println(attendeeLinkedList);
        MeetingDaoHelper.updateAttendeesList(attendeeLinkedList, attendeesEmailList);
        Assert.assertEquals(0,attendeeLinkedList.size());
    }
}
