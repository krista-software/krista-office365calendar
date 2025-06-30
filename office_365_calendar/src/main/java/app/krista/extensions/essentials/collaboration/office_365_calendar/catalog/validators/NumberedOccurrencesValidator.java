package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;

import java.util.Map;

/**
 * Validator implementation for validating numbered occurrences inputs within the Office 365 calendar extension.
 * <p>
 * This class validates that numbered occurrences inputs are valid positive integers greater than 0,
 * specifically for parameters like number of occurrences for recurring meetings.
 * It provides user-facing messages for fetch, confirmation, and error steps.
 * </p>
 */
public class NumberedOccurrencesValidator implements Validator {

    @Override
    public Boolean validate(String occurrencesStr, Map<ValidationResource, String> context) {
        try {
            if (occurrencesStr == null || occurrencesStr.isEmpty()) {
                // Numbered occurrences is optional, so null/empty is considered valid
                return true;
            }

            // Try to parse as double first (since it comes from Double parameter)
            double occurrences = Double.parseDouble(occurrencesStr);

            // Check if it's a positive number
            if (occurrences <= 0) {
                return false;
            }

            // Check if it's an integer (allow for floating point precision issues)
            if (Math.abs(occurrences - Math.round(occurrences)) > 0.0001) {
                return false;
            }

            // Check if it's within reasonable bounds (e.g., max 999 occurrences)
            return occurrences <= 999;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return "Numbered Occurrences";
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid positive number (1-999) for the numbered occurrences.";
    }

    @Override
    public String getConfirmationStepMessage(String occurrencesStr, Map<ValidationResource, String> context) {
        return String.format("The provided numbered occurrences: %s is invalid. Please enter a positive number between 1 and 999.", occurrencesStr);
    }

    @Override
    public String getErrMessage(String occurrencesStr) {
        return String.format("Invalid numbered occurrences: %s. Must be a positive number between 1 and 999.", occurrencesStr);
    }
}
