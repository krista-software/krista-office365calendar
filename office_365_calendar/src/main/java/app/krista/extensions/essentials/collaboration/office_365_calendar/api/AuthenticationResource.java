package app.krista.extensions.essentials.collaboration.office_365_calendar.api;

import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.microsoft.graph.http.GraphServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.IOException;
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

    public static final String ERROR_DESCRIPTION = "error_description";
    private static final String CODE = "code";
    private static final String STATE = "state";
    private final Office365CalendarAttributeStore office365CalendarAttributeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final GraphServiceClientProviderFactory providerFactory;
    private final AuthorizationContext context;
    private final AuthorizationListener authorizationListener;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);

    @Inject
    public AuthenticationResource(Office365CalendarAttributeStore office365CalendarAttributeStore, RefreshTokenStore refreshTokenStore, GraphServiceClientProviderFactory providerFactory, AuthorizationContext context, AuthorizationListener authorizationListener) {
        LOGGER.info("→ AuthenticationResource: Initializing with injected dependencies");

        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        LOGGER.debug("→ Office365CalendarAttributeStore set");

        this.refreshTokenStore = refreshTokenStore;
        LOGGER.debug("→ RefreshTokenStore set");

        this.providerFactory = providerFactory;
        LOGGER.debug("→ GraphServiceClientProviderFactory set");

        this.context = context;
        LOGGER.debug("→ AuthorizationContext set with authenticated: {}", context.isAuthenticated());

        this.authorizationListener = authorizationListener;
        LOGGER.debug("→ AuthorizationListener set");

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
        LOGGER.info("→ getCallBack: Processing OAuth callback with state: {}", state);

        Objects.requireNonNull(code);
        LOGGER.debug("→ Authorization code received and validated");

        String[] parts = state.split("#");
        LOGGER.debug("→ Split state into {} parts", parts.length);

        if (parts[0].isBlank() || parts.length > 2) {
            LOGGER.error("→ getCallBack: Invalid state parameters - parts[0].isBlank(): {}, parts.length: {}", parts[0].isBlank(), parts.length);
            throw new BadRequestException(Constants.INVALID_STATE_PARAMETERS);
        }

        String key = parts[0];
        String authContextId = parts.length == 2 ? parts[1] : null;
        LOGGER.debug("→ Extracted key: {}, authContextId: {}", key, authContextId);
        try {
            GraphServiceClientProvider clientProvider;
            if (authContextId == null) {
                LOGGER.debug("→ Using default client provider (no authContextId)");
                clientProvider = providerFactory.create();
            } else {
                LOGGER.debug("→ Loading attributes for authContextId: {}", authContextId);
                Office365CalendarAttributes effectiveOffice365CalendarAttributes = office365CalendarAttributeStore.load(authContextId);
                clientProvider = providerFactory.create(effectiveOffice365CalendarAttributes);
                LOGGER.debug("→ Created client provider with custom attributes");
            }

            OAuth2AccessToken accessToken = clientProvider.getOffice365CalendarAttributes().getOAuth20Service().getAccessToken(code);
            LOGGER.debug("→ Successfully obtained OAuth2 access token");

            refreshTokenStore.put(key, accessToken.getRefreshToken());
            LOGGER.debug("→ Stored refresh token for key: {}", key);

            if (!key.startsWith(Constants.WS_CONTACT) && !hasUserAccess(clientProvider)) {
                LOGGER.warn("→ User access validation failed for key: {}", key);
                refreshTokenStore.remove(key);
                LOGGER.debug("→ Removed refresh token due to access validation failure");
                return "Unauthorised user. User email '" + key + "'configured in the setup does not match with authenticated user.";
            }
            if (context.isAuthenticated()) {
                LOGGER.debug("→ Context is authenticated, notifying authorization listener");
                authorizationListener.authorized();
                LOGGER.info("→ getCallBack: User authenticated successfully (context authenticated)");
                return "user authenticated successfully. Please close the dialog.";
            }
            LOGGER.info("→ getCallBack: User authenticated successfully (context not authenticated)");
            return "User authenticated successfully. Save the changes.";
        } catch (OAuthException cause) {
            String errorDescription = getErrorDescription(cause);
            LOGGER.error("→ getCallBack: OAuth exception occurred - {}", errorDescription, cause);
            throw new IllegalStateException(errorDescription, cause.getCause());
        } catch (IOException | ExecutionException exception) {
            LOGGER.error("→ getCallBack: IOException/ExecutionException occurred - {}", exception.getMessage(), exception);
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, exception.getCause());
        } catch (InterruptedException exception) {
            LOGGER.error("→ getCallBack: InterruptedException occurred - {}", exception.getMessage(), exception);
            Thread.currentThread().interrupt();
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, exception.getCause());
        } finally {
            if (authContextId != null) {
                LOGGER.debug("→ Cleaning up authContextId: {}", authContextId);
                office365CalendarAttributeStore.remove(authContextId);
                LOGGER.debug("→ Removed authContextId from attribute store");
            }
        }
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
}



