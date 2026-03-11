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

public class ToDateValidator implements Validator {

    @Override
    public Boolean validate(String dateRange, Map<ValidationResource, String> context) {
        try {
            String fromDateStr = context.get(ValidationResource.FROM_DATE);
            String toDateStr = context.get(ValidationResource.TO_DATE);
            
            if (fromDateStr == null || fromDateStr.isEmpty() || toDateStr == null || toDateStr.isEmpty()) {
                return false;
            }
            
            Long fromDate = Long.parseLong(fromDateStr);
            Long toDate = Long.parseLong(toDateStr);
            
            // Validate that from date is before to date
            return fromDate <= toDate;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return Constants.TO_DATE;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please select an end date that is after or equal to the start date.";
    }

    @Override
    public String getConfirmationStepMessage(String dateRange, Map<ValidationResource, String> context) {
        return "The 'To' date must be after or equal to the 'From' date. Would you like to adjust your date range?";
    }

    @Override
    public String getErrMessage(String dateRange) {
        return "Invalid date range: The 'To' date must be after or equal to the 'From' date. Please select a valid date range.";
    }
}