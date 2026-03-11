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

import java.util.List;

import app.krista.extension.impl.anno.*;



@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "2ab5b937-0a80-4bb4-894a-cd72104f864a")
@Entity(name = "Event Details", id = "localDomainEntity_b13aad88-786e-440a-8131-84351124a9cf", primaryKey = "Event Id", supportStore = false)
public class EventDetails {

    @Field(name = "Event Id", type = "Text", required = false)
    public String eventId;

    @Field(name = "Location", type = "Text", required = false)
    public String location;

    @Searchable
    @Field(name = "Title", type = "Text", required = false)
    public String title;

    @Field.Desc(name = "Contacts", type = "[ Text ]", required = false)
    public List<String> contacts;

    @Field(name = "Details", type = "RichText", required = false)
    public String details;

    @Field(name = "Date From", type = "Date", required = false, attributes = @Attribute(name = "includeTimeOfDay", value = "true"))
    public Long dateFrom;

    @Field(name = "Date To", type = "Date", required = false, attributes = @Attribute(name = "includeTimeOfDay", value = "true"))
    public Long dateTo;

    @Field.Text(name = "Time Zone", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String timeZone;

    @Field.Text(name = "Organizer", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String organizer;
}
