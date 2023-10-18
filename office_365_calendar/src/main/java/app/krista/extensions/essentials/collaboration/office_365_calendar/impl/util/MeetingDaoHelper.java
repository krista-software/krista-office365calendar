package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import com.microsoft.graph.core.DateOnly;
import com.microsoft.graph.models.*;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRulesException;
import java.util.Calendar;
import java.util.List;
import java.util.*;

public final class MeetingDaoHelper {

    public static final String UNEXPECTED_RECURRENCE_VALUE = "Unexpected recurrence value: ";
    public static final String END_DATE = "End date";
    public static final String NUMBERED_OCCURRENCES = "Numbered occurrences";
    public static final String ABSOLUTE_RECURRENCE = "Absolute recurrence";
    public static final String RELATIVE_RECURRENCE = "Relative recurrence";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String UTC = "UTC";
    public static final String INVALID_TIME_ZONE_PLEASE_PROVIDE_A_VALID_TIME_ZONE_STRING = "Invalid time zone. Please provide a valid time zone string.";

    private MeetingDaoHelper() {
    }

    /**
     * This function will get the given email address into microsoft {@link Attendee} list
     *
     * @param requiredAttendeeEmail list of required attendees emails
     * @param optionalAttendeeEmail list of optional attendees email
     * @return List of {@link Attendee} for the event
     */
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

    /**
     * This function will remove attendees from the event
     *
     * @param attendeesList list of existing attendees
     * @param attendeeMail  list of attendees to be removed
     */
    public static void updateAttendeesList(List<Attendee> attendeesList, List<String> attendeeMail) {
        List<Attendee> removableAttendee = new ArrayList<>();
        for (Attendee attendee : attendeesList) {
            if (attendee.emailAddress != null && attendeeMail.contains(attendee.emailAddress.address)) {
                removableAttendee.add(attendee);
            }
        }
        attendeesList.removeAll(removableAttendee);
    }

    /**
     * This function will check if the given event is upcoming or not
     *
     * @param event event to be checked
     * @return {@link Boolean} value if the event is upcoming or not
     */
    public static boolean isUpcomingEvent(Event event) {
        Date eventDate = getEventDate(event);
        long currentDate = getCurrentDateCorrespondingToUTCTZ();
        return currentDate < eventDate.getTime();
    }


    /**
     * This function will get the event date in format
     *
     * @param event event to get the date
     * @return {@link Date} for the event
     */
    private static Date getEventDate(Event event) {
        Date eventDate;
        SimpleDateFormat sfd = new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS);
        try {
            eventDate = sfd.parse(Objects.requireNonNull(event.start).dateTime);
        } catch (ParseException parseException) {
            throw new IllegalStateException("Date :" + event.start.dateTime + " can't be parsed. " + parseException.getMessage(), parseException.getCause());
        }
        return eventDate;
    }

    /**
     * This function will get time stamp with respect to UTC
     *
     * @return time stamp
     */
    private static long getCurrentDateCorrespondingToUTCTZ() {
        ZoneId sourceTimeZone = ZoneId.ofOffset(UTC, ZoneOffset.UTC);
        return new Date().toInstant().atZone(sourceTimeZone).toEpochSecond();
    }

    /**
     * This function will get the boolean value for event accepted nor declined for the given user
     *
     * @param event                  event to get the details
     * @param currentUserMailAddress user to know accepted or not
     * @return {@link Boolean} value if the event is accepted or not by user
     */
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

    /**
     * This function will check if the current user is organizer or not
     *
     * @param currentUserMailAddress current user email address
     * @param attendee               attendee email address
     * @return {@link Boolean} value
     */
    private static boolean isCurrentUserOrganizer(String currentUserMailAddress, String attendee) {
        return currentUserMailAddress.equals(attendee);
    }

    /**
     * This function will give boolean value if the current user accepted or declined the event
     *
     * @param currentUserMailAddress current user email address
     * @param attendee               {@link Attendee} of the event
     * @return Boolean value
     */
    private static boolean isCurrentUserNeitherAcceptedNorDeclinedMeeting(String currentUserMailAddress, Attendee attendee) {
        return currentUserMailAddress.equals(Objects.requireNonNull(attendee.emailAddress).address) &&
                !(ResponseType.ACCEPTED.equals(Objects.requireNonNull(attendee.status).response) ||
                        ResponseType.DECLINED.equals(attendee.status.response));
    }

    /**
     * This function will get the events details and type of text in the event
     *
     * @param details details of the event
     * @param type    html or text type
     * @return {@link ItemBody}
     */
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

    /**
     * This function will get the location of the event
     *
     * @param location location string
     * @return {@link Location} object
     */
    public static Location getLocation(String location) {
        Location local = new Location();
        local.displayName = location;
        return local;
    }

    /**
     * This function will get the recurrence of the event
     *
     * @param recurrenceValue     recurrence value
     * @param recurrenceInterval  interval of recurring event
     * @param dateFrom            from date of event
     * @param endRecurrence       recurrence end type
     * @param endDate             end date of recurring event
     * @param numberedOccurrences end event after this number of occurrences
     * @param daysOfWeek          days of the week of the recurring event
     * @param recurrenceType      absolute or relative recurrence type
     * @param dayOfMonth          day of month for absolute recurrence
     * @param repeatIndex         repeat index for relative recurrence
     * @param repeatDay           repeat day for absolute or relative recurrence
     * @param repeatMonth         repeat month of the event f
     * @return {@link PatternedRecurrence} object for the recurring event
     */
    public static PatternedRecurrence getRecurrence(String recurrenceValue, Double recurrenceInterval, Long dateFrom, String endRecurrence, Long endDate, Double numberedOccurrences, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth) {

        PatternedRecurrence recurrence = new PatternedRecurrence();
        RecurrenceRange range = new RecurrenceRange();
        Calendar startDateDateOnly = Calendar.getInstance();
        startDateDateOnly.setTimeInMillis(dateFrom);
        range.startDate = new DateOnly(startDateDateOnly.get(Calendar.YEAR), startDateDateOnly.get(Calendar.MONTH) + 1, startDateDateOnly.get(Calendar.DAY_OF_MONTH));

        RecurrencePattern pattern = new RecurrencePattern();

        switch (recurrenceValue) {
            case "Daily":
                setDailyRecurrence(pattern);
                break;
            case "Weekly":
                setWeeklyRecurrence(daysOfWeek, pattern);
                break;
            case "Monthly":
                setMonthlyRecurrence(recurrenceType, dayOfMonth, repeatIndex, repeatDay, startDateDateOnly, pattern);
                break;
            case "Yearly":
                setYearlyRecurrence(recurrenceType, dayOfMonth, repeatIndex, repeatDay, repeatMonth, startDateDateOnly, pattern);
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

    /**
     * This function will set the patter as daily recurrence
     *
     * @param pattern {@link RecurrencePattern}
     */
    private static void setDailyRecurrence(RecurrencePattern pattern) {
        pattern.type = RecurrencePatternType.DAILY;
    }

    /**
     * This function will set the recurrence pattern as weekly recurrence
     *
     * @param daysOfWeek days of the week in recurring event
     * @param pattern    {@link RecurrencePattern} object
     */
    private static void setWeeklyRecurrence(List<String> daysOfWeek, RecurrencePattern pattern) {
        pattern.type = RecurrencePatternType.WEEKLY;
        pattern.daysOfWeek = getDayOfWeeks(daysOfWeek);
    }

    /**
     * This function will set the recurrence pattern as yearly recurrence
     *
     * @param recurrenceType    absolute or relative recurrence
     * @param dayOfMonth        day of month for repeating event
     * @param repeatIndex       index for repeating the event
     * @param repeatDay         day of week for repeating the event
     * @param repeatMonth       repeat month for recurring event
     * @param startDateDateOnly start date object {@link Calendar}
     * @param pattern           {@link RecurrencePattern}
     */
    private static void setYearlyRecurrence(String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, String repeatMonth, Calendar startDateDateOnly, RecurrencePattern pattern) {
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

    /**
     * This function will set the recurrence pattern as monthly
     *
     * @param recurrenceType    absolute or relative recurrence
     * @param dayOfMonth        day of month for repeating event
     * @param repeatIndex       index for repeating the event
     * @param repeatDay         day of week for repeating the event
     * @param startDateDateOnly start date object {@link Calendar}
     * @param pattern           {@link RecurrencePattern}
     */
    private static void setMonthlyRecurrence(String recurrenceType, Double dayOfMonth, String repeatIndex, String repeatDay, Calendar startDateDateOnly, RecurrencePattern pattern) {
        if (recurrenceType.equalsIgnoreCase(ABSOLUTE_RECURRENCE)) {
            pattern.type = RecurrencePatternType.ABSOLUTE_MONTHLY;
            pattern.dayOfMonth = (dayOfMonth != null) ? dayOfMonth.intValue() : startDateDateOnly.get(Calendar.DAY_OF_MONTH);
        } else if (recurrenceType.equalsIgnoreCase(RELATIVE_RECURRENCE)) {
            pattern.type = RecurrencePatternType.RELATIVE_MONTHLY;
            pattern.daysOfWeek = getDayOfWeeks(List.of(repeatDay));
            pattern.index = getWeekIndex(repeatIndex);
        }
    }

    /**
     * This function will return the integer for the repeat month
     *
     * @param repeatMonth month
     * @return integer for the month
     */
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

    /**
     * This function will return the {@link WeekIndex} for the repeat index of the week or month
     *
     * @param repeatIndex input repeat index
     * @return {@link WeekIndex}
     */
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

    /**
     * This function will get the list of days of week for recurring event
     *
     * @param daysOfWeek input days of week list
     * @return {@link DayOfWeek} list for the recurring event
     */
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

    /**
     * This function will get reminder Integer form the given reminder string
     *
     * @param reminder reminder for the event
     * @return minutes before the event
     */
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

    /**
     * This function will give list of {@link Recipient} from the given attendee emails list
     *
     * @param attendeeEmail list of attendee emails
     * @return List of {@link Recipient}
     */
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

    /**
     * This function will get the list of {@link AttendeeBase} for scheduling a meeting with required attendees
     *
     * @param requiredAttendee list of required attendee emails
     * @param optionalAttendee list of optional attendee emails
     * @return list of {@link AttendeeBase} for the required attendees
     */
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

    /**
     * This function will give {@link DateTimeTimeZone} for the given timestamp and time zone
     *
     * @param timeStamp      input timestamp
     * @param timeZone       timezone for event
     * @param clientTimeZone timezone of the client scheduling the event
     * @return {@link DateTimeTimeZone} object
     */
    public static DateTimeTimeZone getDateTimeTimeZone(Long timeStamp, String timeZone, String clientTimeZone) {
        SimpleDateFormat sfd = new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS);
        sfd.setTimeZone(TimeZone.getTimeZone(clientTimeZone));
        DateTimeTimeZone dateTimeTimeZone = new DateTimeTimeZone();
        dateTimeTimeZone.dateTime = sfd.format(timeStamp);
        dateTimeTimeZone.timeZone = (timeZone != null) ? timeZone : clientTimeZone;
        return dateTimeTimeZone;
    }

    public static long convertDateTimeToEpochInTimeZone(String date, String timeZone) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        formatter.setTimeZone(TimeZone.getTimeZone(UTC));
        Date parsedDate = formatter.parse(date);
        try {
            ZoneId zoneId = ZoneId.of(timeZone);
            ZonedDateTime zonedDateTime = parsedDate.toInstant().atZone(zoneId);
            return zonedDateTime.toInstant().toEpochMilli();
        } catch (ZoneRulesException cause) {
            throw new IllegalStateException(INVALID_TIME_ZONE_PLEASE_PROVIDE_A_VALID_TIME_ZONE_STRING);
        }

    }

    public static String convertDateToFormattedString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        return formatter.format(date);
    }


}
