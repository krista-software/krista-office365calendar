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

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;
import javax.inject.Inject;
import java.util.Map;

/**
 * Manages the persistent state related to error handling within the Office 365 Calendar extension.
 * <p>
 * This class is responsible for storing, retrieving, and managing key-value pairs that represent
 * error states, validation results, and remediation actions during the execution of calendar operations.
 * It leverages the {@link app.krista.extensions.util.KeyValueStore} for underlying storage and
 * serializes values to JSON for consistency and interoperability.
 * </p>
 * ErrorHandlingStateManager is typically used by error handlers and validation orchestrators to
 * maintain context across user interactions, enabling features such as remediation flows,
 * user prompts, and error recovery.
 */
@Service
public class ErrorHandlingStateManager {

    private final KeyValueStore entries;

    @Inject
    public ErrorHandlingStateManager(KeyValueStore internalState) {
        this.entries = internalState;
    }

    public void put(String key, Map<String, Object> value) {
        Object keystoreValue = Constants.GSON.toJson(value);
        entries.put(key, keystoreValue);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String key) {
        final String value = String.valueOf(entries.get(key));
        return Constants.GSON_OBJECT.fromJson(value, Map.class);
    }
}
