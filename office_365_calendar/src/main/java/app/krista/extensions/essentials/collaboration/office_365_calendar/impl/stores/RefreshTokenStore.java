package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores;

import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Set;

@Service
public final class RefreshTokenStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTokenStore.class);
    private final KeyValueStore keyValueStore;

    @Inject
    public RefreshTokenStore(KeyValueStore keyValueStore) {
        LOGGER.debug("→ RefreshTokenStore: Initializing with KeyValueStore dependency");
        this.keyValueStore = keyValueStore;
        LOGGER.debug("→ KeyValueStore dependency set");
        LOGGER.debug("→ RefreshTokenStore: Initialization complete");
    }

    /**
     * This function stores the refresh token in the Database
     *
     * @param key      key for the refresh token to be saved
     * @param refToken refresh token
     */
    public void put(String key, String refToken) {
        LOGGER.debug("→ put: Storing refresh token for key: {}", key);
        keyValueStore.put(key, refToken);
        LOGGER.debug("→ put: Refresh token stored successfully");
    }

    /**
     * This function will fetch the refresh token
     *
     * @param key key to be fetched
     * @return stored refresh token for the given key
     */
    public String get(String key) {
        LOGGER.debug("→ get: Retrieving refresh token for key: {}", key);
        try {
            String result = keyValueStore.get(key, String.class);
            LOGGER.debug("→ get: Token retrieved successfully: {}", result != null ? "found" : "not found");
            return result;
        } catch (Exception e) {
            LOGGER.warn("→ get: Error parsing value for key: {}, attempting fallback: {}", key, e.getMessage());
            // Fallback to raw object and convert to string
            try {
                Object rawValue = keyValueStore.get(key);
                String result = rawValue != null ? rawValue.toString() : null;
                LOGGER.debug("→ get: Fallback successful, token retrieved: {}", result != null ? "found" : "not found");
                return result;
            } catch (Exception fallbackException) {
                LOGGER.error("→ get: Fallback also failed for key: {}, error: {}", key, fallbackException.getMessage(), fallbackException);
                return null;
            }
        }
    }

    /**
     * @param key key to be removed
     */
    public void remove(String key) {
        LOGGER.debug("→ remove: Removing refresh token for key: {}", key);
        keyValueStore.remove(key);
        LOGGER.debug("→ remove: Refresh token removed successfully");
    }

    /**
     * Prints all key-value pairs for debugging purposes.
     * This method iterates through all stored refresh tokens and prints them directly to console.
     */
    public void getAllKeyValuePairsAsString() {
        LOGGER.debug("→ getAllKeyValuePairsAsString: Retrieving all stored refresh tokens");

        Set<String> keys = keyValueStore.getKeys();
        LOGGER.info("→ getAllKeyValuePairsAsString: Found {} stored refresh tokens", keys.size());

        if (keys.isEmpty()) {
            LOGGER.info("→ No refresh tokens stored");
        } else {
            for (String key : keys) {
                try {
                    Object rawValue = keyValueStore.get(key);
                    String value = rawValue != null ? rawValue.toString() : "null";
                    LOGGER.debug("→ Key: {} | Value: {}", key, value);
                } catch (Exception e) {
                    LOGGER.warn("→ Key: {} | Value: [Error retrieving value: {}]", key, e.getMessage());
                }
            }
        }

        LOGGER.debug("→ getAllKeyValuePairsAsString: Completed listing {} entries", keys.size());
    }

    /**
     * Removes all stored refresh tokens from the KeyValueStore.
     * This method iterates through all keys and removes each entry.
     *
     * @return A string message indicating the number of tokens removed
     */
    public String removeAll() {
        LOGGER.debug("→ removeAll: Starting removal of all refresh tokens");

        Set<String> keys = keyValueStore.getKeys();
        int initialCount = keys.size();
        LOGGER.info("→ removeAll: Found {} refresh tokens to remove", initialCount);

        if (initialCount == 0) {
            String message = "No refresh tokens found to remove.";
            LOGGER.info("→ removeAll: {}", message);
            return message;
        }

        int removedCount = 0;
        for (String key : keys) {
            try {
                keyValueStore.remove(key);
                removedCount++;
                LOGGER.debug("→ removeAll: Removed key: {}", key);
            } catch (Exception e) {
                LOGGER.warn("→ removeAll: Failed to remove key: {}, error: {}", key, e.getMessage());
            }
        }

        String message = "Successfully removed " + removedCount + " out of " + initialCount + " refresh tokens.";
        LOGGER.info("→ removeAll: {}", message);
        return message;
    }
}
