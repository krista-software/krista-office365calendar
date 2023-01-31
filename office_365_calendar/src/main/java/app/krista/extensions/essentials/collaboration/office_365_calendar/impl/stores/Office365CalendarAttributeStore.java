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
        return new Office365CalendarAttributes(invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                (String) attributes.get(Office365CalendarAttributes.CLIENT_ID),
                (String) attributes.get(Office365CalendarAttributes.CLIENT_SECRET),
                (String) attributes.get(Office365CalendarAttributes.EMAIL),
                (String) attributes.get(Office365CalendarAttributes.TENANT_ID),
                (String) attributes.get(Office365CalendarAttributes.TIMEZONE));
    }

    public String save(Office365CalendarAttributes office365CalendarAttributes) {
        String authContextId = UUID.randomUUID().toString();
        store.put(authContextId, Constants.GSON.toJson(Map.of(Office365CalendarAttributes.CLIENT_ID, office365CalendarAttributes.getClientId(),
                Office365CalendarAttributes.CLIENT_SECRET, office365CalendarAttributes.getClientSecret(),
                Office365CalendarAttributes.TENANT_ID, office365CalendarAttributes.getTenantId(),
                Office365CalendarAttributes.EMAIL, office365CalendarAttributes.getEmail())));
        return authContextId;
    }

    public void remove(String authContextId) {
        store.remove(authContextId);
    }
}
