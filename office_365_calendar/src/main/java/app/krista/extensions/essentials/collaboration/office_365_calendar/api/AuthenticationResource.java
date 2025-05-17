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
        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.providerFactory = providerFactory;
        this.context = context;
        this.authorizationListener = authorizationListener;
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
        Objects.requireNonNull(code);
        String[] parts = state.split("#");
        if (parts[0].isBlank() || parts.length > 2) {
            throw new BadRequestException(Constants.INVALID_STATE_PARAMETERS);
        }
        String key = parts[0];
        String authContextId = parts.length == 2 ? parts[1] : null;
        try {
            GraphServiceClientProvider clientProvider;
            if (authContextId == null) {
                clientProvider = providerFactory.create();
            } else {
                Office365CalendarAttributes effectiveOffice365CalendarAttributes = office365CalendarAttributeStore.load(authContextId);
                clientProvider = providerFactory.create(effectiveOffice365CalendarAttributes);
            }
            OAuth2AccessToken accessToken = clientProvider.getOffice365CalendarAttributes().getOAuth20Service().getAccessToken(code);
            refreshTokenStore.put(key, accessToken.getRefreshToken());
            if (!key.startsWith(Constants.WS_CONTACT) && !hasUserAccess(clientProvider)) {
                refreshTokenStore.remove(key);
                return "Unauthorised user. User email '" + key + "'configured in the setup does not match with authenticated user.";
            }
            if (context.isAuthenticated()) {
                authorizationListener.authorized();
                return "user authenticated successfully. Please close the dialog.";
            }
            return "User authenticated successfully. Save the changes.";
        } catch (OAuthException cause) {
            throw new IllegalStateException(getErrorDescription(cause), cause.getCause());
        } catch (IOException | ExecutionException exception) {
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, exception.getCause());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, exception.getCause());
        } finally {
            if (authContextId != null) {
                office365CalendarAttributeStore.remove(authContextId);
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
        String errorDescription = "Error occurred during authorization. ";
        if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
            var exceptionMap = Constants.GSON.fromJson(cause.getMessage(), Map.class);
            if (exceptionMap.containsKey(ERROR_DESCRIPTION)) {
                errorDescription = errorDescription + exceptionMap.get(ERROR_DESCRIPTION);
            }
        }
        return errorDescription;
    }

    /**
     * Checks if the current user has access rights using the provided GraphServiceClientProvider.
     *
     * @param graphServiceClientForAdmin The GraphServiceClientProvider instance configured for admin access.
     * @return {@code true} if the user has access; {@code false} otherwise.
     */
    private boolean hasUserAccess(GraphServiceClientProvider graphServiceClientForAdmin) {
        try {
            graphServiceClientForAdmin.getUserRequestBuilder(null, null).calendars().buildRequest().get();
            return true;
        } catch (GraphServiceException ignore) {
            LOGGER.debug(Constants.IGNORED_EXCEPTION);
            return false;
        }
    }
}



