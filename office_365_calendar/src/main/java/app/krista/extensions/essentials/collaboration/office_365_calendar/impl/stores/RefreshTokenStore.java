package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores;

import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

@Service
public final class RefreshTokenStore {

    private final KeyValueStore keyValueStore;

    @Inject
    public RefreshTokenStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    /**
     * This function stores the refresh token in the Database
     *
     * @param key      key for the refresh token to be saved
     * @param refToken refresh token
     */
    public void put(String key, String refToken) {
        keyValueStore.put(key, refToken);
    }

    /**
     * This function will fetch the refresh token
     *
     * @param key key to be fetched
     * @return stored refresh token for the given key
     */
    public String get(String key) {
        return keyValueStore.get(key, String.class);
    }

    /**
     * @param key key to be removed
     */
    public void remove(String key) {
        keyValueStore.remove(key);
    }
}
