package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;
import javax.inject.Inject;
import java.util.Map;

@Service
public class ErrorHandlingStateManager {

    private final KeyValueStore internalState;

    @Inject
    public ErrorHandlingStateManager(KeyValueStore internalState) {
        this.internalState = internalState;

    }

    public void put(String key, Object value) {
        internalState.put(key, value);
    }

    public Map<String, Object> get(String key) {
        final String map = String.valueOf(internalState.get(key));
        return Constants.GSON_OBJECT.fromJson(map, Map.class);
    }
}
