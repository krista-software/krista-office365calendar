package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores;

import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Set;

@Service
public final class RefreshTokenStore {

    private final KeyValueStore keyValueStore;

    @Inject
    public RefreshTokenStore(KeyValueStore keyValueStore) {
        System.out.println("RefreshTokenStore constructor - START");
        this.keyValueStore = keyValueStore;
        System.out.println("RefreshTokenStore constructor - keyValueStore set");
        System.out.println("RefreshTokenStore constructor - END");
    }

    /**
     * This function stores the refresh token in the Database
     *
     * @param key      key for the refresh token to be saved
     * @param refToken refresh token
     */
    public void put(String key, String refToken) {
        System.out.println("put method - START with key: " + key);
        keyValueStore.put(key, refToken);
        System.out.println("put method - END");
    }

    /**
     * This function will fetch the refresh token
     *
     * @param key key to be fetched
     * @return stored refresh token for the given key
     */
    public String get(String key) {
        System.out.println("get method - START with key: " + key);
        try {
            String result = keyValueStore.get(key, String.class);
            System.out.println("get method - END with result: " + (result != null ? "not null " + result : "null"));
            return result;
        } catch (Exception e) {
            System.out.println("get method - Error parsing value for key: " + key + ", error: " + e.getMessage());
            // Fallback to raw object and convert to string
            try {
                Object rawValue = keyValueStore.get(key);
                String result = rawValue != null ? rawValue.toString() : null;
                System.out.println("get method - END with fallback result: " + (result != null ? "not null " + result : "null"));
                return result;
            } catch (Exception fallbackException) {
                System.out.println("get method - Fallback also failed: " + fallbackException.getMessage());
                return null;
            }
        }
    }

    /**
     * @param key key to be removed
     */
    public void remove(String key) {
        System.out.println("remove method - START with key: " + key);
        keyValueStore.remove(key);
        System.out.println("remove method - END");
    }

    /**
     * Prints all key-value pairs for debugging purposes.
     * This method iterates through all stored refresh tokens and prints them directly to console.
     */
    public void getAllKeyValuePairsAsString() {
        System.out.println("getAllKeyValuePairsAsString method - START");

        Set<String> keys = keyValueStore.getKeys();
        System.out.println("getAllKeyValuePairsAsString method - found " + keys.size() + " keys");

        if (keys.isEmpty()) {
            System.out.println("No refresh tokens stored.");
        } else {
            for (String key : keys) {
                try {
                    Object rawValue = keyValueStore.get(key);
                    String value = rawValue != null ? rawValue.toString() : "null";
                    System.out.println("Key: " + key + " | Value: " + value);
                } catch (Exception e) {
                    System.out.println("Key: " + key + " | Value: [Error retrieving value: " + e.getMessage() + "]");
                }
            }
        }

        System.out.println("getAllKeyValuePairsAsString method - END with " + keys.size() + " entries");
    }

    /**
     * Removes all stored refresh tokens from the KeyValueStore.
     * This method iterates through all keys and removes each entry.
     *
     * @return A string message indicating the number of tokens removed
     */
    public String removeAll() {
        System.out.println("removeAll method - START");

        Set<String> keys = keyValueStore.getKeys();
        int initialCount = keys.size();
        System.out.println("removeAll method - found " + initialCount + " keys to remove");

        if (initialCount == 0) {
            String message = "No refresh tokens found to remove.";
            System.out.println("removeAll method - END with message: " + message);
            return message;
        }

        int removedCount = 0;
        for (String key : keys) {
            try {
                keyValueStore.remove(key);
                removedCount++;
                System.out.println("removeAll method - removed key: " + key);
            } catch (Exception e) {
                System.out.println("removeAll method - failed to remove key: " + key + ", error: " + e.getMessage());
            }
        }

        String message = "Successfully removed " + removedCount + " out of " + initialCount + " refresh tokens.";
        System.out.println("removeAll method - END with message: " + message);
        return message;
    }
}
