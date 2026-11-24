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
import app.krista.sdk.msauth.extension.mcp.McpProductConfig;
import app.krista.sdk.msauth.extension.mcp.McpSearchResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Office 365 Calendar-specific implementation of {@link McpProductConfig}.
 *
 * <p>Uses Microsoft Graph {@code /search/query} API with KQL
 * (Keyword Query Language) to search for calendar events.
 * Also provides an intent classification prompt that can
 * detect {@code create_event} requests alongside search queries.</p>
 */
public class CalendarMcpProductConfig implements McpProductConfig {

    private static final Gson GSON = new Gson();
    private static final String SEARCH_URL = "https://graph.microsoft.com/v1.0/search/query";

    @Override
    public String productName() {
        return "Office 365 Calendar";
    }

    @Override
    public String queryLanguage() {
        return "KQL";
    }

    @Override
    public String queryLanguageFullName() {
        return "KQL (Keyword Query Language)";
    }

    @Override
    public String searchUrl() {
        return SEARCH_URL;
    }

    @Override
    public String searchHttpMethod() {
        return "POST";
    }

    @Override
    public List<String> standardFields() {
        return List.of("subject", "organizer", "start", "end", "location", "webLink");
    }

    @Override
    public List<ColumnDef> resultColumns() {
        // Return empty — columns are driven entirely by the runtime payload
        // in CalendarMcpSetupResource.convertToNlqResponse().
        return List.of();
    }

    @Override
    public List<String> nlExamples() {
        return List.of(
                "Fetch all events",
                "List my calendars",
                "Create a meeting tomorrow at 10am"
        );
    }

    @Override
    public List<String> kqlExamples() {
        return List.of(
                "subject:standup",
                "organizer:john@company.com",
                "location:\"Conference Room\""
        );
    }

    @Override
    public String buildSearchRequestBody(String query, int maxResults, boolean allFields) {
        JsonObject queryObj = new JsonObject();
        queryObj.addProperty("queryString", query);

        JsonArray entityTypes = new JsonArray();
        entityTypes.add("event");

        JsonObject request = new JsonObject();
        request.add("entityTypes", entityTypes);
        request.add("query", queryObj);
        request.addProperty("from", 0);
        request.addProperty("size", maxResults);

        JsonArray requests = new JsonArray();
        requests.add(request);

        JsonObject body = new JsonObject();
        body.add("requests", requests);

        return body.toString();
    }

    @Override
    public McpSearchResult parseSearchResponse(String json, String baseUrl) {
        List<Map<String, Object>> rows = new ArrayList<>();
        int total = 0;

        JsonObject root = GSON.fromJson(json, JsonObject.class);
        JsonArray values = root.has("value") ? root.getAsJsonArray("value") : new JsonArray();

        for (JsonElement valEl : values) {
            JsonObject val = valEl.getAsJsonObject();
            JsonArray hitsContainers = val.has("hitsContainers")
                    ? val.getAsJsonArray("hitsContainers") : new JsonArray();

            for (JsonElement hcEl : hitsContainers) {
                JsonObject hc = hcEl.getAsJsonObject();
                total += hc.has("total") ? hc.get("total").getAsInt() : 0;
                JsonArray hits = hc.has("hits") ? hc.getAsJsonArray("hits") : new JsonArray();

                for (JsonElement hitEl : hits) {
                    JsonObject hit = hitEl.getAsJsonObject();
                    JsonObject resource = hit.has("resource")
                            ? hit.getAsJsonObject("resource") : new JsonObject();

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("subject", getString(resource, "subject"));
                    row.put("organizer", extractOrganizer(resource));
                    row.put("start", formatDateTime(extractNestedDateTime(resource, "start")));
                    row.put("end", formatDateTime(extractNestedDateTime(resource, "end")));
                    row.put("location", extractLocation(resource));
                    row.put("url", getString(resource, "webLink"));
                    rows.add(row);
                }
            }
        }

        return new McpSearchResult("kql", total, rows.size(), rows);
    }

    @Override
    public List<FreeForm> parseStandardResponse(String json, String baseUrl) {
        List<FreeForm> rows = new ArrayList<>();
        JsonObject root = GSON.fromJson(json, JsonObject.class);
        JsonArray values = root.has("value") ? root.getAsJsonArray("value") : new JsonArray();

        for (JsonElement valEl : values) {
            JsonObject val = valEl.getAsJsonObject();
            JsonArray hitsContainers = val.has("hitsContainers")
                    ? val.getAsJsonArray("hitsContainers") : new JsonArray();

            for (JsonElement hcEl : hitsContainers) {
                JsonObject hc = hcEl.getAsJsonObject();
                JsonArray hits = hc.has("hits") ? hc.getAsJsonArray("hits") : new JsonArray();

                for (JsonElement hitEl : hits) {
                    JsonObject hit = hitEl.getAsJsonObject();
                    JsonObject resource = hit.has("resource")
                            ? hit.getAsJsonObject("resource") : new JsonObject();

                    FreeForm ff = new FreeForm();
                    ff.put("Subject", " Text ", getString(resource, "subject"));
                    ff.put("Organizer", " Text ", extractOrganizer(resource));
                    ff.put("Start", " Text ", formatDateTime(extractNestedDateTime(resource, "start")));
                    ff.put("End", " Text ", formatDateTime(extractNestedDateTime(resource, "end")));
                    ff.put("Location", " Text ", extractLocation(resource));
                    ff.put("Link", " Text ", getString(resource, "webLink"));
                    rows.add(ff);
                }
            }
        }
        return rows;
    }

    @Override
    public List<Map<String, String>> parseAllFieldsResponse(String json, String baseUrl) {
        List<Map<String, String>> rows = new ArrayList<>();
        JsonObject root = GSON.fromJson(json, JsonObject.class);
        JsonArray values = root.has("value") ? root.getAsJsonArray("value") : new JsonArray();

        for (JsonElement valEl : values) {
            JsonObject val = valEl.getAsJsonObject();
            JsonArray hitsContainers = val.has("hitsContainers")
                    ? val.getAsJsonArray("hitsContainers") : new JsonArray();

            for (JsonElement hcEl : hitsContainers) {
                JsonObject hc = hcEl.getAsJsonObject();
                JsonArray hits = hc.has("hits") ? hc.getAsJsonArray("hits") : new JsonArray();

                for (JsonElement hitEl : hits) {
                    JsonObject hit = hitEl.getAsJsonObject();
                    JsonObject resource = hit.has("resource")
                            ? hit.getAsJsonObject("resource") : new JsonObject();

                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("title", getString(resource, "subject"));
                    row.put("key", getString(resource, "subject"));
                    row.put("subject", getString(resource, "subject"));
                    row.put("organizer", extractOrganizer(resource));
                    row.put("start", formatDateTime(extractNestedDateTime(resource, "start")));
                    row.put("end", formatDateTime(extractNestedDateTime(resource, "end")));
                    row.put("location", extractLocation(resource));
                    row.put("url", getString(resource, "webLink"));

                    String summary = hit.has("summary") ? getString(hit, "summary") : "";
                    if (!summary.isEmpty()) {
                        row.put("summary", summary);
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    @Override
    public String queryGenerationPrompt(String question) {
        return "You are an Office 365 Calendar search expert. Convert the following natural language "
                + "question into a KQL (Keyword Query Language) query for Microsoft Graph Search API "
                + "targeting calendar events.\n\n"
                + "KQL syntax rules for calendar events:\n"
                + "- Use property:value for field-specific searches (no spaces around the colon)\n"
                + "- Common properties: subject, organizer, attendees, start, end, location, body\n"
                + "- Use quotes for multi-word values: subject:\"Team Standup\"\n"
                + "- Boolean operators: AND, OR, NOT (must be uppercase)\n"
                + "- Wildcards: * for prefix matching (e.g. project*)\n"
                + "- Date filters: start>=2024-01-01, end<=2024-06-30\n\n"
                + "Examples:\n"
                + "- \"Meetings with John\" → attendees:John\n"
                + "- \"Team standups this week\" → subject:standup\n"
                + "- \"Meetings in the London office\" → location:London\n"
                + "- \"Events organized by Jane\" → organizer:Jane\n"
                + "- \"Upcoming project reviews\" → subject:\"project review\"\n\n"
                + "Output a JSON object with exactly these fields:\n"
                + "  \"query\": the KQL query string\n"
                + "  \"specific\": true if the question asks about a specific attribute of a named "
                + "event (e.g. who organized a specific meeting, what is the location of an event), "
                + "false if it asks for a list of events\n"
                + "  \"field\": (only when specific=true) the field name being asked about "
                + "(e.g. \"organizer\", \"start\", \"location\", \"attendees\")\n\n"
                + "Output ONLY raw JSON. No markdown, no backticks, no explanation.\n\n"
                + "Question: " + question;
    }

    @Override
    public String answerExtractionPrompt(String question, String contentData) {
        return "Given the following Office 365 Calendar event data, answer the user's question concisely.\n\n"
                + "Question: " + question + "\n\n"
                + "Calendar data:\n" + contentData + "\n"
                + "Instructions:\n"
                + "- Answer directly and concisely\n"
                + "- If the data doesn't contain the answer, say so\n"
                + "- Do not invent or hallucinate data\n"
                + "- Reference specific event subjects, organizers, or dates when relevant";
    }

    /**
     * Builds a prompt that instructs the AI to classify the user's question
     * as either a search query or a create-event action, and to extract the
     * relevant fields for each intent.
     *
     * @param question the user's natural language question
     * @return the intent classification prompt
     */
    public String intentClassificationPrompt(String question) {
        return "You are an Office 365 Calendar assistant. Classify the user's intent as either "
                + "\"search\" or \"create_event\".\n\n"
                + "Intent rules:\n"
                + "- \"create_event\": The user wants to SCHEDULE, CREATE, or ADD a meeting/event.\n"
                + "  Extract: title, startDateTime (ISO format: yyyy-MM-ddTHH:mm:ss), "
                + "endDateTime (ISO format), attendees (comma-separated emails), "
                + "location (optional), body (optional description)\n"
                + "- \"search\": The user wants to FIND, SHOW, LIST, or GET existing events.\n\n"
                + "Output ONLY raw JSON (no markdown, no backticks, no explanation).\n\n"
                + "For create_event intent:\n"
                + "{\"intent\": \"create_event\", \"title\": \"Meeting Title\", "
                + "\"startDateTime\": \"2024-01-15T10:00:00\", "
                + "\"endDateTime\": \"2024-01-15T11:00:00\", "
                + "\"attendees\": \"user@example.com\", \"location\": \"\", \"body\": \"\"}\n\n"
                + "For search intent:\n"
                + "{\"intent\": \"search\"}\n\n"
                + "Rules:\n"
                + "- If no end time is specified, default to 1 hour after start.\n"
                + "- If no time is specified, use 09:00:00 for start and 10:00:00 for end.\n"
                + "- If no date is specified, use today's date.\n"
                + "- attendees and location default to empty string if not mentioned.\n"
                + "- The attendees field must contain valid email addresses.\n\n"
                + "Question: " + question;
    }

    /**
     * Builds a prompt that instructs the AI to determine the exact Microsoft
     * Graph API call needed to answer the user's question — without any
     * hardcoded intent classification.
     *
     * <p>The AI returns {@code {method, path, body}} which the caller executes
     * generically against the Graph API, so any valid Graph endpoint works
     * without code changes.</p>
     *
     * @param question the user's natural language question
     * @return the Graph API planning prompt
     */
    public String graphApiPlanningPrompt(String question) {
        String eventSelect = "$select=id,subject,organizer,start,end,location,bodyPreview,"
                + "attendees,webLink,importance,isAllDay,isCancelled,isOnlineMeeting,"
                + "onlineMeetingUrl,showAs,categories";
        return "You are a Microsoft Graph API expert for Office 365 Calendar.\n"
                + "Given the user's question, determine the exact Microsoft Graph API call needed.\n\n"
                + "Output ONLY a raw JSON object (no markdown, no backticks, no explanation):\n"
                + "  \"method\": \"GET\", \"POST\", \"PATCH\", or \"DELETE\"\n"
                + "  \"path\":   relative path starting with /me/ "
                + "(e.g. \"/me/events\", \"/me/calendars\")\n"
                + "  \"body\":   full JSON string for POST requests, or null for GET/DELETE\n\n"
                + "Common Graph API patterns for Calendar:\n"
                + "- List all events:          GET /me/events?$top=10"
                + "&$orderby=start/dateTime+asc&" + eventSelect + "\n"
                + "- Upcoming events (view):   GET /me/calendarView"
                + "?startDateTime={now}&endDateTime={now+7days}&$top=20&" + eventSelect + "\n"
                + "- Events in date range:     GET /me/events"
                + "?$filter=start/dateTime ge '{date}T00:00:00' and end/dateTime le '{date}T23:59:59'"
                + "&$top=20&" + eventSelect + "\n"
                + "- Get event by ID:          GET /me/events/{eventId}?" + eventSelect + "\n"
                + "- Search events (KQL):      POST /search/query\n"
                + "    body: {\"requests\":[{\"entityTypes\":[\"event\"],"
                + "\"query\":{\"queryString\":\"KQL\"},\"from\":0,\"size\":10}]}\n"
                + "- List calendars:           GET /me/calendars?$top=20\n"
                + "- Create calendar event:    POST /me/events\n"
                + "    body: {\"subject\":\"TITLE\","
                + "\"start\":{\"dateTime\":\"2024-01-15T10:00:00\",\"timeZone\":\"UTC\"},"
                + "\"end\":{\"dateTime\":\"2024-01-15T11:00:00\",\"timeZone\":\"UTC\"},"
                + "\"location\":{\"displayName\":\"LOCATION\"},"
                + "\"body\":{\"contentType\":\"Text\",\"content\":\"DESCRIPTION\"},"
                + "\"attendees\":[{\"emailAddress\":{\"address\":\"EMAIL\"},\"type\":\"required\"}]}\n"
                + "- Cancel/delete event:      DELETE /me/events/{eventId}\n"
                + "    body: null\n"
                + "- Today's events:           GET /me/events"
                + "?$filter=start/dateTime ge '{today}T00:00:00' and start/dateTime le '{today}T23:59:59'"
                + "&$top=20&$orderby=start/dateTime+asc&" + eventSelect + "\n"
                + "- Events with attendee:     GET /me/events"
                + "?$filter=attendees/any(a:a/emailAddress/address eq 'EMAIL')"
                + "&$top=10&" + eventSelect + "\n"
                + "- Update event subject:     PATCH /me/events/{eventId}\n"
                + "    body: {\"subject\":\"NEW TITLE\"}\n"
                + "- Update event time:        PATCH /me/events/{eventId}\n"
                + "    body: {\"start\":{\"dateTime\":\"2024-01-15T10:00:00\",\"timeZone\":\"UTC\"},"
                + "\"end\":{\"dateTime\":\"2024-01-15T11:00:00\",\"timeZone\":\"UTC\"}}\n"
                + "- Update event location:    PATCH /me/events/{eventId}\n"
                + "    body: {\"location\":{\"displayName\":\"NEW LOCATION\"}}\n"
                + "- Update event description: PATCH /me/events/{eventId}\n"
                + "    body: {\"body\":{\"contentType\":\"Text\",\"content\":\"NEW DESCRIPTION\"}}\n\n"
                + "IMPORTANT: Use {eventId} as a placeholder in the path for delete/update operations "
                + "— the system will automatically resolve it by searching for the event subject "
                + "mentioned in the question.\n\n"
                + "Question: " + question;
    }

    // ── Private helpers ──────────────────────────────────

    private static String getString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return "";
        return obj.get(key).getAsString();
    }

    /**
     * Extracts a dateTime value from a nested object like {@code start.dateTime}.
     * Graph calendar events use: {@code start: { dateTime, timeZone }}.
     */
    private static String extractNestedDateTime(JsonObject resource, String outerKey) {
        if (!resource.has(outerKey) || resource.get(outerKey).isJsonNull()) return "";
        JsonElement el = resource.get(outerKey);
        if (el.isJsonObject()) {
            return getString(el.getAsJsonObject(), "dateTime");
        }
        return el.getAsString();
    }

    /**
     * Extracts the organizer display string from the {@code organizer.emailAddress} structure.
     * Graph calendar events use: {@code organizer: { emailAddress: { name, address } }}.
     */
    private static String extractOrganizer(JsonObject resource) {
        if (!resource.has("organizer") || resource.get("organizer").isJsonNull()) return "";
        JsonObject organizer = resource.getAsJsonObject("organizer");
        if (!organizer.has("emailAddress") || organizer.get("emailAddress").isJsonNull()) return "";
        JsonObject emailAddress = organizer.getAsJsonObject("emailAddress");
        String name = getString(emailAddress, "name");
        String address = getString(emailAddress, "address");
        if (!name.isEmpty() && !address.isEmpty()) {
            return name + " <" + address + ">";
        }
        return !name.isEmpty() ? name : address;
    }

    private static String extractLocation(JsonObject resource) {
        if (!resource.has("location") || resource.get("location").isJsonNull()) return "";
        JsonElement el = resource.get("location");
        if (el.isJsonObject()) {
            return getString(el.getAsJsonObject(), "displayName");
        }
        return el.getAsString();
    }

    private static String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) return "";
        // Truncate seconds/sub-seconds for display: 2024-01-15T10:00:00 → 2024-01-15 10:00
        int tIdx = isoDateTime.indexOf('T');
        if (tIdx < 0) return isoDateTime;
        String date = isoDateTime.substring(0, tIdx);
        String time = isoDateTime.substring(tIdx + 1);
        int colonTwo = time.indexOf(':', time.indexOf(':') + 1);
        if (colonTwo > 0) {
            time = time.substring(0, colonTwo);
        }
        return date + " " + time;
    }
}
