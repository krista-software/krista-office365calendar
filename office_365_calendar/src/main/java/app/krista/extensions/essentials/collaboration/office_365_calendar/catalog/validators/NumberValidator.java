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
        return "Please enter how many meetings you'd like to retrieve (between 1 and 1000).";
    }

    @Override
    public String getConfirmationStepMessage(String numberStr, Map<ValidationResource, String> context) {
        return String.format("The number '%s' is invalid. Please enter a whole number between 1 and 1000.", numberStr);
    }

    @Override
    public String getErrMessage(String numberStr) {
        return String.format("Invalid number: '%s'. Please enter a whole number between 1 and 1000.", numberStr);
    }
}
