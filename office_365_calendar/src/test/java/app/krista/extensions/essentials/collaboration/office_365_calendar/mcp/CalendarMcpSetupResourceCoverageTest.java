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
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.model.base.FreeForm;
import app.krista.sdk.msauth.extension.mcp.McpProductConfig;
import app.krista.sdk.msauth.token.ITokenStore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CalendarMcpSetupResource}.
 *
 * <p>Covers protected methods ({@code getProductConfig}, {@code getJaxrsId},
 * {@code interceptNlQuery}) as well as private helpers ({@code convertToNlqResponse},
 * {@code extractFreeFormValue}, {@code toColumnKey}, {@code toColumnLabel},
 * {@code isLinkColumn}, {@code adaptTokenStore}) via reflection.</p>
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarMcpSetupResourceCoverageTest {

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private Invoker invoker;

    @Mock
    private Office365CalendarAttributeStore attributeStore;

    @Mock
    private CalendarMcpQueryService queryService;

    private CalendarMcpSetupResource resource;

    @Before
    public void setUp() {
        lenient().when(invoker.getInvokerId()).thenReturn("test-invoker");
        lenient().when(attributeStore.load("test-invoker")).thenReturn(null);
        lenient().when(refreshTokenStore.get(anyString())).thenReturn(null);
        resource = new CalendarMcpSetupResource(refreshTokenStore, invoker, attributeStore, queryService);
    }

    // ======================================================================
    // getProductConfig (protected) — via reflection
    // ======================================================================

    @Test
    public void getProductConfig_returnsCalendarMcpProductConfig() throws Exception {
        Method method = CalendarMcpSetupResource.class.getDeclaredMethod("getProductConfig");
        method.setAccessible(true);
        Object result = method.invoke(resource);
        assertNotNull(result);
        assertTrue(result instanceof CalendarMcpProductConfig);
    }

    // ======================================================================
    // getJaxrsId (protected) — via reflection
    // ======================================================================

    @Test
    public void getJaxrsId_returnsCalendar() throws Exception {
        Method method = CalendarMcpSetupResource.class.getDeclaredMethod("getJaxrsId");
        method.setAccessible(true);
        Object result = method.invoke(resource);
        assertEquals("calendar", result);
    }

    // ======================================================================
    // interceptNlQuery (protected) — via reflection
    // ======================================================================

    @Test
    public void interceptNlQuery_success_returns200WithJson() throws Exception {
        FreeForm ff = new FreeForm();
        ff.put("Subject", " Text ", "Team Meeting");

        Map<String, Object> payload = new HashMap<>();
        payload.put("Response", List.of(ff));

        ExtensionResponse extResponse = new ExtensionResponse(
                ExtensionResponse.Result.SUCCESS, payload, null, null, null);
        when(queryService.query("show meetings")).thenReturn(extResponse);

        Response response = invokeInterceptNlQuery("show meetings");

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        String json = response.getEntity().toString();
        assertTrue(json.contains("subject"));
    }

    @Test
    public void interceptNlQuery_failureWithError_returns400() throws Exception {
        ExtensionResponse.Error error = new ExtensionResponse.Error(
                "Something went wrong",
                System.currentTimeMillis(),
                ExtensionResponse.Error.ExceptionType.LOGIC_ERROR,
                "");
        ExtensionResponse extResponse = new ExtensionResponse(
                ExtensionResponse.Result.FAILURE, null, error, null, null);
        when(queryService.query("bad query")).thenReturn(extResponse);

        Response response = invokeInterceptNlQuery("bad query");

        assertEquals(400, response.getStatus());
    }

    @Test
    public void interceptNlQuery_failureWithNullError_returns400WithDefaultMessage() throws Exception {
        ExtensionResponse extResponse = new ExtensionResponse(
                ExtensionResponse.Result.FAILURE, null, null, null, null);
        when(queryService.query("another bad query")).thenReturn(extResponse);

        Response response = invokeInterceptNlQuery("another bad query");

        assertEquals(400, response.getStatus());
        String entity = response.getEntity().toString();
        assertTrue(entity.contains("Query failed"));
    }

    @Test
    public void interceptNlQuery_exception_returns400() throws Exception {
        when(queryService.query("exploding query")).thenThrow(new RuntimeException("boom"));

        Response response = invokeInterceptNlQuery("exploding query");

        assertEquals(400, response.getStatus());
        String entity = response.getEntity().toString();
        assertTrue(entity.contains("boom"));
    }

    // ======================================================================
    // convertToNlqResponse (private) — via reflection
    // ======================================================================

    @Test
    @SuppressWarnings("unchecked")
    public void convertToNlqResponse_successWithFreeFormRows() throws Exception {
        FreeForm ff = new FreeForm();
        ff.put("Subject", " Text ", "Standup");
        ff.put("Location", " Text ", "Room A");

        Map<String, Object> payload = new HashMap<>();
        payload.put("Response", List.of(ff));

        ExtensionResponse extResponse = new ExtensionResponse(
                ExtensionResponse.Result.SUCCESS, payload, null, null, null);

        Map<String, Object> result = invokeConvertToNlqResponse(extResponse, "my question");

        assertNotNull(result);
        assertEquals(1, result.get("total"));
        assertEquals(1, result.get("shown"));
        assertEquals("my question", result.get("generatedQuery"));
        assertEquals(false, result.get("specific"));

        List<Map<String, String>> columns = (List<Map<String, String>>) result.get("columns");
        assertNotNull(columns);
        assertFalse(columns.isEmpty());

        List<Map<String, String>> rows = (List<Map<String, String>>) result.get("rows");
        assertNotNull(rows);
        assertEquals(1, rows.size());
        assertEquals("Standup", rows.get(0).get("subject"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void convertToNlqResponse_generatedPlanInPayload() throws Exception {
        FreeForm ff = new FreeForm();
        ff.put("Subject", " Text ", "Review");

        Map<String, Object> payload = new HashMap<>();
        payload.put("Response", List.of(ff));
        payload.put("generatedPlan", "GET /me/events");

        ExtensionResponse extResponse = new ExtensionResponse(
                ExtensionResponse.Result.SUCCESS, payload, null, null, null);

        Map<String, Object> result = invokeConvertToNlqResponse(extResponse, "original question");

        assertEquals("GET /me/events", result.get("generatedQuery"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void convertToNlqResponse_missingResponseKey_totalIsZero() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("SomethingElse", "value");

        ExtensionResponse extResponse = new ExtensionResponse(
                ExtensionResponse.Result.SUCCESS, payload, null, null, null);

        Map<String, Object> result = invokeConvertToNlqResponse(extResponse, "q");

        assertEquals(0, result.get("total"));
        assertEquals(0, result.get("shown"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void convertToNlqResponse_emptyResponseList_totalIsZero() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("Response", new ArrayList<>());

        ExtensionResponse extResponse = new ExtensionResponse(
                ExtensionResponse.Result.SUCCESS, payload, null, null, null);

        Map<String, Object> result = invokeConvertToNlqResponse(extResponse, "q");

        assertEquals(0, result.get("total"));
        assertEquals(0, result.get("shown"));
    }

    // ======================================================================
    // extractFreeFormValue (private static) — via reflection
    // ======================================================================

    @Test
    public void extractFreeFormValue_listValueWithTypeAndValue() throws Exception {
        FreeForm ff = new FreeForm();
        ff.put("Key", " Text ", "value");

        String result = invokeExtractFreeFormValue(ff, "Key");

        assertEquals("value", result);
    }

    @Test
    public void extractFreeFormValue_listValueWithNullSecondElement() throws Exception {
        FreeForm ff = new FreeForm();
        // Manually set a list with null second element
        ff.put("Key", " Text ", null);

        String result = invokeExtractFreeFormValue(ff, "Key");

        assertNull(result);
    }

    @Test
    public void extractFreeFormValue_listValueWithEmptyString() throws Exception {
        FreeForm ff = new FreeForm();
        ff.put("Key", " Text ", "");

        String result = invokeExtractFreeFormValue(ff, "Key");

        assertNull(result);
    }

    @Test
    public void extractFreeFormValue_nonListValue() throws Exception {
        // FreeForm.put(key, type, value) stores values internally as [type, value] lists.
        // The extractFreeFormValue's non-list branch handles cases where the raw value
        // is not a list. Since FreeForm always wraps in lists, test the list path
        // with a normal string value instead.
        FreeForm ff = new FreeForm();
        ff.put("Key", " Text ", "simple-value");

        String result = invokeExtractFreeFormValue(ff, "Key");

        assertEquals("simple-value", result);
    }

    @Test
    public void extractFreeFormValue_nonListNullValue() throws Exception {
        FreeForm ff = new FreeForm();
        // Key not present at all - get returns null
        String result = invokeExtractFreeFormValue(ff, "NonExistent");

        assertNull(result);
    }

    // ======================================================================
    // toColumnKey (private static) — via reflection
    // ======================================================================

    @Test
    public void toColumnKey_null_returnsNull() throws Exception {
        assertNull(invokeToColumnKey(null));
    }

    @Test
    public void toColumnKey_empty_returnsEmpty() throws Exception {
        assertEquals("", invokeToColumnKey(""));
    }

    @Test
    public void toColumnKey_subject_returnsLowerCaseFirst() throws Exception {
        assertEquals("subject", invokeToColumnKey("Subject"));
    }

    @Test
    public void toColumnKey_singleChar_returnsLowerCase() throws Exception {
        assertEquals("a", invokeToColumnKey("A"));
    }

    // ======================================================================
    // toColumnLabel (private static) — via reflection
    // ======================================================================

    @Test
    public void toColumnLabel_null_returnsNull() throws Exception {
        assertNull(invokeToColumnLabel(null));
    }

    @Test
    public void toColumnLabel_empty_returnsEmpty() throws Exception {
        assertEquals("", invokeToColumnLabel(""));
    }

    @Test
    public void toColumnLabel_isAllDay_returnsSpacedLabel() throws Exception {
        assertEquals("Is All Day", invokeToColumnLabel("isAllDay"));
    }

    @Test
    public void toColumnLabel_singleChar_returnsUpperCase() throws Exception {
        assertEquals("A", invokeToColumnLabel("a"));
    }

    // ======================================================================
    // isLinkColumn (private static) — via reflection
    // ======================================================================

    @Test
    public void isLinkColumn_link_returnsTrue() throws Exception {
        assertTrue(invokeIsLinkColumn("link"));
    }

    @Test
    public void isLinkColumn_url_returnsTrue() throws Exception {
        assertTrue(invokeIsLinkColumn("url"));
    }

    @Test
    public void isLinkColumn_onlineMeetingUrl_returnsTrue() throws Exception {
        assertTrue(invokeIsLinkColumn("onlineMeetingUrl"));
    }

    @Test
    public void isLinkColumn_onlineMeetingJoinUrl_returnsTrue() throws Exception {
        assertTrue(invokeIsLinkColumn("onlineMeetingJoinUrl"));
    }

    @Test
    public void isLinkColumn_subject_returnsFalse() throws Exception {
        assertFalse(invokeIsLinkColumn("subject"));
    }

    // ======================================================================
    // adaptTokenStore (private static) — via reflection
    // ======================================================================

    @Test
    public void adaptTokenStore_saveGetDeleteRoundTrip() throws Exception {
        RefreshTokenStore mockStore = mock(RefreshTokenStore.class);
        Map<String, String> backingMap = new HashMap<>();

        doAnswer(inv -> {
            backingMap.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(mockStore).put(anyString(), anyString());

        doAnswer(inv -> backingMap.get(inv.<String>getArgument(0)))
                .when(mockStore).get(anyString());

        doAnswer(inv -> {
            backingMap.remove(inv.<String>getArgument(0));
            return null;
        }).when(mockStore).remove(anyString());

        ITokenStore tokenStore = invokeAdaptTokenStore(mockStore);
        assertNotNull(tokenStore);

        // save and get
        tokenStore.save("myKey", "myValue");
        assertEquals("myValue", tokenStore.get("myKey"));

        // delete
        tokenStore.delete("myKey");
        assertNull(tokenStore.get("myKey"));
    }

    // ======================================================================
    // Reflection helpers
    // ======================================================================

    private Response invokeInterceptNlQuery(String question) throws Exception {
        Method method = CalendarMcpSetupResource.class.getDeclaredMethod("interceptNlQuery", String.class);
        method.setAccessible(true);
        try {
            return (Response) method.invoke(resource, question);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> invokeConvertToNlqResponse(ExtensionResponse extResponse, String question)
            throws Exception {
        Method method = CalendarMcpSetupResource.class.getDeclaredMethod(
                "convertToNlqResponse", ExtensionResponse.class, String.class);
        method.setAccessible(true);
        try {
            return (Map<String, Object>) method.invoke(resource, extResponse, question);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private static String invokeExtractFreeFormValue(FreeForm ff, Object key) throws Exception {
        Method method = CalendarMcpSetupResource.class.getDeclaredMethod(
                "extractFreeFormValue", FreeForm.class, Object.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(null, ff, key);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private static String invokeToColumnKey(String freeFormKey) throws Exception {
        Method method = CalendarMcpSetupResource.class.getDeclaredMethod("toColumnKey", String.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(null, freeFormKey);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private static String invokeToColumnLabel(String columnKey) throws Exception {
        Method method = CalendarMcpSetupResource.class.getDeclaredMethod("toColumnLabel", String.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(null, columnKey);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private static boolean invokeIsLinkColumn(String key) throws Exception {
        Method method = CalendarMcpSetupResource.class.getDeclaredMethod("isLinkColumn", String.class);
        method.setAccessible(true);
        try {
            return (boolean) method.invoke(null, key);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private static ITokenStore invokeAdaptTokenStore(RefreshTokenStore store) throws Exception {
        Method method = CalendarMcpSetupResource.class.getDeclaredMethod("adaptTokenStore", RefreshTokenStore.class);
        method.setAccessible(true);
        try {
            return (ITokenStore) method.invoke(null, store);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }
}
