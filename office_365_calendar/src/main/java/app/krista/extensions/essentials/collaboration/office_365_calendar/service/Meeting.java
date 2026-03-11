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

package app.krista.extensions.essentials.collaboration.office_365_calendar.service;

import java.util.List;

public interface Meeting {
    /**
     * Returns the meeting id of the Office 365 calendar event
     *
     * @return meetings id of the meeting
     */
    String getId();

    /**
     * Returns the display name of the location of Office 365 calendar event
     *
     * @return location of the meeting
     */
    String getLocation();

    /**
     * Returns the title of the Office 365 calendar event
     *
     * @return the title of the meeting
     */
    String getTitle();

    /**
     * Returns the list of attendees of the Office 365 calendar event
     *
     * @return {@link List<String>} list of meeting attendees
     */
    List<String> getContacts();

    /**
     * Returns the description of the Office 365 calendar event
     *
     * @return the description of the meeting
     */
    String getDetails();

    /**
     * Returns the start date and time of the Office 365 calendar event
     *
     * @return start date and time of the meeting
     */
    Long getDateFrom();

    /**
     * Returns the end date and time of the Office 365 calendar event
     *
     * @return the end date and time of the meeting
     */
    Long getDateTo();

    /**
     * Returns the timezone of the Office 365 calendar event
     *
     * @return location of the meeting
     */
    String getTimeZone();

    /**
     * Returns the organizer of the Office 365 calendar event
     *
     * @return meeting organizer's email
     */
    String getOrganizer();


}
