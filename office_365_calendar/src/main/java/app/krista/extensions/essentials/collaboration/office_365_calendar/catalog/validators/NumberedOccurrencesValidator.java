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
        return "Please enter how many times the meeting should occur (between 1 and 999).";
    }

    @Override
    public String getConfirmationStepMessage(String occurrencesStr, Map<ValidationResource, String> context) {
        return String.format("The number '%s' is invalid. Please enter a whole number between 1 and 999 for the number of occurrences.", occurrencesStr);
    }

    @Override
    public String getErrMessage(String occurrencesStr) {
        return String.format("Invalid number of occurrences: '%s'. Please enter a whole number between 1 and 999.", occurrencesStr);
    }
}
