# Release Notes - Office 365 Calendar Extension

## Version 2.0.24
**Release Date**: January, 2026
**Global Catalog Version**: GC-2026.1.5
### What's New
**[KE-2893](https://antbrains.atlassian.net/browse/KE-2893)** Enhance O365 Calendar extension to return NULL when EventID is not found (instead of error)
## Version 2.0.23
**Release Date**: October 19, 2025

### What's New
- Fixed reauthentication issues (KE-2729)
- Fixed incorrect callback URL format in documentation
- Corrected broken image links in setup guides
- Updated Microsoft Entra ID configuration instructions

### Action Required
If you previously configured redirect URIs, update them in your Microsoft Entra ID app registration:
- **Private Authentication**: `https://extension.qa-in.eng.krista.app/extension/api/{ROUTING_ID}/rest/calendar/callback`
- **Public Authentication**: `https://extension.qa-in.eng.krista.app/extension/api/rest/v3/oauth/callback`

---

## Version 2.0.18
**Release Date**: October 13, 2025

### What's New
- Complete extension rewrite with improved architecture
- Enhanced error handling with clear, actionable messages
- Comprehensive documentation with visual setup guides
- Automatic retry mechanism for failed requests

### New Features
**Calendar Management**
- Schedule, update, and cancel meetings
- Search and retrieve events
- Manage meeting invitations (accept, decline, tentative)
- Forward events to other users
- Add and remove attendees

**Query Operations**
- Get upcoming meetings
- Get unaccepted events
- Get user's calendar events
- Check availability and free time slots
- Retrieve meeting attachments

### Limitations
- Maximum 500 attendees per meeting
- Maximum 150 MB attachment size per meeting
- Recurring events limited to 999 occurrences

### Action Required
This is a major release. Reconfigure the extension and test all workflows before production use.

---

## Version 2.0.17
**Release Date**: October 1, 2025

### What's New
- Improved documentation clarity and organization
- Optimized documentation images
- Enhanced authentication flow handling

### Bug Fixes
- Fixed authentication tab change handling (KE-1478)
- Fixed documentation links and references

---

## Version 2.0.16
**Release Date**: September 30, 2025

### What's New
- Improved authentication flow for Public and Private modes
- Enhanced delegation support for shared calendars
- Better error messages for authentication and permission issues

### Bug Fixes
- Fixed token management and refresh mechanisms
- Improved handling of delegation permissions
- Enhanced validation for organizer and delegate email parameters

### Required Permissions
Ensure your Microsoft Entra ID app has these delegated permissions:
- Calendars.ReadWrite
- Calendars.ReadWrite.Shared
- offline_access
- openid
- profile

---

## Previous Versions

For information about versions 2.0.15 and earlier, contact your Krista administrator.
