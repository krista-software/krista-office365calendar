# Release Notes - Office 365 Calendar Extension

## Version 4.0.7 — Time Zone Fix for Scheduled Meetings (KE-3527)

**Release Date**: August, 2026

### Bug Fixes

**Meeting times were sent in the extension's default time zone instead of the event's time zone**

Scheduling a meeting whose event time zone differed from the extension's configured/default time zone (which defaults to UTC) produced calendar invites shifted by the offset between the two zones. For example, a 9:00 AM–5:00 PM America/Chicago shift appeared to recipients as 3:00 PM–11:00 PM, and an 8:00 AM Eastern shift appeared as 12:00 PM. The email/body content showed the correct time; only the calendar invite was wrong.

- **Root cause**: in `MeetingDaoHelper.buildDateTimeTimeZone`, the epoch was formatted into the `dateTime` string using the extension's effective/default time zone, while the Microsoft Graph `DateTimeTimeZone.timeZone` label was set from the event-specific time zone. When the two zones differed, Graph interpreted the wall-clock time against the wrong offset, shifting the meeting.
- **Fix**: the event's time zone is now resolved once and used for **both** formatting the `dateTime` string and labelling the Graph object, so the two always agree. Java's time-zone handling applies the correct DST/standard-time offset automatically.
- The per-event time zone is honored on every meeting — no global/default time zone change is required, so events across multiple time zones are each scheduled correctly.

### Technical Changes

- Fixed `MeetingDaoHelper.buildDateTimeTimeZone` to format and label using a single resolved event time zone
- Added regression unit tests in `MeetingDaoHelperTest` (Eastern DST, Central DST, Central standard-time, and null/empty time-zone fallback) using the reported epoch values
- Added `MeetingSchedulingLiveIntegrationTest` — an opt-in, token-gated live integration test that schedules real events via Microsoft Graph and asserts the stored instant round-trips
- Added `TZ-TRACE` diagnostic logging across the schedule path (input fields → resolved zones → Graph payload → stored response) to make time-zone issues traceable end-to-end

---

## Version 4.0.6 — MCP AI Assistant (KE-3377)

**Release Date**: July, 2026

### New Features

**MCP AI Assistant — Natural Language Calendar Queries**

The extension now exposes a new `Ask Agent` catalog request under the **MCP** area, enabling Claude Desktop and other MCP-compatible AI clients to query and interact with Office 365 Calendar using plain English.

- New `Ask Agent` (MCP area, `tool = true`) accepts a free-text `Query` parameter and returns matching calendar events as `FreeForm` rows
- AI-driven Graph API planning — the service asks the configured AI provider (Anthropic Claude, OpenAI, or Google Gemini) to determine the exact Microsoft Graph API call (`method`, `path`, `body`) needed to answer the question, then executes it generically. No hardcoded intents — any valid Graph endpoint is reachable without code changes
- Automatic fallback to the parent KQL search pipeline if the AI returns an unparseable plan
- Credentials are reloaded from the attribute store on every call, so changes in the Authentication tab take effect immediately without a restart

**NQL Tab Integration**

- New `/calendar/mcp-setup` JAX-RS resource (`CalendarMcpSetupResource`) exposes the NQL (Natural Query Language) tab endpoint for direct natural-language search from the Krista UI
- Converts AI-generated Graph API responses into the structured row/column format expected by the NQL tab

**AI Authentication Bridge**

- `CalendarMcpAuthBridge` bridges the existing `Office365CalendarAttributes` / `RefreshTokenStore` to the SDK's `McpAuthBridge` interface without requiring changes to the existing auth infrastructure
- Supports both Public (OAuth 2.0 refresh-token flow via MSAL4J) and Private (Client Credentials via MSAL4J) authentication modes

### Technical Changes

- Added `MCPArea.java` — HK2 catalog domain entry point for the `Ask Agent` request
- Added `CalendarMcpQueryService.java` — extends `AbstractMcpQueryService`; overrides `query()` with AI-driven Graph API planning
- Added `CalendarMcpProductConfig.java` — Calendar-specific `McpProductConfig` providing `graphApiPlanningPrompt()` and response parsing
- Added `CalendarMcpSetupResource.java` — JAX-RS resource at `/mcp-setup` for NQL tab integration
- Added `CalendarMcpAuthBridge.java` — `McpAuthBridge` implementation wrapping existing Calendar auth stores
- Registered `MCPArea` and `CalendarMcpSetupResource` in `Office365CalendarApplication`
- Updated `Office365CalendarAuditService` to support MCP call context
- Added `AiAssistant.md` documentation page and sidebar entry

---

## Version 4.0.4 — Microsoft Auth SDK Integration

**Release Date**: May, 2026

### New Features

**Authentication Tab (SDK-Powered)**

The extension now includes a dedicated Authentication Tab powered by the Microsoft Auth SDK (`microsoft-auth-sdk:1.0.1`). This tab replaces the need to configure credentials through the Setup tab and provides:

- Support for three authentication flows: Public (Auth Code), Private (Auth Code), and Service to Service (Client Credentials)
- In-tab **Test Connection** with Microsoft OAuth popup for Public/Private flows
- Scope panel displaying all required Microsoft Graph API permissions and their associated catalog requests
- Credential save with automatic audit counter reset

**Audit Dashboard**

A new Calendar Audit tab provides real-time monitoring of catalog request activity:

- **Summary cards** — total requests, failure count, success rate, P95 latency, last request timestamp
- **Credential & Token Health** — auth type, refresh token status, client secret expiry, and authentication event counts
- **Audit event log** — every catalog request execution recorded with TRIGGERED/SUCCESS/FAILURE status, duration, exception details, and triggering user
- **Filtering** — by date range, event type, status, and free-text search
- **CSV export** — download filtered audit data as `calendar-audit-*.csv`
- **Health alerts** — automatic detection of token expiry, invalid credentials, password changes, and configuration errors

### Technical Changes

- Added `microsoft-auth-sdk:1.0.1` dependency
- Created `Office365CalendarAuthConfig.java` with `@MsAuthExtension`, `@AuthScope`, `@AuditConfig`, and `@AuditEventGroup` annotations
- Created `Office365CalendarExtensionDescriptor.java` (SPI descriptor) and `META-INF/services` registration file for SDK extension discovery
- Created `Office365CalendarAuditService.java` — HK2 service delegating to SDK's `MsAuthAuditService` with KV-backed ring buffer (500 records)
- Created `Office365CalendarAuditResource.java` — JAX-RS resource extending `AbstractAuditResource` with `/audit/data`, `/audit/health`, and audit HTML endpoints
- Added `/msauth/metadata/{extensionId}` REST endpoint for SDK UI configuration loading
- Added `/authentication/{subPath}` and `/audit/{subPath}` endpoints serving SDK HTML pages
- Registered `Office365CalendarAuditResource` in `Office365CalendarApplication`
- All 19 catalog requests in `MeetingsArea` and 1 in `SetupArea` wrapped with audit recording (TRIGGERED/SUCCESS/FAILURE)
- Replaced React UI build tasks with SDK HTML extraction from the SDK JAR (`auth/index.html`, `audit/index.html`)
- Removed legacy React UI source (`src/main/ui/office365calendaroauth/` — 21 files) since auth and audit UIs are now served from the SDK
- Removed `node {}` configuration block and `cleanDir` task from `build.gradle` (no longer needed; Node/Yarn are not invoked during builds)
- Extension custom tabs updated: Authentication, Documentation, Calendar Audit

### Audited Catalog Requests

All catalog requests now record audit events:

Cancel Event, Get Next Upcoming Meetings, Decline Invitation, Tentative Invitation, Forward Event, Schedule Meeting, Add Attendees, Update Event, Accept Invitation, Remove Attendee, Get Free Slots, Get Schedules, Get Event, Get Events For A Given User, Get Attachments For A Given User, Get Attachments, Check Public Auth Expiry, Get All Unaccepted Events, Search Events, Health Check

---

## Version 4.0.3 — Search Events date-range filtering (KE-3244)

**Release Date**: May, 2026

### Enhancement

**`searchEvents` now supports optional `Date From` / `Date To` parameters.**

Previously, `searchEvents` only accepted a free-text `Search Terms` parameter and routed every call through Microsoft Graph's `/search/query` endpoint, which is hard-capped at **25 results per page** for the EVENT entity (Microsoft constraint, not ours). For popular search terms like "MBL" or recurring meeting names, the page filled up with the 25 most-recent matches and the MCP/agent caller could not scope to a window like "yesterday" or "this week."

**Fix:** When `Date From` and/or `Date To` are provided, `searchEvents` routes to `/me/calendar/calendarView` instead — Microsoft's canonical date-range endpoint. This:

- Supports up to **1000 events per page** (vs. 25),
- **Expands recurring meeting instances** into individual occurrences within the window,
- Returns results **chronologically ordered** by start time.

The `Search Terms` keyword is then applied as a case-insensitive `contains` filter on subject and body preview, in-memory. Server-side `$filter=contains(subject,...)` was intentionally avoided because of a known Microsoft Graph HTTP 500 bug when events have null subjects.

When neither date is provided, behavior is unchanged: `/search/query` is used and Microsoft's 25-result cap applies. The catalog `description` now documents this trade-off so MCP LLMs know to pass dates when they want to scope by time or retrieve more than 25 events.

| Scenario | Endpoint used | Cap | Recurring instances |
|----------|---------------|-----|---------------------|
| No dates provided | `/search/query` (EVENT) | 25 per page (hard) | mixed series+instances |
| `Date From` and/or `Date To` provided | `/me/calendar/calendarView` | 1000 per page | expanded |

If only one of `Date From` / `Date To` is provided, a one-year window is assumed from the supplied bound. If `Date From > Date To`, an `IllegalArgumentException` is raised.

### Tests Added

Five new test cases in `MeetingDaoImplTest` covering: subject-keyword match, body-preview match, blank-query returns all in window, only-from bound applies one-year window, and `from > to` rejection. Existing `MeetingsAreaTest` mocks updated for the new signature plus a new test covering the date-range path through the catalog layer.

---

## Version 4.0.2 — Fix: Get Next Upcoming Meetings missing attendee events (KE-3238)

**Release Date**: May, 2026

### Bug Fix

**`getMeetings` switched from `events()` + `$filter` to `calendarView()` to surface attendee events.**

**Root cause:** `GET /me/calendar/events?$filter=start/dateTime ge '...'` only returns events stored in the user's own calendar copy. Events where the user is an attendee (not organizer) are stored in the organizer's timezone, so the raw string filter comparison failed for attendee-only events — they were silently excluded. Additionally, without `$orderby`, the `$top=N` limit picked events by internal index rather than chronological order, causing organizer events to always surface first.

**Fix:** Switched to `GET /me/calendarView` with UTC `startDateTime`/`endDateTime` bounds and `$orderby=start/dateTime asc`. The `calendarView` endpoint expands recurring instances and returns both organizer and attendee events correctly ordered by start time.

| Before | After |
|--------|-------|
| `calendar().events().buildRequest().filter(...).top(N)` | `calendar().calendarView().buildRequest(queryOptions)` |
| Filter string with no timezone offset — attendee events excluded | UTC ISO_OFFSET_DATE_TIME bounds — all events in range returned |
| No `$orderby` — `$top` picked by internal index | `$orderby=start/dateTime asc` — chronological top-N |

### Tests Added

Nine new test cases added to `MeetingDaoImplTest` covering: organizer event, attendee-only event (regression), empty calendar, multiple events, `calendarView()` called (not `events()`), all four query option keys present, `$top` value, UTC offset on `startDateTime`, and `GraphServiceException` wrapping.

---

## Version 4.0.1 — MCP per-user auth factory fix

**Release Date**: May, 2026

### Bug Fix

**`invokeAsUser=true` now always forces PUBLIC OAuth credentials in `GraphServiceClientProviderFactory`.**

Previously, `create(RequestContext)` (and `create()` which delegates to it) loaded stored Calendar attributes from the attribute store before checking `invokeAsUser`. If an operator had configured Setup → Private, the factory would load those Private (service-principal) credentials and build a provider backed by them — even on an MCP per-user call. The service principal would then authenticate silently as the tenant app, bypassing the per-user OAuth flow. `getGraphServiceClientForSystem()` posed an additional risk: it called `client_credentials` directly without any RT lookup, completely bypassing per-user identity.

**Fix:** `create(RequestContext)` now checks `context.invokeAsUser()` **before** touching the attribute store. When `invokeAsUser=true`, the factory immediately returns a PUBLIC provider (`authType = PUBLIC`, null credentials) so that the provider's RT lookup fires normally and triggers `MustAuthorizeException` for first-time users — regardless of what is stored under the invokerId.

**Impact:** All 4 Calendar `tool=true` catalog requests (`get_all_unaccepted_events`, `get_next_upcoming_meetings`, `schedule_meeting`, `search_events`) call `factory.create()`, which delegates through `create(requestContext)` and are fully covered. No non-MCP (`invokeAsUser=false`) paths are affected.

| Before | After |
|--------|-------|
| `create(RequestContext)`: loaded stored attrs first, checked `invokeAsUser` only if attrs null | `create(RequestContext)`: checks `invokeAsUser` first, returns PUBLIC provider immediately |
| `create()`: delegated to above, inheriting the same late check | `create()`: delegates to above, now picks up the early PUBLIC path |

---

## Version 4.0.0 — Auth Architecture Recovery

**Release Date**: May, 2026

### Why this release exists

This release rolls back the "MCP / unified auth state" feature work introduced in 2.0.29 (and earlier MCP-era patches), which created two identity-bleed bug families that could not be cleanly fixed without architectural changes:

- **Per-user-mode bleed**: when an operator filled Setup → Public/Private and a workflow with "Have People authenticate with their account" was run by a different user, that user silently received the operator's refresh token. End-user B saw operator A's calendar.
- **MCP-vs-MCP bleed**: when Setup was blank and an MCP user authenticated first, a subsequent MCP user inherited the first user's `attrs.email` from the shared MCP-INV attribute slot, hit a shared lookup, and read the first user's RT.

Both bugs shared one root cause: the per-user code path was reading operator-keyspace state (`attrs.email`). The 4.0.0 release restores the simple two-keyspace model and adds only the small set of pieces that were missing for clean MCP support.

### Architecture rules locked in

1. **Two hermetic keyspaces.** Per-user RTs live under `<wsContact>` keys; operator RTs live under `<email>_<clientId>_<authType>` keys. The two never share a key shape and are never read from the wrong code path.
2. **`invokeAsUser=true` always uses the default Public OAuth client** baked into `config.properties`. Setup's Public/Private fields are consulted only when `invokeAsUser=false` (operator-shared paths). MCP and "Have People authenticate" workflows are by definition `invokeAsUser=true`, so they are isolated to the default Public app and to the per-user keyspace.
3. **`MustAuthorizeException` propagates through every catalog/DAO catch block.** No swallowing of MAE behind a generic `Exception` handler.

### What's new on top of the 4.0.1-equivalent recovery base

- **Default-Public OAuth client routing** — `GraphServiceClientProvider#chooseEffectiveAttributes` selects the Public OAuth credentials from `config.properties` whenever `requestContext.invokeAsUser()` is true. `Office365CalendarRequestAuthenticator.getMustAuthorizeResponse` short-circuits to default Public for any wsContact-prefixed userId. `AuthenticationResource.getGraphServiceClientProvider` does the same for OAuth callbacks whose state's parts[0] is wsContact-prefixed. All three sites agree, so token exchange always uses the same OAuth client that built the auth URL.
- **State encoding simplified** — the MCP "4-part state" (`wsContact_..#INV_..#tempUUID#forwardPath`) is removed. State is now max 3 parts: `<userId>#<authContextId>#<forwardPath>`. The `clientKey` (invokerId) is no longer embedded in the state, and the OAuth callback no longer dual-writes the refresh token under the invokerId slot or overwrites operator attrs with each per-user authentication.
- **MCP tool exposure** — every catalog request is now MCP-discoverable (`tool = true`) except for any future async-pair operations. Existing 3 tools plus 17 newly marked. Setup-tab Health Check included per the "all except async" rule.
- **MAE rethrow pattern** inserted across catch blocks in every auth-touching call chain.
- **Removed `extractEmailFromAccessToken` / per-MCP-user attrs persistence**. The OAuth callback no longer mutates the operator's attribute slot with each MCP user's email.

### What was NOT brought forward

Explicitly excluded from this release:

- The MCP 4-part state encoding and `clientKey` (invokerId) embedding.
- Dual-write of refresh tokens under both `<wsContact>` and `<invokerId>` keys.
- The "save attrs permanently under invokerId with extracted email" pattern in the OAuth callback.
- Any cross-flow RT reuse mechanism.

### Upgrade guidance

Customers upgrading from 2.0.29 (or any pre-recovery 2.0.x):

- **Existing wsContact-keyed RTs continue to work** — no migration required.
- **Existing email-composite operator RTs continue to work** — no migration required.
- **RTs that 2.0.29 also wrote under `<invokerId>` are orphaned** — never read by lookup logic anyway. No symptom.
- **Operator's attribute slot may have been mutated by prior MCP authentications**: after upgrade, operator should re-save their Setup → Public/Private credentials to ensure the slot reflects intended operator config rather than the last MCP user's email.
- **First per-user-mode call after upgrade may re-prompt for authentication** if the prior RT was issued under a different OAuth client (e.g. the operator's Private app instead of default Public). This is intentional and confirms the bleed is fixed.

### Reference

- Recovery doc applied: SharePoint `docs/4.0.4-auth-recovery.md` (the seven categories of change).
- Branch: `bugfix/office365calendarMCPfix`.

---

## Version 2.0.29
**Release Date**: April, 2026
**Global Catalog Version**: GC-2026.4.4

### What's New
**MCP Support** - Added Model Context Protocol (MCP) compatibility for AI assistant integration

### New Features
- Added `tool = true` annotation to all catalog requests for MCP tool discovery
- Implemented MCP authentication flow with state encoding for user-level authorization
- Added `getAuthContextId()` method in `AuthenticationResource` to support MCP 4-part state format
- Added email extraction from access token in OAuth callback for MCP attribute persistence

### Improvements
- Updated krista-apis dependency to 1.0.125-sp1 for MCP compatibility
- Authentication is now verified before input validation in `getQueryResults()`, ensuring `MustAuthorizeException` is properly surfaced when credentials are missing
- Extension version updated to 2.0.29

### Bug Fixes
- Fixed `IllegalArgumentException` in `searchEvents` when "Search Terms" parameter is null or blank. The field was marked as optional (`required = false`) but the implementation rejected null/empty values. Now returns an empty list when search terms are not provided, matching the catalog contract.

### Technical Changes
- Modified `GraphServiceClientProviderFactory.create(RequestContext)` to support MCP invokeAsUser path with proper state management
- Updated `Office365CalendarRequestAuthenticator` constructor to accept `routingUrl` for MCP fallback scenarios
- Added `CLIENT_USER_ID` and `USER_ID` constants for MCP state encoding
- Enhanced `Office365CalendarExtension` to pass `routingUrl` to request authenticator

---

## Version 2.0.28
**Release Date**: April, 2026
**Global Catalog Version**: GC-2026.4.4
### Bug Fixes
- Fixed `NullPointerException` in `getEventsForUser` caused by attendees with no email address (e.g. display-only contacts like "Template Practice Leader") — null entries were being added to the contacts list and crashing the platform response handler. Added null guard on `emailAddress.address` in `MeetingImpl.getContacts()`.

### Improvements
- Added `WARN` logging in `MeetingImpl.getContacts()` when an attendee is skipped due to missing email address, including attendee name and event ID for traceability.

---

## Version 2.0.27
**Release Date**: April, 2026
**Global Catalog Version**: GC-2026.4.4
### What's New
**[KE-3156](https://antbrains.atlassian.net/browse/KE-3156)** Fixed persistent failures when downloading large number of calendar events

### Performance Improvements
- Cached system-level GraphServiceClient to eliminate redundant OAuth token acquisition on every `getEventsForUser` call. Previously, a new client credentials token (~250ms–1.2s) was acquired for each user in a sequential batch. Now the token is cached and reused across all users within the same invoker lifecycle, auto-refreshing 5 minutes before expiry.

### Bug Fixes
- Fixed `MustAuthorizeException` not surfacing properly in `getEventsForUser` — expired tokens were being wrapped as generic `IllegalStateException` instead of triggering a re-authentication prompt
- Added catch for `IllegalArgumentException` and `NullPointerException` in `getEventsForUser` to prevent input validation errors from being silently swallowed
- Added general `Exception` catch in `getEventsForUser` for unexpected errors with meaningful error message

---

## Version 2.0.26
**Release Date**: April, 2026
**Global Catalog Version**: GC-2026.4.1
### What's New
**[KE-3108](https://antbrains.atlassian.net/browse/KE-3108)** Fixed user authentication not working for schedule meeting and other catalog requests

### Bug Fixes
- Fixed refresh token storage key mismatch preventing user-level authentication from working
- Fixed OAuth callback `getAuthContextId` incorrectly treating forward-path URLs as auth context IDs
- Added `MustAuthorizeException` propagation across all catalog requests so re-authentication prompts are correctly surfaced to the user
- Fixed `GraphServiceClientProvider` passing compound key instead of plain user ID when creating authorization exceptions
- Added null-safety checks in `GraphServiceClientProviderFactory` when loading stored attributes
- Added tenant ID fallback derivation from email domain when tenant ID is not configured

### Tests Added
- Added unit tests for `AuthenticationResource`, `GraphServiceClientProvider`, `GraphServiceClientProviderFactory`, and `MeetingsAreaSubCatalogRequestHelper`

---

## Version 2.0.25
**Release Date**: February, 2026
**Global Catalog Version**: GC-2026.02.03
### What's New
**Added change log support
## Version 2.0.24
**Release Date**: January, 2026
**Global Catalog Version**: GC-2026.02.02
### What's New
**[KE-2893](https://antbrains.atlassian.net/browse/KE-2893)** Enhance O365 Calendar extension to return NULL when EventID is not found (instead of error)
## Version 2.0.23
**Release Date**: October 19, 2025

### What's New
- Fixed reauthentication issues (KE-2729)
- Fixed incorrect callback URL format in documentation
- Corrected broken image links in setup guides
- Updated Microsoft Entra ID configuration instructions

### Action Required
If you previously configured redirect URIs, update them in your Microsoft Entra ID app registration:
- **Private Authentication**: `https://extension.qa-in.eng.krista.app/extension/api/{ROUTING_ID}/rest/calendar/callback`
- **Public Authentication**: `https://extension.qa-in.eng.krista.app/extension/api/rest/v3/oauth/callback`

---

## Version 2.0.18
**Release Date**: October 13, 2025

### What's New
- Complete extension rewrite with improved architecture
- Enhanced error handling with clear, actionable messages
- Comprehensive documentation with visual setup guides
- Automatic retry mechanism for failed requests

### New Features
**Calendar Management**
- Schedule, update, and cancel meetings
- Search and retrieve events
- Manage meeting invitations (accept, decline, tentative)
- Forward events to other users
- Add and remove attendees

**Query Operations**
- Get upcoming meetings
- Get unaccepted events
- Get user's calendar events
- Check availability and free time slots
- Retrieve meeting attachments

### Limitations
- Maximum 500 attendees per meeting
- Maximum 150 MB attachment size per meeting
- Recurring events limited to 999 occurrences

### Action Required
This is a major release. Reconfigure the extension and test all workflows before production use.

---

## Version 2.0.17
**Release Date**: October 1, 2025

### What's New
- Improved documentation clarity and organization
- Optimized documentation images
- Enhanced authentication flow handling

### Bug Fixes
- Fixed authentication tab change handling (KE-1478)
- Fixed documentation links and references

---

## Version 2.0.16
**Release Date**: September 30, 2025

### What's New
- Improved authentication flow for Public and Private modes
- Enhanced delegation support for shared calendars
- Better error messages for authentication and permission issues

### Bug Fixes
- Fixed token management and refresh mechanisms
- Improved handling of delegation permissions
- Enhanced validation for organizer and delegate email parameters

### Required Permissions
Ensure your Microsoft Entra ID app has these delegated permissions:
- Calendars.ReadWrite
- Calendars.ReadWrite.Shared
- offline_access
- openid
- profile

---

## Previous Versions

For information about versions 2.0.15 and earlier, contact your Krista administrator.
