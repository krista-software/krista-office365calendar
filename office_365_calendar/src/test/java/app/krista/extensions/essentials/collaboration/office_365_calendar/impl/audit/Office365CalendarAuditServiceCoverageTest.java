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

package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.audit;

import app.krista.extensions.util.KeyValueStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@RunWith(MockitoJUnitRunner.class)
public class Office365CalendarAuditServiceCoverageTest {

    @Mock
    private KeyValueStore keyValueStore;

    private Office365CalendarAuditService service;

    @Before
    public void setUp() throws Exception {
        // Reset static fields between tests
        Field totalField = Office365CalendarAuditService.class.getDeclaredField("GLOBAL_TOTAL_REQUESTS");
        totalField.setAccessible(true);
        ((AtomicLong) totalField.get(null)).set(0);

        Field firstRecordedField = Office365CalendarAuditService.class.getDeclaredField("GLOBAL_FIRST_RECORDED_AT");
        firstRecordedField.setAccessible(true);
        ((AtomicLong) firstRecordedField.get(null)).set(0);

        Field loadedField = Office365CalendarAuditService.class.getDeclaredField("GLOBAL_COUNTER_LOADED");
        loadedField.setAccessible(true);
        loadedField.set(null, false);

        // Allow any KV store interactions from the delegate
        lenient().when(keyValueStore.get(anyString())).thenReturn(null);

        service = new Office365CalendarAuditService(keyValueStore);
    }

    @Test
    public void record_withCatalogRequestTriggered_incrementsGlobalCounter() {
        service.record("invoker1", "CATALOG_REQUEST_TRIGGERED", "info",
                "catalog1", null, null, null);

        Map<String, Object> counter = service.getGlobalCounter();
        assertEquals(1L, ((Number) counter.get("totalRequests")).longValue());
    }

    @Test
    public void record_withCatalogRequestSuccess_doesNotIncrementGlobalCounter() {
        service.record("invoker1", "CATALOG_REQUEST_SUCCESS", "OK",
                "catalog1", null, null, null);

        Map<String, Object> counter = service.getGlobalCounter();
        assertEquals(0L, ((Number) counter.get("totalRequests")).longValue());
    }

    @Test
    public void record_withCatalogRequestFailure_doesNotIncrementGlobalCounter() {
        service.record("invoker1", "CATALOG_REQUEST_FAILURE", "FAIL",
                "catalog1", "reason", null, null);

        Map<String, Object> counter = service.getGlobalCounter();
        assertEquals(0L, ((Number) counter.get("totalRequests")).longValue());
    }

    @Test
    public void record_withOtherEventType_doesNotIncrementCounter() {
        service.record("invoker1", "SOME_OTHER_EVENT", "OK",
                "catalog1", null, null, null);

        Map<String, Object> counter = service.getGlobalCounter();
        assertEquals(0L, ((Number) counter.get("totalRequests")).longValue());
    }

    @Test
    public void getGlobalCounter_returnsMapWithTotalRequestsAndFirstRecordedAt() {
        Map<String, Object> counter = service.getGlobalCounter();

        assertNotNull(counter);
        assertTrue("Expected totalRequests key", counter.containsKey("totalRequests"));
        assertTrue("Expected firstRecordedAt key", counter.containsKey("firstRecordedAt"));
    }

    @Test
    public void ensureGlobalCounterLoaded_loadsFromKvStoreOnFirstAccess() {
        String json = "{\"totalRequests\":42,\"firstRecordedAt\":1000}";
        lenient().when(keyValueStore.get("calendar_global_counter")).thenReturn(json);

        // Re-create service so ensureGlobalCounterLoaded triggers with the new stub
        service = new Office365CalendarAuditService(keyValueStore);

        Map<String, Object> counter = service.getGlobalCounter();
        assertEquals(42L, ((Number) counter.get("totalRequests")).longValue());
        assertEquals(1000L, ((Number) counter.get("firstRecordedAt")).longValue());
    }

    @Test
    public void ensureGlobalCounterLoaded_handlesNullKvStoreValue() {
        lenient().when(keyValueStore.get("calendar_global_counter")).thenReturn(null);

        service = new Office365CalendarAuditService(keyValueStore);

        Map<String, Object> counter = service.getGlobalCounter();
        assertEquals(0L, ((Number) counter.get("totalRequests")).longValue());
        assertEquals(0L, ((Number) counter.get("firstRecordedAt")).longValue());
    }

    @Test
    public void ensureGlobalCounterLoaded_handlesBlankKvStoreValue() {
        lenient().when(keyValueStore.get("calendar_global_counter")).thenReturn("   ");

        service = new Office365CalendarAuditService(keyValueStore);

        Map<String, Object> counter = service.getGlobalCounter();
        assertEquals(0L, ((Number) counter.get("totalRequests")).longValue());
        assertEquals(0L, ((Number) counter.get("firstRecordedAt")).longValue());
    }

    @Test
    public void ensureGlobalCounterLoaded_handlesValidJsonFromKvStore() {
        String json = "{\"totalRequests\":100,\"firstRecordedAt\":5000}";
        lenient().when(keyValueStore.get("calendar_global_counter")).thenReturn(json);

        service = new Office365CalendarAuditService(keyValueStore);

        Map<String, Object> counter = service.getGlobalCounter();
        assertEquals(100L, ((Number) counter.get("totalRequests")).longValue());
        assertEquals(5000L, ((Number) counter.get("firstRecordedAt")).longValue());
    }
}
