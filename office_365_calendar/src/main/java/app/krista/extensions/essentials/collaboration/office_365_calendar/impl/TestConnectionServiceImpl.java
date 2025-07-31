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
        System.out.println("TestConnectionServiceImpl constructor - START");
        this.providerFactory = providerFactory;
        System.out.println("TestConnectionServiceImpl constructor - providerFactory set");
        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        System.out.println("TestConnectionServiceImpl constructor - office365CalendarAttributeStore set");
        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        System.out.println("TestConnectionServiceImpl constructor - END with baseRoutingUrl: " + baseRoutingUrl);
    }

    public String testConnection(Office365CalendarAttributes office365CalendarAttributes) {
        System.out.println("testConnection method - START");
        LOGGER.info("Testing connection with Microsoft Graph API");
        String authContextId = providerFactory.createAttributes(office365CalendarAttributes);
        System.out.println("testConnection method - created authContextId: " + authContextId);
        String authUrl = null;

        try {
            System.out.println("testConnection method - entering try block");
            LOGGER.info("Verifying API access for email: {}", office365CalendarAttributes.getEmail());
            System.out.println("testConnection method - verifying API access for email: " + office365CalendarAttributes.getEmail());
            providerFactory.create(authContextId).getGraphServiceClientForAdmin().me().calendars().buildRequest().get();

            System.out.println("testConnection method - test connection successful");
            LOGGER.info("Test connection successful");
            return createTestConnectionResponse(true, null, null);
        } catch (GraphServiceException cause) {
            System.out.println("testConnection method - GraphServiceException: " + cause.getMessage());
            LOGGER.error("Graph API connection failed: {}", cause.getMessage());
            return createTestConnectionResponse(false, "An error occurred during test connection.", null);
        } catch (MustAuthorizeException cause) {
            System.out.println("testConnection method - MustAuthorizeException: authorization required");
            LOGGER.info("Authorization required, generating auth URL");
            String state = createStateParameter(cause, office365CalendarAttributes);
            System.out.println("testConnection method - created state parameter: " + state);
            OAuth20Service oAuth20Service = new OAuthService(office365CalendarAttributes).getOAuth20Service();
            authUrl = oAuth20Service.getAuthorizationUrl(state) + AUTH_URL_QUERY_PARAMS;
            System.out.println("testConnection method - generated authUrl: " + authUrl);
            return createTestConnectionResponse(false, AUTHORIZATION_PROMPT, authUrl);
        } finally {
            System.out.println("testConnection method - entering finally block");
            if (authUrl == null) {
                System.out.println("testConnection method - authUrl is null, removing authContextId");
                office365CalendarAttributeStore.remove(authContextId);
            }
            System.out.println("testConnection method - END");
        }
    }

    private String createTestConnectionResponse(boolean isSuccess, String errorMessage, String url) {
        System.out.println("createTestConnectionResponse method - START with isSuccess: " + isSuccess + ", errorMessage: " + errorMessage + ", url: " + url);
        String result;
        if (isSuccess) {
            System.out.println("createTestConnectionResponse method - success case");
            result = GSON.toJson(new AuthenticationResponse(true, null, null));
        } else if (url != null) {
            System.out.println("createTestConnectionResponse method - error case with URL");
            result = GSON.toJson(new AuthenticationResponse(false, errorMessage, url));
        } else {
            System.out.println("createTestConnectionResponse method - error case without URL");
            result = GSON.toJson(new AuthenticationResponse(false,
                    Objects.requireNonNullElse(errorMessage, "Unknown Error"), null));
        }
        System.out.println("createTestConnectionResponse method - END with result: " + result);
        return result;
    }

    private String createStateParameter(MustAuthorizeException cause, Office365CalendarAttributes office365CalendarAttributes) {
        System.out.println("createStateParameter method (2 params) - START");
        String userId = (String) cause.getDetails().getFirst().getValue();
        System.out.println("createStateParameter method (2 params) - extracted userId: " + userId);
        String result = createStateParameter(cause, office365CalendarAttributes, userId);
        System.out.println("createStateParameter method (2 params) - END with result: " + result);
        return result;
    }

    private String createStateParameter(MustAuthorizeException cause, Office365CalendarAttributes office365CalendarAttributes, String userId) {
        System.out.println("createStateParameter method (3 params) - START with userId: " + userId);
        Optional<NamedValuedField> authContextIdField = cause.getDetails().stream()
                .filter(namedValuedField -> Objects.equals(namedValuedField.getName(), Constants.AUTH_CONTEXT_ID))
                .findFirst();
        System.out.println("createStateParameter method (3 params) - authContextIdField present: " + authContextIdField.isPresent());
        String state = userId;
        if (authContextIdField.isPresent()) {
            String authContextId = (String) authContextIdField.get().getValue();
            System.out.println("createStateParameter method (3 params) - authContextId: " + authContextId);
            state += Constants.HASH + authContextId;
        }
        if (Constants.PUBLIC.equals(office365CalendarAttributes.getAuthType())) {
            System.out.println("createStateParameter method (3 params) - authType is PUBLIC, adding forward path");
            state += Constants.HASH + office365CalendarAttributes.getForwardPath();
        }
        System.out.println("createStateParameter method (3 params) - END with state: " + state);
        return state;
    }
}
