# Get Schedules

## Overview

Retrieves the list of scheduled meetings for specified attendees within a given date range and time zone. Useful for viewing availability and scheduled meetings for multiple people.

## Request Details

- **Area**: Meetings
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| Date From | Date | Yes | Start date and time for the schedule query |
| Date To | Date | Yes | End date and time for the schedule query |
| Attendees | Email List | Yes | List of attendee email addresses to get schedules for |
| Time Zone | Text | No | Time zone for the schedule (e.g., "America/New_York") |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| List of Meetings | List | List of meetings for each attendee with scheduled times and status |

### Output Structure

Each item contains:
- **Attendee**: Email address
- **Schedules**: List of scheduled time slots with status

## Usage Examples

### Example: Get Schedules for Team Members

**Input**:
```
Date From: "2024-10-10T08:00:00"
Date To: "2024-10-10T18:00:00"
Attendees: ["member1@company.com", "member2@company.com"]
Time Zone: "America/New_York"
```

**Output**:
```json
{
  "List of Meetings": [
    {
      "Attendee": "member1@company.com",
      "Schedules": [
        {
          "Scheduled Time": "2024-10-10T09:00:00 - 2024-10-10T10:00:00",
          "Status": "Busy"
        },
        {
          "Scheduled Time": "2024-10-10T14:00:00 - 2024-10-10T15:00:00",
          "Status": "Busy"
        }
      ]
    },
    {
      "Attendee": "member2@company.com",
      "Schedules": [
        {
          "Scheduled Time": "2024-10-10T10:00:00 - 2024-10-10T11:00:00",
          "Status": "Busy"
        }
      ]
    }
  ]
}
```

## Business Rules

1. **Date Range Required**: Both From and To dates are mandatory
2. **Attendees Required**: At least one attendee email must be provided
3. **Time Zone Optional**: Defaults to user's time zone if not specified

## Related Catalog Requests

- [Get Free Slots](pages/GetFreeSlots.md)
- [Get Events for a given User](pages/GetEventsForAGivenUser.md)
- [Schedule Meeting](pages/ScheduleMeeting.md)

## See Also

- [Microsoft Graph Get Schedule](https://learn.microsoft.com/en-us/graph/api/calendar-getschedule)

