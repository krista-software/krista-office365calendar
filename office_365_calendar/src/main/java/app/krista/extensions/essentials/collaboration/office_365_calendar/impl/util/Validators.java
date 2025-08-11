package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Utility class providing common validation methods for Office 365 calendar integration.
 * <p>
 * This class contains static helper methods to validate input data such as email addresses,
 * event IDs, and other parameters required for collaboration and calendar operations.
 * </p>
 */
public class Validators {

    /**
     * Adds an attribute to the map if the value is not null.
     *
     * @param attributeMap   the map to add the attribute to
     * @param attributeName  the name of the attribute
     * @param attributeValue the value of the attribute
     */
    public static void addAttributeIfNotNull(Map<String, Object> attributeMap, String attributeName,
                                           Object attributeValue) {
        if (attributeValue != null) {
            attributeMap.put(attributeName, attributeValue);
        }
    }

    /**
     * This function will validate if the string is empty or null
     *
     * @param inputString input string
     * @param errorMsg    variable to be displayed for error message
     */
    public static void validateString(String inputString, String errorMsg) {
        if (inputString == null || inputString.isBlank()) {
            throw new IllegalArgumentException("Given " + errorMsg + " is null or blank.");
        }
    }

    /**
     * This function will return the empty or input string
     *
     * @param inputString input string
     */
    public static String getNullOrEmptyDefault(String inputString) {
        return inputString == null ? "" : inputString;
    }

    /**
     * This function will validate if the list is null or empty
     *
     * @param inputList input list
     * @param errorMsg  type of list to be displayed for error message
     */
    public static void validateStringsList(List<String> inputList, String errorMsg) {
        if (inputList == null || inputList.isEmpty()) {
            throw new IllegalArgumentException("Given " + errorMsg + " list is null or empty.");
        }
    }

    /**
     * This function will validate for the recurrence pattern values
     *
     * @param recurrenceValue recurrence value for repeating the event
     * @param daysOfWeek      days of week in recurring event
     * @param recurrenceType  absolute or relative recurrence
     * @param dayOfMonth      of the month in the recurring event
     * @param repeatDay       repeat day of week in the recurring event
     */
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

    /**
     * This function will check for monthly recurrence or yearly recurrence
     *
     * @param recurrenceValue recurrence value monthly or yearly
     * @return {@link Boolean} value
     */
    private static boolean isMonthlyOrYearlyRecurrence(String recurrenceValue) {
        return recurrenceValue.equalsIgnoreCase("Monthly") ||
                recurrenceValue.equalsIgnoreCase("Yearly");
    }

    /**
     * This function will validate with the supported time zones
     *
     * @param timeZone input timezone
     * @throws IOException if there is error in input
     */
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
