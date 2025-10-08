# Update Event

## Overview

Modifies an existing calendar event with new details such as title, location, date/time, attendees, recurrence settings, and other meeting properties. All attendees will be notified of the changes. Supports delegation to update events on behalf of other users.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes - The extension validates all input parameters and prompts for corrections if validation fails

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Organizer Email | Email | No | Email address of the person whose calendar contains the event. When provided, updates the event in the specified organizer's calendar | organizer@company.com |
| Event ID | Text | Yes | Unique identifier of the event to update | AAMkAGI1...AAA= |
| Title | Text | No | New subject or title of the meeting | "Updated Company Meeting" |
| Location | Text | No | New meeting location | "Conference Room B" |
| Date From | Date | No | New start date and time | 6 Jun 2024 17:00 |
| Date To | Date | No | New end date and time | 6 Jun 2024 18:30 |
| Time Zone | Text | No | New time zone for the meeting | "America/New_York" |
| Details | Rich Text | No | New meeting description or agenda | "Updated agenda items" |
| Required Attendee Email | Email | No | New list of required attendees (replaces existing) | requiredattendee@email.com |
| Optional Attendee Email | Email | No | New list of optional attendees (replaces existing) | optionalattendee@email.com |
| Is All Day | Yes/No | No | Change to all-day event | True, False |
| Reminder | Pick One | No | New reminder time | "15 min. before" |
| Recurrence | Pick One | No | New recurrence pattern | "Weekly", "Monthly" |
| Recurrence Interval | Number | No | New interval between occurrences | 2, 3 |
| Days Of Week | String | No | New days of week for recurrence | "Monday,Wednesday" |
| Recurrence Type | Pick One | No | Absolute or Relative recurrence | "Relative Recurrence" |
| Day Of Month | Number | No | New day of month for recurrence | 15 |
| Repeat Index | Pick One | No | Which occurrence in the month | "First", "Third" |
| Repeat Day | Pick One | No | Day of week to repeat | "Monday" |
| Repeat Month | Pick One | No | Month for yearly recurrence | "June" |
| End Recurrence | Pick One | No | How recurrence ends | "End Date", "After Occurrences" |
| End Date | Date | No | New end date for recurrence | 31 Dec 2024 |
| Numbered Occurrences | Number | No | New number of occurrences | 30 |
| Is Teams Meeting | Yes/No | No | Convert to/from Teams meeting | True, False |

### Parameter Details

#### Event ID
- **Required**: Yes - This is the only mandatory parameter
- **Format**: Unique event identifier from Microsoft Graph
- **Source**: Obtained from Get Event, Schedule Meeting, or other event operations
- **Validation**: Must be a valid event ID that exists in the calendar

#### Organizer Email
- **Purpose**: Enables delegation - update events in another user's calendar
- **Permissions Required**: Current user must have appropriate delegation permissions
- **Behavior**: When provided, updates event in organizer's calendar; when omitted, updates in current user's calendar

#### Attendee Lists
- **Behavior**: Providing new attendee lists **replaces** existing attendees (not additive)
- **To Add Attendees**: Use [Add Attendees](pages/AddAttendees.md) catalog request instead
- **To Remove Attendees**: Use [Remove Attendee](pages/RemoveAttendee.md) catalog request instead
- **Format**: Comma-separated email addresses

#### Recurrence Changes
- **Important**: Changing recurrence affects all future occurrences
- **Single Instance**: To update a single occurrence, use a different event ID for that instance
- **Validation**: All recurrence parameters must be valid together

#### Is Teams Meeting
- **Adding Teams**: Set to True to add Teams meeting link
- **Removing Teams**: Set to False to remove Teams meeting link
- **Existing Teams Meeting**: Changing other properties preserves Teams link unless explicitly set to False

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Complete details of the updated event |

### Event Details Entity Structure

| Field Name | Type | Description |
|------------|------|-------------|
| Event ID | Text | Unique identifier of the event |
| Title | Text | Updated meeting subject |
| Date From | Date | Updated start date/time |
| Date To | Date | Updated end date/time |
| Location | Text | Updated location |
| Time Zone | Text | Updated time zone |
| Organizer | Email | Event organizer email |
| Contacts | List | Updated attendee list |
| Details | Rich Text | Updated meeting description |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Event ID is null or empty | "Event ID is required" | Provide a valid event ID |
| Event ID not found | "Event not found" | Verify the event exists and ID is correct |
| Invalid date range | "End date must be after start date" | Ensure Date To is later than Date From |
| Invalid email format | "Invalid email address format" | Provide valid email addresses |
| Invalid time zone | "Invalid time zone specified" | Use a valid IANA time zone identifier |
| Invalid recurrence parameters | "Invalid recurrence pattern" | Provide complete and valid recurrence parameters |
| Insufficient permissions | "Insufficient permissions to update event" | Verify calendar access permissions |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters

**Common Scenarios**:
- Event ID not provided
- Invalid event ID format
- Invalid email addresses
- Invalid date format
- Invalid time zone

**Error Messages**:
- "Event ID is required"
- "Invalid event ID"
- "Invalid email address"
- "Invalid date format"

**Resolution**:
1. Verify Event ID is provided and correct
2. Check date formats match expected format
3. Validate email addresses
4. Use valid time zone identifiers

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic validation failures

**Common Scenarios**:
- Date To is before Date From
- Invalid recurrence configuration
- Conflicting parameters
- Event not found

**Error Messages**:
- "Event not found"
- "End date must be after start date"
- "Invalid recurrence pattern"

**Resolution**:
1. Verify the event exists
2. Ensure dates are logical
3. Verify recurrence parameters are complete and valid
4. Check for parameter conflicts

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API errors or connection issues

**Common Scenarios**:
- Network connectivity problems
- Office 365 service unavailable
- Authentication token expired
- Insufficient permissions
- Event locked by another process

**Error Messages**:
- "Error occurred in update event"
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
- User lacks permission to update events
- Delegation permissions not granted for Organizer Email
- User is not the organizer and doesn't have edit permissions

**Resolution**:
1. Verify user has Calendars.ReadWrite permission
2. For delegation, ensure proper calendar sharing is configured
3. Check Microsoft Entra ID app permissions
4. Verify user has edit rights to the event

## Usage Examples

### Example 1: Update Meeting Time

**Scenario**: Reschedule a meeting to a different time

**Input**:
```
Event ID: "AAMkAGI1...AAA="
Date From: "11 Oct 2024 10:00"
Date To: "11 Oct 2024 11:00"
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...AAA=",
    "Title": "Team Standup",
    "Date From": "2024-10-11T10:00:00",
    "Date To": "2024-10-11T11:00:00",
    "Location": "Conference Room A",
    "Organizer": "currentuser@company.com"
  }
}
```

**Result**: Meeting time updated successfully, attendees notified

### Example 2: Update Meeting Details and Location

**Scenario**: Change meeting location and add agenda

**Input**:
```
Event ID: "AAMkAGI1...BBB="
Location: "Virtual - Teams"
Details: "Agenda: Q4 Planning, Budget Review, Team Updates"
Is Teams Meeting: True
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...BBB=",
    "Title": "Quarterly Planning",
    "Location": "Virtual - Teams",
    "Details": "Agenda: Q4 Planning, Budget Review, Team Updates",
    "Teams Meeting Link": "https://teams.microsoft.com/l/meetup-join/..."
  }
}
```

**Result**: Meeting updated with new location, agenda, and Teams link added

### Example 3: Update Recurring Meeting with Delegation

**Scenario**: Update a recurring meeting in manager's calendar

**Input**:
```
Organizer Email: "manager@company.com"
Event ID: "AAMkAGI1...CCC="
Title: "Updated Weekly Sync"
Recurrence: "Weekly"
Days Of Week: "Tuesday,Thursday"
Recurrence Interval: 1
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...CCC=",
    "Title": "Updated Weekly Sync",
    "Organizer": "manager@company.com",
    "Recurrence": "Weekly on Tuesday, Thursday"
  }
}
```

**Result**: Recurring meeting updated in manager's calendar, now occurs on Tuesday and Thursday

## Business Rules

1. **Event ID Required**: Event ID is mandatory to identify which event to update
2. **Partial Updates**: Only provided parameters are updated; omitted parameters remain unchanged
3. **Attendee Replacement**: Providing attendee lists replaces all existing attendees
4. **Recurrence Updates**: Changing recurrence affects all future occurrences of the series
5. **Notification**: All attendees receive update notifications automatically
6. **Delegation**: Organizer Email requires proper calendar delegation permissions
7. **Teams Meeting**: Can add or remove Teams meeting link by changing Is Teams Meeting parameter
8. **Time Zone**: Changing time zone affects how times are displayed to attendees

## Limitations

1. **Past Events**: Cannot update events that have already ended (may be restricted by Office 365)
2. **Recurring Series**: Updating a series affects all future occurrences
3. **Single Occurrence**: To update one occurrence, must use that occurrence's specific event ID
4. **Attendee Limit**: Maximum 500 attendees per meeting (Office 365 limit)
5. **Attachment Updates**: Cannot update attachments through this request (use separate attachment operations)
6. **Organizer Change**: Cannot change the event organizer
7. **Delegation**: Requires proper calendar sharing and permissions

## Best Practices

### 1. Verify Event Exists First
Use [Get Event](pages/GetEvent.md) to verify the event exists before updating.

**Benefit**: Avoids errors and provides current event details.

### 2. Update Only Necessary Fields
Only provide parameters that need to be changed.

**Benefit**: Preserves existing settings and reduces risk of unintended changes.

### 3. Use Add/Remove Attendees for Attendee Changes
For adding or removing specific attendees, use dedicated catalog requests.

**Recommendation**: Use [Add Attendees](pages/AddAttendees.md) or [Remove Attendee](pages/RemoveAttendee.md) instead of replacing entire attendee list.

### 4. Notify Attendees of Major Changes
For significant changes (time, location), consider adding a note in Details field.

**Benefit**: Provides context for the update.

### 5. Test Delegation First
Before updating events in another user's calendar, verify delegation permissions.

**Check**: Test with a simple update first.

## Common Use Cases

### 1. Reschedule Meeting
```
Scenario: Move meeting to different day/time
Action:
  1. Get Event ID from original meeting
  2. Set new Date From and Date To
  3. Execute Update Event
Result: Meeting rescheduled, attendees notified
```

### 2. Add Teams Link to Existing Meeting
```
Scenario: Convert in-person meeting to Teams meeting
Action:
  1. Get Event ID
  2. Set Is Teams Meeting: True
  3. Optionally update Location: "Microsoft Teams"
Result: Teams link added, attendees can join virtually
```

### 3. Update Recurring Meeting Pattern
```
Scenario: Change weekly meeting from Monday to Wednesday
Action:
  1. Get Event ID of recurring series
  2. Set Days Of Week: "Wednesday"
  3. Execute Update Event
Result: All future occurrences moved to Wednesday
```

## Related Catalog Requests

- [Schedule Meeting](pages/ScheduleMeeting.md) - Create new calendar events
- [Cancel Event](pages/CancelEvent.md) - Cancel meetings
- [Get Event](pages/GetEvent.md) - Retrieve event details before updating
- [Add Attendees](pages/AddAttendees.md) - Add attendees without replacing existing ones
- [Remove Attendee](pages/RemoveAttendee.md) - Remove specific attendees

## Technical Implementation

### Helper Class
- **Class**: UpdateEventHelper
- **Package**: app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.helper
- **Validation**: ValidationOrchestrator validates all input parameters
- **Service**: MeetingDao.updateMeeting()

### Validation Flow
1. Event ID validation (required, format, existence)
2. Date validation (logical date range)
3. Email validation (format and syntax)
4. Time zone validation (valid IANA identifier)
5. Recurrence validation (complete parameter set)

### Telemetry Metrics
- UPDATE_EVENT_SUCCESS: Successful event update
- UPDATE_EVENT_FAILURE: Failed event update
- UPDATE_EVENT_VALIDATION_ERROR: Validation failures
- UPDATE_EVENT_DELEGATION_USED: Delegation feature used

## Troubleshooting

### Issue: "Event ID is required"

**Cause**: Event ID not provided

**Solution**:
1. Provide a valid Event ID
2. Use Get Event or other operations to obtain Event ID
3. Verify Event ID is not null or empty

### Issue: "Event not found"

**Cause**: Event ID doesn't exist or user doesn't have access

**Solution**:
1. Verify Event ID is correct
2. Check if event was deleted
3. Ensure user has access to the calendar
4. For delegation, verify Organizer Email is correct

### Issue: "Insufficient permissions to update event"

**Cause**: User lacks permission to update the event

**Solution**:
1. Verify user is the organizer or has edit permissions
2. For delegation, ensure calendar sharing is configured
3. Check Microsoft Entra ID app has Calendars.ReadWrite.Shared permission
4. Verify user has "Editor" or "Delegate" permission on the calendar

### Issue: "Attendees not updated"

**Cause**: Attendee list parameter not provided or empty

**Solution**:
1. Provide complete attendee lists to replace existing attendees
2. Use Add Attendees or Remove Attendee for incremental changes
3. Verify email addresses are valid
4. Check that attendee parameters are not null

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configure the extension
- [Authentication](pages/Authentication.md) - Understanding authentication modes
- [Creating Office 365 App](pages/CreatingOffice365App.md) - Microsoft Entra ID setup
- [Schedule Meeting](pages/ScheduleMeeting.md) - Create new events
- [Microsoft Graph Update Event API](https://learn.microsoft.com/en-us/graph/api/event-update) - Official API documentation

