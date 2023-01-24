package app.krista.extensions.essentials.collaboration.office_365_calendar.api;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Map;
import java.util.Objects;

@Path("/")
public final class AuthenticationResource {

    private final String CODE = "code";
    private final String STATE = "state";
    private final Office365CalendarAttributeStore office365CalendarAttributeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final GraphServiceClientProviderFactory providerFactory;
    private final AuthorizationContext context;
    private final AuthorizationListener authorizationListener;

    @Inject
    public AuthenticationResource(Office365CalendarAttributeStore office365CalendarAttributeStore, RefreshTokenStore refreshTokenStore, GraphServiceClientProviderFactory providerFactory, AuthorizationContext context, AuthorizationListener authorizationListener) {
        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.providerFactory = providerFactory;
        this.context = context;
        this.authorizationListener = authorizationListener;
    }


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
            if (!key.startsWith("wsContact") && !key.equalsIgnoreCase(getAuthenticatedUserEmail(clientProvider.getGraphServiceClientForAdmin()))) {
                refreshTokenStore.remove(key);
                return "Unauthorised user. User email '" + key + "'configured in the setup does not match with authenticated user.";
            }
            if (context.isAuthenticated()) {
                authorizationListener.authorized();
            }
            return "User authenticated successfully. Save the changes.";
        } catch (OAuthException cause) {
            throw new IllegalStateException(getErrorDescription(cause), cause.getCause());
        } catch (Exception exception) {
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, exception.getCause());
        } finally {
            if (authContextId != null) {
                office365CalendarAttributeStore.remove(authContextId);
            }
        }
    }

    private String getErrorDescription(OAuthException cause) {
        String errorDescription = Constants.ERROR_OCCURRED_DURING_AUTHORIZATION;
        if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
            var exceptionMap = Constants.GSON.fromJson(cause.getMessage(), Map.class);
            if (exceptionMap.containsKey(Constants.ERROR_DESCRIPTION)) {
                errorDescription = errorDescription + exceptionMap.get(Constants.ERROR_DESCRIPTION);
            }
        }
        return errorDescription;
    }

    private String getAuthenticatedUserEmail(GraphServiceClient graphServiceClientForAdmin) {
        User user = graphServiceClientForAdmin.me().buildRequest().get();
        return user != null ? user.mail : "";
    }
}
