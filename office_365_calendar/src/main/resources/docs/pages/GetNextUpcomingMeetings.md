# Get Next Upcoming Meetings

## Overview

Retrieves a specified number of upcoming meetings from your calendar, ordered chronologically by start time. Useful for getting a quick overview of your next scheduled events.

## Request Details

- **Area**: Meetings
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| Number of Meetings | Number | Yes | Number of upcoming meetings to retrieve (must be greater than 0) |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | List | List of upcoming event details ordered by start time |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Number of Meetings <= 0 | "Number of meetings should be greater than 0" | Provide a positive number |

## Usage Examples

### Example: Get Next 5 Meetings

**Input**:
```
Number of Meetings: 5
```

**Output**:
```json
[
  {
    "Event ID": "AAMkAGI1...AAA=",
    "Title": "Team Standup",
    "Date From": "2024-10-10T09:00:00",
    "Date To": "2024-10-10T09:15:00"
  },
  {
    "Event ID": "AAMkAGI1...BBB=",
    "Title": "Client Meeting",
    "Date From": "2024-10-10T14:00:00",
    "Date To": "2024-10-10T15:00:00"
  }
]
```

## Business Rules

1. **Positive Number Required**: Number of meetings must be greater than 0
2. **Chronological Order**: Results ordered by start time
3. **Future Events Only**: Returns only upcoming events
4. **Current User Calendar**: Retrieves from current user's calendar

## Related Catalog Requests

- [Get Event](pages/GetEvent.md)
- [Search Events](pages/SearchEvents.md)
- [Get Events for a given User](pages/GetEventsForAGivenUser.md)

## See Also

- [Microsoft Graph List Events](https://learn.microsoft.com/en-us/graph/api/calendar-list-events)

