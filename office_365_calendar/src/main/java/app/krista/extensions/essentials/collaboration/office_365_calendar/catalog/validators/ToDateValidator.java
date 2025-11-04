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