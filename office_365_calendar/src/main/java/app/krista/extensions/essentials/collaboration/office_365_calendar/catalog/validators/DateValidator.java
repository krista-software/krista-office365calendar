package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;

import java.util.Map;

/**
 * Validator implementation for validating date ranges within the Office 365 calendar extension.
 * <p>
 * This class validates that date ranges are valid (from date is before to date) and that
 * the dates are not null or invalid. It provides user-facing messages for fetch, confirmation,
 * and error steps related to date validation.
 * </p>
 */
public class DateValidator implements Validator {

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
        return Constants.FROM_DATE;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please select a valid date range. The 'From' date must be before or equal to the 'To' date.";
    }

    @Override
    public String getConfirmationStepMessage(String dateRange, Map<ValidationResource, String> context) {
        return "The date range is invalid. The 'From' date must be before or equal to the 'To' date. Would you like to adjust your dates?";
    }

    @Override
    public String getErrMessage(String dateRange) {
        return "Invalid date range: The 'From' date must be before or equal to the 'To' date. Please select a valid date range.";
    }
}
