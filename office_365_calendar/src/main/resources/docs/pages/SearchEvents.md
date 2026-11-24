# Search Events

## Overview

Searches for calendar events that match the given search keywords. Returns a list of matching events with their IDs, titles, and scheduled times. Useful for finding specific events when you don't have the event ID.

## Request Details

- **Area**: Meetings
- **Type**: QUERY_SYSTEM
- **Retry Support**: ❌ No - Returns results directly without validation prompts

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Search Terms | Text | No | Keywords to search for in event titles, descriptions, and other fields | "team meeting", "client" |

### Parameter Details

#### Search Terms
- **Required**: No
- **Behavior when empty/null**: Returns empty list (no events)
- **Search Scope**: Searches event titles, descriptions, locations, and attendee names
- **Case Insensitive**: Search is not case-sensitive
- **Partial Matching**: Matches partial words and phrases
- **Multiple Keywords**: Separate keywords with spaces

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Search Results | List | List of matching events with basic information |

### Search Results Structure

Each result contains:

| Field Name | Type | Description |
|------------|------|-------------|
| Event ID | Text | Unique identifier of the event |
| Event Title | Text | Subject/title of the event |
| Scheduled Time | DateTimeRange | Start and end time of the event |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Search returns no results | Empty list returned | Try different search terms |
| Empty/null search terms | Empty list returned | Provide search terms to find events |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Common Scenarios**:
- Network connectivity problems
- Office 365 service unavailable
- Authentication token expired
- Search service error

**Resolution**:
1. Verify network connectivity
2. Re-validate extension authentication
3. Check Office 365 service health
4. Retry the search

## Usage Examples

### Example 1: Search by Title

**Input**:
```
Search Terms: "team standup"
```

**Output**:
```json
[
  {
    "Event ID": "AAMkAGI1...AAA=",
    "Event Title": "Daily Team Standup",
    "Scheduled Time": "2024-10-10T09:00:00 - 2024-10-10T09:15:00"
  },
  {
    "Event ID": "AAMkAGI1...BBB=",
    "Event Title": "Weekly Team Standup",
    "Scheduled Time": "2024-10-14T09:00:00 - 2024-10-14T09:30:00"
  }
]
```

### Example 2: Search by Attendee

**Input**:
```
Search Terms: "client@external.com"
```

**Output**:
```json
[
  {
    "Event ID": "AAMkAGI1...CCC=",
    "Event Title": "Client Presentation",
    "Scheduled Time": "2024-10-15T14:00:00 - 2024-10-15T15:30:00"
  }
]
```

### Example 3: No Results

**Input**:
```
Search Terms: "nonexistent meeting"
```

**Output**:
```json
[]
```

### Example 4: Empty Search Terms

**Input**:
```
Search Terms: (empty)
```

**Output**:
```json
[]
```

**Note**: When search terms are not provided (null or blank), an empty list is returned. Use [Get Next Upcoming Meetings](pages/GetNextUpcomingMeetings.md) or [Get Events for a given User](pages/GetEventsForAGivenUser.md) to retrieve events without search terms.

## Business Rules

1. **Maximum Results**: Returns up to 25 matching events
2. **Current User Calendar**: Searches only the current user's accessible calendars
3. **No Validation**: Does not validate search terms
4. **Empty Search**: Returns empty list if search terms are null or blank
5. **Chronological Order**: Results are typically ordered by event start time

## Limitations

1. **Result Limit**: Maximum 25 results returned
2. **No Pagination**: Cannot retrieve more than 25 results
3. **Current User Only**: Cannot search other users' calendars directly
4. **Basic Information**: Returns only ID, title, and time (not full event details)
5. **No Advanced Filters**: Cannot filter by date range, location, or other criteria

## Best Practices

### 1. Use Specific Keywords
Use specific terms to narrow down results.

### 2. Get Full Details After Search
Use Get Event with the Event ID to retrieve complete event information.

### 3. Handle Empty Results
Implement logic to handle cases where no events match.

## Common Use Cases

### 1. Find Event by Title
```
Action: Search for "quarterly review"
Result: List of matching events with IDs
Next: Use Get Event to retrieve full details
```

### 2. Find Events with Specific Attendee
```
Action: Search for attendee email
Result: Events including that attendee
```

### 3. Find Event to Update
```
Action: Search for event title
Result: Get Event ID
Next: Use Update Event with the ID
```

## Related Catalog Requests

- [Get Event](pages/GetEvent.md) - Retrieve full event details using Event ID from search results
- [Get Next Upcoming Meetings](pages/GetNextUpcomingMeetings.md) - Get upcoming events without searching
- [Update Event](pages/UpdateEvent.md) - Update events found through search
- [Cancel Event](pages/CancelEvent.md) - Cancel events found through search

## Technical Implementation

### Service
- **Service**: MeetingDao.getQueryResults()
- **Package**: app.krista.extensions.essentials.collaboration.office_365_calendar.service

### Telemetry Metrics
- SEARCH_EVENTS_SUCCESS: Successful search
- SEARCH_EVENTS_FAILURE: Failed search

## Troubleshooting

### Issue: No Results Returned

**Cause**: No events match the search terms

**Solution**:
1. Try broader search terms
2. Check spelling
3. Try searching for partial words
4. Verify events exist in your calendar

### Issue: Search Fails

**Cause**: System error

**Solution**:
1. Verify network connectivity
2. Re-validate extension
3. Check Office 365 service health
4. Retry the search

## See Also

- [Get Event](pages/GetEvent.md) - Retrieve full event details
- [Get Next Upcoming Meetings](pages/GetNextUpcomingMeetings.md) - Get upcoming events
- [Microsoft Graph Search Events](https://learn.microsoft.com/en-us/graph/search-concept-events) - Official documentation

