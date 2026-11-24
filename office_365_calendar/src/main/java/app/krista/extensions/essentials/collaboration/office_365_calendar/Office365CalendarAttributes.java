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

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants.*;
import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Validators.addAttributeIfNotNull;

/**
 * Main extension entry point for Office 365 Calendar integration with the Krista platform.
 * <p>
 * This class manages authentication, connection validation, and configuration updates for the Office 365 Calendar extension.
 * It exposes invoker requests for authentication, documentation tabs, attribute validation, connection testing, and attribute updates.
 * </p>
 */
public final class Office365CalendarAttributes {

    private static final Logger LOGGER = LoggerFactory.getLogger(Office365CalendarAttributes.class);
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
    private String privateAuthMode;

    public Office365CalendarAttributes(String clientId, String clientSecret, String tenantId, String email,
                                       String timeZone, String authType, String routingUrl) {
        loadPublicConfig();
        this.clientId = clientId == null ? publicClientId : clientId;
        this.clientSecret = clientSecret == null ? publicClientSecret : clientSecret;
        this.tenantId = tenantId;
        this.email = email;
        this.authType = authType;
        this.timeZone = timeZone;
        this.routingUrl = routingUrl.replace(LOCAL_EXTN_URL, LOCAL_EXTN_REPLACE_URL);
    }




    public static Office365CalendarAttributes create(JsonObject authPayload, String baseurl) {
        String authType = authPayload.get("authType").getAsString();
        if (Constants.PUBLIC.equals(authType)) {
            String publicAuthMode = authPayload.has("publicAuthMode") ? authPayload.get("publicAuthMode").getAsString() : "krista";
            if ("own".equals(publicAuthMode)) {
                return new Office365CalendarAttributes(
                        authPayload.get(CLIENT_ID).getAsString(),
                        authPayload.get(CLIENT_SECRET).getAsString(),
                        authPayload.has(TENANT_ID) && !authPayload.get(TENANT_ID).getAsString().isBlank()
                                ? authPayload.get(TENANT_ID).getAsString() : null,
                        authPayload.get(EMAIL).getAsString(),
                        authPayload.has(TIMEZONE) ? authPayload.get(TIMEZONE).getAsString() : "",
                        authType, baseurl);
            }
            return new Office365CalendarAttributes(null, null, null, authPayload.get(EMAIL).getAsString(),
                    authPayload.has(TIMEZONE) ? authPayload.get(TIMEZONE).getAsString() : "", authType, baseurl);
        } else if (Constants.PRIVATE.equals(authType)) {
            Office365CalendarAttributes result = new Office365CalendarAttributes(
                    authPayload.get(CLIENT_ID).getAsString(),
                    authPayload.get(CLIENT_SECRET).getAsString(),
                    authPayload.get(TENANT_ID).getAsString(),
                    authPayload.get(EMAIL).getAsString(),
                    authPayload.has(TIMEZONE) ? authPayload.get(TIMEZONE).getAsString() : "",
                    authType, baseurl);
            result.setPrivateAuthMode("own");
            return result;
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

    public String getPrivateAuthMode() {
        return privateAuthMode;
    }

    public void setPrivateAuthMode(String privateAuthMode) {
        this.privateAuthMode = privateAuthMode;
    }

    public String getCallbackEndPoint() {
        if (Constants.PUBLIC.equals(authType)) {
            LOGGER.debug("→ getCallbackEndPoint: Using PUBLIC routing URL: {}", routingUrl);
            return routingUrl.replaceAll("/extension/api/.*", DEFAULT_CALLBACK_PATH);
        } else if (Constants.PRIVATE.equals(authType)) {
            return routingUrl + "/rest/calendar/callback";
        } else {
            return EMPTY_STRING;
        }
    }

    public String getForwardPath() {
        if (Constants.PUBLIC.equals(authType)) {
            return routingUrl + EXTENSION_FORWARD_PATH;
        } else {
            return EMPTY_STRING;
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
        if (Constants.PUBLIC.equals(authType)) {
            boolean isOwn = clientId != null && publicClientId != null && !clientId.equals(publicClientId);
            attributesMap.put("publicAuthMode", isOwn ? "own" : "krista");
        }
        if (Constants.PRIVATE.equals(authType) && privateAuthMode != null) {
            attributesMap.put("privateAuthMode", privateAuthMode);
        }
        return attributesMap;
    }



    public synchronized OAuth20Service getOAuth20Service() {
        if (oAuth20Service == null) {
            // Use tenant-specific endpoint when tenantId is available (even for PUBLIC auth
            // with a single-tenant Azure AD app). Fall back to deriving tenant from email domain.
            String effectiveTenant = tenantId;
            if (effectiveTenant == null || effectiveTenant.isBlank()) {
                if (email != null && email.contains("@")) {
                    effectiveTenant = email.substring(email.indexOf("@") + 1);
                    LOGGER.info("TenantId not configured, derived from email domain: {}", effectiveTenant);
                }
            }
            boolean useTenantEndpoint = effectiveTenant != null && !effectiveTenant.isBlank();
            oAuth20Service = new ServiceBuilder(clientId)
                    .apiSecret(clientSecret)
                    .defaultScope(Constants.REQUIRED_SCOPE)
                    .callback(getCallbackEndPoint())
                    .build(useTenantEndpoint
                            ? MicrosoftAzureActiveDirectory20Api.custom(effectiveTenant)
                            : MicrosoftAzureActiveDirectory20Api.instance());
        }
        return oAuth20Service;
    }
}
