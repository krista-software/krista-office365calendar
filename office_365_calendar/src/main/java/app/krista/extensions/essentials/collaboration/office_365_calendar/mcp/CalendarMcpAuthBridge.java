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

package app.krista.extensions.essentials.collaboration.office_365_calendar.mcp;

import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.sdk.msauth.extension.mcp.McpAuthBridge;
import app.krista.sdk.msauth.extension.mcp.McpProductConfig;
import app.krista.sdk.msauth.extension.mcp.McpSearchContext;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.RefreshTokenParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom {@link McpAuthBridge} for the Office 365 Calendar extension.
 *
 * <p>Bridges the Calendar's {@link Office365CalendarAttributes} and
 * {@link RefreshTokenStore} (which do not implement the SDK's
 * {@code MsAuthAttributes} interface) to the MCP infrastructure.</p>
 *
 * <p>Token acquisition mirrors the logic in {@link
 * app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider}:
 * <ul>
 *   <li>Public (OAuth 2.0): MSAL4J {@code RefreshTokenParameters} using the
 *       stored refresh token keyed as {@code email_clientId_Public}.</li>
 *   <li>Private (Client Credentials): MSAL4J {@code ClientCredentialParameters}
 *       with {@code https://graph.microsoft.com/.default} scope.</li>
 * </ul>
 */
public class CalendarMcpAuthBridge implements McpAuthBridge {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarMcpAuthBridge.class);
    private static final String CLIENT_CREDENTIAL_SCOPE = "https://graph.microsoft.com/.default";
    private static final String AUTHORITY_BASE = "https://login.microsoftonline.com/";
    private static final String ORG_AUTHORITY = "https://login.microsoftonline.com/organizations/";

    private final Office365CalendarAttributes attrs;
    private final RefreshTokenStore tokenStore;

    public CalendarMcpAuthBridge(Office365CalendarAttributes attrs, RefreshTokenStore tokenStore) {
        this.attrs = attrs;
        this.tokenStore = tokenStore;
    }

    @Override
    public boolean isAuthenticated() {
        if (attrs == null) {
            return false;
        }
        if (Constants.PRIVATE.equals(attrs.getAuthType())) {
            return notBlank(attrs.getClientId())
                    && notBlank(attrs.getClientSecret())
                    && notBlank(attrs.getTenantId());
        }
        // Public (OAuth 2.0): check that a refresh token exists
        String key = tokenKey();
        return key != null && tokenStore.get(key) != null;
    }

    @Override
    public McpSearchContext buildSearchContext(McpProductConfig config) {
        if (!isAuthenticated()) {
            throw new IllegalStateException(
                    "Not authenticated. Configure credentials in the Authentication tab.");
        }
        try {
            String accessToken = Constants.PRIVATE.equals(attrs.getAuthType())
                    ? acquireTokenClientCredentials()
                    : acquireTokenRefresh();
            return new McpSearchContext(config.searchUrl(), "Bearer " + accessToken,
                    "https://graph.microsoft.com");
        } catch (Exception e) {
            LOG.error("[MCP-CAL] Failed to acquire access token", e);
            throw new IllegalStateException("Failed to acquire access token: " + e.getMessage(), e);
        }
    }

    @Override
    public String authSummary() {
        if (attrs == null) {
            return null;
        }
        if (Constants.PRIVATE.equals(attrs.getAuthType())) {
            String clientId = attrs.getClientId();
            String label = (clientId != null && clientId.length() > 4)
                    ? clientId.substring(0, 4) + "\u2026" : (clientId != null ? clientId : "");
            return "Service to Service (App: " + label + ")";
        }
        String email = attrs.getEmail();
        if (notBlank(email)) {
            return "OAuth 2.0 (" + email + ")";
        }
        return "OAuth 2.0";
    }

    @Override
    public String siteUrl() {
        return "https://graph.microsoft.com";
    }

    @Override
    public String userEmail() {
        return attrs != null ? attrs.getEmail() : null;
    }

    @Override
    public boolean isOAuth() {
        return true;
    }

    @Override
    public String maskedClientId() {
        return mask(attrs != null ? attrs.getClientId() : null, 4);
    }

    @Override
    public String maskedClientSecret() {
        return mask(attrs != null ? attrs.getClientSecret() : null, 0);
    }

    // ── Token acquisition ────────────────────────────────────────────────

    private String acquireTokenRefresh() throws Exception {
        String key = tokenKey();
        String refreshToken = tokenStore.get(key);
        if (refreshToken == null) {
            throw new IllegalStateException(
                    "No refresh token found. Please re-authenticate in the Authentication tab.");
        }

        Set<String> scopes = Arrays.stream(Constants.REQUIRED_SCOPE.split(" "))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        IClientCredential credential = ClientCredentialFactory.createFromSecret(attrs.getClientSecret());
        String authority = resolveAuthority();

        ConfidentialClientApplication cca = ConfidentialClientApplication
                .builder(attrs.getClientId(), credential)
                .authority(authority)
                .build();

        RefreshTokenParameters params = RefreshTokenParameters.builder(scopes, refreshToken).build();
        IAuthenticationResult result = cca.acquireToken(params).get();
        return result.accessToken();
    }

    private String acquireTokenClientCredentials() throws Exception {
        String tenantId = attrs.getTenantId();
        if (!notBlank(tenantId) && notBlank(attrs.getEmail()) && attrs.getEmail().contains("@")) {
            tenantId = attrs.getEmail().substring(attrs.getEmail().indexOf('@') + 1);
        }
        String authority = AUTHORITY_BASE + tenantId;

        IClientCredential credential = ClientCredentialFactory.createFromSecret(attrs.getClientSecret());
        ConfidentialClientApplication cca = ConfidentialClientApplication
                .builder(attrs.getClientId(), credential)
                .authority(authority)
                .build();

        ClientCredentialParameters params = ClientCredentialParameters
                .builder(Collections.singleton(CLIENT_CREDENTIAL_SCOPE))
                .build();
        IAuthenticationResult result = cca.acquireToken(params).get();
        return result.accessToken();
    }

    private String resolveAuthority() {
        if (notBlank(attrs.getTenantId())) {
            return AUTHORITY_BASE + attrs.getTenantId();
        }
        if (notBlank(attrs.getEmail()) && attrs.getEmail().contains("@")) {
            return AUTHORITY_BASE + attrs.getEmail().substring(attrs.getEmail().indexOf('@') + 1);
        }
        return ORG_AUTHORITY;
    }

    /**
     * Token key matching the format used by
     * {@code GraphServiceClientProvider.getRefTokenStoreKey()}:
     * {@code email_clientId_authType}.
     */
    private String tokenKey() {
        if (attrs == null || attrs.getEmail() == null) {
            return null;
        }
        return attrs.getEmail()
                + Constants.UNDER_SCORE + attrs.getClientId()
                + Constants.UNDER_SCORE + attrs.getAuthType();
    }

    private static String mask(String value, int visibleChars) {
        if (value == null || value.isBlank()) {
            return "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022";
        }
        if (visibleChars > 0 && value.length() > visibleChars) {
            return value.substring(0, visibleChars) + "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022";
        }
        return "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022";
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
