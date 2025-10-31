# Error Messages Reference

This guide provides a comprehensive reference for all error messages in the Office 365 Calendar Extension, along with their meanings and recommended actions.

## Table of Contents

- [Validation Errors](#validation-errors)
  - [Email Validation](#email-validation)
  - [Event ID Validation](#event-id-validation)
  - [Date Range Validation](#date-range-validation)
  - [Number Validation](#number-validation)
  - [Time Zone Validation](#time-zone-validation)
  - [Recurrence Validation](#recurrence-validation)
  - [Attachment Validation](#attachment-validation)
  - [Meeting Duration Validation](#meeting-duration-validation)
- [Calendar Operation Errors](#calendar-operation-errors)
- [Authentication Errors](#authentication-errors)
- [Connection Errors](#connection-errors)

---

## Validation Errors

### Email Validation

#### Error: "Invalid email address: '[email]'. Please use a valid email format (e.g., user@company.com)."

**What it means:** The email address you entered is not in a valid format.

**How to fix:**
- Ensure the email follows the format: `username@domain.com`
- Check for typos or missing characters (@ symbol, domain extension)
- Example of valid email: `john.doe@company.com`

---

### Event ID Validation

#### Error: "Event ID '[id]' not found. Please verify the Event ID and try again. Tip: Use 'Get Meetings' to view available events and their IDs."

**What it means:** The system couldn't find an event with the specified ID.

**How to fix:**
- Verify the Event ID is correct
- Use the 'Get Meetings' or 'Get Next Upcoming Meetings' request to view available events
- Check that you have permission to access this event
- Ensure the event hasn't been deleted

---

### Date Range Validation

#### Error: "Invalid date range: The 'From' date must be before or equal to the 'To' date. Please select a valid date range."

**What it means:** The start date you selected is after the end date.

**How to fix:**
- Ensure the 'From' date is before or equal to the 'To' date
- Double-check your date selections
- Example: From: Jan 1, 2024 → To: Jan 31, 2024 ✓

#### Error: "The 'To' date must be after or equal to the 'From' date. Would you like to adjust your date range?"

**What it means:** The end date is before the start date.

**How to fix:**
- Select an end date that comes after the start date
- Verify both dates are correct

---

### Number Validation

#### Error: "Invalid number: '[number]'. Please enter a whole number between 1 and 1000."

**What it means:** The number you entered is either not a whole number, is negative, or exceeds the maximum limit.

**How to fix:**
- Enter a positive whole number (no decimals)
- Ensure the number is between 1 and 1000
- Examples: 5, 10, 100 ✓ | 0, -5, 1.5, 1001 ✗

---

### Time Zone Validation

#### Error: "Invalid time zone: '[timezone]'. Please use a standard time zone identifier (e.g., 'America/New_York', 'Europe/London', 'UTC')."

**What it means:** The time zone you entered is not recognized.

**How to fix:**
- Use standard IANA time zone identifiers
- Common examples:
  - `America/New_York` (Eastern Time)
  - `America/Los_Angeles` (Pacific Time)
  - `Europe/London` (GMT/BST)
  - `Asia/Tokyo` (Japan Standard Time)
  - `UTC` (Coordinated Universal Time)
- Avoid abbreviations like "EST" or "PST"

---

### Recurrence Validation

#### Error: "Invalid recurrence interval: '[interval]'. Please enter a whole number between 1 and 365."

**What it means:** The recurrence interval is invalid.

**How to fix:**
- Enter a whole number between 1 and 365
- Examples:
  - `1` = every occurrence
  - `2` = every other occurrence
  - `7` = every 7th occurrence (weekly if daily recurrence)

#### Error: "Invalid number of occurrences: '[number]'. Please enter a whole number between 1 and 999."

**What it means:** The number of occurrences for a recurring meeting is invalid.

**How to fix:**
- Enter a whole number between 1 and 999
- This determines how many times the meeting will repeat

#### Error: "Invalid days of the week: '[days]'. Please select valid day names without duplicates (e.g., Monday, Wednesday, Friday)."

**What it means:** The days of the week contain invalid names or duplicates.

**How to fix:**
- Use valid day names: Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
- Don't include duplicate days
- Example: `Monday, Wednesday, Friday` ✓ | `Mon, Wed, Fri` ✗

---

### Attachment Validation

#### Error: "Invalid file attachments. Please use supported file formats: Documents (PDF, DOC, DOCX, RTF, TXT), Spreadsheets (XLS, XLSX, CSV), Presentations (PPT, PPTX), Images (PNG, JPG, JPEG, GIF, BMP, TIF, TIFF), Archives (ZIP, RAR)."

**What it means:** One or more files have unsupported formats.

**How to fix:**
- Use only supported file formats:
  - **Documents:** PDF, DOC, DOCX, RTF, TXT
  - **Spreadsheets:** XLS, XLSX, CSV
  - **Presentations:** PPT, PPTX
  - **Images:** PNG, JPG, JPEG, GIF, BMP, TIF, TIFF
  - **Archives:** ZIP, RAR
- Convert unsupported files to a supported format

---

### Meeting Duration Validation

#### Error: "Invalid duration format: '[duration]'. Please use format like '30M', '1H', or '2H30M'. Duration must be between 15 minutes and 8 hours."

**What it means:** The meeting duration format is incorrect or out of range.

**How to fix:**
- Use the correct format:
  - `30M` = 30 minutes
  - `1H` = 1 hour
  - `2H30M` = 2 hours and 30 minutes
- Duration must be between 15 minutes and 8 hours
- Examples: `15M`, `45M`, `1H`, `1H30M`, `8H` ✓

---

## Calendar Operation Errors

### Meeting Operations

#### Error: "Unable to create the meeting. Please check your calendar permissions and try again."

**What it means:** The system couldn't create the meeting.

**How to fix:**
- Verify you have permission to create meetings in the calendar
- Check that all required fields are filled in correctly
- Ensure the meeting time doesn't conflict with calendar restrictions
- Try again after a few moments

#### Error: "Unable to update the calendar event. Please verify you have permission to modify this event and try again."

**What it means:** The system couldn't update the event.

**How to fix:**
- Verify you're the organizer or have edit permissions
- Check that the event still exists
- Ensure you're not trying to modify a past event that's locked
- Verify all updated fields contain valid data

#### Error: "Unable to cancel the event. Please verify you have permission to cancel this event and try again."

**What it means:** The system couldn't cancel the event.

**How to fix:**
- Verify you're the organizer or have delete permissions
- Check that the event hasn't already been cancelled
- Ensure the event still exists

### Attendee Operations

#### Error: "Unable to add attendees to the meeting. Please check the email addresses and try again."

**What it means:** The system couldn't add the specified attendees.

**How to fix:**
- Verify all email addresses are valid and correctly formatted
- Check that the attendees exist in your organization
- Ensure you have permission to add attendees to this meeting
- Try adding attendees one at a time to identify problematic addresses

#### Error: "Unable to remove the attendee from the meeting. Please verify you have permission to modify this event."

**What it means:** The system couldn't remove the attendee.

**How to fix:**
- Verify you're the organizer or have edit permissions
- Check that the attendee is actually part of the meeting
- Ensure the meeting still exists

### Invitation Operations

#### Error: "Unable to accept the meeting invitation. Please try again or accept directly in your calendar."

**What it means:** The system couldn't accept the invitation.

**How to fix:**
- Verify the invitation still exists
- Check that you haven't already accepted it
- Try accepting directly in Outlook/Office 365
- Refresh and try again

#### Error: "Unable to decline the meeting invitation. Please try again or decline directly in your calendar."

**What it means:** The system couldn't decline the invitation.

**How to fix:**
- Verify the invitation still exists
- Check that you haven't already declined it
- Try declining directly in Outlook/Office 365
- Refresh and try again

### Retrieval Operations

#### Error: "Unable to retrieve calendar events. Please check your permissions and try again."

**What it means:** The system couldn't fetch calendar events.

**How to fix:**
- Verify you have read permissions for the calendar
- Check your internet connection
- Ensure you're authenticated properly
- Try again after a few moments

#### Error: "Unable to retrieve the event details. Please verify the event ID and try again."

**What it means:** The system couldn't fetch the specific event.

**How to fix:**
- Verify the Event ID is correct
- Check that the event still exists
- Ensure you have permission to view this event
- Use 'Get Meetings' to find the correct Event ID

#### Error: "Unable to retrieve event attachments. Please check your permissions and try again."

**What it means:** The system couldn't fetch attachments for the event.

**How to fix:**
- Verify you have permission to view attachments
- Check that the event has attachments
- Ensure the attachments haven't been removed
- Try viewing the event directly in Outlook

#### Error: "No events found matching your search criteria. Try using different keywords or expanding your search terms."

**What it means:** The search didn't find any matching events.

**How to fix:**
- Try different search keywords
- Broaden your search terms
- Check your date range
- Verify you're searching in the correct calendar

---

## Authentication Errors

#### Error: "Your session has expired. Please sign in again to continue."

**What it means:** Your authentication token has expired.

**How to fix:**
- Click the authorization link to sign in again
- Complete the authentication process
- Save your changes after re-authenticating

#### Error: "Authentication failed. Please verify your credentials and try again."

**What it means:** The system couldn't authenticate you.

**How to fix:**
- Verify your username and password are correct
- Check that your account is active
- Ensure you have the necessary permissions
- Contact your administrator if the issue persists

#### Error: "Authentication failed: No valid access token was provided. Please sign in again."

**What it means:** The authentication token is missing or invalid.

**How to fix:**
- Sign in again through the authorization flow
- Ensure cookies are enabled in your browser
- Clear browser cache and try again

---

## Connection Errors

#### Error: "Unable to connect to Office 365 using the provided credentials. Please verify your client ID and secret are correct."

**What it means:** The system couldn't establish a connection to Office 365.

**How to fix:**
- Verify the Client ID is correct
- Verify the Client Secret is correct and hasn't expired
- Check that the app registration is active in Azure
- Ensure the redirect URI is configured correctly
- Contact your administrator to verify the app configuration

#### Error: "Unable to access your Office 365 account. Please verify your authentication and try again."

**What it means:** The system couldn't access your Office 365 account.

**How to fix:**
- Re-authenticate using the authorization flow
- Verify your account is active
- Check that you have the necessary permissions
- Ensure your organization allows access to this application

---

## General Errors

#### Error: "The request contains invalid parameters. Please check your input and try again."

**What it means:** One or more parameters in your request are invalid.

**How to fix:**
- Review all input fields for errors
- Ensure required fields are filled in
- Verify data formats are correct
- Check for any validation messages on specific fields

#### Error: "The following required fields were not provided: [fields]. Please provide these values to continue."

**What it means:** Required information is missing.

**How to fix:**
- Fill in all required fields listed in the error message
- Ensure no fields are left blank
- Verify all mandatory information is provided

---

## Need More Help?

If you continue to experience issues after following these troubleshooting steps:

1. Check the [Release Notes](releaseNotes.md) for known issues
2. Review the specific operation documentation (e.g., [Schedule Meeting](ScheduleMeeting.md), [Get Events](GetEventsForAGivenUser.md))
3. Contact your system administrator
4. Reach out to Krista support with:
   - The exact error message
   - Steps to reproduce the issue
   - Screenshots if applicable

