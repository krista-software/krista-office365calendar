package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validator implementation for validating days of week inputs within the Office 365 calendar extension.
 * <p>
 * This class validates that days of week inputs contain valid day names and are properly formatted.
 * It provides user-facing messages for fetch, confirmation, and error steps.
 * </p>
 */
public class DaysOfWeekValidator implements Validator {

    private static final Set<String> VALID_DAYS = new HashSet<>(Arrays.asList(
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    ));

    private static final Gson gson = new Gson();

    @Override
    public Boolean validate(String daysStr, Map<ValidationResource, String> context) {
        try {
            if (daysStr == null || daysStr.trim().isEmpty()) {
                // Days of week is optional, so null/empty is considered valid
                return true;
            }

            // Try to parse as JSON array
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> days = gson.fromJson(daysStr, listType);

            if (days == null || days.isEmpty()) {
                // Empty list is valid
                return true;
            }

            // Check if all days are valid
            for (String day : days) {
                if (day == null || !VALID_DAYS.contains(day.trim())) {
                    return false;
                }
            }

            // Check for duplicates
            Set<String> uniqueDays = new HashSet<>();
            for (String day : days) {
                if (!uniqueDays.add(day.trim())) {
                    return false; // Duplicate found
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return "Days Of Week";
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid days of the week separated by commas (e.g., Monday, Wednesday, Friday). Valid days are: Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday.";
    }

    @Override
    public String getConfirmationStepMessage(String daysStr, Map<ValidationResource, String> context) {
        return String.format("The days you entered '%s' contain invalid day names or duplicates. Please use valid day names like 'Monday, Tuesday, Wednesday' without duplicates.", daysStr);
    }

    @Override
    public String getErrMessage(String daysStr) {
        return String.format("Invalid days of the week: %s. Please enter valid day names separated by commas (e.g., Monday, Wednesday, Friday).", daysStr);
    }
}
