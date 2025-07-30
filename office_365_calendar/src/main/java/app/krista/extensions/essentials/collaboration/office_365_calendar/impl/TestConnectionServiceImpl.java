package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.OAuthService;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.AuthenticationResponse;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.microsoft.graph.http.GraphServiceException;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants.*;

@Service
public class TestConnectionServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestConnectionServiceImpl.class);

    private final GraphServiceClientProviderFactory providerFactory;
    private final Office365CalendarAttributeStore office365CalendarAttributeStore;
    private final String baseRoutingUrl;

    @Inject
    public TestConnectionServiceImpl(GraphServiceClientProviderFactory providerFactory,
                                   Office365CalendarAttributeStore office365CalendarAttributeStore,
                                   Invoker invoker) {
        this.providerFactory = providerFactory;
        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
    }

    public String testConnection(Office365CalendarAttributes office365CalendarAttributes) {
        LOGGER.info("Testing connection with Microsoft Graph API");
        String authContextId = providerFactory.createAttributes(office365CalendarAttributes);
        String authUrl = null;
        
        try {
            LOGGER.info("Verifying API access for email: {}", office365CalendarAttributes.getEmail());
            providerFactory.create(authContextId).getGraphServiceClientForAdmin().me().calendars().buildRequest().get();

            LOGGER.info("Test connection successful");
            return createTestConnectionResponse(true, null, null);
        } catch (GraphServiceException cause) {
            LOGGER.error("Graph API connection failed: {}", cause.getMessage());
            return createTestConnectionResponse(false, "An error occurred during test connection.", null);
        } catch (MustAuthorizeException cause) {
            LOGGER.info("Authorization required, generating auth URL");
            String state = createStateParameter(cause, office365CalendarAttributes);
            OAuth20Service oAuth20Service = new OAuthService(office365CalendarAttributes).getOAuth20Service();
            authUrl = oAuth20Service.getAuthorizationUrl(state) + AUTH_URL_QUERY_PARAMS;
            return createTestConnectionResponse(false, AUTHORIZATION_PROMPT, authUrl);
        } finally {
            if (authUrl == null) {
                office365CalendarAttributeStore.remove(authContextId);
            }
        }
    }

    private String createTestConnectionResponse(boolean isSuccess, String errorMessage, String url) {
        if (isSuccess) {
            return GSON.toJson(new AuthenticationResponse(true, null, null));
        } else if (url != null) {
            return GSON.toJson(new AuthenticationResponse(false, errorMessage, url));
        } else {
            return GSON.toJson(new AuthenticationResponse(false,
                    Objects.requireNonNullElse(errorMessage, "Unknown Error"), null));
        }
    }

    private String createStateParameter(MustAuthorizeException cause, Office365CalendarAttributes office365CalendarAttributes) {
        String userId = (String) cause.getDetails().getFirst().getValue();
        return createStateParameter(cause, office365CalendarAttributes, userId);
    }

    private String createStateParameter(MustAuthorizeException cause, Office365CalendarAttributes office365CalendarAttributes, String userId) {
        Optional<NamedValuedField> authContextIdField = cause.getDetails().stream()
                .filter(namedValuedField -> Objects.equals(namedValuedField.getName(), Constants.AUTH_CONTEXT_ID))
                .findFirst();
        String state = userId;
        if (authContextIdField.isPresent()) {
            String authContextId = (String) authContextIdField.get().getValue();
            state += Constants.HASH + authContextId;
        }
        if (Constants.PUBLIC.equals(office365CalendarAttributes.getAuthType())) {
            state += Constants.HASH + office365CalendarAttributes.getForwardPath();
        }
        return state;
    }
}
