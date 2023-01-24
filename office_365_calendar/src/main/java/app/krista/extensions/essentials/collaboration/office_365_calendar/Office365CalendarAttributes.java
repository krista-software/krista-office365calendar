package app.krista.extensions.essentials.collaboration.office_365_calendar;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extension.util.InvokerAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;

@Service
public final class Office365CalendarAttributes {

    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String TENANT_ID = "tenantId";
    public static final String EMAIL = "email";
    public static final String TIMEZONE = "timeZone";
    private final String routingUrl;
    private String clientId;
    private String clientSecret;
    private String email;
    private String tenantId;
    private String timeZone;
    private OAuth20Service oAuth20Service;

    public static Office365CalendarAttributes create(Invoker invoker, Map<String, Object> attributes) {
        return new Office365CalendarAttributes(
                invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                attributes);
    }

    @Inject
    public Office365CalendarAttributes(Invoker invoker) {
        this(invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                invoker.getAttributes());
    }

    public Office365CalendarAttributes(String routingUrl, Map<String, Object> attributes) {
        this(routingUrl, InvokerAttributes.getStringOrNull(attributes, CLIENT_ID),
                InvokerAttributes.getStringOrNull(attributes, CLIENT_SECRET),
                InvokerAttributes.getStringOrNull(attributes, EMAIL),
                InvokerAttributes.getStringOrNull(attributes, TENANT_ID),
                InvokerAttributes.getStringOrNull(attributes, TIMEZONE));
    }

    public Office365CalendarAttributes(String routingUrl, String clientId, String clientSecret, String email, String tenantId, String timeZone) {
        this.routingUrl = routingUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.email = email;
        this.tenantId = tenantId;
        this.timeZone = timeZone;
    }

    /**
     * @param newAttributes
     * This function updates the attributes provided by user
     */
    public void update(Map<String, Object> newAttributes) {
        clientId = InvokerAttributes.getStringOrNull(newAttributes, CLIENT_ID);
        clientSecret = InvokerAttributes.getStringOrNull(newAttributes, CLIENT_SECRET);
        email = InvokerAttributes.getStringOrNull(newAttributes, EMAIL);
        tenantId = InvokerAttributes.getStringOrNull(newAttributes, TENANT_ID);
        timeZone = InvokerAttributes.getStringOrNull(newAttributes, TIMEZONE);
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getEmail() { return email; }

    public String getTimeZone() {
        return timeZone;
    }

    public String getCallbackUrl() {
//        return routingUrl + "/rest/calendar/callback";
        return "http://localhost:8765/extension/api/QnEz1fIRGysvecLScUj5ZQ_e_e/rest/calendar/callback";
    }

    public synchronized OAuth20Service getOAuth20Service() {
        if (oAuth20Service == null) {
            oAuth20Service = new ServiceBuilder((clientId))
                    .apiSecret(clientSecret)
                    .defaultScope(GraphServiceClientProvider.REQUIRED_SCOPE)
                    .callback(getCallbackUrl())
                    .build(MicrosoftAzureActiveDirectory20Api.custom(tenantId));
        }
        return oAuth20Service;
    }
}
