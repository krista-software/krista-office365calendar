# Office 365 Calendar Extension

## Overview

The Krista Office 365 Calendar Extension enables seamless integration with Microsoft Office 365 Calendar, allowing you to manage meetings, events, and calendar operations directly from your Krista applications. This extension provides comprehensive calendar management capabilities including scheduling meetings, managing attendees, handling invitations, and querying calendar availability.

## Key Features

- **Meeting Management**: Schedule, update, and cancel calendar events
- **Invitation Handling**: Accept, decline, or tentatively respond to meeting invitations
- **Attendee Management**: Add and remove attendees from scheduled events
- **Calendar Queries**: Retrieve upcoming events, search for specific meetings, and check availability
- **Free/Busy Scheduling**: Find available time slots and view schedules for multiple attendees
- **Attachment Support**: Add and retrieve attachments for calendar events
- **Delegation Support**: Perform operations on behalf of other users with proper permissions
- **Teams Integration**: Create Microsoft Teams meetings directly from calendar events
- **Recurrence Patterns**: Support for daily, weekly, monthly, and yearly recurring events

## Quick Start Guide

### 1. Prerequisites

- Microsoft Office 365 account with calendar access
- Microsoft Entra ID (formerly Azure AD) tenant
- Application registration in Microsoft Entra ID
- Appropriate calendar permissions

### 2. Setup Steps

1. **Create Microsoft Entra ID App Registration**
   - Follow the guide in [Creating Office 365 App](pages/CreatingOffice365App)
   - Obtain Client ID, Tenant ID, and Client Secret

2. **Configure the Extension**
   - Add the extension to your Krista application
   - Configure authentication parameters
   - See [Extension Configuration](pages/ExtensionConfiguration) for details

3. **Authenticate**
   - Choose between Service Account or User Account authentication
   - Complete OAuth 2.0 authorization flow
   - See [Authentication](pages/Authentication.md) for details

4. **Start Using Catalog Requests**
   - Browse available catalog requests below
   - Each request has detailed documentation with examples

## Documentation Structure

### Getting Started

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configure the extension with your Office 365 credentials
- [Authentication](pages/Authentication.md) - Understanding OAuth 2.0 authentication modes
- [Creating Office 365 App](pages/CreatingOffice365App.md) - Step-by-step guide for Microsoft Entra ID app registration

### Event Management

- [Schedule Meeting](pages/ScheduleMeeting.md) - Create new calendar events with attendees and recurrence
- [Update Event](pages/UpdateEvent.md) - Modify existing calendar events
- [Cancel Event](pages/CancelEvent.md) - Cancel scheduled events
- [Get Event](pages/GetEvent.md) - Retrieve details for a specific event
- [Search Events](pages/SearchEvents.md) - Find events matching search criteria

### Invitation Management

- [Accept Invitation](pages/AcceptInvitation.md) - Accept meeting invitations
- [Decline Invitation](pages/DeclineInvitation.md) - Decline meeting invitations
- [Tentative Invitation](pages/TentativeInvitation.md) - Mark invitation as tentative
- [Forward Event](pages/ForwardEvent.md) - Forward events to additional attendees

### Attendee Management

- [Add Attendees](pages/AddAttendees.md) - Add attendees to existing events
- [Remove Attendee](pages/RemoveAttendee.md) - Remove attendees from events

### Query Operations

- [Get Next Upcoming Meetings](pages/GetNextUpcomingMeetings.md) - Retrieve upcoming events
- [Get All Unaccepted Events](pages/GetAllUnacceptedEvents.md) - Find events awaiting response
- [Get Events For A Given User](pages/GetEventsForAGivenUser.md) - Query events for specific users
- [Get Schedules](pages/GetSchedules.md) - View scheduled meetings for attendees
- [Get Free Slots](pages/GetFreeSlots.md) - Find available time slots for scheduling

### Attachment Operations

- [Get Attachments](pages/GetAttachments.md) - Retrieve attachments for an event
- [Get Attachments For A Given User](pages/GetAttachmentsForAGivenUser.md) - Get attachments for user-specific events

### Additional Resources

- [Release Notes](pages/releaseNotes) - Version history and updates
- [Field Details](pages/details.md) - Detailed field descriptions and supported values

## Support & Resources

### Extension Information

- **Version**: 2.0.16
- **Developer**: Vaibhav Choudhary
- **Krista Service APIs Java**: 1.0.115
- **Global Catalog Version**: GC-2025.10.1

### Microsoft Resources

- [Microsoft Graph Calendar API Documentation](https://learn.microsoft.com/en-us/graph/api/resources/calendar)
- [Microsoft Entra ID Documentation](https://learn.microsoft.com/en-us/azure/active-directory/)
- [Office 365 Calendar Documentation](https://support.microsoft.com/en-us/office/calendar)

### Getting Help

For support with this extension:

1. Review the documentation for your specific use case
2. Check the troubleshooting sections in individual catalog request pages
3. Verify your Microsoft Entra ID app registration and permissions
4. Contact your Krista administrator for assistance

## Important Notes

### Delegation Support

The extension supports delegation functionality for certain operations:

- **Schedule Meeting**: Use "Organizer Email" parameter to schedule on behalf of another user
- **Update Event**: Use "Organizer Email" parameter to update events in another user's calendar
- **Cancel Event**: Use "Delegate Email" parameter to cancel events from another user's calendar

Delegation requires proper Office 365 permissions and calendar sharing settings.

### Time Zones

The extension supports all standard time zones. For a complete list of supported time zones, see the [Field Details](pages/details.md) page.

### Recurrence Patterns

The extension supports complex recurrence patterns including:

- Daily, weekly, monthly, and yearly recurrence
- Custom recurrence intervals
- Specific days of the week
- Absolute and relative monthly recurrence
- End dates or numbered occurrences

### Microsoft Teams Integration

Events can be created as Microsoft Teams meetings by setting the "Is Teams Meeting" parameter to true when scheduling meetings.

## Security Considerations

- **Client Secret Protection**: Store your Client Secret securely and never expose it in client-side code
- **Token Management**: Access tokens expire after 1 hour; the extension handles automatic refresh
- **Permissions**: Grant only the minimum required permissions for your use case
- **Delegation**: Ensure proper authorization before performing operations on behalf of other users

## What's New in Version 2.0.16

- Fixed issue with changing authentication from Public to Private (KE-2559)
- Added support for delegated email in error handling flow (KE-2456)
- Enhanced error handling and validation
- Improved Microsoft Entra ID integration

---

**Ready to get started?** Begin with [Extension Configuration](pages/ExtensionConfiguration) to set up your Office 365 Calendar integration.
