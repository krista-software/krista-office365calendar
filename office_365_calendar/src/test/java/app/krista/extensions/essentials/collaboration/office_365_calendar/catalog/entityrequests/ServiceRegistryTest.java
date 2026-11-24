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

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entityrequests;

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entityrequests.impl.EventDetailsImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRegistryTest {

    @Mock private MeetingDaoImpl meetingDao;

    private ServiceRegistry registry;

    @Before
    public void setUp() {
        registry = new ServiceRegistry(meetingDao);
    }

    @Test
    public void getServiceMap_notNull() {
        assertNotNull(registry.getServiceMap());
    }

    @Test
    public void getServiceMap_containsEventDetailsKey() {
        Map<String, EntityImplementationService> map = registry.getServiceMap();
        assertTrue("Expected EVENT_DETAILS key in registry", map.containsKey(Constants.EVENT_DETAILS));
    }

    @Test
    public void getServiceMap_eventDetailsImpl_isCorrectType() {
        EntityImplementationService impl = registry.getServiceMap().get(Constants.EVENT_DETAILS);
        assertNotNull(impl);
        assertTrue(impl instanceof EventDetailsImpl);
    }

    @Test
    public void getServiceMap_hasExactlyOneEntry() {
        assertEquals(1, registry.getServiceMap().size());
    }
}
