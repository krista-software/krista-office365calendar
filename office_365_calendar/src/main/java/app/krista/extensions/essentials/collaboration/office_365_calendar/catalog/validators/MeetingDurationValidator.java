/*
 * Office365Calendar Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;
import app.krista.extension.authorization.MustAuthorizeException;

/**
 * Validator for meeting duration field.
 * <p>
 * This validator ensures that meeting duration is provided in a valid ISO 8601 duration format
 * that is compatible with Microsoft Graph API. The format should be like:
 * - "30M" (30 minutes)
 * - "1H" (1 hour)
 * - "2H30M" (2 hours 30 minutes)
 * - "90M" (90 minutes)
 * </p>
 */
public class MeetingDurationValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeetingDurationValidator.class);

    // Pattern to match valid duration formats: [number]H[number]M, [number]H, or [number]M
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(?:(\\d+)H)?(?:(\\d+)M)?$");
    
    // Minimum duration: 15 minutes
    private static final int MIN_DURATION_MINUTES = 15;
    
    // Maximum duration: 8 hours (480 minutes)
    private static final int MAX_DURATION_MINUTES = 480;

    @Override
    public Boolean validate(String meetingDuration, Map<ValidationResource, String> context) {
        LOGGER.debug("→ MeetingDurationValidator.validate()");
        LOGGER.debug("  → Input meeting duration: '{}'", meetingDuration);

        // Meeting duration is optional, so null/empty is considered valid
        if (meetingDuration == null || meetingDuration.trim().isEmpty()) {
            LOGGER.debug("  → Meeting duration is null/empty - considered valid (optional field)");
            return true;
        }

        String trimmedDuration = meetingDuration.trim();
        LOGGER.debug("  → Trimmed duration: '{}'", trimmedDuration);

        // Check if the format matches the expected pattern
        if (!DURATION_PATTERN.matcher(trimmedDuration).matches()) {
            LOGGER.debug("  → Duration format is invalid - does not match pattern");
            return false;
        }

        try {
            // Parse and validate the duration
            int totalMinutes = parseDurationToMinutes(trimmedDuration);
            LOGGER.debug("  → Parsed total minutes: {}", totalMinutes);

            // Check if duration is within acceptable range
            if (totalMinutes < MIN_DURATION_MINUTES) {
                LOGGER.debug("  → Duration too short - minimum is {} minutes", MIN_DURATION_MINUTES);
                return false;
            }

            if (totalMinutes > MAX_DURATION_MINUTES) {
                LOGGER.debug("  → Duration too long - maximum is {} minutes", MAX_DURATION_MINUTES);
                return false;
            }

            LOGGER.debug("  → Meeting duration validation passed");
            return true;

        } catch (MustAuthorizeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("  → Exception during duration parsing: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Parses a duration string like "2H30M", "1H", or "90M" into total minutes.
     * 
     * @param duration the duration string to parse
     * @return total minutes
     */
    private int parseDurationToMinutes(String duration) {
        LOGGER.debug("    → Parsing duration to minutes: '{}'", duration);
        
        java.util.regex.Matcher matcher = DURATION_PATTERN.matcher(duration);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration format: " + duration);
        }
        
        String hoursStr = matcher.group(1);
        String minutesStr = matcher.group(2);
        
        int hours = 0;
        int minutes = 0;
        
        if (hoursStr != null && !hoursStr.isEmpty()) {
            hours = Integer.parseInt(hoursStr);
            LOGGER.debug("    → Parsed hours: {}", hours);
        }
        
        if (minutesStr != null && !minutesStr.isEmpty()) {
            minutes = Integer.parseInt(minutesStr);
            LOGGER.debug("    → Parsed minutes: {}", minutes);
        }
        
        // Must have at least hours or minutes
        if (hours == 0 && minutes == 0) {
            throw new IllegalArgumentException("Duration must specify at least hours or minutes");
        }
        
        int totalMinutes = (hours * 60) + minutes;
        LOGGER.debug("    → Total minutes calculated: {}", totalMinutes);
        
        return totalMinutes;
    }

    @Override
    public String getFetchFieldName() {
        return Constants.MEETING_DURATION;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter the meeting duration. Examples: '30M' for 30 minutes, '1H' for 1 hour, or '2H30M' for 2 hours 30 minutes. Duration must be between 15 minutes and 8 hours.";
    }

    @Override
    public String getConfirmationStepMessage(String meetingDuration, Map<ValidationResource, String> context) {
        if (meetingDuration == null || meetingDuration.trim().isEmpty()) {
            return "Meeting duration is required. Would you like to enter a duration? Examples: '30M' for 30 minutes, '1H' for 1 hour, or '2H30M' for 2 hours 30 minutes.";
        }

        return "The duration '" + meetingDuration + "' is invalid. Would you like to try again? Please use format like '30M' (30 minutes), '1H' (1 hour), or '2H30M' (2 hours 30 minutes). Duration must be between 15 minutes and 8 hours.";
    }

    @Override
    public String getErrMessage(String meetingDuration) {
        if (meetingDuration == null || meetingDuration.trim().isEmpty()) {
            return "Meeting duration is required. Please enter a valid duration.";
        }

        return "Invalid duration format: '" + meetingDuration + "'. Please use format like '30M', '1H', or '2H30M'. Duration must be between 15 minutes and 8 hours.";
    }
}
