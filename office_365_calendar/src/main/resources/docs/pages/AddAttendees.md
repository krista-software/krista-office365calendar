# Add Attendees

## Overview

Adds new attendees to an existing calendar event. You can specify both required and optional attendees who will receive meeting invitations for the event.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| Event ID | Text | Yes | Unique identifier of the event to add attendees to |
| Attendee Email | Email List | Yes | List of required attendee email addresses to add |
| Optional Attendee Email | Email List | No | List of optional attendee email addresses to add |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Updated event details with new attendees |

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
- Attendee limit exceeded

## Usage Examples

### Example: Add Required and Optional Attendees

**Input**:
```
Event ID: "AAMkAGI1...AAA="
Attendee Email: ["newmember@company.com"]
Optional Attendee Email: ["observer@company.com"]
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...AAA=",
    "Title": "Team Meeting",
    "Contacts": ["original@company.com", "newmember@company.com", "observer@company.com"]
  }
}
```

## Business Rules

1. **Event ID Required**: Event ID is mandatory
2. **At Least One Attendee**: Must provide at least one required attendee
3. **Additive Operation**: Adds to existing attendees, doesn't replace
4. **Invitations Sent**: New attendees receive meeting invitations
5. **Organizer Permissions**: Must have permission to modify the event

## Limitations

1. **Maximum Attendees**: Office 365 limit of 500 attendees per meeting
2. **Organizer Only**: Typically only organizer can add attendees
3. **Duplicate Handling**: Adding existing attendees may have no effect

## Best Practices

### 1. Verify Event First
Use Get Event to check current attendees before adding.

### 2. Validate Email Addresses
Ensure email addresses are correct before adding.

### 3. Use Optional for Non-Critical Attendees
Mark attendees as optional if their presence isn't required.

## Common Use Cases

### 1. Add New Team Member
```
Action: Add new team member to recurring team meeting
Result: New member receives invitation and is added to all future occurrences
```

### 2. Include Additional Stakeholders
```
Action: Add stakeholders to project meeting
Result: Stakeholders receive invitation
```

## Related Catalog Requests

- [Remove Attendee](pages/RemoveAttendee.md) - Remove attendees from events
- [Update Event](pages/UpdateEvent.md) - Replace entire attendee list
- [Forward Event](pages/ForwardEvent.md) - Forward event to additional people
- [Get Event](pages/GetEvent.md) - View current attendees

## Technical Implementation

- **Service**: MeetingDao.addAttendees()
- **Validation**: ValidationOrchestrator validates event ID and emails

## Troubleshooting

### Issue: "Event ID is required"
**Solution**: Provide a valid event ID

### Issue: "Attendee Email is required"
**Solution**: Provide at least one required attendee email

### Issue: "Invalid email address"
**Solution**: Verify email addresses are correctly formatted

### Issue: "Attendee limit exceeded"
**Solution**: Office 365 limits meetings to 500 attendees

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication](pages/Authentication.md)
- [Microsoft Graph Update Event](https://learn.microsoft.com/en-us/graph/api/event-update)

