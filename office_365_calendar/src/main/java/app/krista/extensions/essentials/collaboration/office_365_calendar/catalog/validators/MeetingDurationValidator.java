package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;

import java.util.Map;
import java.util.regex.Pattern;

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

    // Pattern to match valid duration formats: [number]H[number]M, [number]H, or [number]M
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(?:(\\d+)H)?(?:(\\d+)M)?$");
    
    // Minimum duration: 15 minutes
    private static final int MIN_DURATION_MINUTES = 15;
    
    // Maximum duration: 8 hours (480 minutes)
    private static final int MAX_DURATION_MINUTES = 480;

    @Override
    public Boolean validate(String meetingDuration, Map<ValidationResource, String> context) {
        System.out.println("→ MeetingDurationValidator.validate()");
        System.out.println("  → Input meeting duration: '" + meetingDuration + "'");
        
        // Meeting duration is optional, so null/empty is considered valid
        if (meetingDuration == null || meetingDuration.trim().isEmpty()) {
            System.out.println("  → Meeting duration is null/empty - considered valid (optional field)");
            return true;
        }
        
        String trimmedDuration = meetingDuration.trim();
        System.out.println("  → Trimmed duration: '" + trimmedDuration + "'");
        
        // Check if the format matches the expected pattern
        if (!DURATION_PATTERN.matcher(trimmedDuration).matches()) {
            System.out.println("  → Duration format is invalid - does not match pattern");
            return false;
        }
        
        try {
            // Parse and validate the duration
            int totalMinutes = parseDurationToMinutes(trimmedDuration);
            System.out.println("  → Parsed total minutes: " + totalMinutes);
            
            // Check if duration is within acceptable range
            if (totalMinutes < MIN_DURATION_MINUTES) {
                System.out.println("  → Duration too short - minimum is " + MIN_DURATION_MINUTES + " minutes");
                return false;
            }
            
            if (totalMinutes > MAX_DURATION_MINUTES) {
                System.out.println("  → Duration too long - maximum is " + MAX_DURATION_MINUTES + " minutes");
                return false;
            }
            
            System.out.println("  → Meeting duration validation passed");
            return true;
            
        } catch (Exception e) {
            System.out.println("  → Exception during duration parsing: " + e.getMessage());
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
        System.out.println("    → Parsing duration to minutes: '" + duration + "'");
        
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
            System.out.println("    → Parsed hours: " + hours);
        }
        
        if (minutesStr != null && !minutesStr.isEmpty()) {
            minutes = Integer.parseInt(minutesStr);
            System.out.println("    → Parsed minutes: " + minutes);
        }
        
        // Must have at least hours or minutes
        if (hours == 0 && minutes == 0) {
            throw new IllegalArgumentException("Duration must specify at least hours or minutes");
        }
        
        int totalMinutes = (hours * 60) + minutes;
        System.out.println("    → Total minutes calculated: " + totalMinutes);
        
        return totalMinutes;
    }

    @Override
    public String getFetchFieldName() {
        return Constants.MEETING_DURATION;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please provide a valid meeting duration in format like '30M' (30 minutes), '1H' (1 hour), or '2H30M' (2 hours 30 minutes). Duration must be between 15 minutes and 8 hours.";
    }

    @Override
    public String getConfirmationStepMessage(String meetingDuration, Map<ValidationResource, String> context) {
        if (meetingDuration == null || meetingDuration.trim().isEmpty()) {
            return "Meeting duration is not specified. Please provide a valid duration in format like '30M', '1H', or '2H30M'.";
        }
        
        return "The meeting duration '" + meetingDuration + "' is invalid. Please provide a valid duration in format like '30M' (30 minutes), '1H' (1 hour), or '2H30M' (2 hours 30 minutes). Duration must be between 15 minutes and 8 hours.";
    }

    @Override
    public String getErrMessage(String meetingDuration) {
        if (meetingDuration == null || meetingDuration.trim().isEmpty()) {
            return "Invalid meeting duration: duration is required.";
        }
        
        return "Invalid meeting duration format: '" + meetingDuration + "'. Expected format like '30M', '1H', or '2H30M'. Duration must be between 15 minutes and 8 hours.";
    }
}
