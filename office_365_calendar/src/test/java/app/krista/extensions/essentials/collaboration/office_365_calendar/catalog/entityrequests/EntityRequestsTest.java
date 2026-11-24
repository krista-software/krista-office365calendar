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

import app.krista.extension.executor.SearchQuery;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EntityRequestsTest {

    @Mock private ServiceRegistry registry;
    @Mock private EntityImplementationService impl;
    @Mock private EntityType entityType;
    @Mock private SearchQuery searchQuery;

    private EntityRequests entityRequests;

    @Before
    public void setUp() {
        entityRequests = new EntityRequests(registry);
        when(entityType.getEntityName()).thenReturn("Event Details");
        Map<String, EntityImplementationService> map = new HashMap<>();
        map.put("Event Details", impl);
        when(registry.getServiceMap()).thenReturn(map);
    }

    @Test
    public void create_routesToImplCreate() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("Title", "Test");
        EntityValue expected = new EntityValue("Event Details", fields);
        when(impl.create(entityType, fields)).thenReturn(expected);

        EntityValue result = entityRequests.create(entityType, fields);

        assertSame(expected, result);
        verify(impl).create(entityType, fields);
    }

    @Test
    public void update_routesToImplUpdate() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("Event Id", "evt-1");
        EntityValue entity = new EntityValue("Event Details", fields);
        when(impl.update(entityType, entity)).thenReturn(entity);

        EntityValue result = entityRequests.update(entityType, entity);

        assertSame(entity, result);
        verify(impl).update(entityType, entity);
    }

    @Test
    public void search_routesToImplSearch() {
        List<EntityValue> expected = Collections.emptyList();
        when(impl.search(entityType, searchQuery, 1L, 10)).thenReturn(expected);

        List<EntityValue> result = entityRequests.search(entityType, searchQuery, 1L, 10);

        assertSame(expected, result);
        verify(impl).search(entityType, searchQuery, 1L, 10);
    }

    @Test
    public void get_routesToImplGet() {
        EntityValue expected = new EntityValue("Event Details", new HashMap<>());
        when(impl.get(entityType, "evt-123")).thenReturn(expected);

        EntityValue result = entityRequests.get(entityType, "evt-123");

        assertSame(expected, result);
        verify(impl).get(entityType, "evt-123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_unknownEntityName_throws() {
        when(entityType.getEntityName()).thenReturn("Unknown Entity");
        entityRequests.create(entityType, new HashMap<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void get_unknownEntityName_throws() {
        when(entityType.getEntityName()).thenReturn("Unknown Entity");
        entityRequests.get(entityType, "some-id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void search_unknownEntityName_throws() {
        when(entityType.getEntityName()).thenReturn("Unknown Entity");
        entityRequests.search(entityType, searchQuery, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_unknownEntityName_throws() {
        when(entityType.getEntityName()).thenReturn("Unknown Entity");
        entityRequests.update(entityType, new EntityValue("Unknown Entity", new HashMap<>()));
    }
}
