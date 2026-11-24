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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import app.krista.sdk.msauth.extension.audit.AuditData;
import app.krista.sdk.msauth.extension.audit.AuditRecorder;
import app.krista.sdk.msauth.extension.service.MsAuthAuditService;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Calendar audit service — thin delegate around the SDK's {@link MsAuthAuditService}.
 *
 * <p>All audit features (duration tracking, user resolution via AuthorizationContext,
 * per-request log capture, auth error classification) are provided generically by
 * the SDK. This class simply wires the SDK service into HK2 for Calendar.</p>
 */
@Service
public class Office365CalendarAuditService implements AuditRecorder {

    private static final String KV_GLOBAL_COUNTER_KEY = "calendar_global_counter";
    private static final AtomicLong GLOBAL_TOTAL_REQUESTS  = new AtomicLong(0);
    private static final AtomicLong GLOBAL_FIRST_RECORDED_AT = new AtomicLong(0);
    private static volatile boolean GLOBAL_COUNTER_LOADED = false;
    private static final Object GLOBAL_COUNTER_LOCK = new Object();

    private final MsAuthAuditService delegate;
    private final KeyValueStore keyValueStore;

    @Inject
    public Office365CalendarAuditService(KeyValueStore keyValueStore) {
        this.delegate = new MsAuthAuditService(keyValueStore, "calendar");
        this.keyValueStore = keyValueStore;
    }

    @Override
    public void record(String invokerId, String eventType, String status,
                       String catalogName, String reason, String exceptionClass,
                       Map<String, String> metadata) {
        delegate.record(invokerId, eventType, status, catalogName, reason, exceptionClass, metadata);
        if ("CATALOG_REQUEST_TRIGGERED".equals(eventType)) {
            ensureGlobalCounterLoaded();
            long now = System.currentTimeMillis();
            GLOBAL_FIRST_RECORDED_AT.compareAndSet(0, now);
            GLOBAL_TOTAL_REQUESTS.incrementAndGet();
            persistGlobalCounter();
        }
    }

    private void ensureGlobalCounterLoaded() {
        if (GLOBAL_COUNTER_LOADED) return;
        synchronized (GLOBAL_COUNTER_LOCK) {
            if (GLOBAL_COUNTER_LOADED) return;
            try {
                Object raw = keyValueStore.get(KV_GLOBAL_COUNTER_KEY);
                if (raw != null) {
                    String json = raw.toString();
                    if (!json.isBlank()) {
                        Map<?, ?> gc = Constants.GSON.fromJson(json, Map.class);
                        Object tr  = gc.get("totalRequests");
                        Object fra = gc.get("firstRecordedAt");
                        if (tr  instanceof Number) GLOBAL_TOTAL_REQUESTS.set(((Number) tr).longValue());
                        if (fra instanceof Number) GLOBAL_FIRST_RECORDED_AT.set(((Number) fra).longValue());
                    }
                }
            } catch (Exception ignored) {}
            GLOBAL_COUNTER_LOADED = true;
        }
    }

    private void persistGlobalCounter() {
        try {
            Map<String, Object> gc = new HashMap<>();
            gc.put("totalRequests",   GLOBAL_TOTAL_REQUESTS.get());
            gc.put("firstRecordedAt", GLOBAL_FIRST_RECORDED_AT.get());
            keyValueStore.put(KV_GLOBAL_COUNTER_KEY, Constants.GSON.toJson(gc));
        } catch (Exception ignored) {}
    }

    public Map<String, Object> getGlobalCounter() {
        ensureGlobalCounterLoaded();
        Map<String, Object> gc = new HashMap<>();
        gc.put("totalRequests",   GLOBAL_TOTAL_REQUESTS.get());
        gc.put("firstRecordedAt", GLOBAL_FIRST_RECORDED_AT.get());
        return gc;
    }

    /**
     * Starts log capture and timestamps the beginning of a catalog request.
     * Must be called after recording TRIGGERED and before the catalog action runs.
     */
    public void startRequest() {
        delegate.startRequest();
    }

    /**
     * Convenience method for recording MustAuthorizeException auth errors.
     * Uses the SDK's built-in error classification.
     */
    public void recordAuthError(String invokerId, String catalogName, MustAuthorizeException cause) {
        delegate.recordAuthError(invokerId, catalogName, cause);
    }

    /**
     * Returns filtered audit data from KV store.
     */
    public AuditData getData(String invokerId, Long fromTs, Long toTs, String type, String status) {
        return delegate.getData(invokerId, fromTs, toTs, type, status);
    }

    /**
     * Resets auth error counters and removes auth error records.
     * Called when credentials are saved successfully.
     */
    @Override
    public void resetAuthCounters() {
        delegate.resetAuthCounters();
    }

    /**
     * Clears records within [fromTs, toTs] and recalculates the summary.
     */
    public void clearRecords(String invokerId, Long fromTs, Long toTs) {
        delegate.clearRecords(invokerId, fromTs, toTs);
    }
}
