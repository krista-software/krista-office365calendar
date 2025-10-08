# Remove Attendee

## Overview

Removes specified attendees from an existing calendar event. The removed attendees will be notified that they are no longer invited to the meeting.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| Event ID | Text | Yes | Unique identifier of the event |
| Attendee Email | Email List | Yes | List of attendee email addresses to remove |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Updated event details without removed attendees |

## Business Rules

1. **Event ID Required**: Event ID is mandatory
2. **At Least One Attendee**: Must provide at least one attendee to remove
3. **Removal Notification**: Removed attendees are notified
4. **Organizer Permissions**: Must have permission to modify the event

## Usage Examples

### Example: Remove Attendee

**Input**:
```
Event ID: "AAMkAGI1...AAA="
Attendee Email: ["former@company.com"]
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...AAA=",
    "Title": "Team Meeting",
    "Contacts": ["remaining@company.com"]
  }
}
```

## Related Catalog Requests

- [Add Attendees](pages/AddAttendees.md)
- [Update Event](pages/UpdateEvent.md)
- [Get Event](pages/GetEvent.md)

## See Also

- [Microsoft Graph Update Event](https://learn.microsoft.com/en-us/graph/api/event-update)

