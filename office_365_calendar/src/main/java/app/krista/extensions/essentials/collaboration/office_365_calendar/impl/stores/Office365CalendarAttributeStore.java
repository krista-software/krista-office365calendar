package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

/**
 * Service class for storing, loading, and removing Office 365 calendar authentication attributes.
 * <p>
 * This class uses a {@link KeyValueStore} to persistently store authentication context information
 * (such as client ID, client secret, tenant ID, and email) associated with Office 365 calendar integration.
 * It provides methods to save new attribute sets, load them by context ID, and remove them when no longer needed.
 * </p>
 */
@Service
public final class Office365CalendarAttributeStore {

    private final Invoker invoker;
    private final KeyValueStore store;

    @Inject
    public Office365CalendarAttributeStore(Invoker invoker, KeyValueStore store) {
        this.invoker = invoker;
        this.store = store;
    }

    public Office365CalendarAttributes load(String authContextId) {
        var attributes = Constants.GSON.fromJson(String.valueOf(store.get(authContextId)), Map.class);
        String baseUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        String authType = (String) attributes.get(Office365CalendarAttributes.AUTH_TYPE);

        if (attributes != null) {
            if (Constants.PUBLIC.equals(authType)) {
                return new Office365CalendarAttributes((String) attributes.get(Office365CalendarAttributes.CLIENT_ID),
                        (String) attributes.get(Office365CalendarAttributes.CLIENT_SECRET),
                        null, (String) attributes.get(Office365CalendarAttributes.EMAIL),
                        (String) attributes.get(Office365CalendarAttributes.TIMEZONE), authType,
                        baseUrl);
            } else {
                return new Office365CalendarAttributes((String) attributes.get(Office365CalendarAttributes.CLIENT_ID),
                        (String) attributes.get(Office365CalendarAttributes.CLIENT_SECRET),
                        (String) attributes.get(Office365CalendarAttributes.TENANT_ID),
                        (String) attributes.get(Office365CalendarAttributes.EMAIL),
                        (String) attributes.get(Office365CalendarAttributes.TIMEZONE), authType,
                        baseUrl);
            }
        }
        return null;
    }

    public String save(Office365CalendarAttributes office365CalendarAttributes) {
        String authContextId = UUID.randomUUID().toString();
        Map<String, Object> attributeMap = Map.of(
                Office365CalendarAttributes.CLIENT_ID, office365CalendarAttributes.getClientId(),
                Office365CalendarAttributes.CLIENT_SECRET, office365CalendarAttributes.getClientSecret(),
                Office365CalendarAttributes.TENANT_ID, office365CalendarAttributes.getTenantId() != null ? office365CalendarAttributes.getTenantId() : "",
                Office365CalendarAttributes.EMAIL, office365CalendarAttributes.getEmail(),
                Office365CalendarAttributes.TIMEZONE, office365CalendarAttributes.getTimeZone() != null ? office365CalendarAttributes.getTimeZone() : "",
                Office365CalendarAttributes.AUTH_TYPE, office365CalendarAttributes.getAuthType());
        store.put(authContextId, Constants.GSON.toJson(attributeMap));
        return authContextId;
    }

    public boolean save(Office365CalendarAttributes office365CalendarAttributes, String invokerId) {
        try {
            Map<String, Object> attributeMap = Map.of(
                    Office365CalendarAttributes.CLIENT_ID, office365CalendarAttributes.getClientId(),
                    Office365CalendarAttributes.CLIENT_SECRET, office365CalendarAttributes.getClientSecret(),
                    Office365CalendarAttributes.TENANT_ID, office365CalendarAttributes.getTenantId() != null ? office365CalendarAttributes.getTenantId() : "",
                    Office365CalendarAttributes.EMAIL, office365CalendarAttributes.getEmail(),
                    Office365CalendarAttributes.TIMEZONE, office365CalendarAttributes.getTimeZone() != null ? office365CalendarAttributes.getTimeZone() : "",
                    Office365CalendarAttributes.AUTH_TYPE, office365CalendarAttributes.getAuthType());
            store.put(invokerId, Constants.GSON.toJson(attributeMap));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void remove(String authContextId) {
        store.remove(authContextId);
    }
}
