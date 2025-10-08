# Get Attachments for a given User

## Overview

Retrieves all file attachments from events for a specified user within a given date range. Requires service account authentication with appropriate permissions.

## Request Details

- **Area**: Meetings
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| User Email | Email | Yes | Email address of the user whose event attachments to retrieve |
| From | Date | Yes | Start date of the date range |
| To | Date | Yes | End date of the date range |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Attachments | File List | List of file attachments from events in the specified date range |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| User Email is invalid | "Invalid email address" | Provide valid email address |
| From date is invalid | "Invalid date format" | Provide valid date |
| To date before From | "End date must be after start date" | Ensure To is after From |

## Usage Examples

### Example: Get User's Event Attachments for a Month

**Input**:
```
User Email: "colleague@company.com"
From: "2024-10-01T00:00:00"
To: "2024-10-31T23:59:59"
```

**Output**:
```json
{
  "Attachments": [
    {
      "Name": "Q4_Report.pdf",
      "Size": 345678,
      "ContentType": "application/pdf",
      "Event": "Quarterly Review"
    },
    {
      "Name": "Budget.xlsx",
      "Size": 123456,
      "ContentType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "Event": "Budget Planning"
    }
  ]
}
```

## Business Rules

1. **Service Account Required**: Extension must be configured with service account authentication
2. **Date Range Required**: Both From and To dates are mandatory
3. **Permissions Required**: Service account must have access to user's calendar

## Limitations

1. **Service Account Only**: Only works with service account authentication
2. **Permission Dependent**: Requires calendar read permissions for the specified user

## Related Catalog Requests

- [Get Attachments](pages/GetAttachments.md)
- [Get Events for a given User](pages/GetEventsForAGivenUser.md)
- [Get Event](pages/GetEvent.md)

## See Also

- [Authentication](pages/Authentication.md)
- [Microsoft Graph List Attachments](https://learn.microsoft.com/en-us/graph/api/event-list-attachments)

