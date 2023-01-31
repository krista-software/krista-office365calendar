package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity.EventDetails;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.model.base.File;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "7ecfd34e-b2f0-44a7-97f8-71b9a9e517f7")
public final class MeetingsArea {

    private final MeetingDaoImpl meetingDao;

    @Inject
    public MeetingsArea(MeetingDaoImpl meetingDao) {
        this.meetingDao = meetingDao;
    }

    @CatalogRequest(
            id = "localDomainRequest_44d8fe1f-3f2d-438d-bb9f-bb486b9a9e56",
            name = "Cancel Event",
            description = "Cancel event for given event id",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails cancelEvent(
            @Field(name = "Event Id", type = "Text") String eventId) {
        return CatalogTypes.fromEvent(meetingDao.cancelMeeting(eventId));
    }

    @CatalogRequest(
            id = "localDomainRequest_771024d0-02e5-4163-869e-8f8860a10b62",
            name = "Get Next Upcoming Meetings",
            description = "Get list of next upcoming meetings",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "[ Entity(Event Details) ]", required = false)
    public List<EventDetails> getNextUpcomingMeetings(
            @Field(name = "Number of meeting", type = "Number") Double numberOfMeeting) {
        return meetingDao.getMeetings(numberOfMeeting.intValue()).stream().map(CatalogTypes::fromEvent).collect(Collectors.toList());
    }

    @CatalogRequest(
            id = "localDomainRequest_9de59f5f-b77d-42e3-84da-63f587f65358",
            name = "Decline Invitation",
            description = "Decline invitation",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails declineInvitation(
            @Field(name = "Event Id", type = "Text") String eventId) {
        return CatalogTypes.fromEvent(meetingDao.declineMeetingInvitation(eventId));
    }

    @CatalogRequest(
            id = "localDomainRequest_5bf7458f-953f-40c4-b75c-17b1f76a08b1",
            name = "Tentative Invitation",
            description = "Tentative invitation",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails tentativeInvitation(
            @Field(name = "Event Id", type = "Text") String eventId) {
        return CatalogTypes.fromEvent(meetingDao.tentativeMeetingInvitation(eventId));
    }

    @CatalogRequest(
            id = "localDomainRequest_f2007cf4-d127-4a30-af0c-794b28f41de8",
            name = "Forward Event",
            description = "Forward Event",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "IsForwarded", type = "Switch", required = false)
    public Boolean forwardEvent(
            @Field(name = "Event Id", type = "Text") String eventId,
            @Field.Desc(name = "Attendee Email", type = "[ Email ]") List<String> attendeeEmail) throws IOException {
        try {
            meetingDao.forwardMeeting(eventId, attendeeEmail);
            return true;
        } catch (IOException cause) {
            throw new IOException("Failed to forward event " + cause.getMessage());
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_f9cf27ee-ffe7-4b65-8e0a-ce0afa25f5dc",
            name = "Schedule Meeting",
            description = "Schedule meeting with given details",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails scheduleMeeting(
            @Field(name = "Title", type = "Text", required = false) String title,
            @Field(name = "Location", type = "Text", required = false) String location,
            @Field(name = "Date From", type = "Date", attributes = @Attribute(name = "includeTimeOfDay", value = "true")) Long dateFrom,
            @Field(name = "Date To", type = "Date", attributes = @Attribute(name = "includeTimeOfDay", value = "true"), required = false) Long dateTo,
            @Field.Desc(name = "Required Attendee Email", type = "[ Email ]", required = false) List<String> requiredAttendeeEmail,
            @Field.Desc(name = "Optional Attendee Email", type = "[ Email ]", required = false) List<String> optionalAttendeeEmail,
            @Field(name = "Is All Day", type = "Switch", required = false) Boolean isAllDay,
            @Field(name = "Time Zone", type = "Text", required = false) String timeZone,
            @Field.Desc(name = "Reminder", type = "PickOne(None|Don't remind me|At event start|At time of event|5 minutes before|15 minutes before|30 minutes before|1 hour before|2 hours before|4 hours before|12 hours before|1 day before|1 week before)", required = false) String reminder,
            @Field.Desc(name = "Recurrence", type = "PickOne(Never|Does Not Repeat|Daily|Weekly|Monthly|Yearly)", required = false) String recurrence,
            @Field(name = "Recurrence Interval", type = "Number", required = false) Double recurrenceInterval,
            @Field.Desc(name = "Days Of Week", type = "[ Text ]", required = false) List<String> daysOfWeek,
            @Field.Desc(name = "Recurrence Type", type = "PickOne(Absolute recurrence|Relative recurrence)", required = false) String recurrenceType,
            @Field(name = "Day Of Month", type = "Number", required = false) Double dayOfMonth,
            @Field.Desc(name = "Repeat Index", type = "PickOne(First|Second|Third|Fourth|Last)", required = false) String repeatIndex,
            @Field.Desc(name = "Repeat Day", type = "PickOne(Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday)", required = false) String repeatDay,
            @Field.Desc(name = "Repeat Month", type = "PickOne(January|February|March|April|May|June|July|August|September|October|November|December)", required = false) String repeatMonth,
            @Field.Desc(name = "End Recurrence", type = "PickOne(End date|Numbered occurrences|No end)", required = false) String endRecurrence,
            @Field(name = "End Date", type = "Date", required = false) Long endDate,
            @Field(name = "Numbered Occurrences", type = "Number", required = false) Double numberedOccurrences,
            @Field(name = "Attachments", type = "File", required = false) List<File> attachments,
            @Field(name = "Details", type = "RichText", required = false) String details,
            @Field(name = "Is Teams Meeting", type = "Switch", required = false) Boolean isTeamsMeeting) {
        return CatalogTypes.fromEvent(meetingDao.addMeeting(title, location, dateFrom, dateTo, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, timeZone, reminder, recurrence, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, CatalogTypes.toAttachment(attachments), details, isTeamsMeeting));
    }

    @CatalogRequest(
            id = "localDomainRequest_e0f748a2-17df-4e29-84e0-a1d0d06934d9",
            name = "Get All Unaccepted Events",
            description = "Get list of all unaccepted events",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "[ Entity(Event Details) ]", required = false)
    public List<EventDetails> getAllUnacceptedEvents() {
        return meetingDao.getMeetings().stream().map(CatalogTypes::fromEvent).collect(Collectors.toList());
    }

    @CatalogRequest(
            id = "localDomainRequest_2e73b843-ace4-4b80-bfab-4f466706e5c5",
            name = "Add Attendees",
            description = "Add attendees for the given event",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails addAttendees(
            @Field(name = "Event Id", type = "Text") String eventId,
            @Field.Desc(name = "Attendee Email", type = "[ Email ]") List<String> requiredAttendeeEmail,
            @Field.Desc(name = "Optional Attendee Email", type = "[ Email ]", required = false) List<String> optionalAttendeeEmail) {
        return CatalogTypes.fromEvent(meetingDao.addAttendees(eventId, requiredAttendeeEmail, optionalAttendeeEmail));
    }

    @CatalogRequest(
            id = "localDomainRequest_ff16001f-6709-44a8-9f00-5de09287e70e",
            name = "Update Event",
            description = "Update Event",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails updateEvent(
            @Field(name = "Event Id", type = "Text") String eventId,
            @Field(name = "Title", type = "Text", required = false) String title,
            @Field(name = "Location", type = "Text", required = false) String location,
            @Field(name = "Date From", type = "Date", required = false) Long dateFrom,
            @Field(name = "Date to", type = "Date", required = false) Long dateTo,
            @Field(name = "Details", type = "RichText", required = false) String details,
            @Field(name = "Time Zone", type = "Text", required = false) String timeZone,
            @Field.Desc(name = "Required Attendee Email", type = "[ Email ]", required = false) List<String> requiredAttendeeEmail,
            @Field.Desc(name = "Optional Attendee Email", type = "[ Email ]", required = false) List<String> optionalAttendeeEmail,
            @Field(name = "Is All Day", type = "Switch", required = false) Boolean isAllDay,
            @Field.Desc(name = "Reminder", type = "PickOne(None|At event start|5 minutes before|15 minutes before|30 minutes before|1 hour before|2 hours before|4 hours before|1 day before|1 week before)", required = false) String reminder,
            @Field.Desc(name = "Recurrence", type = "PickOne(Does Not Repeat|Daily|Weekly|Monthly|Yearly)", required = false) String recurrence,
            @Field(name = "Recurrence Interval", type = "Number", required = false) Double recurrenceInterval,
            @Field.Desc(name = "Days Of Week", type = "[ Text ]", required = false) List<String> daysOfWeek,
            @Field.Desc(name = "Recurrence Type", type = "PickOne(Absolute recurrence|Relative recurrence)", required = false) String recurrenceType,
            @Field(name = "Day Of Month", type = "Number", required = false) Double dayOfMonth,
            @Field.Desc(name = "Repeat Index", type = "PickOne(First|Second|Third|Fourth|Last)", required = false) String repeatIndex,
            @Field.Desc(name = "Repeat Day", type = "PickOne(Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday)", required = false) String repeatDay,
            @Field.Desc(name = "Repeat Month", type = "PickOne(January|February|March|April|May|June|July|August|September|October|November|December)", required = false) String repeatMonth,
            @Field.Desc(name = "End Recurrence", type = "PickOne(End date|Numbered occurrences|No end)", required = false) String endRecurrence,
            @Field(name = "End Date", type = "Date", required = false) Long endDate,
            @Field(name = "Numbered Occurrences", type = "Number", required = false) Double numberedOccurrences,
            @Field(name = "Is Teams Meeting", type = "Switch", required = false) Boolean isTeamsMeeting) {

        return CatalogTypes.fromEvent(meetingDao.updateMeeting(eventId, title, details, location, dateFrom, dateTo, timeZone, requiredAttendeeEmail, optionalAttendeeEmail, isAllDay, recurrence, reminder, recurrenceInterval, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, endRecurrence, endDate, numberedOccurrences, isTeamsMeeting));
    }

    @CatalogRequest(
            id = "localDomainRequest_d564897b-ad17-4289-af2c-bd79425c5341",
            name = "Accept Invitation",
            description = "Accept invitation for the given event id",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails acceptInvitation(
            @Field(name = "Event Id", type = "Text") String eventId) {
        return CatalogTypes.fromEvent(meetingDao.acceptMeetingInvitation(eventId));
    }

    @CatalogRequest(
            id = "localDomainRequest_653a694e-2327-41d5-8a8c-48b30b07dc89",
            name = "Remove Attendee",
            description = "Remove attendee from a given event id",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails removeAttendee(
            @Field(name = "Event Id", type = "Text") String eventId,
            @Field.Desc(name = "Attendee Email", type = "[ Email ]") List<String> attendeeEmail) {
        return CatalogTypes.fromEvent(meetingDao.removeAttendees(eventId, attendeeEmail));
    }

    @CatalogRequest(
            id = "localDomainRequest_a732314b-6699-48ae-9a77-3a2091d4d9f8",
            name = "Get Free Slots",
            description = "Get list of meeting suggestions",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "List of Free Slots", type = "[ { Time Slot: DateTimeRange, Attendee Availability: [ { Attendee: Email, Availability: Text } ], Confidence: Percentage, Organizer Availability: Text, Suggestion Reason: Text } ]", required = false)
    public List<Map<String, Object>> getFreeSlots(
            @Field(name = "Date From", type = "Date", attributes = @Attribute(name = "includeTimeOfDay", value = "true")) Long dateFrom,
            @Field(name = "Date To", type = "Date", attributes = @Attribute(name = "includeTimeOfDay", value = "true")) Long dateTo,
            @Field(name = "Is Organizer Optional", type = "Switch", required = false) Boolean isOrganizerOptional,
            @Field.Desc(name = "Required Attendee", type = "[ Email ]") List<String> requiredAttendee,
            @Field.Desc(name = "Optional Attendee", type = "[ Email ]", required = false) List<String> optionalAttendee,
            @Field(name = "Time Zone", type = "Text", required = false) String timeZone,
            @Field(name = "Meeting Duration", type = "Text", required = false) String meetingDuration) throws DatatypeConfigurationException {

        return meetingDao.getFreeSlot(dateFrom, dateTo, isOrganizerOptional, requiredAttendee, optionalAttendee, timeZone, meetingDuration);
    }

    @CatalogRequest(
            id = "localDomainRequest_6c9fb027-ab27-48ef-b966-d0f07a62fc32",
            name = "Get Schedules",
            description = "Get list of scheduled meetings for the given date and attendees",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "List of Meetings", type = "[ { Attendee: Email, Schedules: [ { Scheduled Time: DateTimeRange, Status: Text } ] } ]", required = false)
    public List<Map<String, Object>> getSchedules(
            @Field(name = "Date From", type = "Date", attributes = @Attribute(name = "includeTimeOfDay", value = "true")) Long dateFrom,
            @Field(name = "Date To", type = "Date", attributes = @Attribute(name = "includeTimeOfDay", value = "true")) Long dateTo,
            @Field.Desc(name = "Attendees", type = "[ Email ]") List<String> attendees,
            @Field(name = "Time Zone", type = "Text", required = false) String timeZone) throws IOException {

        return meetingDao.getSchedules(dateFrom, dateTo, attendees, timeZone);
    }

    @CatalogRequest(
            id = "localDomainRequest_48062a11-b237-4307-9394-a6c3496bff14",
            name = "Get Event",
            description = "Get Event details for the given Event ID",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails getEvent(
            @Field(name = "Event ID", type = "Text") String eventID) throws IOException {
        return CatalogTypes.fromEvent(meetingDao.getMeetingById(eventID));
    }


    @CatalogRequest(
            id = "localDomainRequest_c5aabea7-b876-471a-964c-37c154a1a5fe",
            name = "Search Events",
            description = "Get all the events that matches with given search keys",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Search Results", type = "[ { Event ID: Text, Event Title: Text, Scheduled Time: DateTimeRange } ]", required = false)
    public List<Map<String, Object>> searchEvents(
            @Field(name = "Search Terms", type = "Text", required = false) String searchTerms) {
        return meetingDao.getQueryResults(searchTerms);
    }

}
