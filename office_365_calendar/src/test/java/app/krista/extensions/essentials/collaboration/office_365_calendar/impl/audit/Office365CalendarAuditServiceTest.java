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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Office365CalendarAuditServiceTest {

    @Mock private KeyValueStore keyValueStore;

    private Office365CalendarAuditService auditService;

    @Before
    public void setUp() throws Exception {
        resetStaticState();
        auditService = new Office365CalendarAuditService(keyValueStore);
    }

    private void resetStaticState() throws Exception {
        Field loadedField = Office365CalendarAuditService.class.getDeclaredField("GLOBAL_COUNTER_LOADED");
        loadedField.setAccessible(true);
        loadedField.setBoolean(null, false);

        Field totalField = Office365CalendarAuditService.class.getDeclaredField("GLOBAL_TOTAL_REQUESTS");
        totalField.setAccessible(true);
        ((AtomicLong) totalField.get(null)).set(0);

        Field firstAtField = Office365CalendarAuditService.class.getDeclaredField("GLOBAL_FIRST_RECORDED_AT");
        firstAtField.setAccessible(true);
        ((AtomicLong) firstAtField.get(null)).set(0);
    }

    // ── global counter loading ───────────────────────────────────────────────

    @Test
    public void getGlobalCounter_noKvData_returnsZeros() {
        when(keyValueStore.get("calendar_global_counter")).thenReturn(null);

        Map<String, Object> counter = auditService.getGlobalCounter();

        assertNotNull(counter);
        assertEquals(0L, ((Number) counter.get("totalRequests")).longValue());
        assertEquals(0L, ((Number) counter.get("firstRecordedAt")).longValue());
    }

    @Test
    public void getGlobalCounter_withKvData_loadsStoredValues() {
        String json = "{\"totalRequests\":42,\"firstRecordedAt\":1700000000000}";
        when(keyValueStore.get("calendar_global_counter")).thenReturn(json);

        Map<String, Object> counter = auditService.getGlobalCounter();

        assertEquals(42L, ((Number) counter.get("totalRequests")).longValue());
        assertEquals(1700000000000L, ((Number) counter.get("firstRecordedAt")).longValue());
    }

    @Test
    public void getGlobalCounter_kvReturnsBlankString_startsAtZero() {
        when(keyValueStore.get("calendar_global_counter")).thenReturn("   ");

        Map<String, Object> counter = auditService.getGlobalCounter();

        assertEquals(0L, ((Number) counter.get("totalRequests")).longValue());
    }

    @Test
    public void getGlobalCounter_kvThrowsException_gracefullyHandled() {
        when(keyValueStore.get("calendar_global_counter")).thenThrow(new RuntimeException("KV unavailable"));

        Map<String, Object> counter = auditService.getGlobalCounter();

        assertNotNull(counter);
        assertEquals(0L, ((Number) counter.get("totalRequests")).longValue());
    }

    @Test
    public void getGlobalCounter_cachedAfterFirstLoad_kvCalledOnce() {
        when(keyValueStore.get("calendar_global_counter")).thenReturn(null);

        auditService.getGlobalCounter();
        auditService.getGlobalCounter();
        auditService.getGlobalCounter();

        verify(keyValueStore, times(1)).get("calendar_global_counter");
    }

    // ── record() counter increment ───────────────────────────────────────────

    @Test
    public void record_triggeredEvent_incrementsGlobalCounter() {
        when(keyValueStore.get("calendar_global_counter")).thenReturn(null);

        auditService.record("inv-1", "CATALOG_REQUEST_TRIGGERED", "info",
                "Test Catalog", null, null, Collections.emptyMap());

        Map<String, Object> counter = auditService.getGlobalCounter();
        assertEquals(1L, ((Number) counter.get("totalRequests")).longValue());
    }

    @Test
    public void record_nonTriggeredEvent_doesNotIncrementCounter() {
        when(keyValueStore.get("calendar_global_counter")).thenReturn(null);

        auditService.record("inv-1", "CATALOG_REQUEST_SUCCESS", "success",
                "Test Catalog", null, null, Collections.emptyMap());
        auditService.record("inv-1", "CATALOG_REQUEST_FAILURE", "failure",
                "Test Catalog", "error msg", "ExClass", Collections.emptyMap());

        Map<String, Object> counter = auditService.getGlobalCounter();
        assertEquals(0L, ((Number) counter.get("totalRequests")).longValue());
    }

    @Test
    public void record_multipleTriggeredEvents_counterAccumulates() {
        when(keyValueStore.get("calendar_global_counter")).thenReturn(null);

        for (int i = 0; i < 5; i++) {
            auditService.record("inv-1", "CATALOG_REQUEST_TRIGGERED", "info",
                    "Test Catalog", null, null, Collections.emptyMap());
        }

        Map<String, Object> counter = auditService.getGlobalCounter();
        assertEquals(5L, ((Number) counter.get("totalRequests")).longValue());
    }

    @Test
    public void record_triggeredEvent_persistsToKv() {
        when(keyValueStore.get("calendar_global_counter")).thenReturn(null);

        auditService.record("inv-1", "CATALOG_REQUEST_TRIGGERED", "info",
                "Test Catalog", null, null, null);

        verify(keyValueStore, atLeastOnce()).put(eq("calendar_global_counter"), anyString());
    }

    @Test
    public void record_kvPutThrows_doesNotPropagateException() {
        when(keyValueStore.get("calendar_global_counter")).thenReturn(null);
        doThrow(new RuntimeException("KV write failed"))
                .when(keyValueStore).put(anyString(), anyString());

        // Should not throw
        auditService.record("inv-1", "CATALOG_REQUEST_TRIGGERED", "info",
                "Test Catalog", null, null, null);
    }

    // ── startRequest / resetAuthCounters ────────────────────────────────────

    @Test
    public void startRequest_doesNotThrow() {
        auditService.startRequest();
    }

    @Test
    public void resetAuthCounters_doesNotThrow() {
        auditService.resetAuthCounters();
    }
}
