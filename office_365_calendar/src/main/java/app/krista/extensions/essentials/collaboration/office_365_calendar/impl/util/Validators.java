package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import java.io.IOException;
import java.util.List;

public class Validators {

    private Validators() {

    }

    public static void validateString(String inputString, String errorMsg) {
        if (inputString == null || inputString.isBlank()) {
            throw new IllegalArgumentException("Given " + errorMsg + " is null or blank.");
        }
    }

    public static void validateStringsList(List<String> inputList, String errorMsg) {
        if (inputList == null || inputList.isEmpty()) {
            throw new IllegalArgumentException("Given " + errorMsg + " list is null or empty.");
        }
    }

    public static void validateRecurrenceValues(String recurrenceValue, List<String> daysOfWeek, String recurrenceType, Double dayOfMonth, String repeatDay) {
        validateString(recurrenceValue, "recurrence value");
        if (!recurrenceValue.equalsIgnoreCase("Does not repeat") &&
                !recurrenceValue.equalsIgnoreCase("Never")) {
            if (isMonthlyOrYearlyRecurrence(recurrenceValue) &&
                    recurrenceType.equalsIgnoreCase("Absolute Recurrence") &&
                    (dayOfMonth == null || (dayOfMonth < 1 || dayOfMonth > 31))) {
                throw new IllegalArgumentException("Please provide valid day of month!");
            }
            if (isMonthlyOrYearlyRecurrence(recurrenceValue) &&
                    recurrenceType.equalsIgnoreCase("Relative Recurrence")) {
                validateString(repeatDay, "repeat day");
            }
            if (recurrenceValue.equalsIgnoreCase("Weekly")) {
                validateStringsList(daysOfWeek, "repeat days of week");
            }
        }
    }

    private static boolean isMonthlyOrYearlyRecurrence(String recurrenceValue) {
        return recurrenceValue.equalsIgnoreCase("Monthly") ||
                recurrenceValue.equalsIgnoreCase("Yearly");
    }

    public static void validateTimeZone(String timeZone) throws IOException {
        List<String> timeZones = List.of("Etc/GMT+12", "Etc/GMT+11", "Pacific/Honolulu", "America/Anchorage", "America/Santa_Isabel",
                "America/Los_Angeles", "America/Phoenix", "America/Chihuahua", "America/Denver", "America/Guatemala", "America/Chicago",
                "America/Mexico_City", "America/Regina", "America/Bogota", "America/New_York", "America/Indiana/Indianapolis", "America/Caracas",
                "America/Asuncion", "America/Halifax", "America/Cuiaba", "America/La_Paz", "America/Santiago", "America/St_Johns", "America/Sao_Paulo",
                "America/Argentina/Buenos_Aires", "America/Cayenne", "America/Godthab", "America/Montevideo", "America/Bahia", "Etc/GMT+2",
                "Atlantic/Azores", "Atlantic/Cape_Verde", "Africa/Casablanca", "Etc/GMT", "Europe/London", "Atlantic/Reykjavik", "Europe/Berlin",
                "Europe/Budapest", "Europe/Paris", "Europe/Warsaw", "Africa/Lagos", "Africa/Windhoek", "Europe/Bucharest", "Asia/Beirut",
                "Africa/Cairo", "Asia/Damascus", "Africa/Johannesburg", "Europe/Kyiv (Kiev)", "Europe/Istanbul", "Asia/Jerusalem", "Asia/Amman",
                "Asia/Baghdad", "Europe/Kaliningrad", "Asia/Riyadh", "Africa/Nairobi", "Asia/Tehran", "Asia/Dubai", "Asia/Baku",
                "Europe/Moscow", "Indian/Mauritius", "Asia/Tbilisi", "Asia/Yerevan", "Asia/Kabul", "Asia/Karachi", "Asia/Toshkent (Tashkent)", "Asia/Kolkata",
                "Asia/Colombo", "Asia/Kathmandu", "Asia/Astana (Almaty)", "Asia/Dhaka", "Asia/Yekaterinburg", "Asia/Yangon (Rangoon)", "Asia/Bangkok", "Asia/Novosibirsk",
                "Asia/Shanghai", "Asia/Krasnoyarsk", "Asia/Singapore", "Australia/Perth", "Asia/Taipei", "Asia/Ulaanbaatar", "Asia/Irkutsk",
                "Asia/Tokyo", "Asia/Seoul", "Australia/Adelaide", "Australia/Darwin", "Australia/Brisbane", "Australia/Sydney", "Pacific/Port_Moresby", "Australia/Hobart",
                "Asia/Yakutsk", "Pacific/Guadalcanal", "Asia/Vladivostok", "Pacific/Auckland", "Etc/GMT-12", "Pacific/Fiji", "Asia/Magadan", "Pacific/Tongatapu",
                "Pacific/Apia", "Pacific/Kiritimati");
        if (timeZone != null && !timeZones.contains(timeZone)) {
            throw new IOException(Constants.PLEASE_PROVIDE_SUPPORTED_TIME_ZONE);
        }
    }
}
