package app.krista.extensions.essentials.collaboration.office_365_calendar.api;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.TestConnectionServiceImpl;
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
                                Invoker invoker) {
        LOGGER.info("→ AuthenticationResource: Initializing with injected dependencies");

        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        LOGGER.debug("→ Office365CalendarAttributeStore set");

        this.refreshTokenStore = refreshTokenStore;
        System.out.println("AuthenticationResource constructor - RefreshTokenStore set");
        LOGGER.debug("→ RefreshTokenStore set");

        this.providerFactory = providerFactory;
        System.out.println("AuthenticationResource constructor - GraphServiceClientProviderFactory set");
        LOGGER.debug("→ GraphServiceClientProviderFactory set");

        this.context = context;
        System.out.println("AuthenticationResource constructor - AuthorizationContext set with authenticated: " + context.isAuthenticated());
        LOGGER.debug("→ AuthorizationContext set with authenticated: {}", context.isAuthenticated());

        this.authorizationListener = authorizationListener;
        System.out.println("AuthenticationResource constructor - AuthorizationListener set");
        LOGGER.debug("→ AuthorizationListener set");

        this.testConnectionService = testConnectionService;
        System.out.println("AuthenticationResource constructor - TestConnectionService set");
        LOGGER.debug("→ TestConnectionService set");

        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.invokerId = invoker.getInvokerId();
        System.out.println("AuthenticationResource constructor - Base routing URL: " + baseRoutingUrl + ", Invoker ID: " + invokerId);
        LOGGER.debug("→ Base routing URL: {}, Invoker ID: {}", baseRoutingUrl, invokerId);

        System.out.println("AuthenticationResource constructor - END");
        LOGGER.info("→ AuthenticationResource: Initialization completed successfully");
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
        System.out.println("getCallBack method - START with code: " + code + ", state: " + state);
        String result = getAuthenticationResponseMessage(code, state);
        System.out.println("getCallBack method - END with result: " + result);
        return result;
    }

    @GET
    @Path("v3/oauth/callback")
    public String getCallbackForV3Auth(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        System.out.println("getCallbackForV3Auth method - START with code: " + code + ", state: " + state);
        String result = getAuthenticationResponseMessage(code, state);
        System.out.println("getCallbackForV3Auth method - END with result: " + result);
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
            System.out.println("getAuthenticationResponseMessage method - state parameter is invalid or empty");
            LOGGER.error("State parameter is invalid or empty.");
            throw new BadRequestException(Constants.INVALID_STATE_PARAMETERS);
        }

        String key = parts[0];
        String authContextId = getAuthContextId(state);
        System.out.println("getAuthenticationResponseMessage method - parsed key: " + key + ", authContextId: " + authContextId);

        LOGGER.debug("→ Parsed key: [{}] from state. AuthContextId resolved: [{}]", key, authContextId);

        try {
            System.out.println("getAuthenticationResponseMessage method - entering try block");
            LOGGER.info("Retrieving GraphServiceClientProvider using AuthContextId: {}", authContextId);
            GraphServiceClientProvider clientProvider = getGraphServiceClientProvider(authContextId);

            System.out.println("getAuthenticationResponseMessage method - fetching Office365CalendarAttributes");
            LOGGER.debug("Fetching Office 365 Calendar attributes for authentication...");
            Office365CalendarAttributes office365CalendarAttributes = clientProvider.getOffice365CalendarAttributes();

            System.out.println("getAuthenticationResponseMessage method - initializing OAuth20Service for token exchange");
            LOGGER.debug("Initializing OAuth20Service for token exchange...");
            OAuth2AccessToken accessToken = office365CalendarAttributes.getOAuth20Service().getAccessToken(code);
            System.out.println("getAuthenticationResponseMessage method - access token retrieved successfully");
            LOGGER.info("Access token retrieved successfully.");

            refreshTokenStore.put(key, accessToken.getRefreshToken());
            System.out.println("getAuthenticationResponseMessage method - refresh token stored for key: " + key);
            LOGGER.debug("Refresh token stored for key: {}", key);

            if (!key.startsWith(Constants.WS_CONTACT) && !hasUserAccess(clientProvider)) {
                LOGGER.debug("User access check failed. Authenticated user does not match setup user.");
                refreshTokenStore.remove(key);
                return Constants.UNAUTHORISED_USER + " User email '" + key.split(UNDER_SCORE)[0] + "' configured in the setup does not match with authenticated user.";
            }

            if (context.isAuthenticated()) {
                System.out.println("getAuthenticationResponseMessage method - user is authenticated, notifying authorization listener");
                LOGGER.info("User is authenticated. Notifying authorization listener.");
                authorizationListener.authorized();
                return USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST;
            }

            System.out.println("getAuthenticationResponseMessage method - user authenticated successfully but not yet in active context");
            LOGGER.info("User authenticated successfully but not yet in active context.");
            return Constants.USER_AUTHENTICATED_SUCCESSFULLY_SAVE_THE_CHANGES;
        } catch (OAuthException cause) {
            System.out.println("getAuthenticationResponseMessage method - OAuthException occurred: " + cause.getMessage());
            LOGGER.error("OAuthException occurred during token exchange: {}", cause.getMessage(), cause);
            throw new IllegalStateException(getErrorDescription(cause), cause.getCause());

        } catch (IOException | ExecutionException cause) {
            System.out.println("getAuthenticationResponseMessage method - IOException/ExecutionException occurred: " + cause.getMessage());
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
        String[] parts = state.split(Constants.HASH);
        String result = parts.length > 1 ? parts[1] : null;
        LOGGER.debug("→ getAuthContextId: Extracted authContextId: {}", result);
        return result;
    }

    private GraphServiceClientProvider getGraphServiceClientProvider(String authContextId) {
        System.out.println("getGraphServiceClientProvider method - START with authContextId: " + authContextId);
        GraphServiceClientProvider provider;
        if (authContextId == null) {
            System.out.println("getGraphServiceClientProvider method - authContextId is null, creating default provider");
            provider = providerFactory.create();
        } else {
            System.out.println("getGraphServiceClientProvider method - creating provider with authContextId: " + authContextId);
            provider = providerFactory.create(authContextId);
        }
        System.out.println("getGraphServiceClientProvider method - END");
        return provider;
    }

    /**
     * Returns a user-friendly error description based on the provided exception.
     *
     * @param cause The exception encountered during authentication or API calls.
     * @return A string describing the error in a user-friendly manner.
     */
    private String getErrorDescription(OAuthException cause) {
        System.out.println("getErrorDescription method - START");
        LOGGER.debug("→ getErrorDescription: Processing OAuth exception message");

        String errorDescription = "Error occurred during authorization. ";
        if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
            System.out.println("getErrorDescription method - exception message available: " + cause.getMessage());
            LOGGER.debug("→ Exception message available: {}", cause.getMessage());
            try {
                System.out.println("getErrorDescription method - entering try block to parse JSON");
                var exceptionMap = Constants.GSON.fromJson(cause.getMessage(), Map.class);
                System.out.println("getErrorDescription method - parsed exception message as JSON map");
                LOGGER.debug("→ Parsed exception message as JSON map");

                if (exceptionMap.containsKey(ERROR_DESCRIPTION)) {
                    String detailedError = (String) exceptionMap.get(ERROR_DESCRIPTION);
                    errorDescription = errorDescription + detailedError;
                    System.out.println("getErrorDescription method - added detailed error description: " + detailedError);
                    LOGGER.debug("→ Added detailed error description: {}", detailedError);
                } else {
                    System.out.println("getErrorDescription method - no error_description found in exception map");
                    LOGGER.debug("→ No error_description found in exception map");
                }
            } catch (Exception e) {
                System.out.println("getErrorDescription method - failed to parse exception message as JSON: " + e.getMessage());
                LOGGER.warn("→ Failed to parse exception message as JSON: {}", e.getMessage());
            }
        } else {
            System.out.println("getErrorDescription method - no exception message available");
            LOGGER.debug("→ No exception message available");
        }

        System.out.println("getErrorDescription method - END with result: " + errorDescription);
        LOGGER.debug("→ getErrorDescription: Returning error description: {}", errorDescription);
        return errorDescription;
    }

    /**
     * Checks if the current user has access rights using the provided GraphServiceClientProvider.
     *
     * @param graphServiceClientForAdmin The GraphServiceClientProvider instance configured for admin access.
     * @return {@code true} if the user has access; {@code false} otherwise.
     */
    private boolean hasUserAccess(GraphServiceClientProvider graphServiceClientForAdmin) {
        System.out.println("hasUserAccess method - START");
        LOGGER.debug("→ hasUserAccess: Checking user access permissions");

        try {
            System.out.println("hasUserAccess method - entering try block to check calendar access");
            graphServiceClientForAdmin.getUserRequestBuilder(null, null).calendars().buildRequest().get();
            System.out.println("hasUserAccess method - successfully accessed calendars - user has access");
            LOGGER.debug("→ hasUserAccess: Successfully accessed calendars - user has access");
            return true;
        } catch (GraphServiceException ignore) {
            System.out.println("hasUserAccess method - GraphServiceException occurred - user does not have access: " + ignore.getMessage());
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
        System.out.println("saveCredentials method - START");
        LOGGER.info("→ saveCredentials: Processing save credentials request");

        Office365CalendarAttributes attributes = Office365CalendarAttributes.create(authPayload, baseRoutingUrl);
        String authContextId = providerFactory.createAttributes(attributes);
        System.out.println("saveCredentials method - created attributes and authContextId: " + authContextId);

        try {
            System.out.println("saveCredentials method - entering try block to test connection");
            LOGGER.debug("→ Testing connection before saving credentials");
            providerFactory.create(authContextId).getGraphServiceClientForAdmin()
                    .users(attributes.getEmail())
                    .calendars().buildRequest().get();

            System.out.println("saveCredentials method - connection test successful, saving attributes");
            boolean isSaved = office365CalendarAttributeStore.save(attributes, invokerId);
            System.out.println("saveCredentials method - credentials saved: " + isSaved);
            LOGGER.info("→ saveCredentials: Credentials saved successfully: {}", isSaved);

            String result = isSaved
                    ? Constants.GSON.toJson(new AuthenticationResponse(true, null, null))
                    : Constants.GSON.toJson(new AuthenticationResponse(false, "Failed to save attributes", null));
            LOGGER.debug("→ saveCredentials: Returning result for save operation: {}", isSaved ? "success" : "failure");
            return result;
        } catch (Exception cause) {
            LOGGER.error("→ saveCredentials: Failed to save credentials", cause);
            return Constants.GSON.toJson(new AuthenticationResponse(false, "Failed to save attributes", null));
        } finally {
            LOGGER.debug("→ saveCredentials: Cleanup - removing temporary authContextId");
            office365CalendarAttributeStore.remove(authContextId);
            System.out.println("saveCredentials method - END");
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
        System.out.println("testConnection method - START");
        LOGGER.info("→ testConnection: Processing test connection request");

        Office365CalendarAttributes office365CalendarAttributes = Office365CalendarAttributes.create(authPayload, baseRoutingUrl);
        System.out.println("testConnection method - created Office365CalendarAttributes, calling testConnectionService");
        String result = testConnectionService.testConnection(office365CalendarAttributes);
        System.out.println("testConnection method - END with result: " + result);
        return result;
    }

    /**
     * Retrieves stored credentials for a specific authentication type.
     *
     * @param authType The authentication type (Public or Private)
     * @return JSON representation of stored credentials
     */
    @GET
    @Path("/getCredentials")
    @Produces("text/plain")
    public String getCredentials(@QueryParam("authType") String authType) {
        System.out.println("getCredentials method - START with authType: " + authType);
        LOGGER.info("→ getCredentials: Loading attributes for auth-type: {} for invoker {}", authType, invokerId);

        final Office365CalendarAttributes loadedAttributes = office365CalendarAttributeStore.load(invokerId);
        System.out.println("getCredentials method - loaded attributes: " + (loadedAttributes != null ? "not null" : "null"));
        if (loadedAttributes != null && authType.equals(loadedAttributes.getAuthType())) {
            System.out.println("getCredentials method - returning attributes as JSON");
            return Constants.GSON.toJson(loadedAttributes.toMap());
        } else {
            System.out.println("getCredentials method - returning empty string");
            return "";
        }
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
        System.out.println("getAuthKey method - START");
        LOGGER.info("→ getAuthKey: Loading stored authentication key");

        final Office365CalendarAttributes attributes = office365CalendarAttributeStore.load(invokerId);
        System.out.println("getAuthKey method - loaded attributes: " + (attributes != null ? "not null" : "null"));
        if (attributes != null) {
            final String authType = attributes.getAuthType();
            System.out.println("getAuthKey method - authType: " + authType);
            String result = Constants.GSON.toJson(Objects.requireNonNullElse(authType, Constants.PUBLIC));
            System.out.println("getAuthKey method - END with result: " + result);
            return result;
        } else {
            System.out.println("getAuthKey method - END with empty string");
            return "";
        }
    }

    /**
     * Serves UI documentation files.
     *
     * @param subPath The sub-path for the requested file
     * @return InputStream of the requested file
     */
    @GET
    @Path("/docs/{subPath:.*}")
    public InputStream customTabs(@PathParam("subPath") String subPath) {
        System.out.println("customTabs method - START with subPath: " + subPath);
        String filePath = "ui/office365calendaroauth/" + (subPath.isEmpty() ? "index.html" : subPath);
        System.out.println("customTabs method - constructed filePath: " + filePath);
        InputStream result = getClass().getClassLoader().getResourceAsStream(filePath);
        System.out.println("customTabs method - END with result: " + (result != null ? "not null" : "null"));
        return result;
    }
}