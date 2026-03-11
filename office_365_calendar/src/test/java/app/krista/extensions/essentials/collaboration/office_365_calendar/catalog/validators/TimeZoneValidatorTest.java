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

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import org.junit.Test;

import java.time.ZoneId;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for TimeZoneValidator static methods.
 * Tests the utility methods for time zone validation.
 */
public class TimeZoneValidatorTest {

    // Tests for isValidTimeZone method

    @Test
    public void shouldReturnTrueForNullTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone(null);

        // Assert
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForEmptyTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("");

        // Assert
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForBlankTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("   ");

        // Assert
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForValidUTCTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("UTC");

        // Assert
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForValidAmericaNewYorkTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("America/New_York");

        // Assert
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForValidEuropeLondonTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("Europe/London");

        // Assert
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForValidAsiaTokyoTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("Asia/Tokyo");

        // Assert
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseForInvalidTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("Invalid/TimeZone");

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseForCompletelyInvalidTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("NotATimeZone");

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseForNumericTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("12345");

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseForSpecialCharactersTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("@#$%^&*()");

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldHandleTimeZoneWithLeadingAndTrailingSpaces() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("  UTC  ");

        // Assert
        assertTrue(result);
    }

    @Test
    public void shouldHandleTimeZoneWithTabs() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("\t\tUTC\t\t");

        // Assert
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForGMTTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("GMT");

        // Assert
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForZTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("Z");

        // Assert
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseForPartialTimeZone() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("America/");

        // Assert
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseForTimeZoneWithWrongSeparator() {
        // Act
        boolean result = TimeZoneValidator.isValidTimeZone("America\\New_York");

        // Assert
        assertFalse(result);
    }

    // Tests for getAvailableTimeZones method

    @Test
    public void shouldReturnNonNullSetOfTimeZones() {
        // Act
        Set<String> timeZones = TimeZoneValidator.getAvailableTimeZones();

        // Assert
        assertNotNull(timeZones);
    }

    @Test
    public void shouldReturnNonEmptySetOfTimeZones() {
        // Act
        Set<String> timeZones = TimeZoneValidator.getAvailableTimeZones();

        // Assert
        assertFalse(timeZones.isEmpty());
    }

    @Test
    public void shouldContainUTCTimeZone() {
        // Act
        Set<String> timeZones = TimeZoneValidator.getAvailableTimeZones();

        // Assert
        assertTrue(timeZones.contains("UTC"));
    }

    @Test
    public void shouldContainAmericaNewYorkTimeZone() {
        // Act
        Set<String> timeZones = TimeZoneValidator.getAvailableTimeZones();

        // Assert
        assertTrue(timeZones.contains("America/New_York"));
    }

    @Test
    public void shouldContainEuropeLondonTimeZone() {
        // Act
        Set<String> timeZones = TimeZoneValidator.getAvailableTimeZones();

        // Assert
        assertTrue(timeZones.contains("Europe/London"));
    }

    @Test
    public void shouldContainAsiaTokyoTimeZone() {
        // Act
        Set<String> timeZones = TimeZoneValidator.getAvailableTimeZones();

        // Assert
        assertTrue(timeZones.contains("Asia/Tokyo"));
    }

    @Test
    public void shouldReturnSameSetAsZoneIdGetAvailableZoneIds() {
        // Act
        Set<String> timeZones = TimeZoneValidator.getAvailableTimeZones();
        Set<String> zoneIdTimeZones = ZoneId.getAvailableZoneIds();

        // Assert
        assertEquals(zoneIdTimeZones.size(), timeZones.size());
        assertTrue(timeZones.containsAll(zoneIdTimeZones));
        assertTrue(zoneIdTimeZones.containsAll(timeZones));
    }

    @Test
    public void shouldReturnConsistentResultsOnMultipleCalls() {
        // Act
        Set<String> timeZones1 = TimeZoneValidator.getAvailableTimeZones();
        Set<String> timeZones2 = TimeZoneValidator.getAvailableTimeZones();

        // Assert
        assertEquals(timeZones1.size(), timeZones2.size());
        assertTrue(timeZones1.containsAll(timeZones2));
        assertTrue(timeZones2.containsAll(timeZones1));
    }

    @Test
    public void shouldReturnSetWithExpectedSize() {
        // Act
        Set<String> timeZones = TimeZoneValidator.getAvailableTimeZones();

        // Assert
        assertTrue("Time zone set should contain many zones", timeZones.size() > 100);
    }
}
