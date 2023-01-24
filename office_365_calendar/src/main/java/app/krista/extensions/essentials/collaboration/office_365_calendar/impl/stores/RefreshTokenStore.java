package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores;

import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;

@Service
public final class RefreshTokenStore {

    private final KeyValueStore keyValueStore;

    @Inject
    public RefreshTokenStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    /**
     * @param key
     * @param refToken
     * @throws IOException
     * This function stores the refresh token in the Database
     */
    public void put(String key, String refToken) throws IOException {
        keyValueStore.put(key, refToken);
    }

    /**
     * @param key
     * @return
     * @throws IOException
     * This function returns the refresh token.
     */
    public String get(String key) throws IOException {
        return keyValueStore.get(key, String.class);
    }

    /**
     * @param key
     * @throws IOException
     * This function removes the refresh token from database
     */
    public void remove(String key) throws IOException {
        keyValueStore.remove(key);
    }
}
