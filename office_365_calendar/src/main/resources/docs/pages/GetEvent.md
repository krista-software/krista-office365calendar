# Get Event

## Overview

Retrieves complete event details for a specific calendar event by providing the event ID. This is useful for viewing event information, verifying event details before updates, or retrieving event data for processing.

## Request Details

- **Area**: Meetings
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes - The extension validates the event ID and prompts for corrections if validation fails

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Event ID | Text | Yes | Unique identifier of the event to retrieve | AAMkAGI1...AAA= |

### Parameter Details

#### Event ID
- **Required**: Yes - This is the only parameter
- **Format**: Unique event identifier from Microsoft Graph
- **Source**: Obtained from Schedule Meeting, Search Events, Get Next Upcoming Meetings, or other event operations
- **Validation**: Must be a valid event ID that exists in the calendar
- **Case Sensitive**: Yes - Event IDs are case-sensitive

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Complete details of the requested event |

### Event Details Entity Structure

| Field Name | Type | Description |
|------------|------|-------------|
| Event ID | Text | Unique identifier of the event |
| Title | Text | Meeting subject/title |
| Date From | Date | Start date and time of the event |
| Date To | Date | End date and time of the event |
| Location | Text | Meeting location (physical or virtual) |
| Time Zone | Text | Time zone of the event |
| Organizer | Email | Email address of the event organizer |
| Contacts | List | List of attendee email addresses |
| Details | Rich Text | Meeting description/agenda |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Event ID is null or empty | "Event ID is required" | Provide a valid event ID |
| Event ID not found | "The provided event id: {eventId} does not exist" | Verify the event exists and ID is correct |
| Invalid event ID format | "Invalid event id: {eventId}" | Ensure the event ID format is correct |
| Insufficient permissions | "Insufficient permissions to access event" | Verify calendar read permissions |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters

**Common Scenarios**:
- Event ID not provided
- Invalid event ID format
- Event ID doesn't exist
- Event ID is malformed

**Error Messages**:
- "Event ID is required"
- "Please enter valid event id"
- "The provided event id: {eventId} does not exist"
- "Invalid event id: {eventId}"

**Resolution**:
1. Verify Event ID is provided and not empty
2. Check that the event ID is correctly copied (no extra spaces or characters)
3. Use Search Events to find the correct event ID
4. Verify the event hasn't been deleted

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic validation failures

**Common Scenarios**:
- Event not found in calendar
- Event was deleted
- User doesn't have access to the event
- Event is in a different calendar

**Error Messages**:
- "Event not found"
- "The provided event id: {eventId} does not exist"

**Resolution**:
1. Verify the event exists using Search Events
2. Check if event was deleted
3. Ensure you're searching in the correct calendar
4. Verify you have access to the event

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API errors or connection issues

**Common Scenarios**:
- Network connectivity problems
- Office 365 service unavailable
- Authentication token expired
- Insufficient permissions
- API rate limiting

**Error Messages**:
- "Failed to get event"
- "Connection to Office 365 failed"
- "Authentication required"
- "Service temporarily unavailable"

**Resolution**:
1. Verify network connectivity
2. Check extension configuration and authentication
3. Re-validate the extension if authentication expired
4. Ensure user has calendar read permissions
5. Check Office 365 service health status
6. Wait and retry if rate limited

### Authorization Errors

**Cause**: Insufficient permissions to access the event

**Common Scenarios**:
- User lacks permission to read calendars
- Event is in another user's private calendar
- Calendar sharing not configured
- App permissions not granted

**Resolution**:
1. Verify user has Calendars.ReadWrite or Calendars.Read permission
2. Check Microsoft Entra ID app permissions
3. Ensure calendar sharing is configured if accessing shared calendars
4. Verify the event is accessible to the current user

## Usage Examples

### Example 1: Get Event Details

**Scenario**: Retrieve details of a specific meeting

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
    "Time Zone": "America/New_York",
    "Organizer": "manager@company.com",
    "Contacts": ["team1@company.com", "team2@company.com"],
    "Details": "Daily team sync meeting"
  }
}
```

**Result**: Event details retrieved successfully

### Example 2: Verify Event Before Update

**Scenario**: Check current event details before making changes

**Input**:
```
Event ID: "AAMkAGI1...BBB="
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...BBB=",
    "Title": "Client Presentation",
    "Date From": "2024-10-15T14:00:00",
    "Date To": "2024-10-15T15:30:00",
    "Location": "Virtual - Teams",
    "Organizer": "currentuser@company.com",
    "Contacts": ["client@external.com", "team@company.com"]
  }
}
```

**Result**: Current event details retrieved for verification before update

### Example 3: Get Recurring Event Details

**Scenario**: Retrieve details of a recurring event

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
    "Location": "Conference Room B",
    "Time Zone": "UTC",
    "Organizer": "manager@company.com",
    "Contacts": ["team@company.com"]
  }
}
```

**Result**: Recurring event details retrieved (shows next occurrence details)

## Business Rules

1. **Event ID Required**: Event ID is the only mandatory parameter
2. **Read-Only Operation**: This operation does not modify the event
3. **Current User Context**: Retrieves event from current user's accessible calendars
4. **Recurring Events**: Returns details of the next occurrence for recurring events
5. **Deleted Events**: Cannot retrieve events that have been deleted
6. **Private Events**: May not retrieve private events from other users' calendars
7. **Permissions**: Requires read access to the calendar containing the event

## Limitations

1. **Deleted Events**: Cannot retrieve events that have been deleted
2. **Private Events**: May not access private events in other users' calendars without proper sharing
3. **Recurring Events**: Returns next occurrence details, not the entire series
4. **Attachments**: Event Details entity doesn't include attachments (use Get Attachments)
5. **Response Status**: Doesn't include individual attendee response statuses
6. **Calendar Scope**: Only retrieves from calendars accessible to the current user
7. **Historical Events**: Can retrieve past events if they still exist in the calendar

## Best Practices

### 1. Verify Event ID Format
Ensure the event ID is correctly copied without extra spaces or characters.

**Benefit**: Avoids validation errors and failed requests.

### 2. Use Before Updates or Cancellations
Always retrieve event details before updating or cancelling to verify you have the correct event.

**Benefit**: Prevents accidental modifications to wrong events.

### 3. Handle Not Found Gracefully
Implement error handling for events that may have been deleted.

**Benefit**: Provides better user experience when events are not found.

### 4. Cache Event Details Appropriately
If you need event details multiple times, cache the response to reduce API calls.

**Caution**: Be aware that cached data may become stale if the event is updated.

### 5. Use for Validation
Use Get Event to validate that an event exists before performing operations on it.

**Benefit**: Ensures operations are performed on valid events.

## Common Use Cases

### 1. Display Event Information
```
Scenario: Show event details to user
Action:
  1. Get Event ID from user or previous operation
  2. Execute Get Event
  3. Display Event Details to user
Result: User sees complete event information
```

### 2. Verify Before Update
```
Scenario: Check current event details before updating
Action:
  1. Execute Get Event with Event ID
  2. Review current details
  3. Determine what needs to be updated
  4. Execute Update Event with changes
Result: Informed update with knowledge of current state
```

### 3. Validate Event Existence
```
Scenario: Check if an event still exists
Action:
  1. Execute Get Event with Event ID
  2. If successful, event exists
  3. If error "event not found", event was deleted
Result: Confirmation of event existence
```

### 4. Retrieve Event for Processing
```
Scenario: Get event details for workflow processing
Action:
  1. Execute Get Event with Event ID
  2. Extract needed information (attendees, time, location)
  3. Use in subsequent workflow steps
Result: Event data available for processing
```

## Related Catalog Requests

- [Search Events](pages/SearchEvents.md) - Find events by search terms to get event IDs
- [Get Next Upcoming Meetings](pages/GetNextUpcomingMeetings.md) - Get upcoming events with IDs
- [Update Event](pages/UpdateEvent.md) - Modify event after retrieving details
- [Cancel Event](pages/CancelEvent.md) - Cancel event after verifying details
- [Get Attachments](pages/GetAttachments.md) - Retrieve attachments for the event

## Technical Implementation

### Helper Class
- **Class**: GetEventHelper
- **Package**: app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.helper
- **Validation**: ValidationOrchestrator validates event ID
- **Service**: MeetingDao.getMeetingById()

### Validation Flow
1. Event ID validation (required, not empty)
2. Event existence validation (event must exist in calendar)
3. Permission validation (user must have read access)

### Telemetry Metrics
- GET_EVENT_SUCCESS: Successful event retrieval
- GET_EVENT_FAILURE: Failed event retrieval
- GET_EVENT_VALIDATION_ERROR: Validation failures
- GET_EVENT_NOT_FOUND: Event not found errors

## Troubleshooting

### Issue: "Event ID is required"

**Cause**: Event ID parameter not provided

**Solution**:
1. Provide a valid Event ID
2. Ensure Event ID is not null or empty
3. Check that the parameter is correctly passed

### Issue: "The provided event id does not exist"

**Cause**: Event ID doesn't exist in the calendar

**Solution**:
1. Verify Event ID is correct (check for typos)
2. Use Search Events to find the correct event ID
3. Check if event was deleted
4. Ensure you're looking in the correct calendar
5. Verify you have access to the event

### Issue: "Invalid event id"

**Cause**: Event ID format is incorrect

**Solution**:
1. Verify Event ID is correctly copied
2. Check for extra spaces or special characters
3. Ensure the entire event ID is included
4. Event IDs are case-sensitive - verify casing

### Issue: "Insufficient permissions to access event"

**Cause**: User lacks permission to read the event

**Solution**:
1. Verify user has Calendars.ReadWrite or Calendars.Read permission
2. Check Microsoft Entra ID app permissions
3. Ensure calendar sharing is configured for shared calendars
4. Verify the event is not private or restricted
5. Contact Office 365 administrator to grant permissions

### Issue: "Failed to get event"

**Cause**: System error during retrieval

**Solution**:
1. Verify network connectivity
2. Check Office 365 service health status
3. Re-validate the extension authentication
4. Wait a few minutes and retry
5. Check extension logs for detailed error information

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configure the extension
- [Authentication](pages/Authentication.md) - Understanding authentication modes
- [Creating Office 365 App](pages/CreatingOffice365App.md) - Microsoft Entra ID setup
- [Search Events](pages/SearchEvents.md) - Find events by search terms
- [Schedule Meeting](pages/ScheduleMeeting.md) - Create new events
- [Microsoft Graph Get Event API](https://learn.microsoft.com/en-us/graph/api/event-get) - Official API documentation

