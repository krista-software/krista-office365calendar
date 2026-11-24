/*
 * Office365Calendar Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.office_365_calendar;

import app.krista.sdk.msauth.config.AuthFlow;
import app.krista.sdk.msauth.extension.annotations.*;

// ── Module 1: Auth SDK Core ──
@MsAuthExtension(
        extensionType = "calendar",
        callbackPathPrefix = "/calendar",
        supportedFlows = {AuthFlow.AUTH_CODE_PUBLIC, AuthFlow.AUTH_CODE_PRIVATE, AuthFlow.CLIENT_CREDENTIALS_SECRET},
        defaultScopes = {"openid", "offline_access", "Calendars.ReadWrite",
                "Calendars.ReadWrite.Shared", "User.Read"},
        // ── Module 2: Auth Tab UI ──
        headerTitle = "Office 365 Calendar Authentication",
        headerSubtitle = "Configure how Krista connects to your Microsoft calendar",
        showScopePanel = true,
        supportsOwnCredentials = true,
        enableAuditDashboard = true,
        setupGuideUrl = "../../../static/docs/#/pages/overview"
)
// ── Module 2: Scopes ──
@AuthScope(value = "Calendars.ReadWrite", description = "Read and write calendars", required = true,
        catalogRequests = {"Schedule Meeting", "Cancel Event", "Update Event",
                "Get Event", "Get Upcoming Meetings", "Search Events",
                "Accept Invitation", "Decline Invitation", "Tentatively Accept Invitation",
                "Get All Unaccepted Events", "Get Events For Given User",
                "Add Attendees", "Remove Attendee", "Forward Event"})
@AuthScope(value = "Calendars.ReadWrite.Shared", description = "Read and write shared calendars",
        catalogRequests = {"Get Events For Given User (shared)", "Schedule Meeting (shared)"})
@AuthScope(value = "User.Read", description = "Read user profile",
        catalogRequests = {"Test Connection", "Health Check"})
// ── Module 3: Audit Dashboard ──
@AuditConfig(
        dashboardTitle = "Calendar Audit Dashboard",
        enableCredentialHealth = true,
        enableCharts = true,
        csvExportPrefix = "calendar-audit"
)
@AuditEventGroup(name = "Meeting Events", events = {
        @AuditEvent(type = "MEETING_SCHEDULED", description = "New meeting scheduled via Graph API"),
        @AuditEvent(type = "MEETING_CANCELLED", description = "Meeting cancelled"),
        @AuditEvent(type = "MEETING_UPDATED", description = "Meeting details updated"),
        @AuditEvent(type = "INVITATION_ACCEPTED", description = "Meeting invitation accepted"),
        @AuditEvent(type = "INVITATION_DECLINED", description = "Meeting invitation declined")
})
public class Office365CalendarAuthConfig {
}
