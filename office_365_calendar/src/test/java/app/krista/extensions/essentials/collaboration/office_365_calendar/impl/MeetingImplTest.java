package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import com.microsoft.graph.models.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for MeetingImpl class.
 * Tests all methods that wrap Microsoft Graph Event objects.
 */
public class MeetingImplTest {

    private Event event;
    private MeetingImpl meeting;

    @Before
    public void setUp() {
        event = new Event();
        meeting = new MeetingImpl(event);
    }

    @Test
    public void testGetId() {
        // Arrange
        event.id = "test-event-id-123";

        // Act
        String id = meeting.getId();

        // Assert
        assertEquals("Event ID should match", "test-event-id-123", id);
    }

    @Test
    public void testGetId_Null() {
        // Arrange
        event.id = null;

        // Act
        String id = meeting.getId();

        // Assert
        assertNull("Event ID should be null", id);
    }

    @Test
    public void testGetLocation() {
        // Arrange
        Location location = new Location();
        location.displayName = "Conference Room A";
        event.location = location;

        // Act
        String locationName = meeting.getLocation();

        // Assert
        assertEquals("Location should match", "Conference Room A", locationName);
    }

    @Test
    public void testGetLocation_Null() {
        // Arrange
        event.location = null;

        // Act
        String locationName = meeting.getLocation();

        // Assert
        assertNull("Location should be null", locationName);
    }

    @Test
    public void testGetTitle() {
        // Arrange
        event.subject = "Team Meeting";

        // Act
        String title = meeting.getTitle();

        // Assert
        assertEquals("Title should match", "Team Meeting", title);
    }

    @Test
    public void testGetTitle_Null() {
        // Arrange
        event.subject = null;

        // Act
        String title = meeting.getTitle();

        // Assert
        assertNull("Title should be null", title);
    }

    @Test
    public void testGetContacts_WithAttendees() {
        // Arrange
        List<Attendee> attendees = new ArrayList<>();

        Attendee attendee1 = new Attendee();
        attendee1.emailAddress = new EmailAddress();
        attendee1.emailAddress.address = "user1@example.com";
        attendees.add(attendee1);

        Attendee attendee2 = new Attendee();
        attendee2.emailAddress = new EmailAddress();
        attendee2.emailAddress.address = "user2@example.com";
        attendees.add(attendee2);

        event.attendees = attendees;

        // Act
        List<String> contacts = meeting.getContacts();

        // Assert
        assertNotNull("Contacts should not be null", contacts);
        assertEquals("Should have 2 contacts", 2, contacts.size());
        assertTrue("Should contain user1@example.com", contacts.contains("user1@example.com"));
        assertTrue("Should contain user2@example.com", contacts.contains("user2@example.com"));
    }

    @Test
    public void testGetContacts_NullAttendees() {
        // Arrange
        event.attendees = null;

        // Act
        List<String> contacts = meeting.getContacts();

        // Assert
        assertNotNull("Contacts should not be null", contacts);
        assertTrue("Contacts should be empty", contacts.isEmpty());
    }

    @Test
    public void testGetContacts_EmptyAttendees() {
        // Arrange
        event.attendees = new ArrayList<>();

        // Act
        List<String> contacts = meeting.getContacts();

        // Assert
        assertNotNull("Contacts should not be null", contacts);
        assertTrue("Contacts should be empty", contacts.isEmpty());
    }

    @Test
    public void testGetContacts_AttendeeWithNullEmail() {
        // Arrange
        List<Attendee> attendees = new ArrayList<>();
        Attendee attendee = new Attendee();
        attendee.emailAddress = null;
        attendees.add(attendee);
        event.attendees = attendees;

        // Act
        List<String> contacts = meeting.getContacts();

        // Assert
        assertNotNull("Contacts should not be null", contacts);
        assertTrue("Contacts should be empty when email is null", contacts.isEmpty());
    }

    @Test
    public void testGetDetails_WithBody() {
        // Arrange
        ItemBody body = new ItemBody();
        body.content = "This is the meeting description\nwith multiple lines\rand special characters";
        event.body = body;

        // Act
        String details = meeting.getDetails();

        // Assert
        assertNotNull("Details should not be null", details);
        assertFalse("Details should not contain newlines", details.contains("\n"));
        assertFalse("Details should not contain carriage returns", details.contains("\r"));
        assertTrue("Details should contain the text", details.contains("This is the meeting description"));
    }

    @Test
    public void testGetDetails_NullBody() {
        // Arrange
        event.body = null;

        // Act
        String details = meeting.getDetails();

        // Assert
        assertNull("Details should be null", details);
    }

    @Test
    public void testGetDateFrom_WithStartDate() {
        // Arrange
        DateTimeTimeZone startDate = new DateTimeTimeZone();
        startDate.dateTime = "2024-01-15T10:00:00";
        event.start = startDate;

        // Act
        Long dateFrom = meeting.getDateFrom();

        // Assert
        assertNotNull("Date from should not be null", dateFrom);
        assertTrue("Date from should be positive", dateFrom > 0);
    }

    @Test
    public void testGetDateFrom_NullStartDate() {
        // Arrange
        event.start = null;

        // Act
        Long dateFrom = meeting.getDateFrom();

        // Assert
        assertNull("Date from should be null", dateFrom);
    }

    @Test
    public void testGetDateTo_WithEndDate() {
        // Arrange
        DateTimeTimeZone endDate = new DateTimeTimeZone();
        endDate.dateTime = "2024-01-15T11:00:00";
        event.end = endDate;

        // Act
        Long dateTo = meeting.getDateTo();

        // Assert
        assertNotNull("Date to should not be null", dateTo);
        assertTrue("Date to should be positive", dateTo > 0);
    }

    @Test
    public void testGetDateTo_NullEndDate() {
        // Arrange
        event.end = null;

        // Act
        Long dateTo = meeting.getDateTo();

        // Assert
        assertNull("Date to should be null", dateTo);
    }

    @Test
    public void testGetTimeZone() {
        // Arrange
        event.originalStartTimeZone = "America/New_York";

        // Act
        String timeZone = meeting.getTimeZone();

        // Assert
        assertEquals("Time zone should match", "America/New_York", timeZone);
    }

    @Test
    public void testGetTimeZone_Null() {
        // Arrange
        event.originalStartTimeZone = null;

        // Act
        String timeZone = meeting.getTimeZone();

        // Assert
        assertNull("Time zone should be null", timeZone);
    }

    @Test
    public void testGetOrganizer() {
        // Arrange
        Recipient organizer = new Recipient();
        organizer.emailAddress = new EmailAddress();
        organizer.emailAddress.address = "organizer@example.com";
        event.organizer = organizer;

        // Act
        String organizerEmail = meeting.getOrganizer();

        // Assert
        assertEquals("Organizer email should match", "organizer@example.com", organizerEmail);
    }

    @Test
    public void testGetOrganizer_NullOrganizer() {
        // Arrange
        event.organizer = null;

        // Act
        String organizerEmail = meeting.getOrganizer();

        // Assert
        assertNull("Organizer should be null", organizerEmail);
    }

    @Test
    public void testGetOrganizer_NullEmailAddress() {
        // Arrange
        Recipient organizer = new Recipient();
        organizer.emailAddress = null;
        event.organizer = organizer;

        // Act
        String organizerEmail = meeting.getOrganizer();

        // Assert
        assertNull("Organizer email should be null", organizerEmail);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDateFrom_InvalidDateFormat() {
        // Arrange
        DateTimeTimeZone startDate = new DateTimeTimeZone();
        startDate.dateTime = "invalid-date-format";
        event.start = startDate;

        // Act
        meeting.getDateFrom(); // Should throw IllegalStateException
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDateTo_InvalidDateFormat() {
        // Arrange
        DateTimeTimeZone endDate = new DateTimeTimeZone();
        endDate.dateTime = "invalid-date-format";
        event.end = endDate;

        // Act
        meeting.getDateTo(); // Should throw IllegalStateException
    }

    @Test
    public void testGetDateFrom_ValidDateParsing() {
        // Arrange
        DateTimeTimeZone startDate = new DateTimeTimeZone();
        startDate.dateTime = "2024-12-25T09:30:00";
        event.start = startDate;

        // Act
        Long dateFrom = meeting.getDateFrom();

        // Assert
        assertNotNull("Date from should not be null", dateFrom);
        // Verify it's a reasonable timestamp (after year 2000)
        assertTrue("Date should be after year 2000", dateFrom > 946684800000L);
    }

    @Test
    public void testGetDateTo_ValidDateParsing() {
        // Arrange
        DateTimeTimeZone endDate = new DateTimeTimeZone();
        endDate.dateTime = "2024-12-25T10:30:00";
        event.end = endDate;

        // Act
        Long dateTo = meeting.getDateTo();

        // Assert
        assertNotNull("Date to should not be null", dateTo);
        // Verify it's a reasonable timestamp (after year 2000)
        assertTrue("Date should be after year 2000", dateTo > 946684800000L);
    }

    @Test
    public void testGetDetails_RemovesNewlinesAndCarriageReturns() {
        // Arrange
        ItemBody body = new ItemBody();
        body.content = "Line 1\nLine 2\rLine 3\r\nLine 4";
        event.body = body;

        // Act
        String details = meeting.getDetails();

        // Assert
        assertEquals("Should remove all newlines and carriage returns", "Line 1Line 2Line 3Line 4", details);
    }

    @Test
    public void testCompleteEvent() {
        // Arrange - Create a complete event with all fields
        event.id = "complete-event-123";
        event.subject = "Complete Team Meeting";

        Location location = new Location();
        location.displayName = "Main Conference Room";
        event.location = location;

        ItemBody body = new ItemBody();
        body.content = "Detailed meeting description\nwith agenda items";
        event.body = body;

        DateTimeTimeZone startDate = new DateTimeTimeZone();
        startDate.dateTime = "2024-06-15T14:00:00";
        event.start = startDate;

        DateTimeTimeZone endDate = new DateTimeTimeZone();
        endDate.dateTime = "2024-06-15T15:00:00";
        event.end = endDate;

        event.originalStartTimeZone = "UTC";

        Recipient organizer = new Recipient();
        organizer.emailAddress = new EmailAddress();
        organizer.emailAddress.address = "boss@example.com";
        event.organizer = organizer;

        List<Attendee> attendees = new ArrayList<>();
        Attendee attendee1 = new Attendee();
        attendee1.emailAddress = new EmailAddress();
        attendee1.emailAddress.address = "attendee1@example.com";
        attendees.add(attendee1);
        event.attendees = attendees;

        // Act & Assert
        assertEquals("complete-event-123", meeting.getId());
        assertEquals("Complete Team Meeting", meeting.getTitle());
        assertEquals("Main Conference Room", meeting.getLocation());
        assertNotNull(meeting.getDetails());
        assertFalse(meeting.getDetails().contains("\n"));
        assertNotNull(meeting.getDateFrom());
        assertNotNull(meeting.getDateTo());
        assertTrue(meeting.getDateTo() > meeting.getDateFrom());
        assertEquals("UTC", meeting.getTimeZone());
        assertEquals("boss@example.com", meeting.getOrganizer());
        assertEquals(1, meeting.getContacts().size());
        assertTrue(meeting.getContacts().contains("attendee1@example.com"));
    }
}

