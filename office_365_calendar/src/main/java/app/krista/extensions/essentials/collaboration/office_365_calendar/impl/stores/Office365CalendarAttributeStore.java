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
        System.out.println("Office365CalendarAttributeStore constructor - START");
        this.invoker = invoker;
        System.out.println("Office365CalendarAttributeStore constructor - invoker set");
        this.store = store;
        System.out.println("Office365CalendarAttributeStore constructor - store set");
        System.out.println("Office365CalendarAttributeStore constructor - END");
    }


    // This load function need to be changed
    public Office365CalendarAttributes load(String authContextId) {
        System.out.println("load method - START with authContextId: " + authContextId);
        var attributes = Constants.GSON.fromJson(String.valueOf(store.get(authContextId)), Map.class);
        System.out.println("load method - loaded attributes from store");
        String baseUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        System.out.println("load method - baseUrl: " + baseUrl);
        String authType = (String) attributes.get(Office365CalendarAttributes.AUTH_TYPE);
        System.out.println("load method - authType: " + authType);

        if (attributes != null) {
            System.out.println("load method - attributes is not null");
            if (Constants.PUBLIC.equals(authType)) {
                System.out.println("load method - authType is PUBLIC, creating attributes without tenant ID");
                Office365CalendarAttributes result = new Office365CalendarAttributes((String) attributes.get(Office365CalendarAttributes.CLIENT_ID),
                        (String) attributes.get(Office365CalendarAttributes.CLIENT_SECRET),
                        null, (String) attributes.get(Office365CalendarAttributes.EMAIL),
                        (String) attributes.get(Office365CalendarAttributes.TIMEZONE), authType,
                        baseUrl);
                System.out.println("load method - END returning PUBLIC attributes");
                return result;
            } else {
                System.out.println("load method - authType is not PUBLIC, creating attributes with tenant ID");
                Office365CalendarAttributes result = new Office365CalendarAttributes((String) attributes.get(Office365CalendarAttributes.CLIENT_ID),
                        (String) attributes.get(Office365CalendarAttributes.CLIENT_SECRET),
                        (String) attributes.get(Office365CalendarAttributes.TENANT_ID),
                        (String) attributes.get(Office365CalendarAttributes.EMAIL),
                        (String) attributes.get(Office365CalendarAttributes.TIMEZONE), authType,
                        baseUrl);
                System.out.println("load method - END returning PRIVATE attributes");
                return result;
            }
        }
        System.out.println("load method - END returning null");
        return null;
    }

    public String save(Office365CalendarAttributes office365CalendarAttributes) {
        System.out.println("save method (single param) - START");
        String authContextId = UUID.randomUUID().toString();
        System.out.println("save method (single param) - generated authContextId: " + authContextId);
        Map<String, Object> attributeMap = Map.of(
                Office365CalendarAttributes.CLIENT_ID, office365CalendarAttributes.getClientId(),
                Office365CalendarAttributes.CLIENT_SECRET, office365CalendarAttributes.getClientSecret(),
                Office365CalendarAttributes.TENANT_ID, office365CalendarAttributes.getTenantId() != null ? office365CalendarAttributes.getTenantId() : "",
                Office365CalendarAttributes.EMAIL, office365CalendarAttributes.getEmail(),
                Office365CalendarAttributes.TIMEZONE, office365CalendarAttributes.getTimeZone() != null ? office365CalendarAttributes.getTimeZone() : "",
                Office365CalendarAttributes.AUTH_TYPE, office365CalendarAttributes.getAuthType());
        System.out.println("save method (single param) - created attribute map");
        store.put(authContextId, Constants.GSON.toJson(attributeMap));
        System.out.println("save method (single param) - END returning authContextId: " + authContextId);
        return authContextId;
    }

    public boolean save(Office365CalendarAttributes office365CalendarAttributes, String invokerId) {
        System.out.println("save method (with invokerId) - START with invokerId: " + invokerId);
        try {
            System.out.println("save method (with invokerId) - entering try block");
            Map<String, Object> attributeMap = Map.of(
                    Office365CalendarAttributes.CLIENT_ID, office365CalendarAttributes.getClientId(),
                    Office365CalendarAttributes.CLIENT_SECRET, office365CalendarAttributes.getClientSecret(),
                    Office365CalendarAttributes.TENANT_ID, office365CalendarAttributes.getTenantId() != null ? office365CalendarAttributes.getTenantId() : "",
                    Office365CalendarAttributes.EMAIL, office365CalendarAttributes.getEmail(),
                    Office365CalendarAttributes.TIMEZONE, office365CalendarAttributes.getTimeZone() != null ? office365CalendarAttributes.getTimeZone() : "",
                    Office365CalendarAttributes.AUTH_TYPE, office365CalendarAttributes.getAuthType());
            System.out.println("save method (with invokerId) - created attribute map");
            store.put(invokerId, Constants.GSON.toJson(attributeMap));
            System.out.println("save method (with invokerId) - END returning true");
            return true;
        } catch (Exception e) {
            System.out.println("save method (with invokerId) - exception occurred: " + e.getMessage());
            System.out.println("save method (with invokerId) - END returning false");
            return false;
        }
    }

    public void remove(String authContextId) {
        System.out.println("remove method - START with authContextId: " + authContextId);
        store.remove(authContextId);
        System.out.println("remove method - END");
    }
}
