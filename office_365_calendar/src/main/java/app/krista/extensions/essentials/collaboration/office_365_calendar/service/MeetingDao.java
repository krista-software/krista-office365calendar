package app.krista.extensions.essentials.collaboration.office_365_calendar.service;

import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MeetingDao {

    /**
     * Returns {@link Meeting} object of Office 365 calendar event
     *
     * @param id Office 365 Calendar event id
     * @return {@link Meeting} Office 365 Calendar event
     * @throws IOException if there is error in id
     */
    Meeting getMeetingById(String id) throws IOException;

    /**
     * Returns {@link Meeting} object of Office 365 calendar event using delegated access
     *
     * @param id Office 365 Calendar event id
     * @param delegateEmail email of the user to delegate to, can be null or blank for current user
     * @return {@link Meeting} Office 365 Calendar event
     * @throws IOException if there is error in id
     */
    Meeting getMeetingByIdDelegated(String id, String delegateEmail) throws IOException;

    /**
     * Returns {@link List<Meeting>} list of Office 365 calendar events
     *
     * @return {@link List<Meeting>} list of Office 365 calendar events
     */
    List<Meeting> getMeetings();

    /**
     * Return {@link List<Meeting>} list of Office 365 calendar events
     *
     * @param numberOfMeetings size of upcoming meetings
     * @return {@link List<Meeting>} list of Office 365 calendar events
     */
    List<Meeting> getMeetings(int numberOfMeetings);

    /**
     * Returns list of free slots for the given input parameters
     *
     * @param dateFrom            date for which free slots needs to be fetched from
     * @param dateTo              date for which free slots needs to be fetched till
     * @param isOrganizerOptional if the organizer is required for the meeting or optional
     * @param requiredAttendee    list of required attendee email addresses
     * @param optionalAttendee    list of optional attendee email addresses
     * @param timeZone            time zone to get free slots time in required time zones
     * @param meetingDuration     duration of the free slot required
     * @return {@link List<>} list of multi field objects with meeting time suggestions
     * @throws DatatypeConfigurationException if there is error in dateFrom or dateTo
     */
    List<Map<String, Object>> getFreeSlot(Long dateFrom, Long dateTo, Boolean isOrganizerOptional, List<String> requiredAttendee, List<String> optionalAttendee, String timeZone, String meetingDuration) throws DatatypeConfigurationException, IOException;

    /**
     * Returns {@link Meeting} the scheduled meeting object of Office 365 calendar
     *
     * @param title                 title subject of the meeting
     * @param location              location of the meeting
     * @param dateFrom              start date and time of the meeting
     * @param dateTo                end date and time of the meeting
     * @param requiredAttendeeEmail list of attendees email addresses required for the meeting
     * @param optionalAttendeeEmail list of attendees email addresses optional for the meeting
     * @param isAllDay              if the meeting is all day
     * @param timeZone              time zone for the scheduling meeting
     * @param reminder              to remind the attendees
     * @param recurrence            if the meetings are series of recurring meetings
     * @param recurrenceInterval    to repeat meeting after every recurrence interval
     * @param daysOfWeek            to repeat the meeting on the given days of the week
     * @param recurrenceType        type of recurrence meeting for absolute or relative yearly and monthly interval
     * @param dayOfMonth            on which day of the month to repeat the meeting
     * @param repeatIndex           index for the yearly and monthly relative meetings
     * @param repeatDay             on which day to repeat the relative meeting
     * @param repeatMonth           on which month to repeat the yearly meeting
     * @param endRecurrence         when to end the recurrence meeting
     * @param endDate               to end recurrence meeting on this date
     * @param numberedOccurrences   to end recurrence meeting after these many occurrences
     * @param attachments           list of attachments added for the meeting
     * @param details               details of the meeting
     * @param isTeamsMeeting        if the scheduled meeting is online teams meeting
     * @return {@link Meeting} meeting object of the scheduled Office 365 calendar meeting
     */
    Meeting addMeeting(String title, String location, Long dateFrom, Long dateTo, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String timeZone, String reminder, String recurrence, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, List<Attachment> attachments, String details, Boolean isTeamsMeeting);

    // Overload to support scheduling in a delegated organizer's calendar
    Meeting addMeeting(String organizerEmail, String title, String location, Long dateFrom, Long dateTo, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String timeZone, String reminder, String recurrence, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, List<Attachment> attachments, String details, Boolean isTeamsMeeting);


    /**
     * Forward the meeting invitation to the given attendees email addresses
     *
     * @param id            event id of the Office 365 calendar meeting
     * @param attendeeEmail list of emails for forwarding the meeting invitation
     * @throws IOException if there is error in given input
     */
    void forwardMeeting(String id, List<String> attendeeEmail) throws IOException;

    /**
     * Returns the updated meeting of with the given data
     *
     * @param id                    event id of the Office 365 calendar meeting
     * @param title                 title subject of the meeting
     * @param details               details of the meeting
     * @param location              location of the meeting
     * @param dateFrom              start date and time of the meeting
     * @param dateTo                end date and time of the meeting
     * @param timeZone              time zone for the scheduling meeting
     * @param requiredAttendeeEmail list of attendees email addresses required for the meeting
     * @param optionalAttendeeEmail list of attendees email addresses optional for the meeting
     * @param isAllDay              if the meeting is all day
     * @param recurrence            if the meetings are series of recurring meetings
     * @param reminder              to remind the attendees
     * @param recurrenceInterval    to repeat meeting after every recurrence interval
     * @param daysOfWeek            to repeat the meeting on the given days of the week
     * @param recurrenceType        type of recurrence meeting for absolute or relative yearly and monthly interval
     * @param dayOfMonth            on which day of the month to repeat the meeting
     * @param repeatIndex           index for the yearly and monthly relative meetings
     * @param repeatDay             on which day to repeat the relative meeting
     * @param repeatMonth           on which month to repeat the yearly meeting
     * @param endRecurrence         when to end the recurrence meeting
     * @param endDate               to end recurrence meeting on this date
     * @param numberedOccurrences   to end recurrence meeting after these many occurrences
     * @param isTeamsMeeting        if the scheduled meeting to be updated is online teams meeting
     * @return {@link Meeting} meeting object of the updated Office 365 calendar meeting
     */
    Meeting updateMeeting(String id, String title, String details, String location, Long dateFrom, Long dateTo, String timeZone, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String recurrence, String reminder, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, Boolean isTeamsMeeting);

    // Overload to support delegated update using organizer's email
    Meeting updateMeeting(String organizerEmail, String id, String title, String details, String location, Long dateFrom, Long dateTo, String timeZone, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Boolean isAllDay, String recurrence, String reminder, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, Boolean isTeamsMeeting);


    /**
     * Returns the meeting with updated attendees
     *
     * @param id Office 365 calendar meeting id
     * @return {@link Meeting} the Office 365 calendar meeting object with updated meeting attendees
     */
    Meeting acceptMeetingInvitation(String id);

    /**
     * Return the updated meeting object by removing the user
     *
     * @param id Office 365 calendar meeting id
     * @return {@link Meeting} the Office 365 calendar meeting object with updated meeting attendees
     */
    Meeting declineMeetingInvitation(String id);

    /**
     * Returns the updated Meeting object by accepting event tentatively
     *
     * @param id Office 365 calendar meeting id
     * @return {@link Meeting} the Office 365 calendar meeting object with updated meeting attendees
     */
    Meeting tentativeMeetingInvitation(String id);

    // Overload to support delegated cancellation using delegate's email
    Meeting cancelMeeting(String id, String delegateEmail);


    /**
     * Returns the cancelled meeting object of Office 365 calendar
     *
     * @param id Office 365 calendar meeting id
     * @return {@link Meeting} the Office 365 calendar cancelled meeting object
     */
    Meeting cancelMeeting(String id);

    /**
     * Returns the updated meeting with newly added required and optional attendees
     *
     * @param id                    Office 365 calendar meeting id
     * @param requiredAttendeeEmail list of required attendee emails for the meeting
     * @param optionalAttendeeEmail list of optional attendee emails for the meeting
     * @return {@link Meeting} the Office 365 calendar meeting object with newly added attendees
     */
    Meeting addAttendees(String id, List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail);

    /**
     * Returns the updated meeting after removing the given attendees
     *
     * @param id        Office 365 calendar meeting id
     * @param attendees list of attendees email addresses to remove from the scheduled meeting
     * @return {@link Meeting} the Office 365 calendar meeting object after removing the given attendees
     */
    Meeting removeAttendees(String id, List<String> attendees);

    /**
     * Returns the list of scheduled meeting slots for the given attendees
     *
     * @param dateFrom  fetch meeting slots from this date
     * @param dateTo    fetch meeting slots till this date
     * @param attendees fetch meeting slots for these attendees
     * @param timeZone  time zone of the given date and time
     * @return {@link List<>} the list of Multi field objects with attendees and their meeting slots
     */
    List<Map<String, Object>> getSchedules(Long dateFrom, Long dateTo, List<String> attendees, String timeZone) throws IOException;

    /**
     * @param query search terms to match the events data
     * @return {@link List<>} the list of Multi filed objects with event ID, event title and meeting slot
     */
    List<Map<String, Object>> getQueryResults(String query) throws IOException;

    /**
     * This request will return the list of events scheduled for the given user.
     *
     * @param userEmail email of the user whose events need to be fetched
     * @param from      date from which the events needed to be fetched
     * @param to        date till which the events needed to be fetched
     * @return {@link List} of {@link Meeting}
     */
    List<Meeting> getEventsForUser(String userEmail, Long from, Long to);

    /**
     * This request will return the list of attachment for the event for given user.
     *
     * @param userEmail email of the user whose events need to be fetched
     * @return {@link List} of {@link Attachment}
     */
    List<FileAttachment> getAttachmentsForGivenUser(String userEmail, String eventId);

    /**
     * This request will return the list of attachment for the event for current user.
     *
     * @return {@link List} of {@link Attachment}
     */
    List<FileAttachment> getAttachmentsForAuthenticatedUser(String eventId);
}
