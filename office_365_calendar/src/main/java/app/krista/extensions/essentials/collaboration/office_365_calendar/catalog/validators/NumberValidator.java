package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;

import java.util.Map;

/**
 * Validator implementation for validating number inputs within the Office 365 calendar extension.
 * <p>
 * This class validates that number inputs are valid positive integers greater than 0,
 * specifically for parameters like number of meetings to retrieve.
 * It provides user-facing messages for fetch, confirmation, and error steps.
 * </p>
 */
public class NumberValidator implements Validator {

    @Override
    public Boolean validate(String numberStr, Map<ValidationResource, String> context) {
        try {
            if (numberStr == null || numberStr.isEmpty()) {
                return false;
            }

            // Try to parse as double first (since it comes from Double parameter)
            double number = Double.parseDouble(numberStr);

            // Check if it's a positive number
            if (number <= 0) {
                return false;
            }

            // Check if it's an integer (allow for floating point precision issues)
            if (Math.abs(number - Math.round(number)) > 0.0001) {
                return false;
            }

            // Check if it's within reasonable bounds (e.g., max 1000 meetings)
            return number <= 1000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return Constants.NUMBER_OF_MEETINGS;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid positive number (1-1000) for the number of meetings to retrieve.";
    }

    @Override
    public String getConfirmationStepMessage(String numberStr, Map<ValidationResource, String> context) {
        return String.format("The provided number of meetings: %s is invalid. Please enter a positive number between 1 and 1000.", numberStr);
    }

    @Override
    public String getErrMessage(String numberStr) {
        return String.format("Invalid number of meetings: %s. Must be a positive number between 1 and 1000.", numberStr);
    }
}
