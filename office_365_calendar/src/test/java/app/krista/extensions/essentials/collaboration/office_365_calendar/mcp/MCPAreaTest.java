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

package app.krista.extensions.essentials.collaboration.office_365_calendar.mcp;

import app.krista.extension.executor.ExtensionResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MCPAreaTest {

    @Mock
    private CalendarMcpQueryService mcpQueryService;

    private MCPArea mcpArea;

    @Before
    public void setUp() {
        mcpArea = new MCPArea(mcpQueryService);
    }

    // ========================================================================
    // askAgent - delegation
    // ========================================================================

    @Test
    public void testAskAgent_delegatesToMcpQueryServiceAndReturnsResult() {
        Map<String, Object> values = new HashMap<>();
        values.put("Response", "test result");
        ExtensionResponse expectedResponse =
                new ExtensionResponse(ExtensionResponse.Result.SUCCESS, values, null, null, null);

        when(mcpQueryService.query("Show my meetings")).thenReturn(expectedResponse);

        ExtensionResponse result = mcpArea.askAgent("Show my meetings");

        assertSame(expectedResponse, result);
        verify(mcpQueryService).query("Show my meetings");
    }

    // ========================================================================
    // askAgent - null query
    // ========================================================================

    @Test
    public void testAskAgent_withNullQuery() {
        Map<String, Object> values = new HashMap<>();
        values.put("Response", "no query");
        ExtensionResponse expectedResponse =
                new ExtensionResponse(ExtensionResponse.Result.SUCCESS, values, null, null, null);

        when(mcpQueryService.query(isNull())).thenReturn(expectedResponse);

        ExtensionResponse result = mcpArea.askAgent(null);

        assertNotNull(result);
        assertSame(expectedResponse, result);
        verify(mcpQueryService).query(isNull());
    }

    // ========================================================================
    // askAgent - empty query
    // ========================================================================

    @Test
    public void testAskAgent_withEmptyQuery() {
        Map<String, Object> values = new HashMap<>();
        values.put("Response", "empty");
        ExtensionResponse expectedResponse =
                new ExtensionResponse(ExtensionResponse.Result.SUCCESS, values, null, null, null);

        when(mcpQueryService.query("")).thenReturn(expectedResponse);

        ExtensionResponse result = mcpArea.askAgent("");

        assertNotNull(result);
        assertSame(expectedResponse, result);
        verify(mcpQueryService).query(eq(""));
    }

    // ========================================================================
    // askAgent - queryService returns SUCCESS
    // ========================================================================

    @Test
    public void testAskAgent_whenQueryServiceReturnsSuccess() {
        Map<String, Object> values = new HashMap<>();
        values.put("Response", "calendar events found");
        ExtensionResponse successResponse =
                new ExtensionResponse(ExtensionResponse.Result.SUCCESS, values, null, null, null);

        when(mcpQueryService.query("List today's events")).thenReturn(successResponse);

        ExtensionResponse result = mcpArea.askAgent("List today's events");

        assertNotNull(result);
        assertEquals(ExtensionResponse.Result.SUCCESS, result.getResult());
        assertSame(successResponse, result);
    }

    // ========================================================================
    // askAgent - queryService returns FAILURE
    // ========================================================================

    @Test
    public void testAskAgent_whenQueryServiceReturnsFailure() {
        ExtensionResponse.Error error = new ExtensionResponse.Error(
                "AI API key is not configured.",
                System.currentTimeMillis(),
                ExtensionResponse.Error.ExceptionType.LOGIC_ERROR,
                "");
        ExtensionResponse failureResponse =
                new ExtensionResponse(ExtensionResponse.Result.FAILURE, null, error, null, null);

        when(mcpQueryService.query("List today's events")).thenReturn(failureResponse);

        ExtensionResponse result = mcpArea.askAgent("List today's events");

        assertNotNull(result);
        assertEquals(ExtensionResponse.Result.FAILURE, result.getResult());
        assertSame(failureResponse, result);
    }
}
