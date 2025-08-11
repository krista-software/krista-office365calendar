package app.krista.extensions.essentials.collaboration.office_365_calendar;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.OAuthService;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedField;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.*;

import static com.github.scribejava.core.model.OAuthConstants.STATE;

@Service
public class Office365CalendarRequestAuthenticator implements RequestAuthenticator {

    public static final String EXTENSION_OAUTH_VERIFICATION_PATH_V3 = "/calendar/v3/oauth/callback";
    public static final String EXTENSION_OAUTH_VERIFICATION_PATH_V2 = "/calendar/callback";
    private final AuthorizationContext authorizationContext;
    private final Office365CalendarAttributeStore attributeStore;
    private final String invokerId;
    private static final Logger LOGGER = LoggerFactory.getLogger(Office365CalendarRequestAuthenticator.class);

    @Inject
    public Office365CalendarRequestAuthenticator(Office365CalendarAttributeStore attributeStore, String invokerId, AuthorizationContext authorizationContext) {
        this.attributeStore = attributeStore;
        this.invokerId = invokerId;
        this.authorizationContext = authorizationContext;
    }

    @Override
    public String getScheme() {
        LOGGER.debug("→ getScheme: Returning null (no specific scheme)");
        return null;
    }

    @Override
    public Set<String> getSupportedProtocols() {
        LOGGER.debug("→ getSupportedProtocols: Returning empty set");
        Set<String> result = Set.of();
        return result;
    }

    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        LOGGER.debug("→ getAuthenticatedAccountId: Processing authentication request");
        try {
            LOGGER.debug("→ getAuthenticatedAccountId: Parsing HTTP request");
            HttpRequest httpRequest = (HttpRequest) protoRequest;
            if (EXTENSION_OAUTH_VERIFICATION_PATH_V3.equals(httpRequest.getUri().getPath()) ||
                    EXTENSION_OAUTH_VERIFICATION_PATH_V2.equals(httpRequest.getUri().getPath())) {
                LOGGER.debug("→ getAuthenticatedAccountId: Path matches OAuth verification endpoint");
                httpRequest.bufferBody();
                MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
                String state = String.valueOf(queryParameters.get(STATE).get(0));
                LOGGER.debug("→ getAuthenticatedAccountId: Extracted state parameter: {}", state);
                String[] parts = state.split(Constants.HASH);
                LOGGER.debug("→ getAuthenticatedAccountId: Returning authenticated account ID: {}", parts[0]);
                return parts[0];
            } else {
                LOGGER.debug("→ getAuthenticatedAccountId: Path does not match OAuth verification endpoint");
                return null;
            }
        } catch (RuntimeException | IOException cause) {
            LOGGER.error("Failed to get the Authenticated Account Id", cause);
            return null;
        }
    }

    @Override
    public boolean setServiceAuthorization(String key) {
        LOGGER.debug("→ setServiceAuthorization: Called with key: {}, returning false", key);
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        LOGGER.debug("→ getAttributeFields: Returning empty attribute fields map");
        Map<String, NamedField> result = Map.of();
        return result;
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException cause, ProtoRequest request) {
        LOGGER.debug("→ getMustAuthenticateResponse (ProtoResponse): Returning null");
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException cause) {
        LOGGER.debug("→ getMustAuthenticateResponse (AuthorizationResponse): Returning null");
        return null;
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException cause, ProtoRequest request) {
        LOGGER.debug("→ getMustAuthorizeResponse (ProtoResponse): Returning null");
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException cause) {
        LOGGER.debug("→ getMustAuthorizeResponse: Creating authorization response");
        String userId = (String) cause.getDetails().get(0).getValue();
        LOGGER.debug("→ getMustAuthorizeResponse: Extracted userId: {}", userId);

        Optional<NamedValuedField> authContext = cause.getDetails().stream()
                .filter(namedValuedField -> Objects.equals(namedValuedField.getName(), Constants.AUTH_CONTEXT_ID))
                .findFirst();
        LOGGER.debug("→ getMustAuthorizeResponse: AuthContext present: {}", authContext.isPresent());

        String state = userId;
        Office365CalendarAttributes attributes;
        if (authContext.isPresent()) {
            LOGGER.debug("→ getMustAuthorizeResponse: Using authContext for attribute loading");
            String authContextId = (String) authContext.get().getValue();
            LOGGER.debug("→ getMustAuthorizeResponse: AuthContextId: {}", authContextId);
            attributes = attributeStore.load(authContextId);
            state += Constants.HASH + authContextId;
        } else {
            LOGGER.debug("→ getMustAuthorizeResponse: Using invokerId for attribute loading");
            attributes = attributeStore.load(invokerId);
        }
        LOGGER.debug("→ getMustAuthorizeResponse: Attributes loaded, current state: {}", state);

        if (Constants.PUBLIC.equals(attributes.getAuthType())) {
            LOGGER.debug("→ getMustAuthorizeResponse: PUBLIC auth type, adding forward path");
            state += Constants.HASH + attributes.getForwardPath();
        }
        LOGGER.debug("→ getMustAuthorizeResponse: Final state parameter: {}", state);

        OAuth20Service oAuth20Service = new OAuthService(attributes).getOAuth20Service();
        String url = oAuth20Service.getAuthorizationUrl(state) + Constants.AUTH_URL_QUERY_PARAMS;
        LOGGER.debug("→ getMustAuthorizeResponse: Generated authorization URL");

        AuthorizationResponse response = new AuthorizationResponse(url, Collections.emptyList());
        LOGGER.debug("→ getMustAuthorizeResponse: Authorization response created successfully");
        return response;
    }

}
