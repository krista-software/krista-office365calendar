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

package app.krista.extensions.essentials.collaboration.office_365_calendar.api;

import app.krista.extensions.essentials.collaboration.office_365_calendar.mcp.CalendarMcpSetupResource;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class Office365CalendarApplicationCoverageTest {

    private final Office365CalendarApplication application = new Office365CalendarApplication();

    @Test
    public void getClasses_containsCalendarMcpSetupResource() {
        Set<Class<?>> classes = application.getClasses();
        assertTrue("Expected CalendarMcpSetupResource to be registered",
                classes.contains(CalendarMcpSetupResource.class));
    }

    @Test
    public void getClasses_containsAuthenticationResource() {
        Set<Class<?>> classes = application.getClasses();
        assertTrue("Expected AuthenticationResource to be registered",
                classes.contains(AuthenticationResource.class));
    }

    @Test
    public void getClasses_containsOffice365CalendarAuditResource() {
        Set<Class<?>> classes = application.getClasses();
        assertTrue("Expected Office365CalendarAuditResource to be registered",
                classes.contains(Office365CalendarAuditResource.class));
    }

    @Test
    public void getClasses_returnsExactlyThreeClasses() {
        Set<Class<?>> classes = application.getClasses();
        assertEquals("Expected exactly 3 registered classes", 3, classes.size());
    }

    @Test
    public void getClasses_returnsImmutableSet() {
        Set<Class<?>> classes = application.getClasses();
        assertThrows(UnsupportedOperationException.class, () -> classes.add(String.class));
    }
}
