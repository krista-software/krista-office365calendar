package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;

import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

/**
 * Validator implementation for validating time zone identifiers within the Office 365 calendar extension.
 * <p>
 * This class validates time zone strings against the standard Java ZoneId available zones,
 * and provides user-facing messages for fetch, confirmation, and error steps.
 * It ensures that only valid time zone identifiers are accepted for calendar operations.
 * </p>
 */
public class TimeZoneValidator implements Validator {

    private static final Set<String> VALID_TIME_ZONES = ZoneId.getAvailableZoneIds();

    @Override
    public Boolean validate(String timeZone, Map<ValidationResource, String> context) {
        if (timeZone == null || timeZone.trim().isEmpty()) {
            // Time zone is optional in most cases, so null/empty is considered valid
            return true;
        }
        
        // Check if the provided time zone is a valid ZoneId
        try {
            ZoneId.of(timeZone.trim());
            return true;
        } catch (Exception e) {
            // If ZoneId.of() throws an exception, the time zone is invalid
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return Constants.TIME_ZONE;
    }

    @Override
    public String getFetchStepMessage() {
        return Constants.PLEASE_PROVIDE_SUPPORTED_TIME_ZONE;
    }

    @Override
    public String getConfirmationStepMessage(String timeZone, Map<ValidationResource, String> context) {
        return String.format("The time zone '%s' is not recognized. Please use a standard time zone identifier such as 'America/New_York', 'Europe/London', 'Asia/Tokyo', or 'UTC'.", timeZone);
    }

    @Override
    public String getErrMessage(String timeZone) {
        return String.format("Invalid time zone: %s. Please use a standard time zone identifier (e.g., 'America/New_York', 'Europe/London', 'UTC').", timeZone);
    }

    /**
     * Utility method to check if a time zone string is valid.
     * 
     * @param timeZone the time zone string to validate
     * @return true if the time zone is valid, false otherwise
     */
    public static boolean isValidTimeZone(String timeZone) {
        if (timeZone == null || timeZone.trim().isEmpty()) {
            return true; // Empty/null is considered valid (optional field)
        }
        
        try {
            ZoneId.of(timeZone.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the set of all available time zone IDs.
     * 
     * @return a set of valid time zone identifiers
     */
    public static Set<String> getAvailableTimeZones() {
        return VALID_TIME_ZONES;
    }
}
