# Decline Invitation

## Overview

Declines a meeting invitation for a specific event. This updates your response status to 'Declined' and notifies the meeting organizer of your decision.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| Event ID | Text | Yes | Unique identifier of the event invitation to decline |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Details of the declined event |

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

### Example: Decline Meeting Invitation

**Input**:
```
Event ID: "AAMkAGI1...AAA="
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...AAA=",
    "Title": "Optional Meeting",
    "Date From": "2024-10-10T14:00:00",
    "Date To": "2024-10-10T15:00:00",
    "Organizer": "manager@company.com"
  }
}
```

## Business Rules

1. **Event ID Required**: Event ID is mandatory
2. **Response Update**: Updates your response status to 'Declined'
3. **Organizer Notification**: Organizer is automatically notified
4. **Calendar Removal**: Event may be removed from your calendar

## Limitations

1. **Invitations Only**: Can only decline events you've been invited to
2. **Already Declined**: Declining again has no effect

## Best Practices

### 1. Provide Reason
Consider sending a separate message explaining why you're declining.

### 2. Decline Promptly
Decline as soon as possible so organizer can plan accordingly.

## Common Use Cases

### 1. Decline Conflicting Meeting
```
Action: Decline invitation due to schedule conflict
Result: Organizer notified, event removed from calendar
```

## Related Catalog Requests

- [Accept Invitation](pages/AcceptInvitation.md)
- [Tentative Invitation](pages/TentativeInvitation.md)
- [Get All Unaccepted Events](pages/GetAllUnacceptedEvents.md)
- [Get Event](pages/GetEvent.md)

## Technical Implementation

- **Service**: MeetingDao.declineMeetingInvitation()
- **Validation**: ValidationOrchestrator validates event ID

## Troubleshooting

### Issue: "Event ID is required"
**Solution**: Provide a valid event ID

### Issue: "Event not found"
**Solution**: Verify event ID is correct and event exists

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication](pages/Authentication.md)
- [Microsoft Graph Decline Event](https://learn.microsoft.com/en-us/graph/api/event-decline)

