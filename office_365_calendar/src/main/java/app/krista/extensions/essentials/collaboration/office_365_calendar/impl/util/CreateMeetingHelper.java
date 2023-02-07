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

    public static void setEventSubject(String title, Event event, Boolean isCreateEvent) {
        if (Boolean.TRUE.equals(isCreateEvent)) {
            event.subject = (title != null && !title.isBlank()) ? title : Constants.NEW_EVENT;
        } else {
            event.subject = title;
        }
    }

    public static void setEventTransactionId(Boolean isCreateEvent, Event event) {
        if (Boolean.TRUE.equals(isCreateEvent)) {
            event.transactionId = UUID.randomUUID().toString();
        }
    }

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

    public static void setEventRecurrence(Long dateFrom, String recurrenceValue, Double recurrenceInterval, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, String endRecurrence, Long endDate, Double numberedOccurrences, Event event) {
        if (isRecurringEvent(recurrenceValue)) {
            event.recurrence = MeetingDaoHelper.getRecurrence(recurrenceValue, recurrenceInterval, dateFrom, endRecurrence, endDate, numberedOccurrences, daysOfWeek, recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth);
        }
    }

    private static boolean isRecurringEvent(String recurrenceValue) {
        return recurrenceValue != null && !recurrenceValue.isBlank() &&
                !recurrenceValue.equalsIgnoreCase(DOES_NOT_REPEAT) &&
                !recurrenceValue.equalsIgnoreCase(NEVER);
    }

    public static void setEventAttendees(List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail, Event event) {
        if (requiredAttendeeEmail != null && !requiredAttendeeEmail.isEmpty()) {
            event.attendees = MeetingDaoHelper.getAttendees(requiredAttendeeEmail, optionalAttendeeEmail);
        }
    }

    public static void setEventLocation(String location, Event event) {
        if (location != null && !location.isBlank()) {
            event.location = MeetingDaoHelper.getLocation(location);
        }
    }

    public static void setEventBody(String details, Event event) {
        if (details != null && !details.isBlank()) {
            event.body = MeetingDaoHelper.getItemBody(details, Constants.TEXT);
        }
    }

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
            if (dateFrom != null) {
                event.start = getDateTimeTimeZone(dateFrom, timeZone, clientTimeZone);
            } else if (dateTo != null) {
                event.end = getDateTimeTimeZone(dateTo, timeZone, clientTimeZone);
            }
        }
        event.isAllDay = isAllDay;
    }

    private static String setTimezone(String timeZone, String clientTimeZone) {
        return (timeZone == null || timeZone.isBlank()) ? clientTimeZone : timeZone;
    }
}
