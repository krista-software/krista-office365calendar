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

package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import com.microsoft.graph.models.Attendee;
import com.microsoft.graph.models.AttendeeBase;
import com.microsoft.graph.models.AttendeeType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.Recipient;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for MeetingDaoHelper static methods.
 * Tests the utility methods for event processing and attendee management.
 */
public class MeetingDaoHelperTest {

    private Event testEvent;
    private String testUserEmail;
    private String organizerEmail;

    @Before
    public void setUp() {
        testUserEmail = "test.user@company.com";
        organizerEmail = "organizer@company.com";
        
        testEvent = new Event();
        testEvent.organizer = new Recipient();
        testEvent.organizer.emailAddress = new EmailAddress();
        testEvent.organizer.emailAddress.address = organizerEmail;
    }

    // Tests for isEventNeitherAcceptedNorDeclined method

    @Test
    public void shouldReturnFalseWhenEventIsNull() {
        // Arrange & Act
        boolean result = MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(null, testUserEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenCurrentUserMailAddressIsNull() {
        // Arrange & Act
        boolean result = MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(testEvent, null);

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenEventOrganizerIsNull() {
        // Arrange
        testEvent.organizer = null;

        // Act
        boolean result = MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(testEvent, testUserEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenEventOrganizerEmailIsNull() {
        // Arrange
        testEvent.organizer.emailAddress = null;

        // Act
        boolean result = MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(testEvent, testUserEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenEventAttendeesIsNull() {
        // Arrange
        testEvent.attendees = null;

        // Act
        boolean result = MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(testEvent, testUserEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenEventAttendeesIsEmpty() {
        // Arrange
        testEvent.attendees = Collections.emptyList();

        // Act
        boolean result = MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(testEvent, testUserEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenUserIsOrganizer() {
        // Arrange
        testEvent.attendees = createTestAttendees();

        // Act
        boolean result = MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(testEvent, organizerEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenBothParametersAreNull() {
        // Arrange & Act
        boolean result = MeetingDaoHelper.isEventNeitherAcceptedNorDeclined(null, null);

        // Assert
        assertFalse(result);
    }

    // Tests for getRequiredAttendee method

    @Test
    public void shouldReturnEmptyListWhenBothAttendeeListsAreNull() {
        // Arrange & Act
        List<AttendeeBase> result = MeetingDaoHelper.getRequiredAttendee(null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListWhenRequiredAttendeesIsNull() {
        // Arrange
        List<String> optionalAttendees = null; // No optional attendees either

        // Act
        List<AttendeeBase> result = MeetingDaoHelper.getRequiredAttendee(null, optionalAttendees);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListWhenRequiredAttendeesIsEmpty() {
        // Arrange
        List<String> requiredAttendees = Collections.emptyList();
        List<String> optionalAttendees = null; // No optional attendees either

        // Act
        List<AttendeeBase> result = MeetingDaoHelper.getRequiredAttendee(requiredAttendees, optionalAttendees);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldCreateRequiredAttendeesOnlyWhenOptionalIsNull() {
        // Arrange
        List<String> requiredAttendees = Arrays.asList("required1@company.com", "required2@company.com");

        // Act
        List<AttendeeBase> result = MeetingDaoHelper.getRequiredAttendee(requiredAttendees, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        for (AttendeeBase attendee : result) {
            assertEquals(AttendeeType.REQUIRED, attendee.type);
            assertNotNull(attendee.emailAddress);
            assertNotNull(attendee.emailAddress.address);
            assertTrue(requiredAttendees.contains(attendee.emailAddress.address));
        }
    }

    @Test
    public void shouldCreateRequiredAttendeesOnlyWhenOptionalIsEmpty() {
        // Arrange
        List<String> requiredAttendees = Arrays.asList("required1@company.com", "required2@company.com");
        List<String> optionalAttendees = Collections.emptyList();

        // Act
        List<AttendeeBase> result = MeetingDaoHelper.getRequiredAttendee(requiredAttendees, optionalAttendees);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        for (AttendeeBase attendee : result) {
            assertEquals(AttendeeType.REQUIRED, attendee.type);
        }
    }

    @Test
    public void shouldCreateBothRequiredAndOptionalAttendees() {
        // Arrange
        List<String> requiredAttendees = Arrays.asList("required1@company.com", "required2@company.com");
        List<String> optionalAttendees = Arrays.asList("optional1@company.com", "optional2@company.com");

        // Act
        List<AttendeeBase> result = MeetingDaoHelper.getRequiredAttendee(requiredAttendees, optionalAttendees);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        
        // Check required attendees
        long requiredCount = result.stream()
            .filter(attendee -> attendee.type == AttendeeType.REQUIRED)
            .count();
        assertEquals(2, requiredCount);
        
        // Check optional attendees
        long optionalCount = result.stream()
            .filter(attendee -> attendee.type == AttendeeType.OPTIONAL)
            .count();
        assertEquals(2, optionalCount);
    }

    @Test
    public void shouldHandleSingleRequiredAttendee() {
        // Arrange
        List<String> requiredAttendees = Arrays.asList("single@company.com");

        // Act
        List<AttendeeBase> result = MeetingDaoHelper.getRequiredAttendee(requiredAttendees, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(AttendeeType.REQUIRED, result.get(0).type);
        assertEquals("single@company.com", result.get(0).emailAddress.address);
    }

    @Test
    public void shouldHandleSingleOptionalAttendee() {
        // Arrange
        List<String> requiredAttendees = Arrays.asList("required@company.com");
        List<String> optionalAttendees = Arrays.asList("optional@company.com");

        // Act
        List<AttendeeBase> result = MeetingDaoHelper.getRequiredAttendee(requiredAttendees, optionalAttendees);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        AttendeeBase optionalAttendee = result.stream()
            .filter(attendee -> attendee.type == AttendeeType.OPTIONAL)
            .findFirst()
            .orElse(null);
        
        assertNotNull(optionalAttendee);
        assertEquals("optional@company.com", optionalAttendee.emailAddress.address);
    }

    @Test
    public void shouldHandleEmptyEmailAddresses() {
        // Arrange
        List<String> requiredAttendees = Arrays.asList("", "valid@company.com");

        // Act
        List<AttendeeBase> result = MeetingDaoHelper.getRequiredAttendee(requiredAttendees, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify that empty email is still processed
        boolean hasEmptyEmail = result.stream()
            .anyMatch(attendee -> "".equals(attendee.emailAddress.address));
        assertTrue(hasEmptyEmail);
    }

    /**
     * Helper method to create test attendees
     */
    private List<Attendee> createTestAttendees() {
        Attendee attendee1 = new Attendee();
        attendee1.emailAddress = new EmailAddress();
        attendee1.emailAddress.address = testUserEmail;

        Attendee attendee2 = new Attendee();
        attendee2.emailAddress = new EmailAddress();
        attendee2.emailAddress.address = "other@company.com";

        return Arrays.asList(attendee1, attendee2);
    }
}
