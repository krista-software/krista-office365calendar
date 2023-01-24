package app.krista.extensions.essentials.collaboration.office_365_calendar;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.model.field.NamedField;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.oauth.OAuth20Service;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.*;

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

    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        try {
            HttpRequest httpRequest = (HttpRequest) protoRequest;
            httpRequest.bufferBody();
            MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
            String state = String.valueOf(queryParameters.get("state").get(0));
            String[] parts = state.split("#");
            return parts[0];
        } catch (IOException cause) {
            throw new RuntimeException("Failed to authenticate user " + cause.getMessage(), cause.getCause());
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

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException cause) {
        try {
            String userId = (String) cause.getDetails().get(0).getValue();
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
            String urlQueryParam = "&access_type=offline&approval_prompt=force";
            return new AuthorizationResponse(url + urlQueryParam, List.of());
        } catch (IOException ioException) {
            throw new RuntimeException("Failed to get authorize response", ioException);
        }
    }

}
