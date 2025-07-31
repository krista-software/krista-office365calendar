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
import app.krista.ksdk.context.RequestContext;
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
    private final RequestContext requestContext;
    private static final Logger LOGGER = LoggerFactory.getLogger(Office365CalendarRequestAuthenticator.class);

    @Inject
    public Office365CalendarRequestAuthenticator(Office365CalendarAttributeStore attributeStore, AuthorizationContext authorizationContext, RequestContext requestContext) {
        System.out.println("Office365CalendarRequestAuthenticator constructor - START");
        this.attributeStore = attributeStore;
        System.out.println("Office365CalendarRequestAuthenticator constructor - attributeStore set");
        this.authorizationContext = authorizationContext;
        System.out.println("Office365CalendarRequestAuthenticator constructor - authorizationContext set");
        this.requestContext = requestContext;
        System.out.println("Office365CalendarRequestAuthenticator constructor - requestContext set");
        System.out.println("Office365CalendarRequestAuthenticator constructor - END");
    }

    @Override
    public String getScheme() {
        System.out.println("getScheme method - START");
        System.out.println("getScheme method - END returning null");
        return null;
    }

    @Override
    public Set<String> getSupportedProtocols() {
        System.out.println("getSupportedProtocols method - START");
        Set<String> result = Set.of();
        System.out.println("getSupportedProtocols method - END returning empty set");
        return result;
    }

    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        System.out.println("getAuthenticatedAccountId method - START");
        try {
            System.out.println("getAuthenticatedAccountId method - entering try block");
            HttpRequest httpRequest = (HttpRequest) protoRequest;
            if (EXTENSION_OAUTH_VERIFICATION_PATH_V3.equals(httpRequest.getUri().getPath()) ||
                    EXTENSION_OAUTH_VERIFICATION_PATH_V2.equals(httpRequest.getUri().getPath())) {
                System.out.println("getAuthenticatedAccountId method - path matches OAuth verification path");
                httpRequest.bufferBody();
                MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
                String state = String.valueOf(queryParameters.get(STATE).get(0));
                System.out.println("getAuthenticatedAccountId method - extracted state: " + state);
                String[] parts = state.split(Constants.HASH);
                System.out.println("getAuthenticatedAccountId method - END returning parts[0]: " + parts[0]);
                return parts[0];
            } else {
                System.out.println("getAuthenticatedAccountId method - path does not match OAuth verification path");
                System.out.println("getAuthenticatedAccountId method - END returning null");
                return null;
            }
        } catch (RuntimeException | IOException cause) {
            System.out.println("getAuthenticatedAccountId method - exception occurred: " + cause.getMessage());
            LOGGER.error("Failed to get the Authenticated Account Id", cause);
            System.out.println("getAuthenticatedAccountId method - END returning null due to exception");
            return null;
        }
    }

    @Override
    public boolean setServiceAuthorization(String key) {
        System.out.println("setServiceAuthorization method - START with key: " + key);
        System.out.println("setServiceAuthorization method - END returning false");
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        System.out.println("getAttributeFields method - START");
        Map<String, NamedField> result = Map.of();
        System.out.println("getAttributeFields method - END returning empty map");
        return result;
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException cause, ProtoRequest request) {
        System.out.println("getMustAuthenticateResponse method (ProtoResponse) - START");
        System.out.println("getMustAuthenticateResponse method (ProtoResponse) - END returning null");
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException cause) {
        System.out.println("getMustAuthenticateResponse method (AuthorizationResponse) - START");
        System.out.println("getMustAuthenticateResponse method (AuthorizationResponse) - END returning null");
        return null;
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException cause, ProtoRequest request) {
        System.out.println("getMustAuthorizeResponse method (ProtoResponse) - START");
        System.out.println("getMustAuthorizeResponse method (ProtoResponse) - END returning null");
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException cause) {
        System.out.println("getMustAuthorizeResponse method (AuthorizationResponse) - START");
        String userId = (String) cause.getDetails().get(0).getValue();
        System.out.println("getMustAuthorizeResponse method - extracted userId: " + userId);

        Optional<NamedValuedField> authContext = cause.getDetails().stream()
                .filter(namedValuedField -> Objects.equals(namedValuedField.getName(), Constants.AUTH_CONTEXT_ID))
                .findFirst();
        System.out.println("getMustAuthorizeResponse method - authContext present: " + authContext.isPresent());

        String state = userId;
        Office365CalendarAttributes attributes;
        if (authContext.isPresent()) {
            System.out.println("getMustAuthorizeResponse method - authContext is present");
            String authContextId = (String) authContext.get().getValue();
            System.out.println("getMustAuthorizeResponse method - authContextId: " + authContextId);
            attributes = attributeStore.load(authContextId);
            state += Constants.HASH + authContextId;
        } else {
            System.out.println("getMustAuthorizeResponse method - authContext is not present, using invokerId from requestContext");
            String invokerId = requestContext.getInvokerId();
            attributes = attributeStore.load(invokerId);
        }
        System.out.println("getMustAuthorizeResponse method - loaded attributes, state: " + state);

        if (Constants.PUBLIC.equals(attributes.getAuthType())) {
            System.out.println("getMustAuthorizeResponse method - authType is PUBLIC, adding forward path");
            state += Constants.HASH + attributes.getForwardPath();
        }
        System.out.println("getMustAuthorizeResponse method - final state: " + state);

        OAuth20Service oAuth20Service = new OAuthService(attributes).getOAuth20Service();
        String url = oAuth20Service.getAuthorizationUrl(state) + Constants.AUTH_URL_QUERY_PARAMS;
        System.out.println("getMustAuthorizeResponse method - generated URL: " + url);

        AuthorizationResponse response = new AuthorizationResponse(url, Collections.emptyList());
        System.out.println("getMustAuthorizeResponse method - END returning AuthorizationResponse");
        return response;
    }

}
