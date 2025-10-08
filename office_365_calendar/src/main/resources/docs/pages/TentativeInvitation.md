# Tentative Invitation

## Overview

Marks your response to a meeting invitation as 'Tentative', indicating you might attend but are not certain. This notifies the organizer of your provisional acceptance.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| Event ID | Text | Yes | Unique identifier of the event invitation to mark as tentative |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Details of the tentatively accepted event |

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

### Example: Mark Invitation as Tentative

**Input**:
```
Event ID: "AAMkAGI1...AAA="
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...AAA=",
    "Title": "Planning Meeting",
    "Date From": "2024-10-10T14:00:00",
    "Date To": "2024-10-10T15:00:00",
    "Organizer": "manager@company.com"
  }
}
```

## Business Rules

1. **Event ID Required**: Event ID is mandatory
2. **Response Update**: Updates your response status to 'Tentative'
3. **Organizer Notification**: Organizer is automatically notified
4. **Calendar Addition**: Event is added to your calendar with tentative status

## Limitations

1. **Invitations Only**: Can only mark events you've been invited to as tentative
2. **Can Change Later**: Can accept or decline later

## Best Practices

### 1. Update When Certain
Change to Accept or Decline once you know for sure.

### 2. Communicate Uncertainty
Let organizer know why you're tentative if important.

## Common Use Cases

### 1. Uncertain Availability
```
Action: Mark as tentative when unsure of availability
Result: Organizer knows you might attend
```

### 2. Waiting for Confirmation
```
Action: Mark tentative while waiting for other commitments to be confirmed
Result: Placeholder in calendar, can update later
```

## Related Catalog Requests

- [Accept Invitation](pages/AcceptInvitation.md)
- [Decline Invitation](pages/DeclineInvitation.md)
- [Get All Unaccepted Events](pages/GetAllUnacceptedEvents.md)
- [Get Event](pages/GetEvent.md)

## Technical Implementation

- **Service**: MeetingDao.tentativeMeetingInvitation()
- **Validation**: ValidationOrchestrator validates event ID

## Troubleshooting

### Issue: "Event ID is required"
**Solution**: Provide a valid event ID

### Issue: "Event not found"
**Solution**: Verify event ID is correct and event exists

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication](pages/Authentication.md)
- [Microsoft Graph Tentatively Accept Event](https://learn.microsoft.com/en-us/graph/api/event-tentativelyaccept)

