package app.krista.extensions.essentials.collaboration.office_365_calendar;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extension.util.InvokerAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants.*;

/**
 * Main extension entry point for Office 365 Calendar integration with the Krista platform.
 * <p>
 * This class manages authentication, connection validation, and configuration updates for the Office 365 Calendar extension.
 * It exposes invoker requests for authentication, documentation tabs, attribute validation, connection testing, and attribute updates.
 * </p>
 */
@Service
public final class Office365CalendarAttributes {

    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String TENANT_ID = "tenantId";
    public static final String EMAIL = "email";
    public static final String TIMEZONE = "timeZone";
    public static final String AUTH_TYPE = "authType";

    @SerializedName(EMAIL)
    private final String email;
    @SerializedName(CLIENT_ID)
    private final String clientId;
    @SerializedName(CLIENT_SECRET)
    private final String clientSecret;
    @SerializedName(AUTH_TYPE)
    private final String authType;
    @SerializedName(TENANT_ID)
    private final String tenantId;
    @SerializedName(TIMEZONE)
    private final String timeZone;

    private final String routingUrl;
    private String publicClientId;
    private String publicClientSecret;
    private OAuth20Service oAuth20Service;

    @Inject
    public Office365CalendarAttributes(Invoker invoker) {
        this(invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                invoker.getAttributes());
    }

    public Office365CalendarAttributes(String routingUrl, Map<String, Object> attributes) {
        this(InvokerAttributes.getStringOrNull(attributes, CLIENT_ID),
             InvokerAttributes.getStringOrNull(attributes, CLIENT_SECRET),
             InvokerAttributes.getStringOrNull(attributes, TENANT_ID),
             InvokerAttributes.getStringOrNull(attributes, EMAIL),
             InvokerAttributes.getStringOrNull(attributes, TIMEZONE),
             InvokerAttributes.getStringOrNull(attributes, AUTH_TYPE),
             routingUrl);
    }

    public Office365CalendarAttributes(String clientId, String clientSecret, String tenantId, String email,
                                     String timeZone, String authType, String routingUrl) {
        loadPublicConfig();
        this.clientId = clientId == null ? publicClientId : clientId;
        this.clientSecret = clientSecret == null ? publicClientSecret : clientSecret;
        this.tenantId = tenantId;
        this.email = email;
        this.timeZone = timeZone;
        this.authType = authType;
        this.routingUrl = routingUrl.replace(LOCAL_EXTN_URL, LOCAL_EXTN_REPLACE_URL);
    }



    public static Office365CalendarAttributes create(Invoker invoker, Map<String, Object> attributes) {
        return new Office365CalendarAttributes(
                invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                attributes);
    }

    public static Office365CalendarAttributes create(JsonObject authPayload, String baseurl) {
        String authType = authPayload.get("authType").getAsString();
        if (Constants.PUBLIC.equals(authType)) {
            return new Office365CalendarAttributes(null, null, null, authPayload.get(EMAIL).getAsString(),
                    null, authType, baseurl);
        } else if (Constants.PRIVATE.equals(authType)) {
            return new Office365CalendarAttributes(authPayload.get(CLIENT_ID).getAsString(),
                    authPayload.get(CLIENT_SECRET).getAsString(),
                    authPayload.get(TENANT_ID).getAsString(),
                    authPayload.get(EMAIL).getAsString(),
                    authPayload.has(TIMEZONE) ? authPayload.get(TIMEZONE).getAsString() : null,
                    authType, baseurl);
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_AUTH);
        }
    }

    private void loadPublicConfig() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
            this.publicClientId = properties.getProperty("public.clientId");
            this.publicClientSecret = properties.getProperty("public.clientSecret");
        } catch (IOException cause) {
            throw new RuntimeException("Failed to load public configuration file.", cause);
        }
    }

    /**
     * @param newAttributes This function updates the attributes provided by user
     */
    public void update(Map<String, Object> newAttributes) {
        // Note: These fields are final, so we can't update them directly
        // This method might need to be reconsidered or removed
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

    public String getEmail() {
        return email;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getAuthType() {
        return authType;
    }

    public String getCallbackUrl() {
        if (Constants.PUBLIC.equals(authType)) {
            return routingUrl.replaceAll("/extension/api/.*", DEFAULT_CALLBACK_PATH);
        } else if (Constants.PRIVATE.equals(authType)) {
            return routingUrl + "/rest/calendar/callback";
        } else {
            return routingUrl + "/rest/calendar/callback";
        }
    }

    public String getForwardPath() {
        if (Constants.PUBLIC.equals(authType)) {
            return routingUrl + EXTENSION_FORWARD_PATH;
        } else {
            return "";
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> attributesMap = new HashMap<>();
        addAttributeIfNotNull(attributesMap, CLIENT_ID, getClientId());
        addAttributeIfNotNull(attributesMap, CLIENT_SECRET, getClientSecret());
        addAttributeIfNotNull(attributesMap, TENANT_ID, getTenantId());
        addAttributeIfNotNull(attributesMap, EMAIL, getEmail());
        addAttributeIfNotNull(attributesMap, TIMEZONE, getTimeZone());
        addAttributeIfNotNull(attributesMap, AUTH_TYPE, getAuthType());
        return attributesMap;
    }

    private void addAttributeIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    public synchronized OAuth20Service getOAuth20Service() {
        if (oAuth20Service == null) {
            String authority = Constants.PUBLIC.equals(authType) ? ORG_AUTHORITY : AUTHORITY + tenantId + "/";
            oAuth20Service = new ServiceBuilder(clientId)
                    .apiSecret(clientSecret)
                    .defaultScope(Constants.REQUIRED_SCOPE)
                    .callback(getCallbackUrl())
                    .build(MicrosoftAzureActiveDirectory20Api.custom(authority));
        }
        return oAuth20Service;
    }
}
