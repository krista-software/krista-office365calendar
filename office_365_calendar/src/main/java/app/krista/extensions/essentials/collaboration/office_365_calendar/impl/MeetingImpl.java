package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.ItemBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeetingImpl implements Meeting {

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
                if (attendee.emailAddress != null) {
                    contacts.add(attendee.emailAddress.address);
                }
            });
        }
        return contacts;
    }

    @Override
    public String getDetails() {
        ItemBody body = event.body;
        return body != null ? body.content.replace("\n", "").replace("\r", "") : null;
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

    private long getDateInMilliSecond(DateTimeTimeZone dateToParse) {
        try {
            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = sfd.parse(dateToParse.dateTime);
            return date.getTime();
        } catch (ParseException parseException) {
            throw new IllegalStateException("Date :" + dateToParse.dateTime + " can't be parsed. " + parseException.getMessage(), parseException.getCause());
        }
    }
}
