# Get Events for a given User

## Overview

Returns the events scheduled for a given user within a specified date range. This request requires that the extension is configured using a service account with appropriate permissions.

## Request Details

- **Area**: Meetings
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| User Email | Email | Yes | Email address of the user whose events to retrieve |
| From | Date | Yes | Start date of the date range |
| To | Date | Yes | End date of the date range |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Events | List | List of event details for the specified user and date range |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| User Email is invalid | "Invalid email address" | Provide valid email address |
| From date is invalid | "Invalid date format" | Provide valid date |
| To date before From | "End date must be after start date" | Ensure To is after From |

## Usage Examples

### Example: Get User's Events for a Week

**Input**:
```
User Email: "colleague@company.com"
From: "2024-10-10T00:00:00"
To: "2024-10-17T23:59:59"
```

**Output**:
```json
{
  "Events": [
    {
      "Event ID": "AAMkAGI1...AAA=",
      "Title": "Team Meeting",
      "Date From": "2024-10-10T09:00:00",
      "Date To": "2024-10-10T10:00:00"
    },
    {
      "Event ID": "AAMkAGI1...BBB=",
      "Title": "Client Call",
      "Date From": "2024-10-12T14:00:00",
      "Date To": "2024-10-12T15:00:00"
    }
  ]
}
```

## Business Rules

1. **Service Account Required**: Extension must be configured with service account authentication
2. **Date Range Required**: Both From and To dates are mandatory
3. **Permissions Required**: Service account must have access to user's calendar
4. **Delegation**: Requires appropriate calendar delegation permissions

## Limitations

1. **Service Account Only**: Only works with service account authentication
2. **Permission Dependent**: Requires calendar read permissions for the specified user
3. **Date Range**: Limited by Office 365 API constraints

## Related Catalog Requests

- [Get Next Upcoming Meetings](pages/GetNextUpcomingMeetings.md)
- [Get Schedules](pages/GetSchedules.md)
- [Get Event](pages/GetEvent.md)

## See Also

- [Authentication](pages/Authentication.md)
- [Microsoft Graph List Events](https://learn.microsoft.com/en-us/graph/api/calendar-list-events)

