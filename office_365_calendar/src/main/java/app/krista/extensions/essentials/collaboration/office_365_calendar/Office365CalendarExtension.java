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

package app.krista.extensions.essentials.collaboration.office_365_calendar;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.*;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;

import javax.inject.Inject;
import java.util.Map;

@Java(version = Java.Version.JAVA_21)
@Extension(version = "2.0.25", name = "Office 365 Calendar")
@StaticResource(path = "docs", file = "docs")
@ChangeLog(file = "resources/docs/pages/releaseNotes.md")
public final class Office365CalendarExtension {

    private final Office365CalendarRequestAuthenticator requestAuthenticator;
    private final GraphServiceClientProviderFactory providerFactory;
    private final String routingUrl;
    private final TelemetryMetrics telemetryMetrics;

    @Inject
    public Office365CalendarExtension(Invoker invoker, Office365CalendarAttributeStore attributeStore,
                                      GraphServiceClientProviderFactory providerFactory,
                                      AuthorizationContext authorizationContext, TelemetryMetrics telemetryMetrics) {
        this.requestAuthenticator = new Office365CalendarRequestAuthenticator(attributeStore, invoker.getInvokerId(), authorizationContext);
        this.providerFactory = providerFactory;
        this.routingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.telemetryMetrics = telemetryMetrics;
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Authentication", "rest/calendar/docs/", "Documentation", "static/docs");
    }

    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        throw new IllegalArgumentException("Please use Authentication tab for test connection.");
    }
}
