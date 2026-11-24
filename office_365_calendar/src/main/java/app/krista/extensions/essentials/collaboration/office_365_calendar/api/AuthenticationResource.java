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
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.TestConnectionServiceImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.audit.Office365CalendarAuditService;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.AuthenticationResponse;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.gson.JsonObject;
import com.microsoft.graph.http.GraphServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import app.krista.extension.authorization.MustAuthorizeException;

/**
 * REST resource for handling authentication with Office 365 Calendar integration.
 * <p>
 * Provides endpoints for initiating authentication, handling OAuth callbacks,
 * and managing access tokens for Office 365 calendar operations.
 * </p>
 *
 * <p>
 * This resource is typically used by the Krista platform to facilitate secure
 * integration with Microsoft Office 365 services, enabling calendar event
 * synchronization and user authorization.
 * </p>
 *
 * @author [Your Name]
 * @since 1.0
 */
@Path("/")
public final class AuthenticationResource {

    public static final String USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST = "User Authenticated Successfully. Please proceed with the request.";
    public static final String ERROR_DESCRIPTION = "error_description";
    private static final String CODE = "code";
    private static final String STATE = "state";
    private static final String UNDER_SCORE = "_";
    private final Office365CalendarAttributeStore office365CalendarAttributeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final GraphServiceClientProviderFactory providerFactory;
    private final AuthorizationContext context;
    private final AuthorizationListener authorizationListener;
    private final TestConnectionServiceImpl testConnectionService;
    private final Office365CalendarAuditService auditService;
    private final String baseRoutingUrl;
    private final String invokerId;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);

    @Inject
    public AuthenticationResource(Office365CalendarAttributeStore office365CalendarAttributeStore,
                                RefreshTokenStore refreshTokenStore,
                                GraphServiceClientProviderFactory providerFactory,
                                AuthorizationContext context,
                                AuthorizationListener authorizationListener,
                                TestConnectionServiceImpl testConnectionService,
                                Office365CalendarAuditService auditService,
                                Invoker invoker) {
        LOGGER.info("→ AuthenticationResource: Initializing with injected dependencies");

        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.providerFactory = providerFactory;
        this.context = context;
        this.authorizationListener = authorizationListener;
        this.testConnectionService = testConnectionService;
        this.auditService = auditService;
        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.invokerId = invoker.getInvokerId();
    }

    /**
     * Handles the OAuth callback from Microsoft after user authentication.
     *
     * @param code  The authorization code returned by Microsoft.
     * @param state The state parameter to prevent CSRF attacks.
     * @return A map containing the access token and related information.
     * @throws OAuthException        If an OAuth-specific error occurs.
     * @throws ExecutionException    If an error occurs during asynchronous execution.
     * @throws InterruptedException  If the thread is interrupted during execution.
     * @throws IOException           If an I/O error occurs.
     */
    @GET
    @Path("/callback")
    public String getCallBack(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        String result = getAuthenticationResponseMessage(code, state);
        return result;
    }

    @GET
    @Path("v3/oauth/callback")
    public String getCallbackForV3Auth(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        String result = getAuthenticationResponseMessage(code, state);
        return result;
    }

    private String getAuthenticationResponseMessage(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        LOGGER.info("→ getAuthenticationResponseMessage: Processing OAuth callback with code: [{}], state: [{}]", code, state);

        if (code == null) {
            LOGGER.debug("Missing authorization code in callback. Rejecting authentication.");
            return "Authentication Failed. Please re-authorize.";
        }

        String[] parts = state.split(Constants.HASH);
        if (parts[0].isBlank()) {
            LOGGER.error("State parameter is invalid or empty.");
            throw new BadRequestException(Constants.INVALID_STATE_PARAMETERS);
        }

        String key = parts[0];
        String authContextId = getAuthContextId(state);

        LOGGER.debug("→ Parsed key: [{}] from state. AuthContextId resolved: [{}]", key, authContextId);

        try {
            LOGGER.info("Retrieving GraphServiceClientProvider using key='{}' authContextId='{}'", key, authContextId);
            // Per the architectural rule (docs/4.0.4-auth-recovery.md):
            //   key.startsWith(WS_CONTACT) → per-user OAuth callback, force default Public.
            //   The token exchange MUST use the same OAuth client that built the auth URL,
            //   otherwise Microsoft rejects with AADSTS7000215.
            GraphServiceClientProvider clientProvider = getGraphServiceClientProvider(authContextId, key);

            LOGGER.debug("Fetching Office 365 Calendar attributes for authentication...");
            Office365CalendarAttributes office365CalendarAttributes = clientProvider.getOffice365CalendarAttributes();

            // Construct compound key for token storage (matches lookup in getRefTokenStoreKey)
            String refreshTokenKey;
            if (key.startsWith(Constants.WS_CONTACT)) {
                refreshTokenKey = key;
            } else {
                refreshTokenKey = key + UNDER_SCORE
                        + office365CalendarAttributes.getClientId() + UNDER_SCORE
                        + office365CalendarAttributes.getAuthType();
            }

            LOGGER.debug("Initializing OAuth20Service for token exchange...");
            OAuth2AccessToken accessToken = office365CalendarAttributes.getOAuth20Service().getAccessToken(code);
            LOGGER.info("Access token retrieved successfully.");

            refreshTokenStore.put(refreshTokenKey, accessToken.getRefreshToken());
            LOGGER.debug("Refresh token stored for key: {}", refreshTokenKey);

            if (!key.startsWith(Constants.WS_CONTACT) && !hasUserAccess(clientProvider)) {
                LOGGER.debug("User access check failed. Authenticated user does not match setup user.");
                refreshTokenStore.remove(refreshTokenKey);
                return Constants.UNAUTHORISED_USER + " User email '" + key + "' configured in the setup does not match with authenticated user.";
            }

            if (context.isAuthenticated()) {
                LOGGER.info("User is authenticated. Notifying authorization listener.");
                authorizationListener.authorized();
                return USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST;
            }

            LOGGER.info("User authenticated successfully but not yet in active context.");
            return Constants.USER_AUTHENTICATED_SUCCESSFULLY_SAVE_THE_CHANGES;
        } catch (OAuthException cause) {
            LOGGER.error("OAuthException occurred during token exchange: {}", cause.getMessage(), cause);
            throw new IllegalStateException(getErrorDescription(cause), cause.getCause());

        } catch (IOException | ExecutionException cause) {
            LOGGER.error("Exception occurred during authorization (I/O or Execution): {}", cause.getMessage(), cause);
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, cause.getCause());

        } catch (InterruptedException cause) {
            LOGGER.error("InterruptedException occurred during authorization: {}", cause.getMessage(), cause);
            Thread.currentThread().interrupt();
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, cause.getCause());
        }
    }

    private String getAuthContextId(String state) {
        LOGGER.debug("→ getAuthContextId: Extracting authContextId from state: {}", state);
        // Simplified state encoding (4.0.4 recovery): max 3 parts —
        //   <userId>#<authContextId>#<forwardPath>   where forwardPath is optional and only
        //                                            present for Setup-tab Public flows.
        // The 4-part "MCP" encoding (wsContact#INV_..#tempUUID#forwardPath) introduced by
        // 4.0.2/4.0.3 was reverted per docs/4.0.4-auth-recovery.md.
        String[] parts = state.split(Constants.HASH);
        if (parts.length <= 1) {
            LOGGER.debug("→ getAuthContextId: No authContextId in state (single part)");
            return null;
        }
        // For PUBLIC auth without authContext, state is "userId#forwardPath" where forwardPath
        // is a URL (e.g. https://...). Distinguish from a real authContextId (UUID).
        String candidate = parts[1];
        if (candidate.startsWith("http://") || candidate.startsWith("https://") || candidate.startsWith("/")) {
            LOGGER.debug("→ getAuthContextId: parts[1] is a forward path URL, not an authContextId: {}", candidate);
            return null;
        }
        LOGGER.debug("→ getAuthContextId: Extracted authContextId: {}", candidate);
        return candidate;
    }

    /**
     * Creates a GraphServiceClientProvider for the OAuth callback's token-exchange step.
     *
     * <p>Per the architectural rule (docs/4.0.4-auth-recovery.md): the token exchange MUST use
     * the SAME OAuth client (clientId, clientSecret, redirect_uri) that built the auth URL.
     * For per-user callbacks (state's parts[0] is wsContact-prefixed), the auth URL was built
     * with default Public, so token exchange uses default Public too — irrespective of any
     * stored Setup attrs.</p>
     */
    private GraphServiceClientProvider getGraphServiceClientProvider(String authContextId, String key) {
        Office365CalendarAttributes attrs;
        if (key != null && key.startsWith(Constants.WS_CONTACT)) {
            // Per-user callback — same default-Public client that built the auth URL.
            attrs = new Office365CalendarAttributes(null, null, null, "", null, Constants.PUBLIC, baseRoutingUrl);
            return providerFactory.create(attrs);
        }
        if (authContextId != null) {
            return providerFactory.create(authContextId);
        }
        return providerFactory.create();
    }

    /**
     * Returns a user-friendly error description based on the provided exception.
     *
     * @param cause The exception encountered during authentication or API calls.
     * @return A string describing the error in a user-friendly manner.
     */
    private String getErrorDescription(OAuthException cause) {
        LOGGER.debug("→ getErrorDescription: Processing OAuth exception message");

        String errorDescription = "Error occurred during authorization. ";
        if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
            LOGGER.debug("→ Exception message available: {}", cause.getMessage());
            try {
                var exceptionMap = Constants.GSON.fromJson(cause.getMessage(), Map.class);
                LOGGER.debug("→ Parsed exception message as JSON map");

                if (exceptionMap.containsKey(ERROR_DESCRIPTION)) {
                    String detailedError = (String) exceptionMap.get(ERROR_DESCRIPTION);
                    errorDescription = errorDescription + detailedError;
                    LOGGER.debug("→ Added detailed error description: {}", detailedError);
                } else {
                    LOGGER.debug("→ No error_description found in exception map");
                }
            } catch (MustAuthorizeException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.warn("→ Failed to parse exception message as JSON: {}", e.getMessage());
            }
        } else {
            LOGGER.debug("→ No exception message available");
        }

        LOGGER.debug("→ getErrorDescription: Returning error description: {}", errorDescription);
        return errorDescription;
    }

    /**
     * Checks if the current user has access rights using the provided GraphServiceClientProvider.
     *
     * @param graphServiceClientForAdmin The GraphServiceClientProvider instance configured for admin access.
     * @return {@code true} if the user has access; {@code false} otherwise.
     */
    private String extractEmailFromAccessToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                int mod = parts[1].length() % 4;
                String padded = mod > 0 ? parts[1] + "=".repeat(4 - mod) : parts[1];
                byte[] decoded = java.util.Base64.getUrlDecoder().decode(padded);
                Map<?, ?> claims = Constants.GSON.fromJson(new String(decoded), Map.class);
                if (claims.containsKey("preferred_username")) return (String) claims.get("preferred_username");
                if (claims.containsKey("upn")) return (String) claims.get("upn");
                if (claims.containsKey("email")) return (String) claims.get("email");
            }
        } catch (MustAuthorizeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("Failed to extract email from access token: {}", e.getMessage());
        }
        return null;
    }

    private boolean hasUserAccess(GraphServiceClientProvider graphServiceClientForAdmin) {
        LOGGER.debug("→ hasUserAccess: Checking user access permissions");

        try {
            graphServiceClientForAdmin.getUserRequestBuilder(null, null).calendars().buildRequest().get();
            LOGGER.debug("→ hasUserAccess: Successfully accessed calendars - user has access");
            return true;
        } catch (GraphServiceException ignore) {
            LOGGER.debug("→ hasUserAccess: GraphServiceException occurred - user does not have access: {}", ignore.getMessage());
            LOGGER.debug(Constants.IGNORED_EXCEPTION);
            return false;
        }
    }

    /**
     * Saves credentials after validation.
     *
     * @param authPayload JSON payload containing authentication details
     * @return JSON response indicating success or failure
     */
    @POST
    @Path("/saveCredentials")
    @Produces("text/plain")
    public String saveCredentials(JsonObject authPayload) {
        LOGGER.info("→ saveCredentials: Processing save credentials request");

        Office365CalendarAttributes attributes = Office365CalendarAttributes.create(authPayload, baseRoutingUrl);
        String authContextId = providerFactory.createAttributes(attributes);
        String authUrl = null;

        try {
            LOGGER.debug("→ Testing connection before saving credentials");
            if (Constants.PRIVATE.equals(attributes.getAuthType())) {
                // Server-Server (Client Credentials): acquire token directly, no user popup
                providerFactory.create(authContextId).getGraphServiceClientForSystem()
                        .users(attributes.getEmail())
                        .calendars().buildRequest().get();
            } else {
                // OAuth 2.0 (delegated): uses stored refresh token
                providerFactory.create(authContextId).getGraphServiceClientForAdmin()
                        .users(attributes.getEmail())
                        .calendars().buildRequest().get();
            }

            boolean isSaved = office365CalendarAttributeStore.save(attributes, invokerId);
            LOGGER.info("→ saveCredentials: Credentials saved successfully: {}", isSaved);

            if (isSaved) {
                auditService.resetAuthCounters();
            }

            String result = isSaved
                    ? Constants.GSON.toJson(new AuthenticationResponse(true, null, null))
                    : Constants.GSON.toJson(new AuthenticationResponse(false, "Failed to save attributes", null));
            LOGGER.debug("→ saveCredentials: Returning result for save operation: {}", isSaved ? "success" : "failure");
            return result;
        } catch (MustAuthorizeException cause) {
            if (Constants.PRIVATE.equals(attributes.getAuthType())) {
                LOGGER.warn("→ saveCredentials: Client credentials save failed: {}", cause.getMessage());
                auditService.record(invokerId, "AUTH_CONFIG_FAILURE", "failure", "Save Credentials",
                        cause.getMessage(), cause.getClass().getSimpleName(), null);
                return Constants.GSON.toJson(new AuthenticationResponse(false,
                        "Service to Service authentication failed. Please verify your credentials "
                        + "and ensure the app has the required Application permissions with admin consent.", null));
            }
            LOGGER.info("→ saveCredentials: Authorization required for public auth, generating auth URL");
            // Save credentials before OAuth redirect
            office365CalendarAttributeStore.save(attributes, invokerId);
            String state = attributes.getEmail() + Constants.HASH + authContextId;
            authUrl = attributes.getOAuth20Service().getAuthorizationUrl(state) + Constants.AUTH_URL_QUERY_PARAMS;
            return Constants.GSON.toJson(new AuthenticationResponse(false, Constants.AUTHORIZATION_PROMPT, authUrl));
        } catch (Exception cause) {
            LOGGER.error("→ saveCredentials: Failed to save credentials", cause);
            if (Constants.PRIVATE.equals(attributes.getAuthType())) {
                auditService.record(invokerId, "AUTH_CONFIG_FAILURE", "failure", "Save Credentials",
                        cause.getMessage(), cause.getClass().getSimpleName(), null);
                return Constants.GSON.toJson(new AuthenticationResponse(false,
                        "Service to Service authentication failed. Please verify your credentials "
                        + "and ensure the app has the required Application permissions with admin consent.", null));
            }
            return Constants.GSON.toJson(new AuthenticationResponse(false, "Failed to save attributes", null));
        } finally {
            LOGGER.debug("→ saveCredentials: Cleanup phase");
            if (authUrl == null) {
                LOGGER.debug("→ saveCredentials: No auth URL generated, removing temporary authContextId");
                office365CalendarAttributeStore.remove(authContextId);
            }
        }
    }

    /**
     * Tests connection with provided credentials.
     *
     * @param authPayload JSON payload containing authentication details
     * @return JSON response with connection test results
     */
    @POST
    @Path("/testConnection")
    @Produces("text/plain")
    public String testConnection(JsonObject authPayload) {
        LOGGER.info("→ testConnection: Processing test connection request");

        Office365CalendarAttributes office365CalendarAttributes = Office365CalendarAttributes.create(authPayload, baseRoutingUrl);
        return testConnectionService.testConnection(office365CalendarAttributes);
    }

    /**
     * Retrieves stored credentials for a specific authentication type.
     *
     * <p>Both Quick Setup (AUTH_CODE_PUBLIC) and User Authentication (AUTH_CODE_PRIVATE) share
     * {@code authType="Public"}, so {@code authFlow} is used to distinguish them via the
     * {@code publicAuthMode} field stored in attributes ("krista" vs "own").</p>
     *
     * @param authType The authentication type (Public or Private)
     * @param authFlow The authentication flow (AUTH_CODE_PUBLIC, AUTH_CODE_PRIVATE, CLIENT_CREDENTIALS_SECRET)
     * @return JSON representation of stored credentials, or empty string if not found
     */
    @GET
    @Path("/getCredentials")
    @Produces("text/plain")
    public String getCredentials(@QueryParam("authType") String authType,
                                 @QueryParam("authFlow") String authFlow) {
        LOGGER.info("→ getCredentials: Loading attributes for auth-type: {}, auth-flow: {} for invoker {}", authType, authFlow, invokerId);

        final Office365CalendarAttributes loadedAttributes = office365CalendarAttributeStore.load(invokerId);
        if (loadedAttributes == null || !authType.equals(loadedAttributes.getAuthType())) {
            return "";
        }
        // Both Quick Setup and User Authentication share authType="Public".
        // Use authFlow to differentiate: AUTH_CODE_PUBLIC = krista-managed, AUTH_CODE_PRIVATE = own app.
        if (Constants.PUBLIC.equals(authType) && authFlow != null) {
            java.util.Map<String, Object> map = loadedAttributes.toMap();
            String publicAuthMode = (String) map.get("publicAuthMode");
            boolean savedIsKrista = "krista".equals(publicAuthMode);
            boolean requestingQuickSetup = "AUTH_CODE_PUBLIC".equals(authFlow);
            if (savedIsKrista != requestingQuickSetup) {
                return "";
            }
        }
        return Constants.GSON.toJson(loadedAttributes.toMap());
    }

    /**
     * Retrieves the stored authentication key/type.
     *
     * @return JSON representation of the authentication type
     */
    @GET
    @Path("/getAuthKey")
    @Produces("text/plain")
    public String getAuthKey() {
        LOGGER.info("→ getAuthKey: Loading stored authentication key");

        final Office365CalendarAttributes attributes = office365CalendarAttributeStore.load(invokerId);
        if (attributes != null) {
            final String authType = attributes.getAuthType();
            return Constants.GSON.toJson(Objects.requireNonNullElse(authType, Constants.PUBLIC));
        } else {
            return "";
        }
    }

    /**
     * Returns extension metadata JSON for the SDK auth/audit HTML pages.
     */
    @GET
    @Path("/msauth/metadata/{extensionId}")
    @javax.ws.rs.Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public String getExtensionMetadata(@PathParam("extensionId") String extensionId) {
        app.krista.sdk.msauth.extension.registry.ExtensionRegistry.initialize();
        return app.krista.sdk.msauth.extension.registry.ExtensionRegistry.get(extensionId)
                .map(m -> m.toUiJson())
                .orElse("{\"error\":\"Extension not found: " + extensionId + "\"}");
    }

    /**
     * Serves SDK auth tab HTML.
     */
    @GET
    @Path("/authentication/{subPath:.*}")
    public InputStream authenticationTab(@PathParam("subPath") String subPath) {
        String filePath = "auth/" + (subPath.isEmpty() ? "index.html" : subPath);
        return getClass().getClassLoader().getResourceAsStream(filePath);
    }


    /**
     * Serves UI documentation files (legacy).
     *
     * @param subPath The sub-path for the requested file
     * @return InputStream of the requested file
     */
    @GET
    @Path("/docs/{subPath:.*}")
    public InputStream customTabs(@PathParam("subPath") String subPath) {
        String filePath = "ui/office365calendaroauth/" + (subPath.isEmpty() ? "index.html" : subPath);
        return getClass().getClassLoader().getResourceAsStream(filePath);
    }
}