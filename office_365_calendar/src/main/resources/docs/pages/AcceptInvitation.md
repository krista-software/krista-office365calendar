# Accept Invitation

## Overview

Accepts a meeting invitation for a specific event. This updates your response status to 'Accepted' and adds the event to your calendar, notifying the organizer of your acceptance.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| Event ID | Text | Yes | Unique identifier of the event invitation to accept |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Details of the accepted event |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Event ID is null or empty | "Event ID is required" | Provide a valid event ID |
| Event ID not found | "The provided event id does not exist" | Verify the event exists |

## Error Handling

### Input Errors (INPUT_ERROR)
- Event ID not provided
- Invalid event ID format

### System Errors (SYSTEM_ERROR)
- Network connectivity problems
- Office 365 service unavailable
- Authentication token expired

## Usage Examples

### Example: Accept Meeting Invitation

**Input**:
```
Event ID: "AAMkAGI1...AAA="
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...AAA=",
    "Title": "Team Meeting",
    "Date From": "2024-10-10T14:00:00",
    "Date To": "2024-10-10T15:00:00",
    "Organizer": "manager@company.com"
  }
}
```

## Business Rules

1. **Event ID Required**: Event ID is mandatory
2. **Response Update**: Updates your response status to 'Accepted'
3. **Organizer Notification**: Organizer is automatically notified
4. **Calendar Addition**: Event is added to your calendar

## Limitations

1. **Invitations Only**: Can only accept events you've been invited to
2. **Already Accepted**: Accepting again has no effect

## Best Practices

### 1. Verify Event Details
Use Get Event to review details before accepting.

### 2. Check Calendar Conflicts
Ensure no scheduling conflicts before accepting.

## Common Use Cases

### 1. Accept Meeting Request
```
Action: Accept invitation for upcoming meeting
Result: Meeting added to calendar, organizer notified
```

## Related Catalog Requests

- [Decline Invitation](pages/DeclineInvitation.md)
- [Tentative Invitation](pages/TentativeInvitation.md)
- [Get All Unaccepted Events](pages/GetAllUnacceptedEvents.md)
- [Get Event](pages/GetEvent.md)

## Technical Implementation

- **Service**: MeetingDao.acceptMeetingInvitation()
- **Validation**: ValidationOrchestrator validates event ID

## Troubleshooting

### Issue: "Event ID is required"
**Solution**: Provide a valid event ID

### Issue: "Event not found"
**Solution**: Verify event ID is correct and event exists

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication](pages/Authentication.md)
- [Microsoft Graph Accept Event](https://learn.microsoft.com/en-us/graph/api/event-accept)

