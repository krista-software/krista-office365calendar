package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import com.microsoft.graph.core.DateOnly;
import com.microsoft.graph.models.*;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.List;
import java.util.*;

public final class MeetingDaoHelper {

    public static final String UNEXPECTED_RECURRENCE_VALUE = "Unexpected recurrence value: ";
    public static final String END_DATE = "End date";
    public static final String NUMBERED_OCCURRENCES = "Numbered occurrences";
    public static final String ABSOLUTE_RECURRENCE = "Absolute recurrence";
    public static final String RELATIVE_RECURRENCE = "Relative recurrence";

    private MeetingDaoHelper() {
    }

    public static List<Attendee> getAttendees(List<String> requiredAttendeeEmail, List<String> optionalAttendeeEmail) {
        List<Attendee> attendeesList = new LinkedList<>();
        for (String attendeeEmail : requiredAttendeeEmail) {
            Attendee attendees = new Attendee();
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.address = attendeeEmail;
            attendees.emailAddress = emailAddress;
            attendees.type = AttendeeType.REQUIRED;
            attendeesList.add(attendees);
        }
        if (optionalAttendeeEmail != null && !optionalAttendeeEmail.isEmpty()) {
            for (String attendeeEmail : optionalAttendeeEmail) {
                Attendee attendees = new Attendee();
                EmailAddress emailAddress = new EmailAddress();
                emailAddress.address = attendeeEmail;
                attendees.emailAddress = emailAddress;
                attendees.type = AttendeeType.OPTIONAL;
                attendeesList.add(attendees);
            }
        }
        return attendeesList;
    }

    public static void updateAttendeesList(List<Attendee> attendeesList, List<String> attendeeMail) {
        List<Attendee> removableAttendee = new ArrayList<>();
        for (Attendee attendee : attendeesList) {
            if (attendee.emailAddress != null && attendeeMail.contains(attendee.emailAddress.address)) {
                removableAttendee.add(attendee);
            }
        }
        attendeesList.removeAll(removableAttendee);
    }

    public static boolean isUpcomingEvent(Event event) {
        Date eventDate = getEventDate(event);
        long currentDate = getCurrentDateCorrespondingToUTCTZ();
        return currentDate < eventDate.getTime();
    }

    private static Date getEventDate(Event event) {
        Date eventDate;
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            eventDate = sfd.parse(Objects.requireNonNull(event.start).dateTime);
        } catch (ParseException parseException) {
            throw new IllegalStateException("Date :" + event.start.dateTime + " can't be parsed. " + parseException.getMessage(), parseException.getCause());
        }
        return eventDate;
    }

    private static long getCurrentDateCorrespondingToUTCTZ() {
        ZoneId sourceTimeZone = ZoneId.ofOffset("UTC", ZoneOffset.UTC);
        return new Date().toInstant().atZone(sourceTimeZone).toEpochSecond();
    }

    public static boolean isEventNeitherAcceptedNorDeclined(Event event, String currentUserMailAddress) {
        List<Attendee> attendees = event.attendees;
        if (event.organizer != null && event.organizer.emailAddress != null && attendees != null) {
            String organizerAddress = event.organizer.emailAddress.address;
            for (Attendee attendee : attendees) {
                if (!isCurrentUserOrganizer(currentUserMailAddress, organizerAddress) && isCurrentUserNeitherAcceptedNorDeclinedMeeting(currentUserMailAddress, attendee)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isCurrentUserOrganizer(String currentUserMailAddress, String attendee) {
        return currentUserMailAddress.equals(attendee);
    }

    private static boolean isCurrentUserNeitherAcceptedNorDeclinedMeeting(String currentUserMailAddress, Attendee attendee) {
        return currentUserMailAddress.equals(Objects.requireNonNull(attendee.emailAddress).address) &&
                !(ResponseType.ACCEPTED.equals(Objects.requireNonNull(attendee.status).response) ||
                        ResponseType.DECLINED.equals(attendee.status.response));
    }

    public static ItemBody getItemBody(String details, String type) {
        ItemBody body = new ItemBody();
        if (type.equalsIgnoreCase(Constants.TEXT)) {
            body.contentType = BodyType.TEXT;

        } else if (type.equalsIgnoreCase(Constants.HTML)) {
            body.contentType = BodyType.HTML;
        }
        body.content = details;
        return body;
    }

    public static Location getLocation(String location) {
        Location local = new Location();
        local.displayName = location;
        return local;
    }

    public static PatternedRecurrence getRecurrence(String recurrenceValue, Double recurrenceInterval, Long dateFrom, String endRecurrence, Long endDate, Double numberedOccurrences, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth) {

        PatternedRecurrence recurrence = new PatternedRecurrence();
        RecurrenceRange range = new RecurrenceRange();
        Calendar startDateDateOnly = Calendar.getInstance();
        startDateDateOnly.setTimeInMillis(dateFrom);
        range.startDate = new DateOnly(startDateDateOnly.get(Calendar.YEAR), startDateDateOnly.get(Calendar.MONTH) + 1, startDateDateOnly.get(Calendar.DAY_OF_MONTH));

        RecurrencePattern pattern = new RecurrencePattern();

        switch (recurrenceValue) {
            case "Daily":
                getDailyRecurrence(pattern);
                break;
            case "Weekly":
                getWeeklyRecurrence(daysOfWeek, pattern);
                break;
            case "Monthly":
                getMonthlyRecurrence(recurrenceType, dayOfMonth, repeatIndex, repeatDay, startDateDateOnly, pattern);
                break;
            case "Yearly":
                getYearlyRecurrence(recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, startDateDateOnly, pattern);
                break;
            default:
                throw new IllegalStateException(UNEXPECTED_RECURRENCE_VALUE + recurrenceValue);
        }
        pattern.interval = (recurrenceInterval != null) ? recurrenceInterval.intValue() : 1;
        recurrence.pattern = pattern;

        if (endRecurrence.equalsIgnoreCase(END_DATE)) {
            range.type = RecurrenceRangeType.END_DATE;
            Calendar endDateDateOnly = Calendar.getInstance();
            endDateDateOnly.setTimeInMillis(endDate);
            range.endDate = new DateOnly(endDateDateOnly.get(Calendar.YEAR), endDateDateOnly.get(Calendar.MONTH) + 1, endDateDateOnly.get(Calendar.DAY_OF_MONTH));
        } else if (endRecurrence.equalsIgnoreCase(NUMBERED_OCCURRENCES)) {
            range.type = RecurrenceRangeType.NUMBERED;
            range.numberOfOccurrences = numberedOccurrences != null ? numberedOccurrences.intValue() : 10;
        } else {
            range.type = RecurrenceRangeType.NO_END;
        }
        recurrence.range = range;
        return recurrence;
    }

    private static void getDailyRecurrence(RecurrencePattern pattern) {
        pattern.type = RecurrencePatternType.DAILY;
    }

    private static void getWeeklyRecurrence(List<String> daysOfWeek, RecurrencePattern pattern) {
        pattern.type = RecurrencePatternType.WEEKLY;
        pattern.daysOfWeek = getDayOfWeeks(daysOfWeek);
    }

    private static void getYearlyRecurrence(String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, Calendar startDateDateOnly, RecurrencePattern pattern) {
        if (recurrenceType.equalsIgnoreCase(ABSOLUTE_RECURRENCE)) {
            pattern.type = RecurrencePatternType.ABSOLUTE_YEARLY;
            pattern.month = (repeatMonth != null && !repeatMonth.isBlank()) ? getMonthIndex(repeatMonth) : (startDateDateOnly.get(Calendar.MONTH) + 1);
            pattern.dayOfMonth = (dayOfMonth != null) ? dayOfMonth.intValue() : startDateDateOnly.get(Calendar.DAY_OF_MONTH);
        } else if (recurrenceType.equalsIgnoreCase(RELATIVE_RECURRENCE)) {
            pattern.type = RecurrencePatternType.RELATIVE_YEARLY;
            pattern.month = (repeatMonth != null && !repeatMonth.isBlank()) ? getMonthIndex(repeatMonth) : (startDateDateOnly.get(Calendar.MONTH) + 1);
            pattern.index = getWeekIndex(repeatIndex);
            pattern.daysOfWeek = getDayOfWeeks(List.of(repeatDay));
        }
    }

    private static void getMonthlyRecurrence(String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, Calendar startDateDateOnly, RecurrencePattern pattern) {
        if (recurrenceType.equalsIgnoreCase(ABSOLUTE_RECURRENCE)) {
            pattern.type = RecurrencePatternType.ABSOLUTE_MONTHLY;
            pattern.dayOfMonth = (dayOfMonth != null) ? dayOfMonth.intValue() : startDateDateOnly.get(Calendar.DAY_OF_MONTH);
        } else if (recurrenceType.equalsIgnoreCase(RELATIVE_RECURRENCE)) {
            pattern.type = RecurrencePatternType.RELATIVE_MONTHLY;
            pattern.daysOfWeek = getDayOfWeeks(List.of(repeatDay));
            pattern.index = getWeekIndex(repeatIndex);
        }
    }

    @NotNull
    private static Integer getMonthIndex(String repeatMonth) {

        if (repeatMonth.equalsIgnoreCase("January")) {
            return 1;
        } else if (repeatMonth.equalsIgnoreCase("February")) {
            return 2;
        } else if (repeatMonth.equalsIgnoreCase("March")) {
            return 3;
        } else if (repeatMonth.equalsIgnoreCase("April")) {
            return 4;
        } else if (repeatMonth.equalsIgnoreCase("May")) {
            return 5;
        } else if (repeatMonth.equalsIgnoreCase("June")) {
            return 6;
        } else if (repeatMonth.equalsIgnoreCase("July")) {
            return 7;
        } else if (repeatMonth.equalsIgnoreCase("August")) {
            return 8;
        } else if (repeatMonth.equalsIgnoreCase("September")) {
            return 9;
        } else if (repeatMonth.equalsIgnoreCase("October")) {
            return 10;
        } else if (repeatMonth.equalsIgnoreCase("November")) {
            return 11;
        } else if (repeatMonth.equalsIgnoreCase("December")) {
            return 12;
        }
        return 1;
    }

    private static WeekIndex getWeekIndex(String repeatIndex) {

        if (repeatIndex != null && !repeatIndex.isEmpty()) {
            if (repeatIndex.equalsIgnoreCase("First")) {
                return WeekIndex.FIRST;
            } else if (repeatIndex.equalsIgnoreCase("Second")) {
                return WeekIndex.SECOND;
            } else if (repeatIndex.equalsIgnoreCase("Third")) {
                return WeekIndex.THIRD;
            } else if (repeatIndex.equalsIgnoreCase("Fourth")) {
                return WeekIndex.FOURTH;
            } else if (repeatIndex.equalsIgnoreCase("Last")) {
                return WeekIndex.LAST;
            }
        }
        return WeekIndex.FIRST;
    }

    @NotNull
    private static LinkedList<DayOfWeek> getDayOfWeeks(List<String> daysOfWeek) {
        LinkedList<DayOfWeek> daysOfWeekly = new LinkedList<>();
        daysOfWeek.forEach(weekDay -> {
            if (weekDay.equalsIgnoreCase("Monday")) {
                daysOfWeekly.add(DayOfWeek.MONDAY);
            } else if (weekDay.equalsIgnoreCase("Tuesday")) {
                daysOfWeekly.add(DayOfWeek.TUESDAY);
            } else if (weekDay.equalsIgnoreCase("Wednesday")) {
                daysOfWeekly.add(DayOfWeek.WEDNESDAY);
            } else if (weekDay.equalsIgnoreCase("Thursday")) {
                daysOfWeekly.add(DayOfWeek.THURSDAY);
            } else if (weekDay.equalsIgnoreCase("Friday")) {
                daysOfWeekly.add(DayOfWeek.FRIDAY);
            } else if (weekDay.equalsIgnoreCase("Saturday")) {
                daysOfWeekly.add(DayOfWeek.SATURDAY);
            } else if (weekDay.equalsIgnoreCase("Sunday")) {
                daysOfWeekly.add(DayOfWeek.SUNDAY);
            }
        });
        return daysOfWeekly;
    }

    public static Integer getReminder(String reminder) {
        switch (reminder) {
            case "None":
            case "Don't remind me":
                return null;
            case "At event start":
            case "At time of event":
                return 0;
            case "5 minutes before":
                return 5;
            default:
            case "15 minutes before":
                return 15;
            case "30 minutes before":
                return 30;
            case "1 hour before":
                return 60;
            case "2 hours before":
                return 120;
            case "4 hours before":
                return 240;
            case "12 hours before":
                return 720;
            case "1 day before":
                return 1440;
            case "1 week before":
                return 10080;
        }
    }

    public static List<Recipient> getRecipient(List<String> attendeeEmail) {
        List<Recipient> toRecipientsList = new LinkedList<>();
        for (String email : attendeeEmail) {
            Recipient toRecipients = new Recipient();
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.address = email;
            toRecipients.emailAddress = emailAddress;
            toRecipientsList.add(toRecipients);
        }
        return toRecipientsList;
    }

    public static List<AttendeeBase> getRequiredAttendee(List<String> requiredAttendee, List<String> optionalAttendee) {
        List<AttendeeBase> attendeesList = new LinkedList<>();
        for (String attendeeEmail : requiredAttendee) {
            AttendeeBase attendees = new AttendeeBase();
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.address = attendeeEmail;
            attendees.emailAddress = emailAddress;
            attendees.type = AttendeeType.REQUIRED;
            attendeesList.add(attendees);
        }
        if (optionalAttendee != null && !optionalAttendee.isEmpty()) {
            for (String attendeeEmail : optionalAttendee) {
                AttendeeBase attendees = new AttendeeBase();
                EmailAddress emailAddress = new EmailAddress();
                emailAddress.address = attendeeEmail;
                attendees.emailAddress = emailAddress;
                attendees.type = AttendeeType.OPTIONAL;
                attendeesList.add(attendees);
            }
        }
        return attendeesList;
    }

    public static DateTimeTimeZone getDateTimeTimeZone(Long timeStamp, String timeZone, String clientTimeZone) {
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sfd.setTimeZone(TimeZone.getTimeZone(clientTimeZone));
        DateTimeTimeZone dateTimeTimeZone = new DateTimeTimeZone();
        dateTimeTimeZone.dateTime = sfd.format(timeStamp);
        dateTimeTimeZone.timeZone = (timeZone != null) ? timeZone : clientTimeZone;
        return dateTimeTimeZone;
    }
}
