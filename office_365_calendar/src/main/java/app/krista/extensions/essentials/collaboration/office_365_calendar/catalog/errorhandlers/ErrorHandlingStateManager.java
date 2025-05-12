package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;
import javax.inject.Inject;
import java.util.Map;

@Service
public class ErrorHandlingStateManager {

    private final KeyValueStore errorHandlingStateManager;

    @Inject
    public ErrorHandlingStateManager(KeyValueStore internalState) {
        this.errorHandlingStateManager = internalState;
    }

    public void put(String key, Map<String, Object> value) {
        Object keystoreValue = Constants.GSON.toJson(value);
        errorHandlingStateManager.put(key, keystoreValue);
    }

    public Map<String, Object> get(String key) {
        final String map = String.valueOf(errorHandlingStateManager.get(key));
        return Constants.GSON_OBJECT.fromJson(map, Map.class);
    }
}
