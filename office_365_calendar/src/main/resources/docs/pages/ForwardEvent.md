# Forward Event

## Overview

Forwards an existing calendar event to additional attendees. The specified recipients will receive meeting invitations with all the original event details.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| Event ID | Text | Yes | Unique identifier of the event to forward |
| Attendee Email | Email List | Yes | List of email addresses to forward the event to |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| IsForwarded | Switch | Boolean indicating if the event was successfully forwarded (true) |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Event ID is null or empty | "Event ID is required" | Provide a valid event ID |
| Event ID not found | "The provided event id does not exist" | Verify the event exists |
| Attendee Email is empty | "Attendee Email is required" | Provide at least one email address |
| Invalid email format | "Invalid email address" | Provide valid email addresses |

## Error Handling

### Input Errors (INPUT_ERROR)
- Event ID not provided
- Attendee Email not provided
- Invalid email format

### System Errors (SYSTEM_ERROR)
- Network connectivity problems
- Office 365 service unavailable
- Authentication token expired

## Usage Examples

### Example: Forward Event to Additional Attendees

**Input**:
```
Event ID: "AAMkAGI1...AAA="
Attendee Email: ["colleague1@company.com", "colleague2@company.com"]
```

**Output**:
```json
{
  "IsForwarded": true
}
```

## Business Rules

1. **Event ID Required**: Event ID is mandatory
2. **Attendee Email Required**: At least one email address must be provided
3. **Invitation Sent**: Recipients receive meeting invitations
4. **Original Event Unchanged**: Original event and attendees remain unchanged
5. **Forwarding Notification**: Organizer may be notified of the forward

## Limitations

1. **Cannot Forward Private Events**: May not be able to forward private events
2. **Organizer Permissions**: Some events may restrict forwarding
3. **Maximum Recipients**: Subject to Office 365 email limits

## Best Practices

### 1. Verify Event Details
Use Get Event to review details before forwarding.

### 2. Inform Organizer
Consider informing the organizer when forwarding to external attendees.

### 3. Validate Email Addresses
Ensure email addresses are correct before forwarding.

## Common Use Cases

### 1. Share Meeting with Team Members
```
Action: Forward team meeting to new team members
Result: New members receive invitation
```

### 2. Include Additional Stakeholders
```
Action: Forward important meeting to stakeholders
Result: Stakeholders receive invitation and can attend
```

## Related Catalog Requests

- [Add Attendees](pages/AddAttendees.md) - Add attendees directly to the event
- [Get Event](pages/GetEvent.md) - Retrieve event details before forwarding
- [Schedule Meeting](pages/ScheduleMeeting.md) - Create new events

## Technical Implementation

- **Service**: MeetingDao.forwardMeeting()
- **Validation**: ValidationOrchestrator validates event ID and emails

## Troubleshooting

### Issue: "Event ID is required"
**Solution**: Provide a valid event ID

### Issue: "Attendee Email is required"
**Solution**: Provide at least one email address

### Issue: "Invalid email address"
**Solution**: Verify email addresses are correctly formatted

### Issue: "Event not found"
**Solution**: Verify event ID is correct and event exists

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication](pages/Authentication.md)
- [Microsoft Graph Forward Event](https://learn.microsoft.com/en-us/graph/api/event-forward)

