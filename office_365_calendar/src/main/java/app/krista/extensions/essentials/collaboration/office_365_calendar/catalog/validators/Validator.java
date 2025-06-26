package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import java.util.Map;

public interface Validator {

    enum ValidationResource {
        EMAIL,
        EVENT_ID,
        FROM_DATE,
        TO_DATE,
        DATE_RANGE,
        NUMBER_OF_MEETINGS,
        DATE_RANGE_TO, ATTENDEES
    }

    Boolean validate(String resourceId, Map<ValidationResource, String> context);

    String getFetchFieldName();

    String getFetchStepMessage();

    String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context);

    String getErrMessage(String resourceId);
}
