package app.krista.extensions.essentials.collaboration.office_365_calendar;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.model.field.NamedField;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.oauth.OAuth20Service;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.*;

/**
 * Authenticator implementation for Office 365 Calendar integration.
 * <p>
 * Handles authentication and authorization flows for Office 365 calendar requests,
 * including extracting authenticated account IDs from callback requests and generating
 * authorization URLs for user consent.
 * </p>
 */
public class Office365CalendarRequestAuthenticator implements RequestAuthenticator {

    private final Office365CalendarAttributeStore office365CalendarAttributeStore;
    private final Office365CalendarAttributes office365CalendarAttributes;

    public Office365CalendarRequestAuthenticator(Office365CalendarAttributeStore office365CalendarAttributeStore, Office365CalendarAttributes office365CalendarAttributes) {
        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        this.office365CalendarAttributes = office365CalendarAttributes;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public Set<String> getSupportedProtocols() {
        return Set.of();
    }

    /**
     * Extracts the authenticated account ID from the given ProtoRequest, if present.
     * Used primarily for handling OAuth callback requests.
     *
     * @param protoRequest the incoming ProtoRequest
     * @return the authenticated account ID, or null if not found
     */
    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        try {
            HttpRequest httpRequest = (HttpRequest) protoRequest;
            if (httpRequest.getUri().getPath().equals(Constants.CALLBACK_URI)) {
                httpRequest.bufferBody();
                MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
                if (queryParameters == null ||
                        queryParameters.get(Constants.STATE) == null ||
                        queryParameters.get(Constants.STATE).getFirst() == null) {
                    return null;
                }
                String state = String.valueOf(queryParameters.get(Constants.STATE).getFirst());
                String[] parts = state.split("&");
                return parts[0];
            } else {
                return null;
            }
        } catch (RuntimeException | IOException cause) {
            return null;
        }
    }

    @Override
    public boolean setServiceAuthorization(String s) {
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        return Map.of();
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException cause, ProtoRequest request) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException cause) {
        return null;
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException cause, ProtoRequest request) {
        return null;
    }

    /**
     * Returns an AuthorizationResponse for must-authorize scenarios, generating an OAuth authorization URL.
     *
     * @param cause the MustAuthorizeException
     * @return an AuthorizationResponse containing the authorization URL
     */
    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException cause) {
        String userId = (String) cause.getDetails().getFirst().getValue();
        Optional<NamedValuedField> authContextIdField = cause.getDetails().stream().filter(f -> Objects.equals(f.getName(), "authContextId")).findFirst();

        Office365CalendarAttributes effectiveOffice365CalendarAttributes;
        String state;
        if (authContextIdField.isPresent()) {
            String authContextId = (String) authContextIdField.get().getValue();
            effectiveOffice365CalendarAttributes = office365CalendarAttributeStore.load(authContextId);
            state = userId + "#" + authContextId;
        } else {
            effectiveOffice365CalendarAttributes = office365CalendarAttributes;
            state = userId;
        }
        OAuth20Service oAuth20Service = effectiveOffice365CalendarAttributes.getOAuth20Service();
        String url = oAuth20Service.getAuthorizationUrl(state);
        String urlQueryParam = "&access_type=offline&approval_prompt=force&prompt=login";
        return new AuthorizationResponse(url + urlQueryParam, List.of());
    }

}
