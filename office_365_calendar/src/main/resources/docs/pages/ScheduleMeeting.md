# Schedule Meeting

## Overview

Creates a new calendar event in Office 365 Calendar with comprehensive options including attendees, recurrence patterns, reminders, attachments, and Microsoft Teams integration. Supports delegation to schedule meetings on behalf of other users.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes - The extension validates all input parameters and prompts for corrections if validation fails

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Organizer Email | Email | No | Email address of the person on whose behalf the meeting is being scheduled. When provided, the meeting will be created in the specified organizer's calendar instead of the current user's calendar | organizer@company.com |
| Title | Text | No | Subject or title of the meeting | "Company Update" |
| Location | Text | No | Meeting location (physical or virtual) | "Conference Room A" |
| Date From | Date | Yes | Start date and time of the meeting | 5 Jun 2024 17:00 |
| Date To | Date | No | End date and time of the meeting | 5 Jun 2024 18:00 |
| Time Zone | Text | No | Time zone for the meeting | "Asia/Kolkata", "UTC" |
| Details | Rich Text | No | Meeting description or agenda | "Quarterly business review" |
| Required Attendee Email | Email | No | Email addresses of required attendees (comma-separated) | requiredattendee@email.com |
| Optional Attendee Email | Email | No | Email addresses of optional attendees (comma-separated) | optionalattendee@email.com |
| Is All Day | Yes/No | No | Sets the meeting for all day | False, True |
| Reminder | Pick One | No | Reminder time before the meeting | "Don't remind me", "5 min. before" |
| Recurrence | Pick One | No | Recurrence pattern | "Does Not Repeat", "Daily", "Weekly" |
| Recurrence Interval | Number | No | Time between two events (in days/months/years) | 1, 4, 8 |
| Days Of Week | String | No | Specific day of week for recurring events | "Monday", "Wednesday" |
| Recurrence Type | Pick One | No | Absolute (exact date) or Relative (day of week) | "Absolute Recurrence", "Relative Recurrence" |
| Day Of Month | Number | No | Specific day of month for monthly recurrence | 23, 9 |
| Repeat Index | Pick One | No | Which occurrence in monthly/yearly recurrence | "Second", "Last" |
| Repeat Day | Pick One | No | Specific day of week to repeat | "Wednesday", "Friday" |
| Repeat Month | Pick One | No | Month of year for yearly recurrence | "April", "December" |
| End Recurrence | Pick One | No | How the recurrence ends | "End Date", "After Occurrences" |
| End Date | Date | No | End date for recurring events | 25 Apr 2025 |
| Numbered Occurrences | Number | No | Number of occurrences before ending | 25, 56 |
| Attachments | File | No | Files to attach to the meeting | Any file type |
| Is Teams Meeting | Yes/No | No | Create as Microsoft Teams meeting | True, False |

### Parameter Details

#### Organizer Email
- **Purpose**: Enables delegation - schedule meetings on behalf of another user
- **Permissions Required**: The current user must have appropriate delegation permissions to the organizer's calendar
- **Behavior**: When provided, the meeting is created in the organizer's calendar; when omitted, created in current user's calendar

#### Date From
- **Required**: Yes - This is the only mandatory parameter
- **Format**: Date and time in the configured format
- **Validation**: Must be a valid future date/time

#### Time Zone
- **Supported Values**: All standard IANA time zone identifiers
- **Default**: If not provided, uses the user's default time zone
- **Reference**: See [supported time zones](https://learn.microsoft.com/en-us/graph/api/resources/datetimetimezone)

#### Recurrence Parameters
- **Recurrence**: Controls if and how the event repeats
- **Recurrence Interval**: Frequency of recurrence (e.g., every 2 weeks)
- **Days Of Week**: For weekly recurrence (e.g., "Monday,Wednesday,Friday")
- **Recurrence Type**: 
  - Absolute: Specific date (e.g., 15th of every month)
  - Relative: Day position (e.g., second Monday of every month)

#### Is Teams Meeting
- **Purpose**: Creates the event as a Microsoft Teams online meeting
- **Behavior**: When set to True, adds Teams meeting link and dial-in information
- **Requirements**: Requires Microsoft Teams license

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Complete details of the created event including Event ID, attendees, and meeting information |

### Event Details Entity Structure

| Field Name | Type | Description |
|------------|------|-------------|
| Event ID | Text | Unique identifier for the created event |
| Title | Text | Meeting subject |
| Start Time | Date | Meeting start date/time |
| End Time | Date | Meeting end date/time |
| Location | Text | Meeting location |
| Organizer | Email | Meeting organizer email |
| Attendees | List | Required and optional attendees |
| Status | Text | Event status |
| Teams Meeting Link | Text | Microsoft Teams meeting URL (if applicable) |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Date From is null or invalid | "Date From is required and must be a valid date" | Provide a valid start date and time |
| Date From is in the past | "Start date must be in the future" | Provide a future date |
| Date To is before Date From | "End date must be after start date" | Ensure Date To is later than Date From |
| Invalid email format | "Invalid email address format" | Provide valid email addresses |
| Invalid time zone | "Invalid time zone specified" | Use a valid IANA time zone identifier |
| Invalid recurrence interval | "Recurrence interval must be a positive number" | Provide a number greater than 0 |
| Recurrence parameters incomplete | "Missing required recurrence parameters" | Provide all necessary recurrence fields |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters

**Common Scenarios**:
- Date From is not provided
- Invalid date format
- Invalid email addresses
- Invalid time zone

**Error Messages**:
- "Date From is required"
- "Invalid date format"
- "Invalid email address"
- "Invalid time zone"

**Resolution**:
1. Verify all required parameters are provided
2. Check date formats match expected format
3. Validate email addresses
4. Use valid time zone identifiers

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic validation failures

**Common Scenarios**:
- Date From is in the past
- Date To is before Date From
- Invalid recurrence configuration
- Conflicting parameters

**Error Messages**:
- "Start date must be in the future"
- "End date must be after start date"
- "Invalid recurrence pattern"

**Resolution**:
1. Ensure dates are logical and in the future
2. Verify recurrence parameters are complete and valid
3. Check for parameter conflicts

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API errors or connection issues

**Common Scenarios**:
- Network connectivity problems
- Office 365 service unavailable
- Authentication token expired
- Insufficient permissions
- Calendar quota exceeded

**Error Messages**:
- "Failed to create meeting"
- "Connection to Office 365 failed"
- "Authentication required"
- "Insufficient permissions"

**Resolution**:
1. Verify network connectivity
2. Check extension configuration and authentication
3. Re-validate the extension if authentication expired
4. Ensure user has calendar write permissions
5. Check Office 365 service health status

### Authorization Errors

**Cause**: Insufficient permissions or delegation issues

**Common Scenarios**:
- User lacks permission to create events
- Delegation permissions not granted for Organizer Email
- Calendar sharing not configured

**Resolution**:
1. Verify user has Calendars.ReadWrite permission
2. For delegation, ensure proper calendar sharing is configured
3. Check Microsoft Entra ID app permissions

## Usage Examples

### Example 1: Simple Meeting

**Scenario**: Schedule a basic one-hour meeting

**Input**:
```
Title: "Team Standup"
Date From: "10 Oct 2024 09:00"
Date To: "10 Oct 2024 10:00"
Required Attendee Email: "team@company.com"
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...AAA=",
    "Title": "Team Standup",
    "Start Time": "2024-10-10T09:00:00",
    "End Time": "2024-10-10T10:00:00",
    "Organizer": "currentuser@company.com",
    "Attendees": ["team@company.com"],
    "Status": "Confirmed"
  }
}
```

**Result**: Meeting created successfully

### Example 2: Recurring Weekly Meeting

**Scenario**: Schedule a weekly team meeting every Monday

**Input**:
```
Title: "Weekly Team Meeting"
Date From: "14 Oct 2024 14:00"
Date To: "14 Oct 2024 15:00"
Time Zone: "America/New_York"
Required Attendee Email: "team@company.com"
Recurrence: "Weekly"
Recurrence Interval: 1
Days Of Week: "Monday"
End Recurrence: "End Date"
End Date: "31 Dec 2024"
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...BBB=",
    "Title": "Weekly Team Meeting",
    "Start Time": "2024-10-14T14:00:00",
    "End Time": "2024-10-14T15:00:00",
    "Recurrence": "Weekly on Monday",
    "Recurrence End": "2024-12-31",
    "Status": "Confirmed"
  }
}
```

**Result**: Recurring meeting created for every Monday until Dec 31, 2024

### Example 3: Teams Meeting with Delegation

**Scenario**: Schedule a Teams meeting on behalf of a manager

**Input**:
```
Organizer Email: "manager@company.com"
Title: "Client Presentation"
Date From: "15 Oct 2024 10:00"
Date To: "15 Oct 2024 11:30"
Required Attendee Email: "client@external.com,team@company.com"
Details: "Q4 product roadmap presentation"
Is Teams Meeting: True
Reminder: "15 min. before"
```

**Output**:
```json
{
  "Event Details": {
    "Event ID": "AAMkAGI1...CCC=",
    "Title": "Client Presentation",
    "Start Time": "2024-10-15T10:00:00",
    "End Time": "2024-10-15T11:30:00",
    "Organizer": "manager@company.com",
    "Teams Meeting Link": "https://teams.microsoft.com/l/meetup-join/...",
    "Status": "Confirmed"
  }
}
```

**Result**: Teams meeting created in manager's calendar with meeting link

## Business Rules

1. **Date From Required**: Date From is the only mandatory parameter for creating a meeting
2. **Default Duration**: If Date To is not provided, meeting defaults to 30 minutes
3. **Time Zone Handling**: If not specified, uses user's default time zone
4. **Delegation**: Organizer Email requires proper calendar delegation permissions
5. **Recurrence Validation**: All recurrence parameters must be provided together for recurring events
6. **Teams Integration**: Is Teams Meeting requires Microsoft Teams license
7. **Attachment Limits**: Subject to Office 365 attachment size limits (typically 150 MB)
8. **Attendee Limits**: Maximum 500 attendees per meeting (Office 365 limit)

## Limitations

1. **Attachment Size**: Maximum 150 MB total attachment size per meeting
2. **Attendee Count**: Maximum 500 attendees per meeting
3. **Recurrence Limit**: Recurring events limited to 999 occurrences
4. **Time Zone**: Must use valid IANA time zone identifiers
5. **Delegation**: Requires proper calendar sharing and permissions
6. **Teams Meetings**: Requires Microsoft Teams license and proper permissions

## Best Practices

### 1. Always Provide End Time
Specify Date To to avoid default 30-minute duration.

**Recommendation**: Explicitly set meeting duration for clarity.

### 2. Use Time Zones for Cross-Region Meetings
Specify time zone when scheduling with participants in different regions.

**Benefit**: Ensures correct time display for all participants.

### 3. Validate Delegation Permissions
Before using Organizer Email, verify delegation permissions are configured.

**Check**: Test with a simple meeting first.

### 4. Provide Clear Meeting Details
Include comprehensive information in the Details field.

**Include**: Agenda, dial-in information, preparation materials.

### 5. Set Appropriate Reminders
Configure reminders based on meeting importance.

**Recommendation**: 15 minutes for important meetings, 5 minutes for routine meetings.

## Common Use Cases

### 1. Daily Standup Meeting
```
Scenario: Schedule recurring daily standup
Action:
  1. Set Title: "Daily Standup"
  2. Set Date From: Next business day 9:00 AM
  3. Set Date To: 9:15 AM (15 minutes)
  4. Set Recurrence: "Daily"
  5. Set Days Of Week: "Monday,Tuesday,Wednesday,Thursday,Friday"
  6. Add team members as Required Attendees
Result: Daily 15-minute standup created for weekdays
```

### 2. Client Meeting with Teams
```
Scenario: Schedule external client meeting with Teams link
Action:
  1. Set Title with client name
  2. Set appropriate date/time
  3. Add client email as Required Attendee
  4. Set Is Teams Meeting: True
  5. Add meeting agenda in Details
  6. Set Reminder: "15 min. before"
Result: Teams meeting created with automatic meeting link
```

### 3. Executive Meeting (Delegation)
```
Scenario: Schedule meeting on behalf of executive
Action:
  1. Set Organizer Email: executive's email
  2. Configure meeting details
  3. Add attendees
  4. Set appropriate reminder
Result: Meeting appears in executive's calendar as organizer
```

## Related Catalog Requests

- [Update Event](pages/UpdateEvent.md) - Modify scheduled meetings
- [Cancel Event](pages/CancelEvent.md) - Cancel meetings
- [Add Attendees](pages/AddAttendees.md) - Add attendees to existing meetings
- [Get Free Slots](pages/GetFreeSlots.md) - Find available time before scheduling
- [Get Schedules](pages/GetSchedules.md) - Check attendee availability

## Technical Implementation

### Helper Class
- **Class**: ScheduleMeetingHelper
- **Package**: app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.helper
- **Validation**: ValidationOrchestrator validates all input parameters
- **Service**: MeetingService.scheduleMeeting()

### Validation Flow
1. Date validation (Date From required, future date, logical date range)
2. Email validation (format and syntax)
3. Time zone validation (valid IANA identifier)
4. Recurrence validation (complete parameter set)
5. Attachment validation (size and format)

### Telemetry Metrics
- SCHEDULE_MEETING_SUCCESS: Successful meeting creation
- SCHEDULE_MEETING_FAILURE: Failed meeting creation
- SCHEDULE_MEETING_VALIDATION_ERROR: Validation failures
- SCHEDULE_MEETING_DELEGATION_USED: Delegation feature used

## Troubleshooting

### Issue: "Date From is required"

**Cause**: Start date not provided

**Solution**:
1. Provide a valid Date From value
2. Ensure date format matches expected format
3. Verify date is in the future

### Issue: "Insufficient permissions for delegation"

**Cause**: User lacks permission to schedule on behalf of organizer

**Solution**:
1. Verify calendar sharing is configured in Office 365
2. Ensure current user has "Editor" or "Delegate" permission
3. Check Microsoft Entra ID app has Calendars.ReadWrite.Shared permission
4. Contact Office 365 administrator to grant permissions

### Issue: "Teams meeting creation failed"

**Cause**: Microsoft Teams not available or insufficient permissions

**Solution**:
1. Verify user has Microsoft Teams license
2. Check OnlineMeetings.ReadWrite permission is granted
3. Ensure Teams is enabled for the organization
4. Try creating without Teams integration first

### Issue: "Invalid recurrence pattern"

**Cause**: Incomplete or conflicting recurrence parameters

**Solution**:
1. Ensure all required recurrence parameters are provided
2. Verify Recurrence Interval is a positive number
3. Check Days Of Week format (comma-separated)
4. Ensure End Recurrence has corresponding End Date or Numbered Occurrences

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configure the extension
- [Authentication](pages/Authentication.md) - Understanding authentication modes
- [Creating Office 365 App](pages/CreatingOffice365App.md) - Microsoft Entra ID setup
- [Microsoft Graph Calendar API](https://learn.microsoft.com/en-us/graph/api/resources/calendar) - Official API documentation

