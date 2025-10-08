# Get Free Slots

## Overview

Analyzes calendars of specified attendees to find available time slots for scheduling a meeting. Returns suggested meeting times based on everyone's availability within the given date range.

## Request Details

- **Area**: Meetings
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| Date From | Date | Yes | Start date for finding free slots (cannot be in the past) |
| Date To | Date | Yes | End date for finding free slots |
| Is Organizer Optional | Yes/No | No | Whether organizer's availability is optional |
| Required Attendee | Email List | Yes | List of required attendee email addresses |
| Optional Attendee | Email List | No | List of optional attendee email addresses |
| Time Zone | Text | No | Time zone for the suggestions |
| Meeting Duration | Text | Yes | Duration of the meeting (e.g., "PT1H" for 1 hour) |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| List of Free Slots | List | Suggested time slots with availability details |

### Output Structure

Each slot contains:
- **Time Slot**: DateTimeRange
- **Attendee Availability**: List of attendees with their availability status
- **Confidence**: Percentage confidence in the suggestion
- **Organizer Availability**: Organizer's availability status
- **Suggestion Reason**: Reason for the suggestion

## Usage Examples

### Example: Find Free Slots for Team Meeting

**Input**:
```
Date From: "2024-10-10T08:00:00"
Date To: "2024-10-12T18:00:00"
Is Organizer Optional: false
Required Attendee: ["member1@company.com", "member2@company.com"]
Time Zone: "America/New_York"
Meeting Duration: "PT1H"
```

**Output**:
```json
{
  "List of Free Slots": [
    {
      "Time Slot": "2024-10-10T14:00:00 - 2024-10-10T15:00:00",
      "Attendee Availability": [
        {"Attendee": "member1@company.com", "Availability": "Free"},
        {"Attendee": "member2@company.com", "Availability": "Free"}
      ],
      "Confidence": 95,
      "Organizer Availability": "Free",
      "Suggestion Reason": "All attendees available"
    }
  ]
}
```

## Business Rules

1. **Future Dates Only**: Date From cannot be in the past
2. **Meeting Duration Required**: Must specify meeting duration
3. **Required Attendees**: At least one required attendee must be provided

## Related Catalog Requests

- [Get Schedules](pages/GetSchedules.md)
- [Schedule Meeting](pages/ScheduleMeeting.md)

## See Also

- [Microsoft Graph Find Meeting Times](https://learn.microsoft.com/en-us/graph/api/user-findmeetingtimes)

