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

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.krista.extension.impl.anno.*;


@Searchable
@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "2ab5b937-0a80-4bb4-894a-cd72104f864a")
@Entity(name = "Event Details", id = "localDomainEntity_b13aad88-786e-440a-8131-84351124a9cf", primaryKey = "Event Id", supportStore = true,
        description = "Represents a Microsoft Office 365 calendar event. Event Id is the unique Graph identifier. Title is the event subject. Date From and Date To are the start and end of the event in epoch millis. Contacts holds attendee email addresses. Organizer is the email of the meeting host.")
public class EventDetails {

    @Searchable
    @Field(name = "Event Id", type = "Text", required = false,
            description = "Unique identifier assigned by Microsoft Graph for this calendar event. Use this ID to retrieve, update, cancel, or perform other operations on the event. Example: 'AAMkAGI1AAAt9AHjAAA='.")
    public String eventId;

    @Searchable
    @Field(name = "Location", type = "Text", required = false,
            description = "Physical or virtual location where the meeting takes place. Can be a room name, street address, building number, or a virtual meeting link. Example: 'Conference Room B' or '123 Main St, Suite 400'.")
    public String location;

    @Searchable
    @ToString
    @Field(name = "Title", type = "Text", required = false,
            description = "Subject line or title of the calendar event as displayed in the calendar view. Used as the primary identifier when searching for events. Example: 'Weekly Team Standup' or 'Q3 Planning Review'.")
    public String title;

    @Field.Desc(name = "Contacts", type = "[ Text ]", required = false,
            description = "List of attendee email addresses associated with this calendar event. Each entry is a valid email address of an invited participant. Example: ['john@company.com', 'jane@company.com'].")
    public List<String> contacts;

    @Searchable
    @Field(name = "Details", type = "RichText", required = false,
            description = "Rich-text body content of the calendar event containing the meeting description, agenda, notes, or any additional information provided by the organizer. Supports HTML formatting.")
    public String details;

    @Searchable
    @Field(name = "Date From", type = "Date", required = false, attributes = @Attribute(name = "includeTimeOfDay", value = "true"),
            description = "Start date and time of the calendar event as a Unix epoch timestamp in milliseconds. Includes time-of-day precision for scheduling. Example: 1700000000000 represents a specific date and time.")
    public Long dateFrom;

    @Searchable
    @Field(name = "Date To", type = "Date", required = false, attributes = @Attribute(name = "includeTimeOfDay", value = "true"),
            description = "End date and time of the calendar event as a Unix epoch timestamp in milliseconds. Must be equal to or later than 'Date From'. Includes time-of-day precision for scheduling.")
    public Long dateTo;

    @Field.Text(name = "Time Zone", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")},
            description = "IANA time zone identifier for the event's start and end times. Determines how date/time values are interpreted and displayed. Example: 'America/New_York', 'Europe/London', 'Asia/Tokyo', or 'UTC'.")
    public String timeZone;

    @Searchable
    @Field.Text(name = "Organizer", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")},
            description = "Email address or display name of the person who created and organized the calendar event. This is the user who sent the meeting invitation to attendees.")
    public String organizer;

    public Map<String, Object> toFields() {
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Event Id", eventId);
        fieldMap.put("Title", title);
        fieldMap.put("Location", location);
        fieldMap.put("Contacts", contacts);
        fieldMap.put("Details", details);
        fieldMap.put("Date From", dateFrom);
        fieldMap.put("Date To", dateTo);
        fieldMap.put("Time Zone", timeZone);
        fieldMap.put("Organizer", organizer);
        return fieldMap;
    }
}
