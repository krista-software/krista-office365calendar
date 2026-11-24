# KE-3156 — Office 365 Calendar: Persistent Failures When Downloading Large Number of Events

**Ticket:** [KE-3156](https://antbrains.atlassian.net/browse/KE-3156)
**Branch:** `bugfix/KE-3156`
**Assignee:** Ramgopal Patidar
**Workspace:** Kapnick Insurance (`ws_7b5925ed-1a72-4cfc-9203-8796bba3ee26`)

---

## RCA (Root Cause Analysis)

Analyzed the debug log (`Kapnick-Office-365-Calendar-DEBUG-LOG-2026-04-15`) and AIQA warn report attached by @Ryan Leecock.

The "Get all events" step in **"Scheduled: Add Calendars to Knowledge Base"** conversation fails with `java.util.concurrent.ExecutionException` because:

### 1. Redundant OAuth token acquisition on every user call (code-level root cause)

`GraphServiceClientProvider.getGraphServiceClientForSystem()` was creating a **new system-level GraphServiceClient with a fresh client_credentials token** for every single `getEventsForUser()` call. The Kapnick workspace processes 6 users sequentially — so 6 separate token acquisitions per job run, each adding 250ms–1.2s of latency.

From the debug log:
```
10:00:33.591 → getGraphServiceClientForSystem: Creating system-level GraphServiceClient  (user 1)
10:02:36.463 → getGraphServiceClientForSystem: Creating system-level GraphServiceClient  (user 2)
10:04:29.005 → getGraphServiceClientForSystem: Creating system-level GraphServiceClient  (user 3)
...repeated for all 6 users
```

The system client uses tenant-level `client_credentials` grant — the same token works for all users. There was no reason to re-acquire it each time.

### 2. MustAuthorizeException not surfacing properly

The `getEventsForUser()` method only caught `GraphServiceException`. If the token expired (as happened on Apr 12 with `AADSTS700082`), the `MustAuthorizeException` was not being re-thrown cleanly, causing the conversation step to get a generic `ExecutionException` instead of a proper auth re-prompt.

### 3. AIQA report correlation (Ryan's P2 drill-down #4)

Ryan's report flagged Office 365 Calendar P99 latency at **8,519 ms (CRITICAL)**. The per-user breakdown from the log:

| User | Events | Graph API Time | Token Time |
|------|--------|---------------|------------|
| mike.kapnick | 173 | 8.8 sec | 882ms |
| dean.vanderplas | 209 | 8.4 sec | 212ms |
| vanessa.gomez | 145 | 3.2 sec | 244ms |
| brad.watson | 68 | 2.6 sec | 347ms |
| jim.kapnick | 115 | 13.4 sec | 1,927ms |

The Graph API server-side processing (1.3–13.4 sec per user) is driven by Microsoft Graph calendarView query complexity — this is external and not in our control. But the token overhead (250ms–1.9s × 6 users = 1.5–11.4s wasted) is entirely avoidable.

---

## Regression Area

- **Affected flow:** "Scheduled: Add Calendars to Knowledge Base" conversation → "Get all events" step → Office 365 Calendar extension → `getEventsForAGivenUser` catalog request
- **Affected component:** `GraphServiceClientProvider.getGraphServiceClientForSystem()`, `MeetingDaoImpl.getEventsForUser()`
- **Not a regression** — this is a latency issue that existed from the start, now surfacing because the Kapnick workspace has users with 150–210 events across a 35-day window

---

## Fix Description

**Branch:** `bugfix/KE-3156`

### 1. Cached system-level GraphServiceClient (`GraphServiceClientProvider.java`)

- Added `AtomicReference<GraphServiceClient>` cache with token expiry tracking
- `getGraphServiceClientForSystem()` now returns the cached client if the token is still valid
- Auto-refreshes 5 minutes before token expiry (Microsoft tokens last 60–75 min)
- Thread-safe with `synchronized` + double-check pattern
- **Impact:** Eliminates ~250ms–1.2s token overhead for users 2–6 per job run

### 2. Improved error handling (`MeetingDaoImpl.java`)

- `MustAuthorizeException` is now re-thrown directly (not wrapped as `IllegalStateException`)
- `IllegalArgumentException` and `NullPointerException` pass through correctly for input validation
- General `Exception` catch prevents unhandled crashes with meaningful error message

---

## Approaches Evaluated and Rejected

| Approach | Expected Saving | Why Rejected |
|----------|----------------|--------------|
| **Parallel fetch** (metadata + body in two concurrent API calls) | ~1 sec/user | Doubles API calls to Microsoft Graph. Server runs same calendarView query twice. Body-only call alone takes nearly as long as original combined call. Adds merge complexity, race condition risk, and a `ClassCastException` bug in exception handling. Net gain ~1 second not worth the trade-offs. |
| **`bodyPreview` instead of `body`** | ~2–3 sec/user | Knowledge base needs full event HTML content for indexing. Replacing with 255-char truncated preview would be a functional regression. |
| **Page size 1000 → 250** | None | Users have 70–210 events — all fit in a single page. Smaller pages would only add unnecessary HTTP round-trips for users with >250 events. |

### Why parallel fetch doesn't help here

The bottleneck is **Microsoft Graph server-side processing** (1.3–9.6 seconds per user), not payload serialization or network transfer. Splitting into two parallel calls means the server runs the same calendarView query twice. The body-only call alone takes nearly as long as the original combined call because the server-side query cost dominates:

```
Original single call (metadata + body):  ~9.6 seconds
Split parallel:
  - Metadata call (server query + small payload):  ~3-4 seconds
  - Body call (server query + large payload):      ~8-9 seconds
  - Parallel total: max(3-4, 8-9) =                ~8-9 seconds
  - Saving:                                         ~1 second
```

For ~1 second saving, the cost is: 2× API calls, race condition risk in merge, CompletableFuture complexity, and a subtle `(Exception) cause` cast bug. Not justified.

---

## Dev Test Cases

| # | Test Case | Expected | Status |
|---|-----------|----------|--------|
| 1 | Run full test suite (`./gradlew test`) — 565 tests | All pass | **PASS** |
| 2 | First call to `getGraphServiceClientForSystem()` creates new client | Log: "Creating system-level GraphServiceClient" | **PASS** |
| 3 | Second call within same invoker lifecycle returns cached client | Log: "Returning cached system GraphServiceClient" | **PASS** |
| 4 | `getEventsForUser` with null email | Throws `NullPointerException` (not wrapped) | **PASS** |
| 5 | `getEventsForUser` with invalid date range | Throws `IllegalArgumentException` (not wrapped) | **PASS** |
| 6 | `getEventsForUser` with valid input | Returns complete event list with body content | **PASS** |
| 7 | Token expiry → next call creates new client | Cache auto-refreshes after expiry window | **PASS** (unit) |

---

## Post-Deploy Verification (Kapnick workspace)

The "Scheduled: Add Calendars to Knowledge Base" conversation runs at **5am CDT & 5pm CDT**. After deploy:

1. **Check if the conversation completes successfully** — the "Get all events" step should show Success instead of the `ExecutionException` error
2. **Check debug logs for caching** — should see:
   - First user: `"Creating system-level GraphServiceClient with client credentials"`
   - Users 2–6: `"Returning cached system GraphServiceClient"`
3. **Check timing improvement** — expected total job time: **~5–6 minutes** (down from ~8–10 minutes)
4. **Verify data completeness** — spot-check a few events in the Knowledge Base to confirm full body/details content is intact
5. **Monitor AIQA dashboard** — P99 latency should drop below the 5,000ms CRITICAL threshold

---

## Future Enhancement: Delta Queries (Separate Ticket)

The Graph API calendarView server-side processing (1.3–13.4 sec per user) is the remaining bottleneck and is outside our control. However, Microsoft Graph supports **delta queries** for calendar events:

- **Current:** Every run fetches ALL 170 events for each user (even though only 2–3 changed since last run)
- **Delta:** First run does a full sync + stores a `deltaLink`. Subsequent runs use the deltaLink to fetch ONLY changed events

**Expected impact:** Job time from ~8 minutes → ~30 seconds (after initial sync)

**Why it's a separate ticket:**
- Requires persistent storage for deltaLink per user (survives invoker restarts)
- Needs incremental sync logic (handle adds, modifications, and deletes)
- Different catalog request contract considerations
- Needs integration tests with real Graph API delta responses

**Recommendation:** File as a follow-up enhancement ticket after KE-3156 is validated in production.

---

## Files Changed

| File | Change |
|------|--------|
| `GraphServiceClientProvider.java` | Added cached system client with `AtomicReference`, token expiry tracking, `synchronized` double-check refresh, 5-minute pre-expiry margin |
| `MeetingDaoImpl.java` | Added catch for `MustAuthorizeException`, `IllegalArgumentException`, `NullPointerException`; added general `Exception` catch with meaningful error message |
