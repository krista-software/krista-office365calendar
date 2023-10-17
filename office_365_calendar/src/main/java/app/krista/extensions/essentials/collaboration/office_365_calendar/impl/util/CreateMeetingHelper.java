package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.OnlineMeetingProviderType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.MeetingDaoHelper.getDateTimeTimeZone;

public class CreateMeetingHelper {
    public static final String DOES_NOT_REPEAT = "Does Not Repeat";
    public static final String NEVER = "Never";

    private CreateMeetingHelper() {
    }

    /**
     * This function will set reminder to the event
     *
     * @param reminder reminder type
     * @param event    {@link Event} to add reminder
     */
    public static void setEventReminder(String reminder, Event event) {
        if (reminder != null && !reminder.isBlank()) {
            if (!reminder.equalsIgnoreCase(Constants.DONT_REMIND_ME) && !reminder.equalsIgnoreCase(Constants.NONE)) {
                event.reminderMinutesBeforeStart = MeetingDaoHelper.getReminder(reminder);
            } else if (reminder.equalsIgnoreCase(Constants.DONT_REMIND_ME) || reminder.equalsIgnoreCase(Constants.NONE)) {
                event.isReminderOn = false;
            }
        } else {
            event.isReminderOn = true;
        }
    }

    /**
     * This function will set subject for the event
     *
     * @param title         title of the event
     * @param event         {@link Event} to add title
     * @param isCreateEvent is the event new or old
     */
    public static void setEventSubject(String title, Event event, Boolean isCreateEvent) {
        if (Boolean.TRUE.equals(isCreateEvent)) {
            event.subject = (title != null && !title.isBlank()) ? title : Constants.NEW_EVENT;
        } else {
            event.subject = title;
        }
    }

    /**
     * This event will set transactionId for making modifications to event
     *
     * @param isCreateEvent set if the event is new
     * @param event         {@link Event} to be added with transactionId
     */
    public static void setEventTransactionId(Boolean isCreateEvent, Event event) {
        if (Boolean.TRUE.equals(isCreateEvent)) {
            event.transactionId = UUID.randomUUID().toString();
        }
    }

    /**
     * This function will set if the event is teams meeting or offline event
     *
     * @param isTeamsMeeting Boolean value to set the meeting
     * @param event          {@link Event} to be set as teams meeting or offline event
     */
    public static void setIsTeamsMeeting(Boolean isTeamsMeeting, Event event) {
        if (isTeamsMeeting != null && isTeamsMeeting) {
            event.isOnlineMeeting = true;
            event.onlineMeetingProvider = OnlineMeetingProviderType.TEAMS_FOR_BUSINESS;
        } else {
            event.isOnlineMeeting = false;
            event.onlineMeetingProvider = OnlineMeetingProviderType.UNKNOWN;
            event.onlineMeeting = null;
        }
    }

    /**
     * This function will get the type of recurrence and set the recurrence to the event
     *
     * @param dateFrom            from date
     * @param recurrenceValue     recurrence meeting
     * @param recurrenceInterval  interval of the recurring event
     * @param daysOfWeek          repeat days of the recurring event
     * @param recurrenceType      absolute recurrence or relative recurrence
     * @param dayOfMonth          repeat day of the month
     * @param repeatIndex         repeat index for relative index
     * @param repeatDay           repeat day for relative index
     * @param repeatMonth         repeat month for recurring event
     * @param endRecurrence       end type of recurring event
     * @param endDate             end date for recurring event
     * @param numberedOccurrences end recurrence after number of occurrences
     * @param event               event to be set as recurring event
     */
    public static void setEventRecurrence(Long dateFrom, String recurrenceValue, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, Event event) {
        if (isRecurringEvent(recurrenceValue)) {
            event.recurrence = MeetingDaoHelper.getRecurrence(recurrenceValue, recurrenceInterval, dateFrom, endRecurrence, endDate, numberedOccurrences, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth);
        }
    }

    /**
     * This function will check if the event to be set as recurring event or not
     *
     * @param recurrenceValue given type of recurring event
     * @return {@link Boolean} value of recurring event or not
     */
    private static boolean isRecurringEvent(String recurrenceValue) {
        return recurrenceValue != null && !recurrenceValue.isBlank() &&
                !recurrenceValue.equalsIgnoreCase(DOES_NOT_REPEAT) &&
                !recurrenceValue.equalsIgnoreCase(NEVER);
    }

    /**
     * This function will add attendees to the event
     *
     * @param requiredAttendeeEmail list of required attendee emails
     * @param optionalAttendeeEmail list of optional attendee emails
     * @param event                 event to be added with given attendees
     */
    public static void setEventAttendees(List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Event event) {
        if (requiredAttendeeEmail != null && !requiredAttendeeEmail.isEmpty()) {
            event.attendees = MeetingDaoHelper.getAttendees(requiredAttendeeEmail, optionalAttendeeEmail);
        }
    }

    /**
     * This function will set the location for the event
     *
     * @param location location for the event
     * @param event    event to be added with given location
     */
    public static void setEventLocation(String location, Event event) {
        if (location != null && !location.isBlank()) {
            event.location = MeetingDaoHelper.getLocation(location);
        }
    }

    /**
     * This function will add event body details to the given event
     *
     * @param details body details for the event
     * @param event   event to be added with given details
     */
    public static void setEventBody(String details, Event event) {
        if (details != null && !details.isBlank()) {
            event.body = MeetingDaoHelper.getItemBody(details, Constants.TEXT);
        }
    }

    /**
     * This function will set date and time with time zone to the event
     *
     * @param dateFrom       from date for the event
     * @param dateTo         to date
     * @param isAllDay       if the event is all day
     * @param timeZone       timezone of the event to be scheduled
     * @param isCreateEvent  if the event is newly created
     * @param clientTimeZone time zone of the client scheduling the event
     * @param event          event to be set with given date and time
     * @throws ParseException if there is error in parsing the dateFrom and dateTo
     */
    public static void setEventDateTime(Long dateFrom, Long dateTo, Boolean isAllDay, String timeZone, Boolean isCreateEvent, String clientTimeZone, Event event) throws ParseException {
        timeZone = setTimezone(timeZone, clientTimeZone);
        if (isAllDay != null && isAllDay) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.YYYY_DD_MM);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(clientTimeZone));
            String dateString = simpleDateFormat.format(dateFrom);
            Date newDate = simpleDateFormat.parse(dateString);
            event.start = getDateTimeTimeZone(newDate.getTime(), timeZone, clientTimeZone);
            event.end = getDateTimeTimeZone(newDate.getTime() + 86400000, timeZone, clientTimeZone);
        } else if (Boolean.TRUE.equals(isCreateEvent)) {
            event.start = getDateTimeTimeZone(dateFrom, timeZone, clientTimeZone);
            event.end = getDateTimeTimeZone(dateTo != null ? dateTo : dateFrom + 1800000, timeZone, clientTimeZone);
        } else {
            if (dateFrom != null && dateTo != null) {
                event.start = getDateTimeTimeZone(dateFrom, timeZone, clientTimeZone);
                event.end = getDateTimeTimeZone(dateTo, timeZone, clientTimeZone);
            } else if (dateFrom != null) {
                event.start = getDateTimeTimeZone(dateFrom, timeZone, clientTimeZone);
            } else if (dateTo != null) {
                event.end = getDateTimeTimeZone(dateTo, timeZone, clientTimeZone);
            }
        }
        event.isAllDay = isAllDay;
    }

    /**
     * This function will set the timezone of event as client time zone if the client doesn't
     * provide the time zone for the event
     *
     * @param timeZone       timezone for the event
     * @param clientTimeZone timezone of the client scheduling the event
     * @return timezone to be set for the event
     */
    private static String setTimezone(String timeZone, String clientTimeZone) {
        return (timeZone == null || timeZone.isBlank()) ? clientTimeZone : timeZone;
    }
}
