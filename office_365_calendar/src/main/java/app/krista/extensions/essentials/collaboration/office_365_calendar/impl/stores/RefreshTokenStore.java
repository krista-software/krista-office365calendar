package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores;

import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

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
        String result = keyValueStore.get(key, String.class);
        System.out.println("get method - END with result: " + (result != null ? "not null" : "null"));
        return result;
    }

    /**
     * @param key key to be removed
     */
    public void remove(String key) {
        System.out.println("remove method - START with key: " + key);
        keyValueStore.remove(key);
        System.out.println("remove method - END");
    }
}
