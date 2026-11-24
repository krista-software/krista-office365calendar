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

import app.krista.model.base.FreeForm;
import app.krista.sdk.msauth.extension.mcp.McpSearchResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test suite for {@link CalendarMcpProductConfig}.
 * Covers all public methods and private helper methods (via reflection).
 */
public class CalendarMcpProductConfigTest {

    private CalendarMcpProductConfig config;

    @Before
    public void setUp() {
        config = new CalendarMcpProductConfig();
    }

    // ── 1. productName ──────────────────────────────────────

    @Test
    public void testProductName() {
        assertEquals("Office 365 Calendar", config.productName());
    }

    // ── 2. queryLanguage ────────────────────────────────────

    @Test
    public void testQueryLanguage() {
        assertEquals("KQL", config.queryLanguage());
    }

    // ── 3. queryLanguageFullName ─────────────────────────────

    @Test
    public void testQueryLanguageFullName() {
        assertEquals("KQL (Keyword Query Language)", config.queryLanguageFullName());
    }

    // ── 4. searchUrl ────────────────────────────────────────

    @Test
    public void testSearchUrl() {
        assertEquals("https://graph.microsoft.com/v1.0/search/query", config.searchUrl());
    }

    // ── 5. searchHttpMethod ─────────────────────────────────

    @Test
    public void testSearchHttpMethod() {
        assertEquals("POST", config.searchHttpMethod());
    }

    // ── 6. standardFields ───────────────────────────────────

    @Test
    public void testStandardFields() {
        List<String> fields = config.standardFields();
        assertEquals(6, fields.size());
        assertEquals("subject", fields.get(0));
        assertEquals("organizer", fields.get(1));
        assertEquals("start", fields.get(2));
        assertEquals("end", fields.get(3));
        assertEquals("location", fields.get(4));
        assertEquals("webLink", fields.get(5));
    }

    // ── 7. resultColumns ────────────────────────────────────

    @Test
    public void testResultColumnsIsEmpty() {
        assertTrue("resultColumns should be empty", config.resultColumns().isEmpty());
    }

    // ── 8. nlExamples ───────────────────────────────────────

    @Test
    public void testNlExamples() {
        List<String> examples = config.nlExamples();
        assertEquals(3, examples.size());
        assertTrue(examples.contains("Fetch all events"));
        assertTrue(examples.contains("List my calendars"));
        assertTrue(examples.contains("Create a meeting tomorrow at 10am"));
    }

    // ── 9. kqlExamples ──────────────────────────────────────

    @Test
    public void testKqlExamples() {
        List<String> examples = config.kqlExamples();
        assertEquals(3, examples.size());
        assertTrue(examples.contains("subject:standup"));
        assertTrue(examples.contains("organizer:john@company.com"));
        assertTrue(examples.contains("location:\"Conference Room\""));
    }

    // ── 10. buildSearchRequestBody ──────────────────────────

    @Test
    public void testBuildSearchRequestBody_ContainsEntityTypes() {
        String body = config.buildSearchRequestBody("subject:standup", 10, false);
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        JsonArray requests = root.getAsJsonArray("requests");
        assertEquals(1, requests.size());

        JsonObject request = requests.get(0).getAsJsonObject();
        JsonArray entityTypes = request.getAsJsonArray("entityTypes");
        assertEquals(1, entityTypes.size());
        assertEquals("event", entityTypes.get(0).getAsString());
    }

    @Test
    public void testBuildSearchRequestBody_ContainsQueryString() {
        String body = config.buildSearchRequestBody("subject:standup", 10, false);
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        JsonObject request = root.getAsJsonArray("requests").get(0).getAsJsonObject();
        JsonObject query = request.getAsJsonObject("query");
        assertEquals("subject:standup", query.get("queryString").getAsString());
    }

    @Test
    public void testBuildSearchRequestBody_ContainsSize() {
        String body = config.buildSearchRequestBody("test", 25, true);
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        JsonObject request = root.getAsJsonArray("requests").get(0).getAsJsonObject();
        assertEquals(25, request.get("size").getAsInt());
    }

    @Test
    public void testBuildSearchRequestBody_ContainsFrom() {
        String body = config.buildSearchRequestBody("test", 5, false);
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        JsonObject request = root.getAsJsonArray("requests").get(0).getAsJsonObject();
        assertEquals(0, request.get("from").getAsInt());
    }

    // ── 11. parseSearchResponse ─────────────────────────────

    @Test
    public void testParseSearchResponse_EmptyHits() {
        String json = buildSearchResponseJson(0);
        McpSearchResult result = config.parseSearchResponse(json, "https://graph.microsoft.com");
        assertEquals(0, result.rows().size());
    }

    @Test
    public void testParseSearchResponse_SingleEvent() {
        String json = buildSingleEventSearchResponse(
                "Team Standup", "Alice", "alice@company.com",
                "2024-06-15T09:00:00", "2024-06-15T09:30:00",
                "Conference Room A", "https://outlook.com/event/123"
        );
        McpSearchResult result = config.parseSearchResponse(json, "https://graph.microsoft.com");
        assertEquals(1, result.rows().size());

        Map<String, Object> row = result.rows().get(0);
        assertEquals("Team Standup", row.get("subject"));
        assertEquals("Alice <alice@company.com>", row.get("organizer"));
        assertEquals("2024-06-15 09:00", row.get("start"));
        assertEquals("2024-06-15 09:30", row.get("end"));
        assertEquals("Conference Room A", row.get("location"));
        assertEquals("https://outlook.com/event/123", row.get("url"));
    }

    @Test
    public void testParseSearchResponse_MultipleHits() {
        String json = buildMultipleEventsSearchResponse();
        McpSearchResult result = config.parseSearchResponse(json, "https://graph.microsoft.com");
        assertEquals(2, result.rows().size());
    }

    // ── 12. parseStandardResponse ───────────────────────────

    @Test
    public void testParseStandardResponse_Empty() {
        String json = buildSearchResponseJson(0);
        List<FreeForm> rows = config.parseStandardResponse(json, "https://graph.microsoft.com");
        assertTrue("Should return empty list for empty hits", rows.isEmpty());
    }

    @Test
    public void testParseStandardResponse_SingleEvent() {
        String json = buildSingleEventSearchResponse(
                "Sprint Review", "Bob", "bob@company.com",
                "2024-07-01T14:00:00", "2024-07-01T15:00:00",
                "Zoom", "https://outlook.com/event/456"
        );
        List<FreeForm> rows = config.parseStandardResponse(json, "https://graph.microsoft.com");
        assertEquals(1, rows.size());
    }

    @Test
    public void testParseStandardResponse_MultipleEvents() {
        String json = buildMultipleEventsSearchResponse();
        List<FreeForm> rows = config.parseStandardResponse(json, "https://graph.microsoft.com");
        assertEquals(2, rows.size());
    }

    // ── 13. parseAllFieldsResponse ──────────────────────────

    @Test
    public void testParseAllFieldsResponse_Empty() {
        String json = buildSearchResponseJson(0);
        List<Map<String, String>> rows = config.parseAllFieldsResponse(json, "https://graph.microsoft.com");
        assertTrue("Should return empty list for empty hits", rows.isEmpty());
    }

    @Test
    public void testParseAllFieldsResponse_SingleEvent() {
        String json = buildSingleEventWithSummarySearchResponse(
                "Project Sync", "Carol", "carol@company.com",
                "2024-08-10T11:00:00", "2024-08-10T12:00:00",
                "Board Room", "https://outlook.com/event/789",
                "Discuss project milestones"
        );
        List<Map<String, String>> rows = config.parseAllFieldsResponse(json, "https://graph.microsoft.com");
        assertEquals(1, rows.size());

        Map<String, String> row = rows.get(0);
        assertEquals("Project Sync", row.get("title"));
        assertEquals("Project Sync", row.get("key"));
        assertEquals("Project Sync", row.get("subject"));
        assertEquals("Carol <carol@company.com>", row.get("organizer"));
        assertEquals("2024-08-10 11:00", row.get("start"));
        assertEquals("2024-08-10 12:00", row.get("end"));
        assertEquals("Board Room", row.get("location"));
        assertEquals("https://outlook.com/event/789", row.get("url"));
        assertEquals("Discuss project milestones", row.get("summary"));
    }

    @Test
    public void testParseAllFieldsResponse_MultipleEvents() {
        String json = buildMultipleEventsSearchResponse();
        List<Map<String, String>> rows = config.parseAllFieldsResponse(json, "https://graph.microsoft.com");
        assertEquals(2, rows.size());
    }

    // ── 14. queryGenerationPrompt ───────────────────────────

    @Test
    public void testQueryGenerationPrompt_ContainsQuestion() {
        String prompt = config.queryGenerationPrompt("Find my meetings");
        assertTrue(prompt.contains("Find my meetings"));
    }

    @Test
    public void testQueryGenerationPrompt_ContainsKqlSyntax() {
        String prompt = config.queryGenerationPrompt("test");
        assertTrue(prompt.contains("KQL"));
        assertTrue(prompt.contains("syntax"));
    }

    @Test
    public void testQueryGenerationPrompt_ContainsOutputInstruction() {
        String prompt = config.queryGenerationPrompt("test");
        assertTrue(prompt.contains("Output ONLY raw JSON"));
    }

    // ── 15. answerExtractionPrompt ──────────────────────────

    @Test
    public void testAnswerExtractionPrompt_ContainsQuestion() {
        String prompt = config.answerExtractionPrompt("When is my standup?", "event data here");
        assertTrue(prompt.contains("When is my standup?"));
    }

    @Test
    public void testAnswerExtractionPrompt_ContainsContentData() {
        String prompt = config.answerExtractionPrompt("test", "some calendar data");
        assertTrue(prompt.contains("some calendar data"));
    }

    @Test
    public void testAnswerExtractionPrompt_ContainsDoNotInvent() {
        String prompt = config.answerExtractionPrompt("test", "data");
        assertTrue(prompt.contains("Do not invent"));
    }

    // ── 16. intentClassificationPrompt ──────────────────────

    @Test
    public void testIntentClassificationPrompt_ContainsQuestion() {
        String prompt = config.intentClassificationPrompt("Schedule a meeting");
        assertTrue(prompt.contains("Schedule a meeting"));
    }

    @Test
    public void testIntentClassificationPrompt_ContainsCreateEventIntent() {
        String prompt = config.intentClassificationPrompt("test");
        assertTrue(prompt.contains("create_event"));
    }

    @Test
    public void testIntentClassificationPrompt_ContainsSearchIntent() {
        String prompt = config.intentClassificationPrompt("test");
        assertTrue(prompt.contains("search"));
    }

    // ── 17. graphApiPlanningPrompt ──────────────────────────

    @Test
    public void testGraphApiPlanningPrompt_ContainsQuestion() {
        String prompt = config.graphApiPlanningPrompt("Show my events");
        assertTrue(prompt.contains("Show my events"));
    }

    @Test
    public void testGraphApiPlanningPrompt_ContainsMeEvents() {
        String prompt = config.graphApiPlanningPrompt("test");
        assertTrue(prompt.contains("/me/events"));
    }

    @Test
    public void testGraphApiPlanningPrompt_ContainsMeCalendars() {
        String prompt = config.graphApiPlanningPrompt("test");
        assertTrue(prompt.contains("/me/calendars"));
    }

    @Test
    public void testGraphApiPlanningPrompt_ContainsOutputInstruction() {
        String prompt = config.graphApiPlanningPrompt("test");
        assertTrue(prompt.contains("Output ONLY a raw JSON object"));
    }

    // ── 18. getString (private, via reflection) ─────────────

    @Test
    public void testGetString_NullObject() throws Exception {
        Method m = getPrivateMethod("getString", JsonObject.class, String.class);
        assertEquals("", m.invoke(null, null, "key"));
    }

    @Test
    public void testGetString_MissingKey() throws Exception {
        Method m = getPrivateMethod("getString", JsonObject.class, String.class);
        JsonObject obj = new JsonObject();
        assertEquals("", m.invoke(null, obj, "missing"));
    }

    @Test
    public void testGetString_NullValue() throws Exception {
        Method m = getPrivateMethod("getString", JsonObject.class, String.class);
        JsonObject obj = new JsonObject();
        obj.add("key", JsonNull.INSTANCE);
        assertEquals("", m.invoke(null, obj, "key"));
    }

    @Test
    public void testGetString_PresentValue() throws Exception {
        Method m = getPrivateMethod("getString", JsonObject.class, String.class);
        JsonObject obj = new JsonObject();
        obj.addProperty("key", "hello");
        assertEquals("hello", m.invoke(null, obj, "key"));
    }

    // ── 19. extractNestedDateTime (private, via reflection) ──

    @Test
    public void testExtractNestedDateTime_MissingKey() throws Exception {
        Method m = getPrivateMethod("extractNestedDateTime", JsonObject.class, String.class);
        JsonObject obj = new JsonObject();
        assertEquals("", m.invoke(null, obj, "start"));
    }

    @Test
    public void testExtractNestedDateTime_NullValue() throws Exception {
        Method m = getPrivateMethod("extractNestedDateTime", JsonObject.class, String.class);
        JsonObject obj = new JsonObject();
        obj.add("start", JsonNull.INSTANCE);
        assertEquals("", m.invoke(null, obj, "start"));
    }

    @Test
    public void testExtractNestedDateTime_NestedDateTimeObject() throws Exception {
        Method m = getPrivateMethod("extractNestedDateTime", JsonObject.class, String.class);
        JsonObject obj = new JsonObject();
        JsonObject start = new JsonObject();
        start.addProperty("dateTime", "2024-06-15T09:00:00");
        obj.add("start", start);
        assertEquals("2024-06-15T09:00:00", m.invoke(null, obj, "start"));
    }

    @Test
    public void testExtractNestedDateTime_PrimitiveString() throws Exception {
        Method m = getPrivateMethod("extractNestedDateTime", JsonObject.class, String.class);
        JsonObject obj = new JsonObject();
        obj.addProperty("start", "2024-06-15T09:00:00");
        assertEquals("2024-06-15T09:00:00", m.invoke(null, obj, "start"));
    }

    // ── 20. extractOrganizer (private, via reflection) ──────

    @Test
    public void testExtractOrganizer_Missing() throws Exception {
        Method m = getPrivateMethod("extractOrganizer", JsonObject.class);
        JsonObject obj = new JsonObject();
        assertEquals("", m.invoke(null, obj));
    }

    @Test
    public void testExtractOrganizer_NullEmailAddress() throws Exception {
        Method m = getPrivateMethod("extractOrganizer", JsonObject.class);
        JsonObject obj = new JsonObject();
        JsonObject organizer = new JsonObject();
        organizer.add("emailAddress", JsonNull.INSTANCE);
        obj.add("organizer", organizer);
        assertEquals("", m.invoke(null, obj));
    }

    @Test
    public void testExtractOrganizer_NameAndAddress() throws Exception {
        Method m = getPrivateMethod("extractOrganizer", JsonObject.class);
        JsonObject obj = new JsonObject();
        JsonObject organizer = new JsonObject();
        JsonObject emailAddress = new JsonObject();
        emailAddress.addProperty("name", "Alice");
        emailAddress.addProperty("address", "alice@company.com");
        organizer.add("emailAddress", emailAddress);
        obj.add("organizer", organizer);
        assertEquals("Alice <alice@company.com>", m.invoke(null, obj));
    }

    @Test
    public void testExtractOrganizer_OnlyName() throws Exception {
        Method m = getPrivateMethod("extractOrganizer", JsonObject.class);
        JsonObject obj = new JsonObject();
        JsonObject organizer = new JsonObject();
        JsonObject emailAddress = new JsonObject();
        emailAddress.addProperty("name", "Alice");
        organizer.add("emailAddress", emailAddress);
        obj.add("organizer", organizer);
        assertEquals("Alice", m.invoke(null, obj));
    }

    @Test
    public void testExtractOrganizer_OnlyAddress() throws Exception {
        Method m = getPrivateMethod("extractOrganizer", JsonObject.class);
        JsonObject obj = new JsonObject();
        JsonObject organizer = new JsonObject();
        JsonObject emailAddress = new JsonObject();
        emailAddress.addProperty("address", "alice@company.com");
        organizer.add("emailAddress", emailAddress);
        obj.add("organizer", organizer);
        assertEquals("alice@company.com", m.invoke(null, obj));
    }

    // ── 21. extractLocation (private, via reflection) ───────

    @Test
    public void testExtractLocation_Missing() throws Exception {
        Method m = getPrivateMethod("extractLocation", JsonObject.class);
        JsonObject obj = new JsonObject();
        assertEquals("", m.invoke(null, obj));
    }

    @Test
    public void testExtractLocation_NullValue() throws Exception {
        Method m = getPrivateMethod("extractLocation", JsonObject.class);
        JsonObject obj = new JsonObject();
        obj.add("location", JsonNull.INSTANCE);
        assertEquals("", m.invoke(null, obj));
    }

    @Test
    public void testExtractLocation_NestedDisplayName() throws Exception {
        Method m = getPrivateMethod("extractLocation", JsonObject.class);
        JsonObject obj = new JsonObject();
        JsonObject location = new JsonObject();
        location.addProperty("displayName", "Conference Room B");
        obj.add("location", location);
        assertEquals("Conference Room B", m.invoke(null, obj));
    }

    @Test
    public void testExtractLocation_PrimitiveString() throws Exception {
        Method m = getPrivateMethod("extractLocation", JsonObject.class);
        JsonObject obj = new JsonObject();
        obj.addProperty("location", "Board Room");
        assertEquals("Board Room", m.invoke(null, obj));
    }

    // ── 22. formatDateTime (private, via reflection) ────────

    @Test
    public void testFormatDateTime_Null() throws Exception {
        Method m = getPrivateMethod("formatDateTime", String.class);
        assertEquals("", m.invoke(null, (Object) null));
    }

    @Test
    public void testFormatDateTime_Empty() throws Exception {
        Method m = getPrivateMethod("formatDateTime", String.class);
        assertEquals("", m.invoke(null, ""));
    }

    @Test
    public void testFormatDateTime_NoT() throws Exception {
        Method m = getPrivateMethod("formatDateTime", String.class);
        assertEquals("2024-06-15", m.invoke(null, "2024-06-15"));
    }

    @Test
    public void testFormatDateTime_WithT_TruncatesToHHmm() throws Exception {
        Method m = getPrivateMethod("formatDateTime", String.class);
        assertEquals("2024-06-15 09:00", m.invoke(null, "2024-06-15T09:00:00"));
    }

    @Test
    public void testFormatDateTime_WithT_NoSeconds() throws Exception {
        Method m = getPrivateMethod("formatDateTime", String.class);
        assertEquals("2024-06-15 09:00", m.invoke(null, "2024-06-15T09:00"));
    }

    // ── Helper: access private static methods ───────────────

    private Method getPrivateMethod(String name, Class<?>... paramTypes) throws Exception {
        Method m = CalendarMcpProductConfig.class.getDeclaredMethod(name, paramTypes);
        m.setAccessible(true);
        return m;
    }

    // ── Helper: build search response JSON ──────────────────

    /**
     * Builds a minimal search response with zero hits.
     */
    private String buildSearchResponseJson(int totalHits) {
        JsonObject hitsContainer = new JsonObject();
        hitsContainer.addProperty("total", totalHits);
        hitsContainer.add("hits", new JsonArray());

        JsonArray hitsContainers = new JsonArray();
        hitsContainers.add(hitsContainer);

        JsonObject value = new JsonObject();
        value.add("hitsContainers", hitsContainers);

        JsonArray values = new JsonArray();
        values.add(value);

        JsonObject root = new JsonObject();
        root.add("value", values);
        return root.toString();
    }

    /**
     * Builds a search response with a single calendar event hit.
     */
    private String buildSingleEventSearchResponse(
            String subject, String organizerName, String organizerEmail,
            String startDateTime, String endDateTime,
            String locationName, String webLink
    ) {
        JsonObject resource = buildEventResource(
                subject, organizerName, organizerEmail,
                startDateTime, endDateTime, locationName, webLink
        );

        JsonObject hit = new JsonObject();
        hit.add("resource", resource);

        JsonArray hits = new JsonArray();
        hits.add(hit);

        JsonObject hitsContainer = new JsonObject();
        hitsContainer.addProperty("total", 1);
        hitsContainer.add("hits", hits);

        JsonArray hitsContainers = new JsonArray();
        hitsContainers.add(hitsContainer);

        JsonObject value = new JsonObject();
        value.add("hitsContainers", hitsContainers);

        JsonArray values = new JsonArray();
        values.add(value);

        JsonObject root = new JsonObject();
        root.add("value", values);
        return root.toString();
    }

    /**
     * Builds a search response with a single event and a summary on the hit.
     */
    private String buildSingleEventWithSummarySearchResponse(
            String subject, String organizerName, String organizerEmail,
            String startDateTime, String endDateTime,
            String locationName, String webLink, String summary
    ) {
        JsonObject resource = buildEventResource(
                subject, organizerName, organizerEmail,
                startDateTime, endDateTime, locationName, webLink
        );

        JsonObject hit = new JsonObject();
        hit.add("resource", resource);
        hit.addProperty("summary", summary);

        JsonArray hits = new JsonArray();
        hits.add(hit);

        JsonObject hitsContainer = new JsonObject();
        hitsContainer.addProperty("total", 1);
        hitsContainer.add("hits", hits);

        JsonArray hitsContainers = new JsonArray();
        hitsContainers.add(hitsContainer);

        JsonObject value = new JsonObject();
        value.add("hitsContainers", hitsContainers);

        JsonArray values = new JsonArray();
        values.add(value);

        JsonObject root = new JsonObject();
        root.add("value", values);
        return root.toString();
    }

    /**
     * Builds a search response with two calendar event hits.
     */
    private String buildMultipleEventsSearchResponse() {
        JsonObject resource1 = buildEventResource(
                "Standup", "Alice", "alice@company.com",
                "2024-06-15T09:00:00", "2024-06-15T09:30:00",
                "Zoom", "https://outlook.com/event/1"
        );
        JsonObject resource2 = buildEventResource(
                "Retro", "Bob", "bob@company.com",
                "2024-06-15T15:00:00", "2024-06-15T16:00:00",
                "Teams", "https://outlook.com/event/2"
        );

        JsonObject hit1 = new JsonObject();
        hit1.add("resource", resource1);
        JsonObject hit2 = new JsonObject();
        hit2.add("resource", resource2);

        JsonArray hits = new JsonArray();
        hits.add(hit1);
        hits.add(hit2);

        JsonObject hitsContainer = new JsonObject();
        hitsContainer.addProperty("total", 2);
        hitsContainer.add("hits", hits);

        JsonArray hitsContainers = new JsonArray();
        hitsContainers.add(hitsContainer);

        JsonObject value = new JsonObject();
        value.add("hitsContainers", hitsContainers);

        JsonArray values = new JsonArray();
        values.add(value);

        JsonObject root = new JsonObject();
        root.add("value", values);
        return root.toString();
    }

    /**
     * Builds a single event resource JsonObject matching the Graph API structure.
     */
    private JsonObject buildEventResource(
            String subject, String organizerName, String organizerEmail,
            String startDateTime, String endDateTime,
            String locationName, String webLink
    ) {
        JsonObject resource = new JsonObject();
        resource.addProperty("subject", subject);
        resource.addProperty("webLink", webLink);

        // organizer: { emailAddress: { name, address } }
        JsonObject emailAddress = new JsonObject();
        emailAddress.addProperty("name", organizerName);
        emailAddress.addProperty("address", organizerEmail);
        JsonObject organizer = new JsonObject();
        organizer.add("emailAddress", emailAddress);
        resource.add("organizer", organizer);

        // start: { dateTime: "..." }
        JsonObject start = new JsonObject();
        start.addProperty("dateTime", startDateTime);
        resource.add("start", start);

        // end: { dateTime: "..." }
        JsonObject end = new JsonObject();
        end.addProperty("dateTime", endDateTime);
        resource.add("end", end);

        // location: { displayName: "..." }
        JsonObject location = new JsonObject();
        location.addProperty("displayName", locationName);
        resource.add("location", location);

        return resource;
    }
}
