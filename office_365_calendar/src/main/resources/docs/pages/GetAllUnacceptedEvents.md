# Get All Unaccepted Events

## Overview

Retrieves all meeting invitations that you have not yet responded to (neither accepted, declined, nor marked as tentative). Helps you manage pending meeting requests.

## Request Details

- **Area**: Meetings
- **Type**: QUERY_SYSTEM
- **Retry Support**: ❌ No

## Input Parameters

No input parameters required.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | List | List of unaccepted event details |

## Usage Examples

### Example: Get Pending Invitations

**Input**: None

**Output**:
```json
[
  {
    "Event ID": "AAMkAGI1...AAA=",
    "Title": "Project Review",
    "Date From": "2024-10-12T10:00:00",
    "Date To": "2024-10-12T11:00:00",
    "Organizer": "manager@company.com"
  },
  {
    "Event ID": "AAMkAGI1...BBB=",
    "Title": "Team Building",
    "Date From": "2024-10-15T14:00:00",
    "Date To": "2024-10-15T16:00:00",
    "Organizer": "hr@company.com"
  }
]
```

## Business Rules

1. **No Parameters**: No input required
2. **Pending Only**: Returns only events without a response
3. **Current User**: Retrieves from current user's calendar

## Common Use Cases

### 1. Review Pending Invitations
```
Action: Get all unaccepted events
Result: List of pending invitations to review
Next: Accept, Decline, or mark as Tentative
```

## Related Catalog Requests

- [Accept Invitation](pages/AcceptInvitation.md)
- [Decline Invitation](pages/DeclineInvitation.md)
- [Tentative Invitation](pages/TentativeInvitation.md)
- [Get Event](pages/GetEvent.md)

## See Also

- [Microsoft Graph List Events](https://learn.microsoft.com/en-us/graph/api/calendar-list-events)

