# AI Assistant (MCP)

## Overview

The AI Assistant tab enables natural language interaction with Office 365 Calendar. Instead of configuring catalog requests manually, you can ask questions in plain English — the extension translates them into Microsoft Graph API calls automatically.

Both the **NQL tab** in Krista Studio and **Krista Agent / MCP client** tool calls use the same pipeline: the AI determines the exact Graph API call needed (`method`, `path`, `body`), and the extension executes it generically. No hardcoded intents — any valid Graph calendar endpoint works without code changes.

---

## Setup

### Step 1 — Configure Calendar Authentication

The AI Assistant uses the same Calendar connection as all other catalog requests. Complete the **Authentication** tab first (see [Authentication](pages/Authentication.md)).

### Step 2 — Configure an AI API Key

1. Open the extension's **AI Assistant** tab in Krista Studio.
2. Go to **AI Settings**.
3. Select a provider and paste in the API key:

| Provider | Model Used | Where to Get the Key |
|---|---|---|
| **Anthropic (Claude)** | claude-haiku-3-5 | [console.anthropic.com](https://console.anthropic.com) → API keys |
| **OpenAI (GPT)** | gpt-4o-mini | [platform.openai.com](https://platform.openai.com) → API keys |
| **Google (Gemini)** | gemini-2.5-flash | [aistudio.google.com](https://aistudio.google.com) → Get API key |

4. Click **Save**.

If no AI key is configured, queries return:
> _AI API key is not configured. Please set it in the AI Assistant tab → AI Settings._

---

## How It Works

Every natural language question goes through the same pipeline:

```
User question
  → AI plans the Graph API call  { method, path, body }
  → {eventId} placeholder resolved automatically (for delete/update)
  → Extension executes the call against Microsoft Graph
  → Response converted to table rows
  → Columns derived dynamically from response fields
  → GENERATED KQL field shows the executed method + path
```

The AI returns one of four HTTP methods:

| Method | When Used |
|---|---|
| `GET` | Read events, list calendars, get a specific event |
| `POST` | Create events, KQL search via Graph Search API |
| `PATCH` | Update an existing event (subject, time, location, description) |
| `DELETE` | Delete/cancel an event |

---

## Supported Operations

### Listing and Reading Events

| Operation | Example Question | Graph Call |
|---|---|---|
| List all events | "Show my upcoming events" | `GET /me/events?$top=10&$orderby=start/dateTime+asc` |
| Next 7 days (calendar view) | "What meetings do I have this week?" | `GET /me/calendarView?startDateTime=...&endDateTime=...` |
| Today's events | "What is on my calendar today?" | `GET /me/events?$filter=start/dateTime ge '{today}T00:00:00'...` |
| Events in date range | "Show meetings between Jan 1 and Jan 31" | `GET /me/events?$filter=start/dateTime ge '...' and end/dateTime le '...'` |
| Events with a specific attendee | "Events with john@example.com" | `GET /me/events?$filter=attendees/any(a:a/emailAddress/address eq 'EMAIL')` |
| Get a specific event by ID | "Get event with ID abc123" | `GET /me/events/{eventId}` |
| Search events by keyword | "Find standup meetings" | `POST /search/query` with KQL `subject:standup` |
| List all calendars | "Show my calendars" | `GET /me/calendars?$top=20` |

### Creating Events

| Example Question | Graph Call | Body Fields |
|---|---|---|
| "Create a meeting tomorrow at 10am with jane@example.com" | `POST /me/events` | `subject`, `start`, `end`, `attendees`, `location`, `body` |

The body the AI generates for event creation:

```json
{
  "subject": "Meeting Title",
  "start": { "dateTime": "2024-01-15T10:00:00", "timeZone": "UTC" },
  "end":   { "dateTime": "2024-01-15T11:00:00", "timeZone": "UTC" },
  "location": { "displayName": "Conference Room" },
  "body": { "contentType": "Text", "content": "Description" },
  "attendees": [
    { "emailAddress": { "address": "jane@example.com" }, "type": "required" }
  ]
}
```

A successful event creation returns HTTP 201 and displays the created event's details.

### Updating Events (PATCH)

The extension supports updating any field on an existing event. The AI uses `{eventId}` as a placeholder — the system automatically resolves it by searching for the event whose subject matches the keyword in your question.

| Update Type | Example Question | Body Sent |
|---|---|---|
| Update subject | "Rename the standup meeting to Daily Sync" | `{"subject": "Daily Sync"}` |
| Update time | "Move the standup to 11am" | `{"start": {...}, "end": {...}}` |
| Update location | "Change the meeting room for the standup" | `{"location": {"displayName": "Room B"}}` |
| Update description | "Add an agenda to the standup meeting" | `{"body": {"contentType": "Text", "content": "..."}}` |

A successful update returns:
> _Event updated successfully. (Daily Sync)_

### Deleting Events

| Example Question | Graph Call |
|---|---|
| "Delete the standup meeting" | `DELETE /me/events/{eventId}` |
| "Cancel the meeting called 'Project Review'" | `DELETE /me/events/{eventId}` |

A successful deletion returns HTTP 204 and displays:
> _Action completed successfully._

---

## Automatic Event ID Resolution

For `PATCH` and `DELETE` operations, the AI generates a path like `/me/events/{eventId}`. The extension automatically resolves `{eventId}` before executing — no manual ID lookup needed.

**Resolution process:**

1. The extension extracts the event subject keyword from your question.
2. It calls `GET /me/events?$filter=contains(subject,'keyword')&$top=5`.
3. The first matching event's ID is substituted into the path.
4. The call is executed with the real ID.

**Keyword extraction priority:**
1. Quoted strings in the question: `"standup"` or `'team review'`
2. `called/named/titled X` patterns
3. Action-verb patterns: `delete/cancel/update/reschedule the standup`
4. Bare noun before a meeting-type word: `standup meeting`

**If no event is found**, the query returns:
> _Could not find the event mentioned in your question. Please check the event subject and try again._

---

## Result Columns

Columns in the NQL tab are **fully dynamic** — derived at runtime from whatever fields the Graph API returns. There is no fixed column list.

For event list queries, common columns include:

| Column | Graph Field | Description |
|---|---|---|
| Subject | `subject` | Event title |
| Organizer | `organizer.emailAddress` | Organizer name and address (`Name <email>`) |
| Start | `start.dateTime` | Start date and time (`2024-01-15 10:00`) |
| End | `end.dateTime` | End date and time |
| Location | `location.displayName` | Meeting room or location name |
| Body Preview | `bodyPreview` | First ~255 characters of the event description |
| Is All Day | `isAllDay` | `true` or `false` |
| Is Cancelled | `isCancelled` | `true` or `false` |
| Is Online Meeting | `isOnlineMeeting` | `true` or `false` |
| Online Meeting Url | `onlineMeetingUrl` | Link to join the Teams/online meeting |
| Show As | `showAs` | `busy`, `free`, `tentative`, `oof`, etc. |
| Categories | `categories` | Comma-separated category list |
| Importance | `importance` | `low`, `normal`, or `high` |

The **GENERATED KQL** field shows the actual Graph API call that was executed, for example:
```
GET /me/events?$top=10&$orderby=start/dateTime+asc&$select=id,subject,...
```

---

## KQL Reference (Search Queries)

When the question is a keyword or attribute search, the AI may generate a KQL query and execute it via `POST /search/query`. Common KQL patterns:

| KQL | Matches |
|---|---|
| `subject:standup` | Events with "standup" in the title |
| `subject:"Project Review"` | Events with that exact phrase in the title |
| `organizer:jane@example.com` | Events organised by a specific person |
| `attendees:john` | Events that John is attending |
| `location:London` | Events in London |
| `location:"Conference Room A"` | Events in that specific room |
| `subject:standup AND organizer:Jane` | Standups organised by Jane |

Boolean operators: `AND`, `OR`, `NOT` (uppercase). Wildcards: `*` (prefix only, e.g. `project*`).

---

## Error Messages

| Message | Cause | Fix |
|---|---|---|
| `AI API key is not configured.` | No API key in AI Settings | Add a key in the AI Assistant tab → AI Settings |
| `Not connected. Configure credentials in the Authentication tab.` | Calendar auth not set up or expired | Complete or refresh the Authentication tab setup |
| `Could not determine Graph API call. Please try rephrasing your question.` | AI returned a blank response | Rephrase with more specific terms |
| `Could not parse a valid Graph API call from the AI response.` | AI returned unparseable JSON | Rephrase the question more specifically |
| `Could not find the event mentioned in your question.` | `{eventId}` resolution failed — no event matched the subject keyword | Check the event title; use a quoted phrase or the exact event name |
| `Graph API call failed (HTTP 400): ...` | Invalid filter, date format, or unsupported field in the generated query | Check the extension logs; rephrase the question |
| `Graph API call failed (HTTP 403): ...` | Insufficient permissions on the Azure AD application | Add `Calendars.ReadWrite` scope (see [Authentication](pages/Authentication.md)) |
| `Query failed: ...` | Unexpected exception | Check extension logs for the underlying error |

---

## Troubleshooting

### GENERATED KQL shows the original question instead of a Graph API path

This was a bug in earlier versions where the generated plan was not passed through to the UI. It is fixed — the GENERATED KQL field now always shows the actual `method + path` that was executed (e.g. `GET /me/events?$top=10...`).

### Update or delete returns "Could not find the event"

The event ID resolution searches by the subject keyword extracted from your question. Tips:

- Use the exact event title in quotes: `"delete the 'Daily Standup' meeting"`
- Avoid ambiguous keywords — if multiple events match, the first result is used
- For recurring meetings, the first future occurrence is selected

### Created event is missing attendees or location

The AI extracts these fields from your question. Be explicit:

> "Create a meeting called Project Kickoff tomorrow at 2pm in Conference Room A with alice@example.com and bob@example.com"

### PATCH update does not apply

If the question doesn't include the new value clearly, the AI may generate an incomplete body. Be specific:

> "Change the standup meeting time to 9am tomorrow" ✓
> "Move the standup" ✗ (no new time specified)

### Events with online meeting URLs

Online meetings (Teams) include an `onlineMeetingUrl` column rendered as a clickable link. This column only appears for events that have an online meeting attached.

### No results for today's events

The Graph API date filter uses ISO 8601 format. The AI generates today's date automatically. If results are missing, try a broader question:

> "Show all events this week" instead of "Show today's events"

---

## Architecture Notes

- Both the NQL tab and Krista Agent tool calls use `CalendarMcpQueryService.query()` — the same pipeline.
- The AI generates `{method, path, body}` and the extension executes it via a JAX-RS HTTP client.
- All four HTTP methods are supported: `GET`, `POST`, `PATCH`, `DELETE`.
- `{eventId}` in AI-generated paths is auto-resolved via a `contains(subject,...)` filter search.
- `PATCH` responses return a friendly `"Event updated successfully."` message instead of raw event metadata.
- `DELETE`/`POST` with no response body return `"Action completed successfully."`.
- Columns are fully dynamic — derived at runtime from the Graph API response fields.
- Authentication credentials are reloaded on every call so changes to the Authentication tab take effect immediately without a restart.

---

## See Also

- [Authentication](pages/Authentication.md) — configure the Calendar connection
- [Schedule Meeting](pages/ScheduleMeeting.md) — catalog request for creating events programmatically
- [Update Event](pages/UpdateEvent.md) — catalog request for updating event details
- [Cancel Event](pages/CancelEvent.md) — catalog request for cancelling events
- [Search Events](pages/SearchEvents.md) — catalog request for searching calendar events
- [Get Next Upcoming Meetings](pages/GetNextUpcomingMeetings.md) — catalog request for listing upcoming events
