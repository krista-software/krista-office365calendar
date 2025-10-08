# Cancel Event

## Overview

Cancels an existing calendar event by providing the event ID. This will remove the event from all attendees' calendars and send cancellation notifications. Supports delegation to cancel events on behalf of other users.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes - The extension validates the event ID and prompts for corrections if validation fails

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Delegate Email | Email | No | Email address of the person whose calendar contains the event. When provided, cancels the event from the specified delegate's calendar | delegate@company.com |
| Event ID | Text | Yes | Unique identifier of the event to cancel | AAMkAGI1...AAA= |

### Parameter Details

#### Event ID
- **Required**: Yes - This is the mandatory parameter
- **Format**: Unique event identifier from Microsoft Graph
- **Source**: Obtained from Get Event, Schedule Meeting, Search Events, or other event operations
- **Validation**: Must be a valid event ID that exists in the calendar
- **Tooltip**: "Enter the unique identifier of the event you want to cancel"

#### Delegate Email
- **Purpose**: Enables delegation - cancel events in another user's calendar
- **Permissions Required**: Current user must have appropriate delegation permissions to the delegate's calendar
- **Behavior**: When provided, cancels event from delegate's calendar; when omitted, cancels from current user's calendar
- **Use Case**: Assistants canceling meetings on behalf of executives, or managing shared calendars

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Details of the cancelled event before deletion |

### Event Details Entity Structure

| Field Name | Type | Description |
|------------|------|-------------|
| Event ID | Text | Unique identifier of the cancelled event |
| Title | Text | Subject of the cancelled meeting |
| Date From | Date | Original start date/time |
| Date To | Date | Original end date/time |
| Location | Text | Original meeting location |
| Time Zone | Text | Original time zone |
| Organizer | Email | Event organizer email |
| Contacts | List | Original attendee list |
| Details | Rich Text | Original meeting description |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Event ID is null or empty | "Event ID is required" | Provide a valid event ID |
| Event ID not found | "The provided event id: {eventId} does not exist" | Verify the event exists and ID is correct |
| Invalid event ID format | "Invalid event id: {eventId}" | Ensure the event ID format is correct |
| Insufficient permissions | "Insufficient permissions to cancel event" | Verify calendar access permissions |
| Event already cancelled | "Event not found" | Event may have already been cancelled |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters

**Common Scenarios**:
- Event ID not provided
- Invalid event ID format
- Event ID doesn't exist
- Invalid delegate email format

**Error Messages**:
- "Event ID is required"
- "Please enter valid event id"
- "The provided event id: {eventId} does not exist"
- "Invalid email address"

**Resolution**:
1. Verify Event ID is provided and correct
2. Use Get Event or Search Events to find the correct event ID
3. Check that the event hasn't already been cancelled
4. Validate delegate email address format

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic validation failures

**Common Scenarios**:
- Event not found in calendar
- Event already cancelled
- User is not the organizer
- Event is in the past (may be restricted)

**Error Messages**:
- "Event not found"
- "The provided event id: {eventId} does not exist"

**Resolution**:
1. Verify the event exists using Get Event
2. Check if event was already cancelled
3. Ensure you have the correct event ID
4. For delegation, verify delegate email is correct

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API errors or connection issues

**Common Scenarios**:
- Network connectivity problems
- Office 365 service unavailable
- Authentication token expired
- Insufficient permissions
- Event locked by another process

**Error Messages**:
- "Failed to cancel event"
- "Connection to Office 365 failed"
- "Authentication required"
- "Insufficient permissions"

**Resolution**:
1. Verify network connectivity
2. Check extension configuration and authentication
3. Re-validate the extension if authentication expired
4. Ensure user has calendar write permissions
5. Check Office 365 service health status
6. Wait and retry if event is locked

### Authorization Errors

**Cause**: Insufficient permissions or delegation issues

**Common Scenarios**:
- User is not the organizer and doesn't have delete permissions
- Delegation permissions not granted for Delegate Email
- Calendar sharing not configured properly
- User lacks Calendars.ReadWrite permission

**Resolution**:
1. Verify user is the organizer or has delete permissions
2. For delegation, ensure proper calendar sharing is configured
3. Check Microsoft Entra ID app has Calendars.ReadWrite.Shared permission
4. Verify user has "Editor" or "Delegate" permission on the calendar
5. Contact Office 365 administrator to grant permissions

## Usage Examples

### Example 1: Cancel Own Meeting

**Scenario**: Cancel a meeting you organized

**Input**:
```
Event ID: "AAMkAGI1...AAA="
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...AAA=",
    "Title": "Team Standup",
    "Date From": "2024-10-10T09:00:00",
    "Date To": "2024-10-10T10:00:00",
    "Location": "Conference Room A",
    "Organizer": "currentuser@company.com",
    "Contacts": ["team@company.com"]
  }
}
```

**Result**: Meeting cancelled successfully, all attendees notified

### Example 2: Cancel Meeting with Delegation

**Scenario**: Cancel a meeting on behalf of a manager

**Input**:
```
Delegate Email: "manager@company.com"
Event ID: "AAMkAGI1...BBB="
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...BBB=",
    "Title": "Executive Meeting",
    "Date From": "2024-10-15T14:00:00",
    "Date To": "2024-10-15T15:00:00",
    "Organizer": "manager@company.com",
    "Contacts": ["executive1@company.com", "executive2@company.com"]
  }
}
```

**Result**: Meeting cancelled from manager's calendar, attendees notified

### Example 3: Cancel Recurring Meeting

**Scenario**: Cancel an entire recurring meeting series

**Input**:
```
Event ID: "AAMkAGI1...CCC="
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...CCC=",
    "Title": "Weekly Team Sync",
    "Date From": "2024-10-14T10:00:00",
    "Date To": "2024-10-14T11:00:00",
    "Recurrence": "Weekly on Monday",
    "Organizer": "currentuser@company.com"
  }
}
```

**Result**: Entire recurring series cancelled, all future occurrences removed

## Business Rules

1. **Event ID Required**: Event ID is the only mandatory parameter
2. **Organizer Permissions**: Only the organizer or users with appropriate permissions can cancel events
3. **Cancellation Notifications**: All attendees automatically receive cancellation notifications
4. **Recurring Events**: Cancelling a series event ID cancels all future occurrences
5. **Single Occurrence**: To cancel one occurrence of a recurring event, use that occurrence's specific event ID
6. **Delegation**: Delegate Email requires proper calendar sharing and permissions
7. **Permanent Action**: Cancelled events are deleted and cannot be recovered
8. **Past Events**: May not be able to cancel events that have already ended (Office 365 restriction)

## Limitations

1. **Permanent Deletion**: Cancelled events cannot be recovered
2. **Recurring Series**: Cancelling the series event ID cancels all future occurrences
3. **Single Occurrence**: Must use specific occurrence event ID to cancel only one instance
4. **Past Events**: Cannot cancel events that have already ended (Office 365 limitation)
5. **Delegation**: Requires proper Office 365 calendar sharing permissions
6. **Organizer Only**: Typically only the organizer can cancel events (unless delegation is configured)
7. **No Undo**: There is no undo operation for cancelled events

## Best Practices

### 1. Verify Event Before Cancelling
Use [Get Event](pages/GetEvent.md) to verify event details before cancelling.

**Benefit**: Ensures you're cancelling the correct event and provides confirmation details.

### 2. Communicate Cancellation Reason
While the extension sends automatic notifications, consider sending a separate message explaining why the meeting was cancelled.

**Recommendation**: Use email or chat to provide context for the cancellation.

### 3. Check for Recurring Events
Verify if the event is part of a recurring series before cancelling.

**Important**: Cancelling a series event ID cancels all future occurrences.

### 4. Test Delegation First
Before cancelling events in another user's calendar, verify delegation permissions work.

**Check**: Test with a test event first.

### 5. Keep Event Details for Records
Save the returned Event Details for your records before the event is permanently deleted.

**Benefit**: Maintains audit trail and historical records.

## Common Use Cases

### 1. Cancel One-Time Meeting
```
Scenario: Cancel a single meeting
Action:
  1. Get Event ID from the meeting
  2. Execute Cancel Event with Event ID
Result: Meeting cancelled, attendees notified
```

### 2. Cancel Recurring Meeting Series
```
Scenario: Cancel all future occurrences of a recurring meeting
Action:
  1. Get the series Event ID (not a specific occurrence)
  2. Execute Cancel Event with series Event ID
Result: All future occurrences cancelled
```

### 3. Cancel Single Occurrence of Recurring Meeting
```
Scenario: Cancel only one instance of a recurring meeting
Action:
  1. Get the specific occurrence Event ID
  2. Execute Cancel Event with that occurrence's Event ID
Result: Only that occurrence cancelled, series continues
```

### 4. Assistant Cancelling Executive's Meeting
```
Scenario: Executive assistant cancels meeting on behalf of executive
Action:
  1. Set Delegate Email to executive's email
  2. Provide Event ID
  3. Execute Cancel Event
Result: Meeting cancelled from executive's calendar
```

## Related Catalog Requests

- [Schedule Meeting](pages/ScheduleMeeting.md) - Create new calendar events
- [Update Event](pages/UpdateEvent.md) - Modify existing events instead of cancelling
- [Get Event](pages/GetEvent.md) - Retrieve event details before cancelling
- [Search Events](pages/SearchEvents.md) - Find events to cancel
- [Get Next Upcoming Meetings](pages/GetNextUpcomingMeetings.md) - View upcoming meetings

## Technical Implementation

### Helper Class
- **Class**: CancelEventHelper
- **Package**: app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.helper
- **Validation**: ValidationOrchestrator validates event ID
- **Service**: MeetingDao.cancelMeeting()

### Validation Flow
1. Event ID validation (required, not empty)
2. Event existence validation (event must exist in calendar)
3. Delegate email validation (if provided, format and permissions)
4. Permission validation (user must have delete permissions)

### Telemetry Metrics
- CANCEL_EVENT_SUCCESS: Successful event cancellation
- CANCEL_EVENT_FAILURE: Failed event cancellation
- CANCEL_EVENT_VALIDATION_ERROR: Validation failures
- CANCEL_EVENT_DELEGATION_USED: Delegation feature used

## Troubleshooting

### Issue: "Event ID is required"

**Cause**: Event ID not provided

**Solution**:
1. Provide a valid Event ID
2. Use Get Event or Search Events to obtain Event ID
3. Verify Event ID is not null or empty

### Issue: "The provided event id does not exist"

**Cause**: Event ID doesn't exist in the calendar

**Solution**:
1. Verify Event ID is correct (check for typos)
2. Use Get Event to verify the event exists
3. Check if event was already cancelled
4. For delegation, verify Delegate Email is correct
5. Ensure you're looking in the correct calendar

### Issue: "Insufficient permissions to cancel event"

**Cause**: User lacks permission to cancel the event

**Solution**:
1. Verify you are the event organizer
2. For delegation, ensure calendar sharing is configured
3. Check Microsoft Entra ID app has Calendars.ReadWrite.Shared permission
4. Verify user has "Editor" or "Delegate" permission on the calendar
5. Contact Office 365 administrator to grant permissions

### Issue: "Failed to cancel event"

**Cause**: System error during cancellation

**Solution**:
1. Verify network connectivity
2. Check Office 365 service health status
3. Re-validate the extension authentication
4. Wait a few minutes and retry
5. Check if event is locked by another process
6. Verify the event still exists (may have been cancelled by another user)

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configure the extension
- [Authentication](pages/Authentication.md) - Understanding authentication modes
- [Creating Office 365 App](pages/CreatingOffice365App.md) - Microsoft Entra ID setup
- [Schedule Meeting](pages/ScheduleMeeting.md) - Create new events
- [Update Event](pages/UpdateEvent.md) - Modify events instead of cancelling
- [Microsoft Graph Delete Event API](https://learn.microsoft.com/en-us/graph/api/event-delete) - Official API documentation

