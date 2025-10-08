package app.krista.extensions.essentials.collaboration.office_365_calendar;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.Java;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;

import javax.inject.Inject;
import java.util.Map;

@Java(version = Java.Version.JAVA_21)
@Extension(version = "2.0.18", name = "Office 365 Calendar")
@StaticResource(path = "docs", file = "docs")
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
