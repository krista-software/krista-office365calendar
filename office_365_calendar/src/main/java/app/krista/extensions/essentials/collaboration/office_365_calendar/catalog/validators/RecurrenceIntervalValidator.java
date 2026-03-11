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
        return "Please enter how often the meeting should repeat (between 1 and 365). For example, enter '1' for every occurrence, '2' for every other occurrence.";
    }

    @Override
    public String getConfirmationStepMessage(String intervalStr, Map<ValidationResource, String> context) {
        return String.format("The recurrence interval '%s' is invalid. Please enter a whole number between 1 and 365.", intervalStr);
    }

    @Override
    public String getErrMessage(String intervalStr) {
        return String.format("Invalid recurrence interval: '%s'. Please enter a whole number between 1 and 365.", intervalStr);
    }
}
