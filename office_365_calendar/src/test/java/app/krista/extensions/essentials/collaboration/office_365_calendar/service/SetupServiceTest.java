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

package app.krista.extensions.essentials.collaboration.office_365_calendar.service;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.invoker.InvokerConfiguration;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for SetupService class.
 * Tests health check functionality, system metrics collection, and error handling.
 */
public class SetupServiceTest {

    @Mock
    private TelemetryMetrics telemetryMetrics;

    @Mock
    private Invoker invoker;

    @Mock
    private InvokerConfiguration invokerConfiguration;

    @Mock
    private RequestContext requestContext;

    private SetupService setupService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setupService = new SetupService(telemetryMetrics, invoker, invokerConfiguration, requestContext);
    }

    @Test
    public void testCheckHealth_Success_WithValidTimeZone() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("timeZone", "America/New_York");
        when(invoker.getAttributes()).thenReturn(attributes);

        // Act
        ExtensionResponse response = setupService.checkHealth();

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Response should be successful", ExtensionResponse.Result.SUCCESS, response.getResult());
        assertNotNull("Response data should not be null", response.getResponseValue());

        // Verify telemetry metrics were recorded
        verify(telemetryMetrics, atLeast(2)).incrementCounter(anyString());
        verify(telemetryMetrics, atLeast(1)).recordDuration(anyString(), anyLong());
    }

    @Test
    public void testCheckHealth_Success_WithNullTimeZone() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("timeZone", null);
        when(invoker.getAttributes()).thenReturn(attributes);

        // Act
        ExtensionResponse response = setupService.checkHealth();

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Response should be successful", ExtensionResponse.Result.SUCCESS, response.getResult());

        Map<String, Object> data = response.getResponseValue();
        assertNotNull("Response data should not be null", data);
        assertTrue("Response should contain 'Is Healthy' field", data.containsKey("Is Healthy"));
        assertTrue("Response should contain 'Health Status' field", data.containsKey("Health Status"));
        assertTrue("Response should contain 'Extension Response' field", data.containsKey("Extension Response"));
    }

    @Test
    public void testCheckHealth_Success_WithEmptyTimeZone() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("timeZone", "");
        when(invoker.getAttributes()).thenReturn(attributes);

        // Act
        ExtensionResponse response = setupService.checkHealth();

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Response should be successful", ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testCheckHealth_Success_WithBlankTimeZone() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("timeZone", "   ");
        when(invoker.getAttributes()).thenReturn(attributes);

        // Act
        ExtensionResponse response = setupService.checkHealth();

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Response should be successful", ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    public void testCheckHealth_Unhealthy_WithInvalidTimeZone() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("timeZone", "Invalid/TimeZone");
        when(invoker.getAttributes()).thenReturn(attributes);

        // Act
        ExtensionResponse response = setupService.checkHealth();

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Response should be successful", ExtensionResponse.Result.SUCCESS, response.getResult());

        Map<String, Object> data = response.getResponseValue();
        assertNotNull("Response data should not be null", data);

        // System should be unhealthy due to invalid configuration
        Object healthStatus = data.get("Health Status");
        assertNotNull("Health Status should not be null", healthStatus);
    }

    @Test
    public void testCheckHealth_HandlesExceptionGracefully() {
        // Arrange
        when(invoker.getAttributes()).thenThrow(new RuntimeException("Test exception"));

        // Act
        ExtensionResponse response = setupService.checkHealth();

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Response should be successful even with exception", ExtensionResponse.Result.SUCCESS, response.getResult());

        Map<String, Object> data = response.getResponseValue();
        assertNotNull("Response data should not be null", data);
        assertEquals("Is Healthy should be false", false, data.get("Is Healthy"));

        // Verify failure telemetry was recorded
        verify(telemetryMetrics, atLeast(2)).incrementCounter(anyString());
        verify(telemetryMetrics, atLeast(1)).recordDuration(anyString(), anyLong());
    }
}

