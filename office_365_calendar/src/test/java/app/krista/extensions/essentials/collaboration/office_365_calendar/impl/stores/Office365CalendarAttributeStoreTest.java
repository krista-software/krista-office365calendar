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

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Office365CalendarAttributeStoreTest {

    @Mock
    private Invoker invoker;

    @Mock
    private KeyValueStore keyValueStore;

    @Mock
    private RoutingInfo routingInfo;

    private Office365CalendarAttributeStore attributeStore;
    private static final String BASE_URL = "https://test.krista.ai";
    private static final Gson GSON = new Gson();

    @Before
    public void setUp() {
        when(invoker.getRoutingInfo()).thenReturn(routingInfo);
        when(routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE)).thenReturn(BASE_URL);

        attributeStore = new Office365CalendarAttributeStore(invoker, keyValueStore);
    }

    @Test
    public void testSave_GeneratesAuthContextId() {
        // Arrange
        Office365CalendarAttributes attributes = new Office365CalendarAttributes(
                "client-id", "client-secret", "tenant-id", "test@example.com", "UTC", Constants.PRIVATE, BASE_URL);

        // Act
        String authContextId = attributeStore.save(attributes);

        // Assert
        assertNotNull("AuthContextId should not be null", authContextId);
        assertFalse("AuthContextId should not be empty", authContextId.isEmpty());
        verify(keyValueStore).put(eq(authContextId), anyString());
    }

    @Test
    public void testSave_WithInvokerId_Success() {
        // Arrange
        String invokerId = "test-invoker-id";
        Office365CalendarAttributes attributes = new Office365CalendarAttributes(
                "client-id", "client-secret", "tenant-id", "test@example.com", "UTC", Constants.PRIVATE, BASE_URL);

        // Act
        boolean result = attributeStore.save(attributes, invokerId);

        // Assert
        assertTrue("Save should return true", result);
        verify(keyValueStore).put(eq(invokerId), anyString());
    }

    @Test
    public void testSave_WithInvokerId_PublicAuth() {
        // Arrange
        String invokerId = "test-invoker-id";
        Office365CalendarAttributes attributes = new Office365CalendarAttributes(
                "client-id", "client-secret", null, "test@example.com", "America/New_York", Constants.PUBLIC, BASE_URL);

        // Act
        boolean result = attributeStore.save(attributes, invokerId);

        // Assert
        assertTrue("Save should return true", result);
        verify(keyValueStore).put(eq(invokerId), anyString());
    }

    @Test
    public void testSave_WithInvokerId_HandlesException() {
        // Arrange
        String invokerId = "test-invoker-id";
        Office365CalendarAttributes attributes = new Office365CalendarAttributes(
                "client-id", "client-secret", "tenant-id", "test@example.com", "UTC", Constants.PRIVATE, BASE_URL);

        doThrow(new RuntimeException("Test exception")).when(keyValueStore).put(anyString(), anyString());

        // Act
        boolean result = attributeStore.save(attributes, invokerId);

        // Assert
        assertFalse("Save should return false on exception", result);
    }

    @Test
    public void testLoad_PrivateAuth_Success() {
        // Arrange
        String authContextId = "test-auth-context-id";
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put(Office365CalendarAttributes.CLIENT_ID, "client-id");
        attributeMap.put(Office365CalendarAttributes.CLIENT_SECRET, "client-secret");
        attributeMap.put(Office365CalendarAttributes.TENANT_ID, "tenant-id");
        attributeMap.put(Office365CalendarAttributes.EMAIL, "test@example.com");
        attributeMap.put(Office365CalendarAttributes.TIMEZONE, "UTC");
        attributeMap.put(Office365CalendarAttributes.AUTH_TYPE, Constants.PRIVATE);

        String jsonAttributes = GSON.toJson(attributeMap);
        when(keyValueStore.get(authContextId)).thenReturn(jsonAttributes);

        // Act
        Office365CalendarAttributes result = attributeStore.load(authContextId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Client ID should match", "client-id", result.getClientId());
        assertEquals("Client secret should match", "client-secret", result.getClientSecret());
        assertEquals("Tenant ID should match", "tenant-id", result.getTenantId());
        assertEquals("Email should match", "test@example.com", result.getEmail());
        assertEquals("Timezone should match", "UTC", result.getTimeZone());
        assertEquals("Auth type should match", Constants.PRIVATE, result.getAuthType());
    }

    @Test
    public void testLoad_PublicAuth_Success() {
        // Arrange
        String authContextId = "test-auth-context-id";
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put(Office365CalendarAttributes.CLIENT_ID, "client-id");
        attributeMap.put(Office365CalendarAttributes.CLIENT_SECRET, "client-secret");
        attributeMap.put(Office365CalendarAttributes.TENANT_ID, "");
        attributeMap.put(Office365CalendarAttributes.EMAIL, "test@example.com");
        attributeMap.put(Office365CalendarAttributes.TIMEZONE, "America/New_York");
        attributeMap.put(Office365CalendarAttributes.AUTH_TYPE, Constants.PUBLIC);

        String jsonAttributes = GSON.toJson(attributeMap);
        when(keyValueStore.get(authContextId)).thenReturn(jsonAttributes);

        // Act
        Office365CalendarAttributes result = attributeStore.load(authContextId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Client ID should match", "client-id", result.getClientId());
        assertEquals("Client secret should match", "client-secret", result.getClientSecret());
        assertNull("Tenant ID should be null for public auth", result.getTenantId());
        assertEquals("Email should match", "test@example.com", result.getEmail());
        assertEquals("Timezone should match", "America/New_York", result.getTimeZone());
        assertEquals("Auth type should match", Constants.PUBLIC, result.getAuthType());
    }

    @Test
    public void testLoad_NullValue_ReturnsNull() {
        // Arrange
        String authContextId = "test-auth-context-id";
        when(keyValueStore.get(authContextId)).thenReturn(null);

        // Act
        Office365CalendarAttributes result = attributeStore.load(authContextId);

        // Assert
        assertNull("Result should be null when store returns null", result);
    }

    @Test
    public void testLoad_BlankValue_ReturnsNull() {
        // Arrange
        String authContextId = "test-auth-context-id";
        when(keyValueStore.get(authContextId)).thenReturn("   ");

        // Act
        Office365CalendarAttributes result = attributeStore.load(authContextId);

        // Assert
        assertNull("Result should be null when store returns blank string", result);
    }

    @Test
    public void testLoad_EmptyValue_ReturnsNull() {
        // Arrange
        String authContextId = "test-auth-context-id";
        when(keyValueStore.get(authContextId)).thenReturn("");

        // Act
        Office365CalendarAttributes result = attributeStore.load(authContextId);

        // Assert
        assertNull("Result should be null when store returns empty string", result);
    }

    @Test
    public void testLoad_InvalidJson_ReturnsNull() {
        // Arrange
        String authContextId = "test-auth-context-id";
        when(keyValueStore.get(authContextId)).thenReturn("invalid json {{{");

        // Act
        Office365CalendarAttributes result = attributeStore.load(authContextId);

        // Assert
        assertNull("Result should be null when JSON is invalid", result);
    }

    @Test
    public void testLoad_NullTimezone_Success() {
        // Arrange
        String authContextId = "test-auth-context-id";
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put(Office365CalendarAttributes.CLIENT_ID, "client-id");
        attributeMap.put(Office365CalendarAttributes.CLIENT_SECRET, "client-secret");
        attributeMap.put(Office365CalendarAttributes.TENANT_ID, "tenant-id");
        attributeMap.put(Office365CalendarAttributes.EMAIL, "test@example.com");
        attributeMap.put(Office365CalendarAttributes.TIMEZONE, null);
        attributeMap.put(Office365CalendarAttributes.AUTH_TYPE, Constants.PRIVATE);

        String jsonAttributes = GSON.toJson(attributeMap);
        when(keyValueStore.get(authContextId)).thenReturn(jsonAttributes);

        // Act
        Office365CalendarAttributes result = attributeStore.load(authContextId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertNull("Timezone should be null", result.getTimeZone());
    }

    @Test
    public void testRemove_Success() {
        // Arrange
        String authContextId = "test-auth-context-id";

        // Act
        attributeStore.remove(authContextId);

        // Assert
        verify(keyValueStore).remove(authContextId);
    }

    @Test
    public void testSave_WithNullTenantId() {
        // Arrange
        Office365CalendarAttributes attributes = new Office365CalendarAttributes(
                "client-id", "client-secret", null, "test@example.com", "UTC", Constants.PUBLIC, BASE_URL);

        // Act
        String authContextId = attributeStore.save(attributes);

        // Assert
        assertNotNull("AuthContextId should not be null", authContextId);
        verify(keyValueStore).put(eq(authContextId), anyString());
    }

    @Test
    public void testSave_WithNullTimezone() {
        // Arrange
        Office365CalendarAttributes attributes = new Office365CalendarAttributes(
                "client-id", "client-secret", "tenant-id", "test@example.com", null, Constants.PRIVATE, BASE_URL);

        // Act
        String authContextId = attributeStore.save(attributes);

        // Assert
        assertNotNull("AuthContextId should not be null", authContextId);
        verify(keyValueStore).put(eq(authContextId), anyString());
    }
}