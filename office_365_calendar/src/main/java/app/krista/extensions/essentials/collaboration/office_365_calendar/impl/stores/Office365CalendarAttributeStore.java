/*
 * Office365Calendar Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import app.krista.extension.authorization.MustAuthorizeException;

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
        this.invoker = invoker;
        this.store = store;
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

                // Normalize blank tenantId to null — old save() stored "" for null tenantId
                String rawTenantId = (String) attributes.get(Office365CalendarAttributes.TENANT_ID);
                String tenantId = (rawTenantId == null || rawTenantId.isBlank()) ? null : rawTenantId;

                if (Constants.PUBLIC.equalsIgnoreCase(authType)) {
                    LOGGER.debug("→ load: Creating PUBLIC authentication attributes");
                    return new Office365CalendarAttributes(
                            (String) attributes.get(Office365CalendarAttributes.CLIENT_ID),
                            (String) attributes.get(Office365CalendarAttributes.CLIENT_SECRET),
                            tenantId,
                            (String) attributes.get(Office365CalendarAttributes.EMAIL),
                            (String) attributes.get(Office365CalendarAttributes.TIMEZONE),
                            authType,
                            baseUrl);
                } else if (!attributes.containsKey("privateAuthMode")) {
                    // ── Migration ────────────────────────────────────────────────────────
                    // Old "Private" configs (delegated OAuth with own Azure AD app) stored
                    // before Server-Server auth was added have no privateAuthMode key.
                    // Migrate them to User Authentication (Public + own credentials) so
                    // they go through the delegated path instead of Client Credentials.
                    String shortKey = authContextId != null && authContextId.length() > 8
                            ? authContextId.substring(0, 8) + "…" : authContextId;
                    LOGGER.info("Migrating legacy Private auth → User Authentication (delegated) for key {}", shortKey);
                    return new Office365CalendarAttributes(
                            (String) attributes.get(Office365CalendarAttributes.CLIENT_ID),
                            (String) attributes.get(Office365CalendarAttributes.CLIENT_SECRET),
                            tenantId,
                            (String) attributes.get(Office365CalendarAttributes.EMAIL),
                            (String) attributes.get(Office365CalendarAttributes.TIMEZONE),
                            Constants.PUBLIC,
                            baseUrl);
                } else {
                    // New Server-Server (Client Credentials) — explicit privateAuthMode present
                    LOGGER.debug("→ load: Creating PRIVATE (Server-Server) authentication attributes");
                    Office365CalendarAttributes result = new Office365CalendarAttributes(
                            (String) attributes.get(Office365CalendarAttributes.CLIENT_ID),
                            (String) attributes.get(Office365CalendarAttributes.CLIENT_SECRET),
                            tenantId,
                            (String) attributes.get(Office365CalendarAttributes.EMAIL),
                            (String) attributes.get(Office365CalendarAttributes.TIMEZONE),
                            authType,
                            baseUrl);
                    result.setPrivateAuthMode((String) attributes.get("privateAuthMode"));
                    return result;
                }
            } catch (MustAuthorizeException e) {
                throw e;
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
        Map<String, Object> attributeMap = office365CalendarAttributes.toMap();
        LOGGER.debug("→ save: Created attribute map for storage");
        store.put(authContextId, Constants.GSON.toJson(attributeMap));
        LOGGER.info("→ save: Attributes saved successfully with authContextId: {}", authContextId);
        return authContextId;
    }

    public boolean save(Office365CalendarAttributes office365CalendarAttributes, String invokerId) {
        LOGGER.debug("→ save: Saving attributes with invokerId: {}", invokerId);
        try {
            LOGGER.debug("→ save: Creating attribute map for storage");
            Map<String, Object> attributeMap = office365CalendarAttributes.toMap();
            LOGGER.debug("→ save: Storing attributes in KeyValueStore");
            store.put(invokerId, Constants.GSON.toJson(attributeMap));
            LOGGER.info("→ save: Attributes saved successfully for invokerId: {}", invokerId);
            return true;
        } catch (MustAuthorizeException e) {
            throw e;
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
