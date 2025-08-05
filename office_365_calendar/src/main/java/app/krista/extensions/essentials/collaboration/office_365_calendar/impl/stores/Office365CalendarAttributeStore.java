package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(Office365CalendarAttributeStore.class);
    private final Invoker invoker;
    private final KeyValueStore store;

    @Inject
    public Office365CalendarAttributeStore(Invoker invoker, KeyValueStore store) {
        LOGGER.debug("→ Office365CalendarAttributeStore: Initializing with injected dependencies");
        this.invoker = invoker;
        LOGGER.debug("→ Invoker dependency set");
        this.store = store;
        LOGGER.debug("→ KeyValueStore dependency set");
        LOGGER.debug("→ Office365CalendarAttributeStore: Initialization complete");
    }


    /**
     * Loads Office 365 Calendar attributes from the store using the provided key.
     * <p>
     * This method retrieves the serialized attributes from the key-value store,
     * deserializes them, and constructs an {@link Office365CalendarAttributes} object based
     * on the authentication type (public or private).
     *
     * @param authContextId The key used to retrieve the attributes from the store
     * @return The deserialized Office365CalendarAttributes object, or null if not found or empty
     */
    @SuppressWarnings("unchecked")
    public Office365CalendarAttributes load(String authContextId) {
        LOGGER.debug("→ load: Loading attributes for authContextId: {}", authContextId);

        String loadedAttributes = null;
        if (store.get(authContextId) != null) {
            loadedAttributes = (String) store.get(authContextId);
        }
        LOGGER.debug("→ load: Attributes retrieved from store: {}", loadedAttributes != null ? "found" : "not found");

        if (loadedAttributes == null || loadedAttributes.isBlank()) {
            LOGGER.debug("→ load: No attributes found or empty, returning null");
            return null;
        } else {
            try {
                Map<String, Object> attributes = Constants.GSON.fromJson(loadedAttributes, Map.class);
                LOGGER.debug("→ load: Successfully parsed JSON attributes");

                String baseUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
                LOGGER.debug("→ load: Base URL resolved: {}", baseUrl);

                String authType = (String) attributes.get(Office365CalendarAttributes.AUTH_TYPE);
                LOGGER.debug("→ load: Authentication type: {}", authType);

                if (Constants.PUBLIC.equals(authType)) {
                    LOGGER.debug("→ load: Creating PUBLIC authentication attributes");
                    Office365CalendarAttributes result = new Office365CalendarAttributes(
                            (String) attributes.get(Office365CalendarAttributes.CLIENT_ID),
                            (String) attributes.get(Office365CalendarAttributes.CLIENT_SECRET),
                            null,
                            (String) attributes.get(Office365CalendarAttributes.EMAIL),
                            (String) attributes.get(Office365CalendarAttributes.TIMEZONE),
                            authType,
                            baseUrl);
                    LOGGER.debug("→ load: Returning PUBLIC attributes");
                    return result;
                } else {
                    LOGGER.debug("→ load: Creating PRIVATE authentication attributes with tenant ID");
                    Office365CalendarAttributes result = new Office365CalendarAttributes(
                            (String) attributes.get(Office365CalendarAttributes.CLIENT_ID),
                            (String) attributes.get(Office365CalendarAttributes.CLIENT_SECRET),
                            (String) attributes.get(Office365CalendarAttributes.TENANT_ID),
                            (String) attributes.get(Office365CalendarAttributes.EMAIL),
                            (String) attributes.get(Office365CalendarAttributes.TIMEZONE),
                            authType,
                            baseUrl);
                    LOGGER.debug("→ load: Returning PRIVATE attributes");
                    return result;
                }
            } catch (Exception e) {
                LOGGER.error("→ load: Error parsing JSON attributes: {}", e.getMessage(), e);
                return null;
            }
        }
    }

    public String save(Office365CalendarAttributes office365CalendarAttributes) {
        LOGGER.debug("→ save: Saving attributes with generated authContextId");
        String authContextId = UUID.randomUUID().toString();
        LOGGER.debug("→ save: Generated authContextId: {}", authContextId);
        Map<String, Object> attributeMap = Map.of(
                Office365CalendarAttributes.CLIENT_ID, office365CalendarAttributes.getClientId(),
                Office365CalendarAttributes.CLIENT_SECRET, office365CalendarAttributes.getClientSecret(),
                Office365CalendarAttributes.TENANT_ID, office365CalendarAttributes.getTenantId() != null ? office365CalendarAttributes.getTenantId() : "",
                Office365CalendarAttributes.EMAIL, office365CalendarAttributes.getEmail(),
                Office365CalendarAttributes.TIMEZONE, office365CalendarAttributes.getTimeZone() != null ? office365CalendarAttributes.getTimeZone() : "",
                Office365CalendarAttributes.AUTH_TYPE, office365CalendarAttributes.getAuthType());
        LOGGER.debug("→ save: Created attribute map for storage");
        store.put(authContextId, Constants.GSON.toJson(attributeMap));
        LOGGER.info("→ save: Attributes saved successfully with authContextId: {}", authContextId);
        return authContextId;
    }

    public boolean save(Office365CalendarAttributes office365CalendarAttributes, String invokerId) {
        LOGGER.debug("→ save: Saving attributes with invokerId: {}", invokerId);
        try {
            LOGGER.debug("→ save: Creating attribute map for storage");
            Map<String, Object> attributeMap = Map.of(
                    Office365CalendarAttributes.CLIENT_ID, office365CalendarAttributes.getClientId(),
                    Office365CalendarAttributes.CLIENT_SECRET, office365CalendarAttributes.getClientSecret(),
                    Office365CalendarAttributes.TENANT_ID, office365CalendarAttributes.getTenantId() != null ? office365CalendarAttributes.getTenantId() : "",
                    Office365CalendarAttributes.EMAIL, office365CalendarAttributes.getEmail(),
                    Office365CalendarAttributes.TIMEZONE, office365CalendarAttributes.getTimeZone() != null ? office365CalendarAttributes.getTimeZone() : "",
                    Office365CalendarAttributes.AUTH_TYPE, office365CalendarAttributes.getAuthType());
            LOGGER.debug("→ save: Storing attributes in KeyValueStore");
            store.put(invokerId, Constants.GSON.toJson(attributeMap));
            LOGGER.info("→ save: Attributes saved successfully for invokerId: {}", invokerId);
            return true;
        } catch (Exception e) {
            LOGGER.error("→ save: Failed to save attributes for invokerId: {}, error: {}", invokerId, e.getMessage(), e);
            return false;
        }
    }

    public void remove(String authContextId) {
        LOGGER.debug("→ remove: Removing attributes for authContextId: {}", authContextId);
        store.remove(authContextId);
        LOGGER.debug("→ remove: Attributes removed successfully");
    }
}
