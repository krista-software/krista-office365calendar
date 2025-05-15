package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ErrorHandlingStateManagerTest {

    private KeyValueStore keyValueStore;
    private ErrorHandlingStateManager stateManager;

    @Before
    public void setUp() {
        keyValueStore = mock(KeyValueStore.class);
        stateManager = new ErrorHandlingStateManager(keyValueStore);
    }

    @Test
    public void testPutStoresSerializedJson() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("error", "Invalid token");
        testMap.put("code", 401);

        String key = "testKey";
        String expectedJson = Constants.GSON.toJson(testMap);

        stateManager.put(key, testMap);

        verify(keyValueStore).put(eq(key), eq(expectedJson));
    }

    @Test
    public void testGetReturnsDeserializedMap() {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("error", "Expired session");
        expectedMap.put("retry", true);

        String key = "sessionError";
        String jsonValue = Constants.GSON.toJson(expectedMap);

        when(keyValueStore.get(key)).thenReturn(jsonValue);

        Map<String, Object> result = stateManager.get(key);

        assertEquals(expectedMap.get("error"), result.get("error"));
        assertEquals(expectedMap.get("retry"), result.get("retry"));
    }
}

