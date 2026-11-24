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
import app.krista.extension.executor.RemediationAction;
import app.krista.extension.executor.RemediationActions;
import app.krista.extension.executor.impl.InformAPersonAction;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.model.base.FreeForm;
import app.krista.sdk.msauth.extension.mcp.AbstractMcpQueryService;
import app.krista.sdk.msauth.extension.mcp.AnthropicApiClient;
import app.krista.sdk.msauth.extension.mcp.GeminiApiClient;
import app.krista.sdk.msauth.extension.mcp.McpProductConfig;
import app.krista.sdk.msauth.extension.mcp.McpSearchContext;
import app.krista.sdk.msauth.extension.mcp.McpSessionKeys;
import app.krista.sdk.msauth.extension.mcp.OpenAiApiClient;
import app.krista.sdk.msauth.token.ITokenStore;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Office 365 Calendar-specific MCP query service.
 *
 * <p>Overrides {@link #query(String)} to ask the AI to plan the exact
 * Microsoft Graph API call ({@code method}, {@code path}, {@code body})
 * and executes it generically — no hardcoded intents.</p>
 *
 * <p>Credentials are reloaded from the store on every call so that
 * changes in the Authentication tab take effect without a restart.</p>
 */
@Service
public class CalendarMcpQueryService extends AbstractMcpQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarMcpQueryService.class);
    private static final Gson GSON = new Gson();

    private final ITokenStore tokenStore;
    private final Invoker invoker;
    private final Office365CalendarAttributeStore attributeStore;
    private final RefreshTokenStore refreshTokenStore;

    @Inject
    public CalendarMcpQueryService(RefreshTokenStore refreshTokenStore,
                                   Invoker invoker,
                                   Office365CalendarAttributeStore attributeStore) {
        super(adaptTokenStore(refreshTokenStore),
              invoker,
              buildAuthBridge(attributeStore.load(invoker.getInvokerId()), refreshTokenStore));
        this.tokenStore = adaptTokenStore(refreshTokenStore);
        this.invoker = invoker;
        this.attributeStore = attributeStore;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Override
    protected McpProductConfig getProductConfig() {
        return new CalendarMcpProductConfig();
    }

    @Override
    protected String getJaxrsId() {
        return "calendar";
    }

    /**
     * Reloads credentials from the attribute store on every call so that
     * changes in the Authentication tab take effect without a restart.
     */
    private CalendarMcpAuthBridge currentAuthBridge() {
        Office365CalendarAttributes attrs = attributeStore.load(invoker.getInvokerId());
        return new CalendarMcpAuthBridge(attrs, refreshTokenStore);
    }

    /** Returns current attributes, reloaded on every call. */
    private Office365CalendarAttributes currentAttributes() {
        return attributeStore.load(invoker.getInvokerId());
    }

    /**
     * Single entry point for both catalog requests and the NQL tab.
     *
     * <p>Asks the AI to plan the exact Microsoft Graph API call ({@code method},
     * {@code path}, {@code body}) needed to answer the user's question, then
     * executes it generically via {@link #executeGraphCall}. No hardcoded
     * intents — any valid Graph endpoint works without code changes.</p>
     *
     * <p>Falls back to the parent KQL search pipeline if the AI returns an
     * unparseable response.</p>
     */
    @Override
    public ExtensionResponse query(String question) {
        long startTime = System.currentTimeMillis();
        String id = invoker.getInvokerId();
        LOG.info("[MCP-CAL] query START — invokerId: {}, question: '{}'", id, question);

        try {
            // Resolve AI provider + API key
            String provider = tokenStore.get(McpSessionKeys.aiProvider(id));
            String aiKey = blank(provider) ? null : tokenStore.get(McpSessionKeys.aiKey(id, provider));

            if (blank(aiKey)) {
                aiKey = tokenStore.get(McpSessionKeys.claudeApiKey(id));
                if (!blank(aiKey)) {
                    provider = "anthropic";
                }
            }

            if (blank(aiKey)) {
                LOG.warn("[MCP-CAL] query — No AI key configured");
                return failure("AI API key is not configured. "
                        + "Please set it in the AI Assistant tab \u2192 AI Settings.");
            }

            // Reload auth on every call so credential changes take effect immediately
            CalendarMcpAuthBridge authBridge = currentAuthBridge();
            if (!authBridge.isAuthenticated()) {
                return failure("Not connected. Configure credentials in the Authentication tab.");
            }

            // Ask AI to plan the Graph API call
            CalendarMcpProductConfig config = (CalendarMcpProductConfig) getProductConfig();
            String planPrompt = config.graphApiPlanningPrompt(question);
            LOG.info("[MCP-CAL] query — planning Graph API call via AI...");
            String raw = callAi(aiKey, provider, planPrompt);
            LOG.info("[MCP-CAL] query — AI plan: '{}'", raw);

            if (raw == null || raw.isBlank()) {
                LOG.warn("[MCP-CAL] query — AI returned blank response");
                return failure("Could not determine Graph API call. Please try rephrasing your question.");
            }

            if (raw.contains("{")) {
                raw = raw.substring(raw.indexOf('{'), raw.lastIndexOf('}') + 1);
            }

            JsonObject plan = GSON.fromJson(raw, JsonObject.class);
            String method = getJsonString(plan, "method");
            String path   = getJsonString(plan, "path");
            String body   = plan != null && plan.has("body") && !plan.get("body").isJsonNull()
                    ? (plan.get("body").isJsonPrimitive()
                            ? plan.get("body").getAsString()
                            : plan.get("body").toString())
                    : null;

            if (blank(method) || blank(path)) {
                LOG.warn("[MCP-CAL] query — AI returned invalid plan: {}", raw);
                return failure("Could not parse a valid Graph API call from the AI response. Please rephrase your question.");
            }

            LOG.info("[MCP-CAL] query — executing {} {}", method, path);
            McpSearchContext ctx = authBridge.buildSearchContext(config);
            String authHeader = ctx.authHeader();
            String resolvedPathStr = resolvedPath(path);

            // Resolve {eventId} placeholder — AI may generate /me/events/{eventId} for delete/update/patch
            if (resolvedPathStr.contains("{eventId}")) {
                String pathWithId = resolveEventId(resolvedPathStr, question, authHeader);
                if (pathWithId == null) {
                    return failure("Could not find the event mentioned in your question. "
                            + "Please check the event subject and try again.");
                }
                resolvedPathStr = pathWithId;
            }

            String fullUrl = "https://graph.microsoft.com/v1.0" + resolvedPathStr;
            ExtensionResponse result = executeGraphCall(method, fullUrl, body, authHeader);
            // Attach the AI-generated Graph plan string so the UI can display it
            if (result.getResult() == ExtensionResponse.Result.SUCCESS
                    && result.getResponseValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rv = (Map<String, Object>) result.getResponseValue();
                rv.put("generatedPlan", method + " " + path);
            }
            LOG.info("[MCP-CAL] query END — elapsed: {}ms", System.currentTimeMillis() - startTime);
            return result;

        } catch (Exception e) {
            LOG.error("[MCP-CAL] query FAILED — elapsed: {}ms",
                    System.currentTimeMillis() - startTime, e);
            return failure("Query failed: " + e.getMessage());
        }
    }

    /**
     * Replaces {@code /me/} with {@code /users/{email}/} when private auth is
     * configured, so the same AI-generated paths work for both auth modes.
     */
    private String resolvedPath(String path) {
        Office365CalendarAttributes attrs = currentAttributes();
        if (attrs != null && Constants.PRIVATE.equals(attrs.getAuthType())) {
            String email = attrs.getEmail();
            return path.replaceFirst("^/me(/|$)", "/users/" + email + "$1");
        }
        return path;
    }

    /**
     * Executes any Microsoft Graph API call and converts the response into
     * a list of FreeForm rows for the NQL tab.
     *
     * <p>Handles GET, POST, and DELETE. HTTP 202/204 (no body) is treated
     * as a successful action. HTTP 200/201 responses are parsed by
     * {@link #parseGenericResponse}.</p>
     */
    private ExtensionResponse executeGraphCall(String method, String fullUrl,
                                               String body, String authHeader) {
        Client client = ClientBuilder.newClient();
        try {
            javax.ws.rs.core.Response httpResp;
            if ("POST".equalsIgnoreCase(method)) {
                httpResp = client.target(fullUrl)
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeader)
                        .post(Entity.entity(body != null ? body : "{}",
                                MediaType.APPLICATION_JSON));
            } else if ("PATCH".equalsIgnoreCase(method)) {
                httpResp = client.target(fullUrl)
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeader)
                        .method("PATCH", Entity.entity(body != null ? body : "{}",
                                MediaType.APPLICATION_JSON));
            } else if ("DELETE".equalsIgnoreCase(method)) {
                httpResp = client.target(fullUrl)
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeader)
                        .delete();
            } else {
                httpResp = client.target(fullUrl)
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeader)
                        .get();
            }

            int status = httpResp.getStatus();
            LOG.info("[MCP-CAL] executeGraphCall — {} {} → HTTP {}", method, fullUrl, status);

            if (status == 202 || status == 204) {
                FreeForm ff = new FreeForm();
                ff.put("Answer", " Text ", "Action completed successfully.");
                Map<String, Object> actionMap = new HashMap<>();
                actionMap.put("Response", List.of(ff));
                return success(actionMap);
            }

            if (status == 200 || status == 201) {
                String responseBody = httpResp.readEntity(String.class);

                // PATCH 200 → friendly "Event updated successfully" message instead of raw metadata
                if ("PATCH".equalsIgnoreCase(method)) {
                    String updatedSubject = "";
                    try {
                        JsonObject eventObj = GSON.fromJson(responseBody, JsonObject.class);
                        updatedSubject = getStr(eventObj, "subject");
                    } catch (Exception ignored) {}
                    FreeForm ff = new FreeForm();
                    ff.put("Result", " Text ", "Event updated successfully."
                            + (updatedSubject.isEmpty() ? "" : " (" + updatedSubject + ")"));
                    Map<String, Object> patchMap = new HashMap<>();
                    patchMap.put("Response", List.of(ff));
                    return success(patchMap);
                }

                List<FreeForm> rows = parseGenericResponse(responseBody);
                LOG.info("[MCP-CAL] executeGraphCall — parsed {} row(s)", rows.size());
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("Response", rows);
                return success(responseMap);
            }

            String errorBody = httpResp.readEntity(String.class);
            LOG.error("[MCP-CAL] executeGraphCall FAILED — HTTP {}: {}", status, errorBody);
            return failure("Graph API call failed (HTTP " + status + "): " + errorBody);

        } catch (Exception e) {
            LOG.error("[MCP-CAL] executeGraphCall FAILED — {}", e.getMessage(), e);
            return failure("Graph API call failed: " + e.getMessage());
        } finally {
            client.close();
        }
    }

    /**
     * Converts any Graph API JSON response into FreeForm rows.
     *
     * <p>Handles two shapes:
     * <ul>
     *   <li>List response  — flat {@code value[]} array (events, calendars…)</li>
     *   <li>Single object  — no {@code value} key (e.g. GET /me/events/{id})</li>
     * </ul>
     */
    private static List<FreeForm> parseGenericResponse(String json) {
        List<FreeForm> rows = new ArrayList<>();
        JsonObject root = GSON.fromJson(json, JsonObject.class);

        if (!root.has("value") || !root.get("value").isJsonArray()) {
            rows.add(graphObjectToFreeForm(root));
            return rows;
        }

        JsonArray value = root.getAsJsonArray("value");
        if (value.size() == 0) return rows;

        for (JsonElement el : value) {
            if (el.isJsonObject()) {
                rows.add(graphObjectToFreeForm(el.getAsJsonObject()));
            }
        }
        return rows;
    }

    /**
     * Converts a single Graph calendar event/calendar JSON object into a FreeForm row
     * by iterating its fields and mapping known field names to display-friendly keys.
     */
    private static FreeForm graphObjectToFreeForm(JsonObject obj) {
        FreeForm ff = new FreeForm();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            if (entry.getKey().startsWith("@")) continue; // skip @odata.* metadata fields
            String graphKey = entry.getKey();
            String strVal = toDisplayString(graphKey, entry.getValue(), obj);
            if (strVal != null && !strVal.isEmpty()) {
                ff.put(toFreeFormKey(graphKey), " Text ", strVal);
            }
        }
        return ff;
    }

    /**
     * Converts a Graph JSON field value to a displayable string.
     * Handles primitives, nested objects (organizer, location, start/end, body),
     * and arrays (attendees, categories); skips other complex types.
     */
    private static String toDisplayString(String key, JsonElement val, JsonObject parent) {
        if (val == null || val.isJsonNull()) return null;
        if (val.isJsonPrimitive()) {
            return val.getAsString();
        }
        if (val.isJsonObject()) {
            if ("organizer".equals(key)) return extractOrganizer(parent);
            if ("location".equals(key))  return getStr(val.getAsJsonObject(), "displayName");
            if ("start".equals(key) || "end".equals(key)) return formatDateTime(val.getAsJsonObject());
            if ("body".equals(key))      return getStr(val.getAsJsonObject(), "content");
            if ("responseStatus".equals(key)) return getStr(val.getAsJsonObject(), "response");
            if ("onlineMeeting".equals(key))  return getStr(val.getAsJsonObject(), "joinUrl");
        }
        if (val.isJsonArray()) {
            if ("attendees".equals(key))  return extractAttendees(val.getAsJsonArray());
            if ("categories".equals(key)) return extractStringArray(val.getAsJsonArray());
        }
        return null; // skip other complex types
    }

    /**
     * Maps a Graph API field name to a FreeForm PascalCase key.
     * Unknown fields pass through unchanged and appear as extra dynamic columns.
     */
    private static String toFreeFormKey(String graphKey) {
        switch (graphKey) {
            // ── event fields ──────────────────────────────────
            case "id":               return "EventId";
            case "subject":          return "Subject";
            case "organizer":        return "Organizer";
            case "start":            return "Start";
            case "end":              return "End";
            case "location":         return "Location";
            case "body":             return "Body";
            case "bodyPreview":      return "Preview";
            case "attendees":        return "Attendees";
            case "webLink":          return "Link";
            case "importance":       return "Importance";
            case "isAllDay":         return "IsAllDay";
            case "isCancelled":      return "IsCancelled";
            case "isOnlineMeeting":  return "IsOnlineMeeting";
            case "onlineMeetingUrl": return "OnlineMeetingUrl";
            case "onlineMeeting":    return "OnlineMeetingJoinUrl";
            case "showAs":           return "ShowAs";
            case "categories":       return "Categories";
            case "responseStatus":   return "ResponseStatus";
            // ── calendar list fields ──────────────────────────
            case "name":             return "CalendarName";
            case "displayName":      return "CalendarName";
            case "canEdit":          return "CanEdit";
            case "canShare":         return "CanShare";
            default:                 return graphKey; // pass-through for unknown fields
        }
    }

    // ── Field extraction helpers ─────────────────────────────────────────

    private static String extractOrganizer(JsonObject resource) {
        if (!resource.has("organizer") || resource.get("organizer").isJsonNull()) return "";
        JsonObject organizer = resource.getAsJsonObject("organizer");
        if (!organizer.has("emailAddress") || organizer.get("emailAddress").isJsonNull()) return "";
        JsonObject emailAddress = organizer.getAsJsonObject("emailAddress");
        String name = getStr(emailAddress, "name");
        String address = getStr(emailAddress, "address");
        if (!name.isEmpty() && !address.isEmpty()) return name + " <" + address + ">";
        return !name.isEmpty() ? name : address;
    }

    /** Converts a Graph attendees array to a comma-separated "Name <address>" string. */
    private static String extractAttendees(JsonArray arr) {
        if (arr == null || arr.size() == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject attendee = el.getAsJsonObject();
            if (!attendee.has("emailAddress") || attendee.get("emailAddress").isJsonNull()) continue;
            JsonObject emailAddress = attendee.getAsJsonObject("emailAddress");
            String name = getStr(emailAddress, "name");
            String address = getStr(emailAddress, "address");
            String entry = (!name.isEmpty() && !address.isEmpty()) ? name + " <" + address + ">"
                    : !name.isEmpty() ? name : address;
            if (!entry.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(entry);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /** Converts a JSON array of primitive strings to a comma-separated string. */
    private static String extractStringArray(JsonArray arr) {
        if (arr == null || arr.size() == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (JsonElement el : arr) {
            if (el.isJsonPrimitive()) {
                String s = el.getAsString();
                if (!s.isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(s);
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Formats a Graph start/end object {@code {dateTime, timeZone}} to
     * a human-readable string: {@code "2024-06-15 10:00"}.
     */
    private static String formatDateTime(JsonObject obj) {
        String dateTime = getStr(obj, "dateTime");
        if (dateTime.isEmpty()) return "";
        int tIdx = dateTime.indexOf('T');
        if (tIdx < 0) return dateTime;
        String date = dateTime.substring(0, tIdx);
        String time = dateTime.substring(tIdx + 1);
        int colonTwo = time.indexOf(':', time.indexOf(':') + 1);
        if (colonTwo > 0) time = time.substring(0, colonTwo);
        return date + " " + time;
    }

    private static String getStr(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return "";
        return obj.get(key).getAsString();
    }

    // ── Event ID resolution ───────────────────────────────────────────────

    /**
     * Resolves the {@code {eventId}} placeholder in a path by searching
     * for the event by subject keyword extracted from the user's question.
     * Returns the path with the real event ID substituted, or {@code null} if not found.
     */
    private String resolveEventId(String path, String question, String authHeader) {
        String subject = extractEventSubject(question);
        if (subject == null || subject.isEmpty()) {
            LOG.warn("[MCP-CAL] resolveEventId — could not extract event subject from: '{}'", question);
            return null;
        }
        LOG.info("[MCP-CAL] resolveEventId — searching for event with subject keyword: '{}'", subject);
        Client client = ClientBuilder.newClient();
        try {
            String filter = "contains(subject,'" + subject.replace("'", "''") + "')";
            javax.ws.rs.core.Response resp = client
                    .target("https://graph.microsoft.com/v1.0/me/events")
                    .queryParam("$filter", filter)
                    .queryParam("$top", "5")
                    .queryParam("$select", "id,subject")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", authHeader)
                    .get();
            if (resp.getStatus() == 200) {
                String json = resp.readEntity(String.class);
                JsonObject root = GSON.fromJson(json, JsonObject.class);
                if (root.has("value") && root.get("value").isJsonArray()) {
                    JsonArray events = root.getAsJsonArray("value");
                    for (JsonElement el : events) {
                        if (el.isJsonObject()) {
                            String eventId = getStr(el.getAsJsonObject(), "id");
                            if (!eventId.isEmpty()) {
                                LOG.info("[MCP-CAL] resolveEventId — found eventId: {}", eventId);
                                return path.replace("{eventId}", eventId);
                            }
                        }
                    }
                }
            } else {
                String err = resp.readEntity(String.class);
                LOG.warn("[MCP-CAL] resolveEventId — search failed HTTP {}: {}", resp.getStatus(), err);
            }
        } catch (Exception e) {
            LOG.error("[MCP-CAL] resolveEventId FAILED — {}", e.getMessage(), e);
        } finally {
            client.close();
        }
        return null;
    }

    /**
     * Extracts a likely event subject keyword from the user's question for use
     * in resolving {@code {eventId}} placeholders.
     *
     * <p>Priority order:
     * <ol>
     *   <li>Quoted strings: {@code "standup"} or {@code 'team review'}</li>
     *   <li>Patterns like {@code called X}, {@code named X}, {@code titled X}</li>
     *   <li>Action-verb + optional "the" + subject keyword (delete/cancel/update…)</li>
     *   <li>Bare noun before a meeting-type word (e.g. "standup meeting")</li>
     * </ol>
     */
    private static String extractEventSubject(String question) {
        if (question == null) return null;
        // 1. Quoted string: "standup" or 'team review'
        Matcher quoted = Pattern.compile("[\"']([^\"']{2,})[\"']").matcher(question);
        if (quoted.find()) return quoted.group(1).trim();
        // 2. "called/named/titled X" pattern
        Matcher namedAs = Pattern.compile(
                "(?:called|named|titled)\\s+([\\w][\\w\\s]{1,40}?)(?:\\s+(?:meeting|event|appointment))?\\s*$",
                Pattern.CASE_INSENSITIVE).matcher(question);
        if (namedAs.find()) return namedAs.group(1).trim();
        // 3. Action verb + optional "the" + subject keyword
        Matcher action = Pattern.compile(
                "(?:delete|cancel|update|reschedule|edit|change|remove)\\s+(?:the\\s+)?([\\w][\\w\\s]{1,40}?)(?:\\s+(?:meeting|event|appointment|call|sync|review|standup))?\\s*$",
                Pattern.CASE_INSENSITIVE).matcher(question);
        if (action.find()) return action.group(1).trim();
        // 4. Bare noun before a meeting-type word e.g. "standup meeting"
        Matcher bare = Pattern.compile(
                "([\\w][\\w\\s]{1,30}?)\\s+(?:meeting|event|appointment|call|sync|standup)",
                Pattern.CASE_INSENSITIVE).matcher(question);
        if (bare.find()) return bare.group(1).trim();
        return null;
    }

    // ── AI dispatch ──────────────────────────────────────────────────────

    private static String callAi(String apiKey, String provider, String prompt) throws Exception {
        if ("openai".equals(provider)) {
            return OpenAiApiClient.call(apiKey, prompt, "gpt-4o-mini", 0);
        } else if ("gemini".equals(provider)) {
            return GeminiApiClient.call(apiKey, prompt, "gemini-2.5-flash", 0);
        } else {
            return AnthropicApiClient.call(apiKey, prompt, "claude-haiku-3-5", 0);
        }
    }

    // ── Utility ──────────────────────────────────────────────────────────

    private static ExtensionResponse success(Map<String, Object> values) {
        return new ExtensionResponse(ExtensionResponse.Result.SUCCESS, values, null, null, null);
    }

    private static ExtensionResponse failure(String message) {
        ExtensionResponse.Error error = new ExtensionResponse.Error(
                message, System.currentTimeMillis(),
                ExtensionResponse.Error.ExceptionType.LOGIC_ERROR, "");
        RemediationActions remediationActions = new RemediationActions(
                List.of(InformAPersonAction.create(message,
                        RemediationAction.RecipientType.ACTIVE_USER, List.of())), null);
        return new ExtensionResponse(ExtensionResponse.Result.FAILURE, null, error,
                remediationActions, Map.of());
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String getJsonString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return "";
        return obj.get(key).getAsString();
    }

    private static CalendarMcpAuthBridge buildAuthBridge(Office365CalendarAttributes attrs,
                                                          RefreshTokenStore store) {
        return new CalendarMcpAuthBridge(attrs, store);
    }

    /**
     * Adapts the Calendar {@link RefreshTokenStore} (put/get/remove) to
     * the SDK's {@link ITokenStore} interface (save/get/delete).
     */
    static ITokenStore adaptTokenStore(RefreshTokenStore store) {
        return new ITokenStore() {
            @Override
            public void save(String key, String value) {
                store.put(key, value);
            }

            @Override
            public String get(String key) {
                return store.get(key);
            }

            @Override
            public void delete(String key) {
                store.remove(key);
            }
        };
    }
}
