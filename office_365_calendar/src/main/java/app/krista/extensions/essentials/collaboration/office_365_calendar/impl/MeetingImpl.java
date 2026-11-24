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

package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import com.microsoft.graph.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the {@link Meeting} interface that wraps a Microsoft Graph {@link Event} object.
 * Provides convenient access to event details such as ID, location, title, attendees, description,
 * start/end times, time zone, and organizer, mapping Microsoft Graph event data to the Meeting interface.
 */
public class MeetingImpl implements Meeting {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeetingImpl.class);
    private final Event event;

    public MeetingImpl(Event event) {
        this.event = event;
    }

    @Override
    public String getId() {
        return event.id;
    }

    @Override
    public String getLocation() {
        return event.location != null ? event.location.displayName : null;
    }

    @Override
    public String getTitle() {
        return event.subject;
    }

    @Override
    public List<String> getContacts() {
        List<String> contacts = new ArrayList<>();
        if (event.attendees != null) {
            event.attendees.forEach(attendee -> {
                if (attendee.emailAddress != null && attendee.emailAddress.address != null) {
                    contacts.add(attendee.emailAddress.address);
                } else {
                    LOGGER.info("Skipping attendee with no email address: name={}, eventId={}",
                            attendee.emailAddress != null ? attendee.emailAddress.name : "unknown", event.id);
                }
            });
        }
        return contacts;
    }

    @Override
    public String getDetails() {
        ItemBody body = event.body;
        if (body != null) {
            assert body.content != null;
            return body.content.replace("\n", "").replace("\r", "");
        } else {
            return null;
        }
    }

    @Override
    public Long getDateFrom() {
        if (event.start != null) {
            return getDateInMilliSecond(event.start);
        }
        return null;
    }

    @Override
    public Long getDateTo() {
        if (event.end != null) {
            return getDateInMilliSecond(event.end);
        }
        return null;
    }

    /**
     * Converts a {@link com.microsoft.graph.models.DateTimeTimeZone} object to its corresponding epoch milliseconds.
     *
     * @param dateToParse the DateTimeTimeZone object to convert
     * @return the epoch time in milliseconds representing the given date and time
     */
    private long getDateInMilliSecond(DateTimeTimeZone dateToParse) {
        try {
            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = sfd.parse(dateToParse.dateTime);
            return date.getTime();
        } catch (ParseException parseException) {
            throw new IllegalStateException("Date :" + dateToParse.dateTime + " can't be parsed. " + parseException.getMessage(), parseException.getCause());
        }
    }

    @Override
    public String getTimeZone() {
        return event.originalStartTimeZone != null ? event.originalStartTimeZone : null;
    }

    @Override
    public String getOrganizer() {
        if (event.organizer != null && event.organizer.emailAddress != null) {
            return event.organizer.emailAddress.address;
        }
        return null;
    }


}
