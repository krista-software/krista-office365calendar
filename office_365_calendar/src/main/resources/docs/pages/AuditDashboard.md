# Audit Dashboard

## Overview

The Office 365 Calendar Extension includes an **Audit Dashboard** (available from version 4.0.4) that provides visibility into catalog request activity, authentication events, and credential health. The dashboard is powered by the Microsoft Auth SDK and records every catalog request execution with its outcome.

## Accessing the Audit Dashboard

The Audit Dashboard is accessible as a custom tab labeled **"Calendar Audit"** in the Krista extension configuration screen, alongside the Authentication and Documentation tabs.

## Dashboard Components

### Summary Cards

The top section displays aggregate metrics:

| Card | Description |
|------|-------------|
| **Total Requests** | Total number of catalog requests recorded |
| **Failures** | Count of failed catalog requests |
| **Success Rate** | Percentage of successful catalog requests |
| **P95 Latency** | 95th percentile response time across all catalog requests |
| **Last Request** | Timestamp of the most recent catalog request |

### Status Indicator

A colored status dot in the header indicates overall health:

- **Green (Healthy)** — No authentication issues detected
- **Red (Issues)** — One or more authentication problems detected (token expiry, invalid credentials, password changes)

The status considers the following authentication events:

| Event | Meaning |
|-------|---------|
| TOKEN_REFRESHED | Token was successfully refreshed (clears prior token errors) |
| TOKEN_EXPIRY_DETECTED | Access token expired and could not be refreshed |
| INVALID_TOKEN_DETECTED | Permissions were revoked or token is invalid |
| CLIENT_SECRET_EXPIRY_DETECTED | Client secret has expired |
| INVALID_CLIENT_SECRET_DETECTED | Client secret is incorrect |
| AUTH_CONFIG_FAILURE | Authentication configuration error |
| PASSWORD_CHANGE_DETECTED | User changed their Office 365 password |

### Alerts

When authentication issues are detected, the dashboard displays alert banners with actionable guidance (e.g., "Re-authenticate in the Authentication tab" or "Rotate your client secret in Azure AD").

### Audit Event Log

The main table displays individual audit records with the following columns:

| Column | Description |
|--------|-------------|
| **Catalog** | Name of the catalog request (e.g., "Schedule Meeting", "Get All Unaccepted Events") |
| **Event** | Event type (CATALOG_REQUEST_TRIGGERED, CATALOG_REQUEST_SUCCESS, CATALOG_REQUEST_FAILURE) |
| **Status** | Outcome — triggered, success, or failure |
| **Duration** | Execution time in milliseconds (populated on completion events) |
| **Reason** | Error message or exception details (for failures) |
| **Triggered By** | The Krista user who initiated the request |
| **Timestamp** | When the event occurred |

### Credential and Token Health

Click the key icon to open the **Credential & Token Health** panel, which displays:

| Field | Description |
|-------|-------------|
| **Auth Type** | Current authentication mode (Public, Private, or Service to Service) |
| **Email** | Configured user email |
| **Tenant ID** | Masked tenant ID |
| **Client ID** | Masked client ID |
| **Client Secret Expiry** | Expiration date of the public client secret |
| **Has Refresh Token** | Whether a valid refresh token exists for the configured user |
| **Auth Event Counts** | Counts and last-seen timestamps for each authentication event type |

## Audited Catalog Requests

All 19 catalog requests in the Calendar extension are audited:

| # | Catalog Request | Area |
|---|----------------|------|
| 1 | Cancel Event | Meetings |
| 2 | Get Next Upcoming Meetings | Meetings |
| 3 | Decline Invitation | Meetings |
| 4 | Tentative Invitation | Meetings |
| 5 | Forward Event | Meetings |
| 6 | Schedule Meeting | Meetings |
| 7 | Add Attendees | Meetings |
| 8 | Update Event | Meetings |
| 9 | Accept Invitation | Meetings |
| 10 | Remove Attendee | Meetings |
| 11 | Get Free Slots | Meetings |
| 12 | Get Schedules | Meetings |
| 13 | Get Event | Meetings |
| 14 | Get Events For A Given User | Meetings |
| 15 | Get Attachments For A Given User | Meetings |
| 16 | Get Attachments | Meetings |
| 17 | Check Public Auth Expiry | Meetings |
| 18 | Get All Unaccepted Events | Meetings |
| 19 | Search Events | Meetings |
| 20 | Health Check | Setup |

### Audit Recording Flow

For each catalog request execution:

1. **TRIGGERED** — Recorded immediately when the request is received
2. The catalog request executes against the Microsoft Graph API
3. **SUCCESS** or **FAILURE** — Recorded based on the outcome
   - If the response indicates failure (`ExtensionResponse.Result.FAILURE`), a FAILURE event is recorded
   - If a `MustAuthorizeException` is thrown (token expired/missing), an auth error is recorded
   - If any other exception occurs, a FAILURE event is recorded with the exception details
   - Otherwise, a SUCCESS event is recorded

Duration is automatically calculated by pairing the TRIGGERED event with its corresponding SUCCESS/FAILURE event.

## Filtering and Search

The dashboard supports filtering audit records by:

- **Date range** — Filter by From/To dates
- **Event type** — Filter by specific event types (e.g., only failures)
- **Status** — Filter by outcome (triggered, success, failure)
- **Search** — Free-text search across catalog name, reason, and user

Click **Apply** to apply filters, or **Reset** to clear all filters.

## Exporting Data

Click **CSV** to export the current filtered view as a CSV file. The export filename is prefixed with `calendar-audit` followed by a timestamp.

## Clearing Records

Click **Clear** to remove audit records. This action clears all records in the current time range and recalculates the summary metrics. This is useful for clearing stale data after a configuration change or during testing.

## Data Storage

Audit records are persisted in the Krista Key-Value store under the extension's invoker ID. The audit log maintains a rolling buffer of up to 500 records. When the limit is reached, the oldest records are automatically removed.

## Automatic Auth Event Recording

In addition to catalog request auditing, the SDK automatically records authentication-related events when they occur during Graph API calls:

| Event | When Recorded |
|-------|---------------|
| TOKEN_REFRESHED | A Graph API access token was successfully acquired or refreshed |
| TOKEN_EXPIRY_DETECTED | A `TokenExpiredException` was caught during a Graph API call |
| PASSWORD_CHANGE_DETECTED | A `PasswordChangedException` was caught |
| INVALID_CLIENT_SECRET_DETECTED | An `InvalidClientSecretException` was caught |
| INVALID_TOKEN_DETECTED | A `PermissionsRevokedException` was caught |
| AUTH_CONFIG_FAILURE | Any other `MsAuthException` was caught |

These events appear in the audit log alongside catalog request events and contribute to the health status indicator.

## See Also

- [Authentication](pages/Authentication.md) - Authentication configuration and OAuth flows
- [Extension Configuration](pages/ExtensionConfiguration.md) - Extension setup
- [Error Messages](pages/ErrorMessages.md) - Error reference and troubleshooting
