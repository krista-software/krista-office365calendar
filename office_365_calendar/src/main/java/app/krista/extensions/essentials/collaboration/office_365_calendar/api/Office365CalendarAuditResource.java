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

import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.audit.Office365CalendarAuditService;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.sdk.msauth.extension.audit.AbstractAuditResource;
import app.krista.sdk.msauth.extension.audit.AuditData;
import app.krista.sdk.msauth.extension.audit.AuditRecord;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.*;

@Path("/audit")
public class Office365CalendarAuditResource extends AbstractAuditResource {

    private final Office365CalendarAuditService auditService;
    private final Office365CalendarAttributeStore attributeStore;
    private final RefreshTokenStore refreshTokenStore;

    @Inject
    public Office365CalendarAuditResource(Office365CalendarAuditService auditService,
                                           Office365CalendarAttributeStore attributeStore,
                                           RefreshTokenStore refreshTokenStore,
                                           Invoker invoker) {
        super(invoker.getInvokerId());
        this.auditService = auditService;
        this.attributeStore = attributeStore;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Override
    protected AuditData getAuditData(String invokerId, Long from, Long to, String type, String status) {
        return auditService.getData(invokerId, from, to, type, status);
    }

    @Override
    protected void clearAuditRecords(String invokerId, Long from, Long to) {
        auditService.clearRecords(invokerId, from, to);
    }

    @Override
    protected Map<String, Object> buildHealthResponse() {
        Map<String, Object> health = new HashMap<>();

        try {
            Office365CalendarAttributes attrs = attributeStore.load(getInvokerId());
            if (attrs != null) {
                health.put("authType", attrs.getAuthType());
                health.put("email", attrs.getEmail());
                health.put("tenantId", mask(attrs.getTenantId()));
                health.put("clientId", mask(attrs.getClientId()));

                String expiry = null;
                try {
                    java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties");
                    if (is != null) {
                        java.util.Properties props = new java.util.Properties();
                        props.load(is);
                        expiry = props.getProperty("public.expiryDate");
                        is.close();
                    }
                } catch (Exception ignored) {}
                health.put("clientSecretExpiry", expiry != null ? expiry : "\u2014");

                if ("Private".equalsIgnoreCase(attrs.getAuthType())) {
                    // Server-Server (Client Credentials) — no refresh token
                    health.put("hasRefreshToken", false);
                } else {
                    String email = attrs.getEmail();
                    String compositeKey = app.krista.sdk.msauth.MicrosoftAuthSdk.buildTokenKey(
                            email, attrs.getClientId(), attrs.getAuthType());
                    String refreshToken = refreshTokenStore.get(compositeKey);
                    if (refreshToken == null || refreshToken.isEmpty()) {
                        refreshToken = refreshTokenStore.get(email);
                    }
                    health.put("hasRefreshToken", refreshToken != null && !refreshToken.isEmpty());
                }
            } else {
                health.put("authType", "not configured");
                health.put("hasRefreshToken", false);
            }
        } catch (Exception e) {
            health.put("authType", "error");
            health.put("hasRefreshToken", false);
        }

        AuditData data = auditService.getData(getInvokerId(), null, null, null, null);
        List<AuditRecord> records = data.records;

        Set<String> authEventKeys = Set.of(
                "TOKEN_REFRESHED", "TOKEN_EXPIRY_DETECTED", "INVALID_TOKEN_DETECTED",
                "CLIENT_SECRET_EXPIRY_DETECTED", "INVALID_CLIENT_SECRET_DETECTED",
                "AUTH_CONFIG_FAILURE", "PASSWORD_CHANGE_DETECTED"
        );

        Map<String, Map<String, Object>> events = new HashMap<>();
        for (String key : authEventKeys) {
            Map<String, Object> ev = new HashMap<>();
            ev.put("count", 0);
            ev.put("lastSeen", null);
            events.put(key, ev);
        }
        for (AuditRecord r : records) {
            if (r.eventType != null && events.containsKey(r.eventType)) {
                Map<String, Object> ev = events.get(r.eventType);
                int cnt = (int) ev.get("count");
                ev.put("count", cnt + 1);
                Long prev = (Long) ev.get("lastSeen");
                if (prev == null || r.timestamp > prev) {
                    ev.put("lastSeen", r.timestamp);
                }
            }
        }
        health.put("events", events);

        return health;
    }

    @Override
    protected Map<String, Object> getGlobalCounter() {
        return auditService.getGlobalCounter();
    }
}
