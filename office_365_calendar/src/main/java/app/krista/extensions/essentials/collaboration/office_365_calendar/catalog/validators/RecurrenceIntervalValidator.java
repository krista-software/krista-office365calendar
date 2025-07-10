package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;

import java.util.Map;

/**
 * Validator implementation for validating recurrence interval inputs within the Office 365 calendar extension.
 * <p>
 * This class validates that recurrence interval inputs are valid positive integers greater than 0,
 * specifically for parameters like recurrence interval for recurring meetings.
 * It provides user-facing messages for fetch, confirmation, and error steps.
 * </p>
 */
public class RecurrenceIntervalValidator implements Validator {

    @Override
    public Boolean validate(String intervalStr, Map<ValidationResource, String> context) {
        try {
            if (intervalStr == null || intervalStr.isEmpty()) {
                // Recurrence interval is optional, so null/empty is considered valid
                return true;
            }

            // Try to parse as double first (since it comes from Double parameter)
            double interval = Double.parseDouble(intervalStr);

            // Check if it's a positive number
            if (interval <= 0) {
                return false;
            }

            // Check if it's an integer (allow for floating point precision issues)
            if (Math.abs(interval - Math.round(interval)) > 0.0001) {
                return false;
            }

            // Check if it's within reasonable bounds (e.g., max 365 for daily, 52 for weekly, etc.)
            return interval <= 365;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return "Recurrence Interval";
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid positive number (1-365) for the recurrence interval.";
    }

    @Override
    public String getConfirmationStepMessage(String intervalStr, Map<ValidationResource, String> context) {
        return String.format("The provided recurrence interval: %s is invalid. Please enter a positive number between 1 and 365.", intervalStr);
    }

    @Override
    public String getErrMessage(String intervalStr) {
        return String.format("Invalid recurrence interval: %s. Must be a positive number between 1 and 365.", intervalStr);
    }
}
