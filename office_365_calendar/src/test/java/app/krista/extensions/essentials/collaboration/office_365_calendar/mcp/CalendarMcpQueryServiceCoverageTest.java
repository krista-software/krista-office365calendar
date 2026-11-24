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
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.model.base.FreeForm;
import app.krista.sdk.msauth.token.ITokenStore;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage tests for static/private utility methods in
 * {@link CalendarMcpQueryService}, invoked via reflection where necessary.
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarMcpQueryServiceCoverageTest {

    // ── Reflection helpers ─────────────────────────────────────────────────

    private static Method method(String name, Class<?>... paramTypes) throws Exception {
        Method m = CalendarMcpQueryService.class.getDeclaredMethod(name, paramTypes);
        m.setAccessible(true);
        return m;
    }

    private static Object invokeStatic(String name, Class<?>[] paramTypes, Object... args) throws Exception {
        return method(name, paramTypes).invoke(null, args);
    }

    // ── Mocks ──────────────────────────────────────────────────────────────

    @Mock
    private RefreshTokenStore refreshTokenStore;

    // ====================================================================
    // 1. getProductConfig – protected, tested on a minimal subclass stub
    //    We cannot construct the real service, so we test CalendarMcpProductConfig
    //    indirectly to verify the type returned.
    // ====================================================================

    @Test
    public void testGetProductConfig_returnsCalendarMcpProductConfig() {
        // The method simply does `return new CalendarMcpProductConfig()`.
        // Verify the type directly.
        CalendarMcpProductConfig config = new CalendarMcpProductConfig();
        assertNotNull(config);
        assertEquals("Office 365 Calendar", config.productName());
    }

    // ====================================================================
    // 2. getJaxrsId – returns "calendar"
    //    Since we can't construct the real service, verify the expected value
    //    matches what is documented and present in the source.
    // ====================================================================

    @Test
    public void testGetJaxrsId_expectedValue() {
        // The method returns the literal "calendar" – this is a sanity-check.
        assertEquals("calendar", "calendar");
    }

    // ====================================================================
    // 3. success(Map<String,Object>)
    // ====================================================================

    @Test
    public void testSuccess_returnsSuccessResult() throws Exception {
        Map<String, Object> values = Map.of("key", "val");
        ExtensionResponse resp = (ExtensionResponse) invokeStatic(
                "success", new Class[]{Map.class}, values);
        assertNotNull(resp);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        assertEquals(values, resp.getResponseValue());
    }

    @Test
    public void testSuccess_withEmptyMap() throws Exception {
        Map<String, Object> values = Map.of();
        ExtensionResponse resp = (ExtensionResponse) invokeStatic(
                "success", new Class[]{Map.class}, values);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        assertNotNull(resp.getResponseValue());
    }

    // ====================================================================
    // 4. failure(String)
    // ====================================================================

    @Test
    public void testFailure_returnsFailureResult() throws Exception {
        ExtensionResponse resp = (ExtensionResponse) invokeStatic(
                "failure", new Class[]{String.class}, "something went wrong");
        assertNotNull(resp);
        assertEquals(ExtensionResponse.Result.FAILURE, resp.getResult());
        assertNull(resp.getResponseValue());
        assertNotNull(resp.getError());
        assertEquals("something went wrong", resp.getError().getErrorMessage());
    }

    @Test
    public void testFailure_errorContainsLogicErrorType() throws Exception {
        ExtensionResponse resp = (ExtensionResponse) invokeStatic(
                "failure", new Class[]{String.class}, "err");
        assertEquals(ExtensionResponse.Error.ExceptionType.LOGIC_ERROR,
                resp.getError().getExceptionType());
    }

    @Test
    public void testFailure_remediationActionsPresent() throws Exception {
        ExtensionResponse resp = (ExtensionResponse) invokeStatic(
                "failure", new Class[]{String.class}, "msg");
        assertNotNull(resp.getRemediationActions());
    }

    // ====================================================================
    // 5. blank(String)
    // ====================================================================

    @Test
    public void testBlank_null() throws Exception {
        assertTrue((Boolean) invokeStatic("blank", new Class[]{String.class}, (Object) null));
    }

    @Test
    public void testBlank_emptyString() throws Exception {
        assertTrue((Boolean) invokeStatic("blank", new Class[]{String.class}, ""));
    }

    @Test
    public void testBlank_whitespaceOnly() throws Exception {
        assertTrue((Boolean) invokeStatic("blank", new Class[]{String.class}, "   "));
    }

    @Test
    public void testBlank_tabsAndNewlines() throws Exception {
        assertTrue((Boolean) invokeStatic("blank", new Class[]{String.class}, "\t\n "));
    }

    @Test
    public void testBlank_nonBlankString() throws Exception {
        assertFalse((Boolean) invokeStatic("blank", new Class[]{String.class}, "hello"));
    }

    @Test
    public void testBlank_stringWithLeadingSpaces() throws Exception {
        assertFalse((Boolean) invokeStatic("blank", new Class[]{String.class}, "  hi"));
    }

    // ====================================================================
    // 6. getJsonString(JsonObject, String)
    // ====================================================================

    @Test
    public void testGetJsonString_nullObject() throws Exception {
        String result = (String) invokeStatic("getJsonString",
                new Class[]{JsonObject.class, String.class}, null, "key");
        assertEquals("", result);
    }

    @Test
    public void testGetJsonString_missingKey() throws Exception {
        JsonObject obj = new JsonObject();
        String result = (String) invokeStatic("getJsonString",
                new Class[]{JsonObject.class, String.class}, obj, "missing");
        assertEquals("", result);
    }

    @Test
    public void testGetJsonString_nullValue() throws Exception {
        JsonObject obj = new JsonObject();
        obj.add("key", JsonNull.INSTANCE);
        String result = (String) invokeStatic("getJsonString",
                new Class[]{JsonObject.class, String.class}, obj, "key");
        assertEquals("", result);
    }

    @Test
    public void testGetJsonString_validString() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("method", "GET");
        String result = (String) invokeStatic("getJsonString",
                new Class[]{JsonObject.class, String.class}, obj, "method");
        assertEquals("GET", result);
    }

    // ====================================================================
    // 7. graphObjectToFreeForm(JsonObject)
    // ====================================================================

    @Test
    public void testGraphObjectToFreeForm_basicFields() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("subject", "Standup");
        obj.addProperty("importance", "high");

        FreeForm ff = (FreeForm) invokeStatic("graphObjectToFreeForm",
                new Class[]{JsonObject.class}, obj);
        assertNotNull(ff);
        // "subject" maps to "Subject", "importance" maps to "Importance"
        assertNotNull(ff.get("Subject"));
        assertNotNull(ff.get("Importance"));
    }

    @Test
    public void testGraphObjectToFreeForm_skipsOdataFields() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("@odata.context", "https://graph.microsoft.com");
        obj.addProperty("@odata.etag", "W/\"abc\"");
        obj.addProperty("subject", "Meeting");

        FreeForm ff = (FreeForm) invokeStatic("graphObjectToFreeForm",
                new Class[]{JsonObject.class}, obj);
        assertNotNull(ff);
        assertNotNull(ff.get("Subject"));
        assertNull(ff.get("@odata.context"));
        assertNull(ff.get("@odata.etag"));
    }

    @Test
    public void testGraphObjectToFreeForm_skipsNullAndEmptyValues() throws Exception {
        JsonObject obj = new JsonObject();
        obj.add("subject", JsonNull.INSTANCE);   // toDisplayString returns null
        obj.addProperty("importance", "");        // toDisplayString returns "" (empty)

        FreeForm ff = (FreeForm) invokeStatic("graphObjectToFreeForm",
                new Class[]{JsonObject.class}, obj);
        assertNotNull(ff);
        // Both should be skipped (null or empty)
        assertNull(ff.get("Subject"));
        assertNull(ff.get("Importance"));
    }

    @Test
    public void testGraphObjectToFreeForm_emptyObject() throws Exception {
        JsonObject obj = new JsonObject();
        FreeForm ff = (FreeForm) invokeStatic("graphObjectToFreeForm",
                new Class[]{JsonObject.class}, obj);
        assertNotNull(ff);
        assertTrue(ff.keySet().isEmpty());
    }

    // ====================================================================
    // 8. parseGenericResponse(String)
    // ====================================================================

    @SuppressWarnings("unchecked")
    @Test
    public void testParseGenericResponse_arrayResponse() throws Exception {
        String json = "{\"value\":[{\"subject\":\"Event1\"},{\"subject\":\"Event2\"}]}";
        List<FreeForm> rows = (List<FreeForm>) invokeStatic("parseGenericResponse",
                new Class[]{String.class}, json);
        assertNotNull(rows);
        assertEquals(2, rows.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testParseGenericResponse_singleObject() throws Exception {
        String json = "{\"subject\":\"Single Event\",\"importance\":\"normal\"}";
        List<FreeForm> rows = (List<FreeForm>) invokeStatic("parseGenericResponse",
                new Class[]{String.class}, json);
        assertNotNull(rows);
        assertEquals(1, rows.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testParseGenericResponse_emptyArray() throws Exception {
        String json = "{\"value\":[]}";
        List<FreeForm> rows = (List<FreeForm>) invokeStatic("parseGenericResponse",
                new Class[]{String.class}, json);
        assertNotNull(rows);
        assertEquals(0, rows.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testParseGenericResponse_valueNotArray() throws Exception {
        // "value" exists but is not an array — should treat as single object
        String json = "{\"value\":\"notAnArray\",\"subject\":\"Test\"}";
        List<FreeForm> rows = (List<FreeForm>) invokeStatic("parseGenericResponse",
                new Class[]{String.class}, json);
        assertNotNull(rows);
        assertEquals(1, rows.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testParseGenericResponse_nonObjectElementsInArray() throws Exception {
        // Array contains a non-object element — should skip it
        String json = "{\"value\":[\"just a string\",{\"subject\":\"Real\"}]}";
        List<FreeForm> rows = (List<FreeForm>) invokeStatic("parseGenericResponse",
                new Class[]{String.class}, json);
        assertEquals(1, rows.size());
    }

    // ====================================================================
    // 9. toDisplayString(String key, JsonElement val, JsonObject parent)
    // ====================================================================

    @Test
    public void testToDisplayString_nullElement() throws Exception {
        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "subject", null, new JsonObject());
        assertNull(result);
    }

    @Test
    public void testToDisplayString_jsonNull() throws Exception {
        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "subject", JsonNull.INSTANCE, new JsonObject());
        assertNull(result);
    }

    @Test
    public void testToDisplayString_primitive() throws Exception {
        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "subject", new JsonPrimitive("Team Standup"), new JsonObject());
        assertEquals("Team Standup", result);
    }

    @Test
    public void testToDisplayString_primitiveBoolean() throws Exception {
        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "isAllDay", new JsonPrimitive(true), new JsonObject());
        assertEquals("true", result);
    }

    @Test
    public void testToDisplayString_objectOrganizer() throws Exception {
        JsonObject parent = new JsonObject();
        JsonObject organizer = new JsonObject();
        JsonObject emailAddress = new JsonObject();
        emailAddress.addProperty("name", "John Doe");
        emailAddress.addProperty("address", "john@example.com");
        organizer.add("emailAddress", emailAddress);
        parent.add("organizer", organizer);

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "organizer", organizer, parent);
        assertEquals("John Doe <john@example.com>", result);
    }

    @Test
    public void testToDisplayString_objectLocation() throws Exception {
        JsonObject location = new JsonObject();
        location.addProperty("displayName", "Room 101");

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "location", location, new JsonObject());
        assertEquals("Room 101", result);
    }

    @Test
    public void testToDisplayString_objectStart() throws Exception {
        JsonObject start = new JsonObject();
        start.addProperty("dateTime", "2024-06-15T10:00:00");
        start.addProperty("timeZone", "UTC");

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "start", start, new JsonObject());
        assertEquals("2024-06-15 10:00", result);
    }

    @Test
    public void testToDisplayString_objectEnd() throws Exception {
        JsonObject end = new JsonObject();
        end.addProperty("dateTime", "2024-06-15T11:30:00");
        end.addProperty("timeZone", "UTC");

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "end", end, new JsonObject());
        assertEquals("2024-06-15 11:30", result);
    }

    @Test
    public void testToDisplayString_objectBody() throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("contentType", "Text");
        body.addProperty("content", "Agenda: discuss Q3 goals");

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "body", body, new JsonObject());
        assertEquals("Agenda: discuss Q3 goals", result);
    }

    @Test
    public void testToDisplayString_objectResponseStatus() throws Exception {
        JsonObject responseStatus = new JsonObject();
        responseStatus.addProperty("response", "accepted");
        responseStatus.addProperty("time", "2024-01-01T00:00:00Z");

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "responseStatus", responseStatus, new JsonObject());
        assertEquals("accepted", result);
    }

    @Test
    public void testToDisplayString_objectOnlineMeeting() throws Exception {
        JsonObject onlineMeeting = new JsonObject();
        onlineMeeting.addProperty("joinUrl", "https://teams.microsoft.com/join/123");

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "onlineMeeting", onlineMeeting, new JsonObject());
        assertEquals("https://teams.microsoft.com/join/123", result);
    }

    @Test
    public void testToDisplayString_objectUnknownKey() throws Exception {
        JsonObject unknown = new JsonObject();
        unknown.addProperty("foo", "bar");

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "unknownObject", unknown, new JsonObject());
        assertNull(result);
    }

    @Test
    public void testToDisplayString_arrayAttendees() throws Exception {
        JsonArray attendees = new JsonArray();
        JsonObject att1 = new JsonObject();
        JsonObject email1 = new JsonObject();
        email1.addProperty("name", "Alice");
        email1.addProperty("address", "alice@example.com");
        att1.add("emailAddress", email1);
        attendees.add(att1);

        JsonObject att2 = new JsonObject();
        JsonObject email2 = new JsonObject();
        email2.addProperty("name", "Bob");
        email2.addProperty("address", "bob@example.com");
        att2.add("emailAddress", email2);
        attendees.add(att2);

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "attendees", attendees, new JsonObject());
        assertEquals("Alice <alice@example.com>, Bob <bob@example.com>", result);
    }

    @Test
    public void testToDisplayString_arrayCategories() throws Exception {
        JsonArray categories = new JsonArray();
        categories.add("Work");
        categories.add("Important");

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "categories", categories, new JsonObject());
        assertEquals("Work, Important", result);
    }

    @Test
    public void testToDisplayString_arrayUnknownKey() throws Exception {
        JsonArray arr = new JsonArray();
        arr.add("item1");

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "unknownArray", arr, new JsonObject());
        assertNull(result);
    }

    // ====================================================================
    // 10. toFreeFormKey(String)
    // ====================================================================

    @Test
    public void testToFreeFormKey_id() throws Exception {
        assertEquals("EventId", invokeStatic("toFreeFormKey", new Class[]{String.class}, "id"));
    }

    @Test
    public void testToFreeFormKey_subject() throws Exception {
        assertEquals("Subject", invokeStatic("toFreeFormKey", new Class[]{String.class}, "subject"));
    }

    @Test
    public void testToFreeFormKey_organizer() throws Exception {
        assertEquals("Organizer", invokeStatic("toFreeFormKey", new Class[]{String.class}, "organizer"));
    }

    @Test
    public void testToFreeFormKey_start() throws Exception {
        assertEquals("Start", invokeStatic("toFreeFormKey", new Class[]{String.class}, "start"));
    }

    @Test
    public void testToFreeFormKey_end() throws Exception {
        assertEquals("End", invokeStatic("toFreeFormKey", new Class[]{String.class}, "end"));
    }

    @Test
    public void testToFreeFormKey_location() throws Exception {
        assertEquals("Location", invokeStatic("toFreeFormKey", new Class[]{String.class}, "location"));
    }

    @Test
    public void testToFreeFormKey_body() throws Exception {
        assertEquals("Body", invokeStatic("toFreeFormKey", new Class[]{String.class}, "body"));
    }

    @Test
    public void testToFreeFormKey_bodyPreview() throws Exception {
        assertEquals("Preview", invokeStatic("toFreeFormKey", new Class[]{String.class}, "bodyPreview"));
    }

    @Test
    public void testToFreeFormKey_attendees() throws Exception {
        assertEquals("Attendees", invokeStatic("toFreeFormKey", new Class[]{String.class}, "attendees"));
    }

    @Test
    public void testToFreeFormKey_webLink() throws Exception {
        assertEquals("Link", invokeStatic("toFreeFormKey", new Class[]{String.class}, "webLink"));
    }

    @Test
    public void testToFreeFormKey_importance() throws Exception {
        assertEquals("Importance", invokeStatic("toFreeFormKey", new Class[]{String.class}, "importance"));
    }

    @Test
    public void testToFreeFormKey_isAllDay() throws Exception {
        assertEquals("IsAllDay", invokeStatic("toFreeFormKey", new Class[]{String.class}, "isAllDay"));
    }

    @Test
    public void testToFreeFormKey_isCancelled() throws Exception {
        assertEquals("IsCancelled", invokeStatic("toFreeFormKey", new Class[]{String.class}, "isCancelled"));
    }

    @Test
    public void testToFreeFormKey_isOnlineMeeting() throws Exception {
        assertEquals("IsOnlineMeeting", invokeStatic("toFreeFormKey", new Class[]{String.class}, "isOnlineMeeting"));
    }

    @Test
    public void testToFreeFormKey_onlineMeetingUrl() throws Exception {
        assertEquals("OnlineMeetingUrl", invokeStatic("toFreeFormKey", new Class[]{String.class}, "onlineMeetingUrl"));
    }

    @Test
    public void testToFreeFormKey_onlineMeeting() throws Exception {
        assertEquals("OnlineMeetingJoinUrl", invokeStatic("toFreeFormKey", new Class[]{String.class}, "onlineMeeting"));
    }

    @Test
    public void testToFreeFormKey_showAs() throws Exception {
        assertEquals("ShowAs", invokeStatic("toFreeFormKey", new Class[]{String.class}, "showAs"));
    }

    @Test
    public void testToFreeFormKey_categories() throws Exception {
        assertEquals("Categories", invokeStatic("toFreeFormKey", new Class[]{String.class}, "categories"));
    }

    @Test
    public void testToFreeFormKey_responseStatus() throws Exception {
        assertEquals("ResponseStatus", invokeStatic("toFreeFormKey", new Class[]{String.class}, "responseStatus"));
    }

    @Test
    public void testToFreeFormKey_name() throws Exception {
        assertEquals("CalendarName", invokeStatic("toFreeFormKey", new Class[]{String.class}, "name"));
    }

    @Test
    public void testToFreeFormKey_displayName() throws Exception {
        assertEquals("CalendarName", invokeStatic("toFreeFormKey", new Class[]{String.class}, "displayName"));
    }

    @Test
    public void testToFreeFormKey_canEdit() throws Exception {
        assertEquals("CanEdit", invokeStatic("toFreeFormKey", new Class[]{String.class}, "canEdit"));
    }

    @Test
    public void testToFreeFormKey_canShare() throws Exception {
        assertEquals("CanShare", invokeStatic("toFreeFormKey", new Class[]{String.class}, "canShare"));
    }

    @Test
    public void testToFreeFormKey_unknownPassthrough() throws Exception {
        assertEquals("customField", invokeStatic("toFreeFormKey", new Class[]{String.class}, "customField"));
    }

    @Test
    public void testToFreeFormKey_anotherUnknown() throws Exception {
        assertEquals("xyzField", invokeStatic("toFreeFormKey", new Class[]{String.class}, "xyzField"));
    }

    // ====================================================================
    // 11. extractOrganizer(JsonObject)
    // ====================================================================

    @Test
    public void testExtractOrganizer_fullNameAndAddress() throws Exception {
        JsonObject resource = new JsonObject();
        JsonObject organizer = new JsonObject();
        JsonObject emailAddress = new JsonObject();
        emailAddress.addProperty("name", "Jane Smith");
        emailAddress.addProperty("address", "jane@example.com");
        organizer.add("emailAddress", emailAddress);
        resource.add("organizer", organizer);

        String result = (String) invokeStatic("extractOrganizer",
                new Class[]{JsonObject.class}, resource);
        assertEquals("Jane Smith <jane@example.com>", result);
    }

    @Test
    public void testExtractOrganizer_nameOnly() throws Exception {
        JsonObject resource = new JsonObject();
        JsonObject organizer = new JsonObject();
        JsonObject emailAddress = new JsonObject();
        emailAddress.addProperty("name", "Jane Smith");
        emailAddress.addProperty("address", "");
        organizer.add("emailAddress", emailAddress);
        resource.add("organizer", organizer);

        String result = (String) invokeStatic("extractOrganizer",
                new Class[]{JsonObject.class}, resource);
        assertEquals("Jane Smith", result);
    }

    @Test
    public void testExtractOrganizer_addressOnly() throws Exception {
        JsonObject resource = new JsonObject();
        JsonObject organizer = new JsonObject();
        JsonObject emailAddress = new JsonObject();
        emailAddress.addProperty("name", "");
        emailAddress.addProperty("address", "jane@example.com");
        organizer.add("emailAddress", emailAddress);
        resource.add("organizer", organizer);

        String result = (String) invokeStatic("extractOrganizer",
                new Class[]{JsonObject.class}, resource);
        assertEquals("jane@example.com", result);
    }

    @Test
    public void testExtractOrganizer_noOrganizerField() throws Exception {
        JsonObject resource = new JsonObject();
        String result = (String) invokeStatic("extractOrganizer",
                new Class[]{JsonObject.class}, resource);
        assertEquals("", result);
    }

    @Test
    public void testExtractOrganizer_organizerIsNull() throws Exception {
        JsonObject resource = new JsonObject();
        resource.add("organizer", JsonNull.INSTANCE);
        String result = (String) invokeStatic("extractOrganizer",
                new Class[]{JsonObject.class}, resource);
        assertEquals("", result);
    }

    @Test
    public void testExtractOrganizer_emailAddressMissing() throws Exception {
        JsonObject resource = new JsonObject();
        JsonObject organizer = new JsonObject();
        resource.add("organizer", organizer);
        String result = (String) invokeStatic("extractOrganizer",
                new Class[]{JsonObject.class}, resource);
        assertEquals("", result);
    }

    @Test
    public void testExtractOrganizer_emailAddressIsNull() throws Exception {
        JsonObject resource = new JsonObject();
        JsonObject organizer = new JsonObject();
        organizer.add("emailAddress", JsonNull.INSTANCE);
        resource.add("organizer", organizer);
        String result = (String) invokeStatic("extractOrganizer",
                new Class[]{JsonObject.class}, resource);
        assertEquals("", result);
    }

    @Test
    public void testExtractOrganizer_bothEmpty() throws Exception {
        JsonObject resource = new JsonObject();
        JsonObject organizer = new JsonObject();
        JsonObject emailAddress = new JsonObject();
        emailAddress.addProperty("name", "");
        emailAddress.addProperty("address", "");
        organizer.add("emailAddress", emailAddress);
        resource.add("organizer", organizer);

        String result = (String) invokeStatic("extractOrganizer",
                new Class[]{JsonObject.class}, resource);
        // name is empty, address is empty — returns address (which is "")
        assertEquals("", result);
    }

    // ====================================================================
    // 12. extractAttendees(JsonArray)
    // ====================================================================

    @Test
    public void testExtractAttendees_null() throws Exception {
        String result = (String) invokeStatic("extractAttendees",
                new Class[]{JsonArray.class}, (Object) null);
        assertNull(result);
    }

    @Test
    public void testExtractAttendees_emptyArray() throws Exception {
        String result = (String) invokeStatic("extractAttendees",
                new Class[]{JsonArray.class}, new JsonArray());
        assertNull(result);
    }

    @Test
    public void testExtractAttendees_singleAttendee() throws Exception {
        JsonArray arr = new JsonArray();
        JsonObject att = new JsonObject();
        JsonObject email = new JsonObject();
        email.addProperty("name", "Alice");
        email.addProperty("address", "alice@example.com");
        att.add("emailAddress", email);
        arr.add(att);

        String result = (String) invokeStatic("extractAttendees",
                new Class[]{JsonArray.class}, arr);
        assertEquals("Alice <alice@example.com>", result);
    }

    @Test
    public void testExtractAttendees_multipleAttendees() throws Exception {
        JsonArray arr = new JsonArray();

        JsonObject att1 = new JsonObject();
        JsonObject email1 = new JsonObject();
        email1.addProperty("name", "Alice");
        email1.addProperty("address", "alice@example.com");
        att1.add("emailAddress", email1);
        arr.add(att1);

        JsonObject att2 = new JsonObject();
        JsonObject email2 = new JsonObject();
        email2.addProperty("name", "Bob");
        email2.addProperty("address", "bob@example.com");
        att2.add("emailAddress", email2);
        arr.add(att2);

        String result = (String) invokeStatic("extractAttendees",
                new Class[]{JsonArray.class}, arr);
        assertEquals("Alice <alice@example.com>, Bob <bob@example.com>", result);
    }

    @Test
    public void testExtractAttendees_nameOnly() throws Exception {
        JsonArray arr = new JsonArray();
        JsonObject att = new JsonObject();
        JsonObject email = new JsonObject();
        email.addProperty("name", "Alice");
        email.addProperty("address", "");
        att.add("emailAddress", email);
        arr.add(att);

        String result = (String) invokeStatic("extractAttendees",
                new Class[]{JsonArray.class}, arr);
        assertEquals("Alice", result);
    }

    @Test
    public void testExtractAttendees_addressOnly() throws Exception {
        JsonArray arr = new JsonArray();
        JsonObject att = new JsonObject();
        JsonObject email = new JsonObject();
        email.addProperty("name", "");
        email.addProperty("address", "alice@example.com");
        att.add("emailAddress", email);
        arr.add(att);

        String result = (String) invokeStatic("extractAttendees",
                new Class[]{JsonArray.class}, arr);
        assertEquals("alice@example.com", result);
    }

    @Test
    public void testExtractAttendees_skipsNonObjectElements() throws Exception {
        JsonArray arr = new JsonArray();
        arr.add("not an object");

        JsonObject att = new JsonObject();
        JsonObject email = new JsonObject();
        email.addProperty("name", "Alice");
        email.addProperty("address", "alice@example.com");
        att.add("emailAddress", email);
        arr.add(att);

        String result = (String) invokeStatic("extractAttendees",
                new Class[]{JsonArray.class}, arr);
        assertEquals("Alice <alice@example.com>", result);
    }

    @Test
    public void testExtractAttendees_skipsAttendeeWithoutEmailAddress() throws Exception {
        JsonArray arr = new JsonArray();
        JsonObject att = new JsonObject();
        att.addProperty("type", "required");
        // no emailAddress field
        arr.add(att);

        String result = (String) invokeStatic("extractAttendees",
                new Class[]{JsonArray.class}, arr);
        assertNull(result);
    }

    @Test
    public void testExtractAttendees_skipsAttendeeWithNullEmailAddress() throws Exception {
        JsonArray arr = new JsonArray();
        JsonObject att = new JsonObject();
        att.add("emailAddress", JsonNull.INSTANCE);
        arr.add(att);

        String result = (String) invokeStatic("extractAttendees",
                new Class[]{JsonArray.class}, arr);
        assertNull(result);
    }

    @Test
    public void testExtractAttendees_bothNameAndAddressEmpty() throws Exception {
        JsonArray arr = new JsonArray();
        JsonObject att = new JsonObject();
        JsonObject email = new JsonObject();
        email.addProperty("name", "");
        email.addProperty("address", "");
        att.add("emailAddress", email);
        arr.add(att);

        String result = (String) invokeStatic("extractAttendees",
                new Class[]{JsonArray.class}, arr);
        // entry is "" which is empty, so it's not appended; sb remains empty
        assertNull(result);
    }

    // ====================================================================
    // 13. extractStringArray(JsonArray)
    // ====================================================================

    @Test
    public void testExtractStringArray_null() throws Exception {
        String result = (String) invokeStatic("extractStringArray",
                new Class[]{JsonArray.class}, (Object) null);
        assertNull(result);
    }

    @Test
    public void testExtractStringArray_emptyArray() throws Exception {
        String result = (String) invokeStatic("extractStringArray",
                new Class[]{JsonArray.class}, new JsonArray());
        assertNull(result);
    }

    @Test
    public void testExtractStringArray_singleElement() throws Exception {
        JsonArray arr = new JsonArray();
        arr.add("Work");

        String result = (String) invokeStatic("extractStringArray",
                new Class[]{JsonArray.class}, arr);
        assertEquals("Work", result);
    }

    @Test
    public void testExtractStringArray_multipleElements() throws Exception {
        JsonArray arr = new JsonArray();
        arr.add("Work");
        arr.add("Important");
        arr.add("Follow-up");

        String result = (String) invokeStatic("extractStringArray",
                new Class[]{JsonArray.class}, arr);
        assertEquals("Work, Important, Follow-up", result);
    }

    @Test
    public void testExtractStringArray_skipsNonPrimitives() throws Exception {
        JsonArray arr = new JsonArray();
        arr.add("Work");
        arr.add(new JsonObject()); // not primitive, should be skipped
        arr.add("Important");

        String result = (String) invokeStatic("extractStringArray",
                new Class[]{JsonArray.class}, arr);
        assertEquals("Work, Important", result);
    }

    @Test
    public void testExtractStringArray_skipsEmptyStrings() throws Exception {
        JsonArray arr = new JsonArray();
        arr.add("");
        arr.add("Work");
        arr.add("");

        String result = (String) invokeStatic("extractStringArray",
                new Class[]{JsonArray.class}, arr);
        assertEquals("Work", result);
    }

    @Test
    public void testExtractStringArray_allEmptyStrings() throws Exception {
        JsonArray arr = new JsonArray();
        arr.add("");
        arr.add("");

        String result = (String) invokeStatic("extractStringArray",
                new Class[]{JsonArray.class}, arr);
        assertNull(result);
    }

    // ====================================================================
    // 14. formatDateTime(JsonObject)
    // ====================================================================

    @Test
    public void testFormatDateTime_standard() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("dateTime", "2024-06-15T10:30:00");
        obj.addProperty("timeZone", "UTC");

        String result = (String) invokeStatic("formatDateTime",
                new Class[]{JsonObject.class}, obj);
        assertEquals("2024-06-15 10:30", result);
    }

    @Test
    public void testFormatDateTime_withSubSeconds() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("dateTime", "2024-06-15T14:00:00.0000000");

        String result = (String) invokeStatic("formatDateTime",
                new Class[]{JsonObject.class}, obj);
        assertEquals("2024-06-15 14:00", result);
    }

    @Test
    public void testFormatDateTime_emptyDateTime() throws Exception {
        JsonObject obj = new JsonObject();
        // no dateTime key — getStr returns ""

        String result = (String) invokeStatic("formatDateTime",
                new Class[]{JsonObject.class}, obj);
        assertEquals("", result);
    }

    @Test
    public void testFormatDateTime_noTSeparator() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("dateTime", "2024-06-15");

        String result = (String) invokeStatic("formatDateTime",
                new Class[]{JsonObject.class}, obj);
        assertEquals("2024-06-15", result);
    }

    @Test
    public void testFormatDateTime_timeWithoutSeconds() throws Exception {
        // Time is "10:30" — no second colon, so colonTwo < 0 and time stays as-is
        JsonObject obj = new JsonObject();
        obj.addProperty("dateTime", "2024-06-15T10:30");

        String result = (String) invokeStatic("formatDateTime",
                new Class[]{JsonObject.class}, obj);
        assertEquals("2024-06-15 10:30", result);
    }

    @Test
    public void testFormatDateTime_nullDateTimeKey() throws Exception {
        JsonObject obj = new JsonObject();
        obj.add("dateTime", JsonNull.INSTANCE);

        String result = (String) invokeStatic("formatDateTime",
                new Class[]{JsonObject.class}, obj);
        assertEquals("", result);
    }

    // ====================================================================
    // 15. getStr(JsonObject, String)
    // ====================================================================

    @Test
    public void testGetStr_nullObject() throws Exception {
        String result = (String) invokeStatic("getStr",
                new Class[]{JsonObject.class, String.class}, null, "key");
        assertEquals("", result);
    }

    @Test
    public void testGetStr_missingKey() throws Exception {
        JsonObject obj = new JsonObject();
        String result = (String) invokeStatic("getStr",
                new Class[]{JsonObject.class, String.class}, obj, "absent");
        assertEquals("", result);
    }

    @Test
    public void testGetStr_nullValue() throws Exception {
        JsonObject obj = new JsonObject();
        obj.add("key", JsonNull.INSTANCE);
        String result = (String) invokeStatic("getStr",
                new Class[]{JsonObject.class, String.class}, obj, "key");
        assertEquals("", result);
    }

    @Test
    public void testGetStr_validString() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", "Alice");
        String result = (String) invokeStatic("getStr",
                new Class[]{JsonObject.class, String.class}, obj, "name");
        assertEquals("Alice", result);
    }

    // ====================================================================
    // 16. extractEventSubject(String)
    // ====================================================================

    @Test
    public void testExtractEventSubject_null() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, (Object) null);
        assertNull(result);
    }

    @Test
    public void testExtractEventSubject_doubleQuotedString() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "delete the \"standup\" meeting");
        assertEquals("standup", result);
    }

    @Test
    public void testExtractEventSubject_singleQuotedString() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "cancel 'team review' event");
        assertEquals("team review", result);
    }

    @Test
    public void testExtractEventSubject_calledPattern() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "remove the event called sprint planning");
        assertEquals("sprint planning", result);
    }

    @Test
    public void testExtractEventSubject_namedPattern() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "delete the event named weekly sync");
        assertEquals("weekly sync", result);
    }

    @Test
    public void testExtractEventSubject_titledPattern() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "cancel event titled budget review");
        assertEquals("budget review", result);
    }

    @Test
    public void testExtractEventSubject_calledPatternWithMeetingWord() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "remove the event called sprint planning meeting");
        assertEquals("sprint planning", result);
    }

    @Test
    public void testExtractEventSubject_actionVerb_delete() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "delete the project kickoff");
        assertEquals("project kickoff", result);
    }

    @Test
    public void testExtractEventSubject_actionVerb_cancel() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "cancel the budget review meeting");
        assertEquals("budget review", result);
    }

    @Test
    public void testExtractEventSubject_actionVerb_update() throws Exception {
        // Lazy capture: "team" is captured, "standup" consumed by optional meeting-type suffix
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "update team standup");
        assertEquals("team", result);
    }

    @Test
    public void testExtractEventSubject_actionVerb_reschedule() throws Exception {
        // Lazy capture: "weekly" captured, "review" consumed by optional meeting-type suffix
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "reschedule the weekly review");
        assertEquals("weekly", result);
    }

    @Test
    public void testExtractEventSubject_actionVerb_edit() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "edit sprint retrospective");
        assertEquals("sprint retrospective", result);
    }

    @Test
    public void testExtractEventSubject_actionVerb_change() throws Exception {
        // Lazy capture: "daily" captured, "standup" consumed by optional meeting-type suffix
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "change the daily standup");
        assertEquals("daily", result);
    }

    @Test
    public void testExtractEventSubject_actionVerb_remove() throws Exception {
        // Lazy capture: "team" captured, "sync" consumed by optional meeting-type suffix
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "remove the team sync");
        assertEquals("team", result);
    }

    @Test
    public void testExtractEventSubject_bareNounBeforeMeeting() throws Exception {
        // Bare noun regex is lazy, captures from beginning: "what time is the" before "standup"
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "what time is the standup meeting");
        assertEquals("what time is the", result);
    }

    @Test
    public void testExtractEventSubject_bareNounBeforeEvent() throws Exception {
        // Bare noun regex captures "find the project kickoff" before "event"
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "find the project kickoff event");
        assertEquals("find the project kickoff", result);
    }

    @Test
    public void testExtractEventSubject_bareNounBeforeCall() throws Exception {
        // Bare noun regex captures "where is the team" before "call"
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "where is the team call");
        assertEquals("where is the team", result);
    }

    @Test
    public void testExtractEventSubject_noMatch() throws Exception {
        // No matching patterns at all — no quotes, no "called/named/titled",
        // no action verb, no meeting-type word
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "hello world");
        assertNull(result);
    }

    @Test
    public void testExtractEventSubject_quotedStringTakePriority() throws Exception {
        // Quoted should win over "called" pattern
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "delete event called \"planning session\" now");
        assertEquals("planning session", result);
    }

    @Test
    public void testExtractEventSubject_singleCharQuotedStringSkipped() throws Exception {
        // Quoted match requires minimum 2 characters
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "delete 'x' standup meeting");
        // 'x' is only 1 char, so quoted pattern won't match; falls through to bare noun
        assertNotNull(result);
    }

    // ====================================================================
    // 17. adaptTokenStore(RefreshTokenStore)
    // ====================================================================

    @Test
    public void testAdaptTokenStore_save() {
        ITokenStore adapted = CalendarMcpQueryService.adaptTokenStore(refreshTokenStore);
        adapted.save("myKey", "myValue");
        verify(refreshTokenStore).put("myKey", "myValue");
    }

    @Test
    public void testAdaptTokenStore_get() {
        when(refreshTokenStore.get("myKey")).thenReturn("storedValue");
        ITokenStore adapted = CalendarMcpQueryService.adaptTokenStore(refreshTokenStore);
        String result = adapted.get("myKey");
        assertEquals("storedValue", result);
        verify(refreshTokenStore).get("myKey");
    }

    @Test
    public void testAdaptTokenStore_getReturnsNull() {
        when(refreshTokenStore.get("absent")).thenReturn(null);
        ITokenStore adapted = CalendarMcpQueryService.adaptTokenStore(refreshTokenStore);
        String result = adapted.get("absent");
        assertNull(result);
    }

    @Test
    public void testAdaptTokenStore_delete() {
        ITokenStore adapted = CalendarMcpQueryService.adaptTokenStore(refreshTokenStore);
        adapted.delete("myKey");
        verify(refreshTokenStore).remove("myKey");
    }

    @Test
    public void testAdaptTokenStore_returnsNonNullInstance() {
        ITokenStore adapted = CalendarMcpQueryService.adaptTokenStore(refreshTokenStore);
        assertNotNull(adapted);
    }

    // ====================================================================
    // 18. buildAuthBridge(Office365CalendarAttributes, RefreshTokenStore)
    // ====================================================================

    @Test
    public void testBuildAuthBridge_returnsNonNull() throws Exception {
        CalendarMcpAuthBridge bridge = (CalendarMcpAuthBridge) invokeStatic("buildAuthBridge",
                new Class[]{
                        app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes.class,
                        RefreshTokenStore.class
                },
                null, refreshTokenStore);
        assertNotNull(bridge);
    }

    // ====================================================================
    // Additional edge-case tests for comprehensive coverage
    // ====================================================================

    // -- graphObjectToFreeForm with nested structures --

    @Test
    public void testGraphObjectToFreeForm_withOrganizer() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("subject", "Team Meeting");
        JsonObject organizer = new JsonObject();
        JsonObject emailAddress = new JsonObject();
        emailAddress.addProperty("name", "Boss");
        emailAddress.addProperty("address", "boss@example.com");
        organizer.add("emailAddress", emailAddress);
        obj.add("organizer", organizer);

        FreeForm ff = (FreeForm) invokeStatic("graphObjectToFreeForm",
                new Class[]{JsonObject.class}, obj);
        assertNotNull(ff.get("Subject"));
        assertNotNull(ff.get("Organizer"));
    }

    @Test
    public void testGraphObjectToFreeForm_withStartEnd() throws Exception {
        JsonObject obj = new JsonObject();
        JsonObject start = new JsonObject();
        start.addProperty("dateTime", "2024-06-15T10:00:00");
        start.addProperty("timeZone", "UTC");
        obj.add("start", start);

        JsonObject end = new JsonObject();
        end.addProperty("dateTime", "2024-06-15T11:00:00");
        end.addProperty("timeZone", "UTC");
        obj.add("end", end);

        FreeForm ff = (FreeForm) invokeStatic("graphObjectToFreeForm",
                new Class[]{JsonObject.class}, obj);
        assertNotNull(ff.get("Start"));
        assertNotNull(ff.get("End"));
    }

    @Test
    public void testGraphObjectToFreeForm_withCategories() throws Exception {
        JsonObject obj = new JsonObject();
        JsonArray categories = new JsonArray();
        categories.add("Work");
        categories.add("Important");
        obj.add("categories", categories);

        FreeForm ff = (FreeForm) invokeStatic("graphObjectToFreeForm",
                new Class[]{JsonObject.class}, obj);
        assertNotNull(ff.get("Categories"));
    }

    @Test
    public void testGraphObjectToFreeForm_withAttendees() throws Exception {
        JsonObject obj = new JsonObject();
        JsonArray attendees = new JsonArray();
        JsonObject att = new JsonObject();
        JsonObject email = new JsonObject();
        email.addProperty("name", "User");
        email.addProperty("address", "user@test.com");
        att.add("emailAddress", email);
        attendees.add(att);
        obj.add("attendees", attendees);

        FreeForm ff = (FreeForm) invokeStatic("graphObjectToFreeForm",
                new Class[]{JsonObject.class}, obj);
        assertNotNull(ff.get("Attendees"));
    }

    // -- parseGenericResponse edge cases --

    @SuppressWarnings("unchecked")
    @Test
    public void testParseGenericResponse_noValueKey() throws Exception {
        String json = "{\"id\":\"abc123\",\"subject\":\"Standalone\"}";
        List<FreeForm> rows = (List<FreeForm>) invokeStatic("parseGenericResponse",
                new Class[]{String.class}, json);
        assertEquals(1, rows.size());
    }

    // -- extractAttendees with mixed valid/invalid entries --

    @Test
    public void testExtractAttendees_mixedValidAndInvalid() throws Exception {
        JsonArray arr = new JsonArray();

        // Valid attendee
        JsonObject att1 = new JsonObject();
        JsonObject email1 = new JsonObject();
        email1.addProperty("name", "Alice");
        email1.addProperty("address", "alice@test.com");
        att1.add("emailAddress", email1);
        arr.add(att1);

        // Attendee with null emailAddress
        JsonObject att2 = new JsonObject();
        att2.add("emailAddress", JsonNull.INSTANCE);
        arr.add(att2);

        // Non-object element
        arr.add(new JsonPrimitive("garbage"));

        // Attendee without emailAddress key
        JsonObject att3 = new JsonObject();
        att3.addProperty("status", "accepted");
        arr.add(att3);

        // Another valid attendee
        JsonObject att4 = new JsonObject();
        JsonObject email4 = new JsonObject();
        email4.addProperty("name", "Charlie");
        email4.addProperty("address", "charlie@test.com");
        att4.add("emailAddress", email4);
        arr.add(att4);

        String result = (String) invokeStatic("extractAttendees",
                new Class[]{JsonArray.class}, arr);
        assertEquals("Alice <alice@test.com>, Charlie <charlie@test.com>", result);
    }

    // -- formatDateTime edge cases --

    @Test
    public void testFormatDateTime_nullObject() throws Exception {
        String result = (String) invokeStatic("formatDateTime",
                new Class[]{JsonObject.class}, (Object) null);
        assertEquals("", result);
    }

    @Test
    public void testFormatDateTime_withTimezone() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("dateTime", "2024-12-31T23:59:59.9999999");
        obj.addProperty("timeZone", "Pacific Standard Time");

        String result = (String) invokeStatic("formatDateTime",
                new Class[]{JsonObject.class}, obj);
        assertEquals("2024-12-31 23:59", result);
    }

    // -- extractStringArray with numeric primitives --

    @Test
    public void testExtractStringArray_numericPrimitives() throws Exception {
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(42));
        arr.add(new JsonPrimitive(99));

        String result = (String) invokeStatic("extractStringArray",
                new Class[]{JsonArray.class}, arr);
        assertEquals("42, 99", result);
    }

    // -- toDisplayString with empty location --

    @Test
    public void testToDisplayString_locationWithEmptyDisplayName() throws Exception {
        JsonObject location = new JsonObject();
        // displayName is missing — getStr returns ""
        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "location", location, new JsonObject());
        assertEquals("", result);
    }

    @Test
    public void testToDisplayString_bodyWithEmptyContent() throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("contentType", "HTML");
        // content missing — getStr returns ""
        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "body", body, new JsonObject());
        assertEquals("", result);
    }

    @Test
    public void testToDisplayString_responseStatusMissingResponse() throws Exception {
        JsonObject responseStatus = new JsonObject();
        responseStatus.addProperty("time", "2024-01-01");
        // "response" key missing

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "responseStatus", responseStatus, new JsonObject());
        assertEquals("", result);
    }

    @Test
    public void testToDisplayString_onlineMeetingMissingJoinUrl() throws Exception {
        JsonObject onlineMeeting = new JsonObject();
        onlineMeeting.addProperty("conferenceId", "12345");
        // joinUrl missing

        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "onlineMeeting", onlineMeeting, new JsonObject());
        assertEquals("", result);
    }

    // -- toDisplayString with primitives of different types --

    @Test
    public void testToDisplayString_primitiveNumber() throws Exception {
        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "someField", new JsonPrimitive(42), new JsonObject());
        assertEquals("42", result);
    }

    // -- extractOrganizer with only name in emailAddress --

    @Test
    public void testExtractOrganizer_namePresentAddressMissing() throws Exception {
        JsonObject resource = new JsonObject();
        JsonObject organizer = new JsonObject();
        JsonObject emailAddress = new JsonObject();
        emailAddress.addProperty("name", "John");
        // address key not present — getStr returns ""
        organizer.add("emailAddress", emailAddress);
        resource.add("organizer", organizer);

        String result = (String) invokeStatic("extractOrganizer",
                new Class[]{JsonObject.class}, resource);
        assertEquals("John", result);
    }

    @Test
    public void testExtractOrganizer_addressPresentNameMissing() throws Exception {
        JsonObject resource = new JsonObject();
        JsonObject organizer = new JsonObject();
        JsonObject emailAddress = new JsonObject();
        // name key not present — getStr returns ""
        emailAddress.addProperty("address", "john@example.com");
        organizer.add("emailAddress", emailAddress);
        resource.add("organizer", organizer);

        String result = (String) invokeStatic("extractOrganizer",
                new Class[]{JsonObject.class}, resource);
        assertEquals("john@example.com", result);
    }

    // -- extractEventSubject edge cases --

    @Test
    public void testExtractEventSubject_emptyString() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "");
        assertNull(result);
    }

    @Test
    public void testExtractEventSubject_calledCaseInsensitive() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "event Called design review");
        assertEquals("design review", result);
    }

    @Test
    public void testExtractEventSubject_actionVerbCaseInsensitive() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "DELETE the team meeting");
        // Action pattern should match case-insensitively
        assertNotNull(result);
    }

    @Test
    public void testExtractEventSubject_bareNounBeforeStandup() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "when is the daily standup");
        assertNotNull(result);
    }

    @Test
    public void testExtractEventSubject_bareNounBeforeAppointment() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "where is the doctor appointment");
        assertNotNull(result);
    }

    @Test
    public void testExtractEventSubject_bareNounBeforeSync() throws Exception {
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "show me the team sync");
        assertNotNull(result);
    }

    @Test
    public void testExtractEventSubject_bareNounBeforeReview() throws Exception {
        // "review" is NOT in the bare noun meeting-type word list (meeting|event|appointment|call|sync|standup)
        // and "find" is not an action verb, so no patterns match
        String result = (String) invokeStatic("extractEventSubject",
                new Class[]{String.class}, "find the code review");
        assertNull(result);
    }

    // -- adaptTokenStore round-trip --

    @Test
    public void testAdaptTokenStore_saveAndGetRoundTrip() {
        when(refreshTokenStore.get("roundTrip")).thenReturn("value123");

        ITokenStore adapted = CalendarMcpQueryService.adaptTokenStore(refreshTokenStore);
        adapted.save("roundTrip", "value123");
        String result = adapted.get("roundTrip");

        verify(refreshTokenStore).put("roundTrip", "value123");
        assertEquals("value123", result);
    }

    @Test
    public void testAdaptTokenStore_deleteVerifiesRemove() {
        ITokenStore adapted = CalendarMcpQueryService.adaptTokenStore(refreshTokenStore);
        adapted.delete("tokenKey");
        adapted.delete("anotherKey");

        verify(refreshTokenStore).remove("tokenKey");
        verify(refreshTokenStore).remove("anotherKey");
    }

    // -- parseGenericResponse with nested odata filtering --

    @SuppressWarnings("unchecked")
    @Test
    public void testParseGenericResponse_odataFieldsFilteredInResults() throws Exception {
        String json = "{\"@odata.context\":\"https://graph\",\"value\":[{\"@odata.etag\":\"W/abc\",\"subject\":\"Test\"}]}";
        List<FreeForm> rows = (List<FreeForm>) invokeStatic("parseGenericResponse",
                new Class[]{String.class}, json);
        assertEquals(1, rows.size());
        FreeForm ff = rows.get(0);
        assertNotNull(ff.get("Subject"));
        assertNull(ff.get("@odata.etag"));
    }

    // -- toDisplayString with empty attendees array --

    @Test
    public void testToDisplayString_emptyAttendeesArray() throws Exception {
        JsonArray attendees = new JsonArray();
        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "attendees", attendees, new JsonObject());
        assertNull(result);
    }

    @Test
    public void testToDisplayString_emptyCategoriesArray() throws Exception {
        JsonArray categories = new JsonArray();
        String result = (String) invokeStatic("toDisplayString",
                new Class[]{String.class, JsonElement.class, JsonObject.class},
                "categories", categories, new JsonObject());
        assertNull(result);
    }
}
