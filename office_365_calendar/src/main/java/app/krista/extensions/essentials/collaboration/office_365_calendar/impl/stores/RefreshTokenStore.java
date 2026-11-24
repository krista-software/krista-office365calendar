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

import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Set;
import app.krista.extension.authorization.MustAuthorizeException;

@Service
public final class RefreshTokenStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTokenStore.class);
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
        try {
            String result = keyValueStore.get(key, String.class);
            return result;
        } catch (MustAuthorizeException e) {
            throw e;
        } catch (Exception e) {
            // Fallback to raw object and convert to string
            try {
                Object rawValue = keyValueStore.get(key);
                String result = rawValue != null ? rawValue.toString() : null;
                return result;
            } catch (MustAuthorizeException fallbackException) {
                throw fallbackException;
            } catch (Exception fallbackException) {
                return null;
            }
        }
    }

    /**
     * @param key key to be removed
     */
    public void remove(String key) {
        keyValueStore.remove(key);
    }


    /**
     * Removes all stored refresh tokens from the KeyValueStore.
     * This method iterates through all keys and removes each entry.
     *
     * @return A string message indicating the number of tokens removed
     */
    public String removeAll() {

        Set<String> keys = keyValueStore.getKeys();
        int initialCount = keys.size();

        if (initialCount == 0) {
            String message = "No refresh tokens found to remove.";
            return message;
        }

        int removedCount = 0;
        for (String key : keys) {
            keyValueStore.remove(key);
            removedCount++;
        }

        return "Successfully removed " + removedCount + " out of " + initialCount + " refresh tokens.";

    }
}
