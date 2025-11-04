package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator implementation for validating email addresses within the Office 365 calendar extension.
 * <p>
 * This class uses Apache Commons EmailValidator to check the validity of email addresses,
 * and provides user-facing messages for fetch, confirmation, and error steps.
 * It also includes a utility method to extract a valid email address from a given string using regex.
 * </p>
 */
public class EmailValidator implements Validator {

    @Override
    public Boolean validate(String emailId, Map<ValidationResource, String> context) {
        org.apache.commons.validator.routines.EmailValidator emailValidator = org.apache.commons.validator.routines.EmailValidator.getInstance();
        return emailValidator.isValid(emailExtractor(context.get(ValidationResource.EMAIL)));
    }

    @Override
    public String getFetchFieldName() {
        return Constants.EMAIL;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid email address. Example: user@company.com";
    }

    @Override
    public String getConfirmationStepMessage(String emailId, Map<ValidationResource, String> context) {
        return String.format("We couldn't find the email address '%s'. This email may not exist or may be invalid. Would you like to try a different email address?", emailId);
    }

    @Override
    public String getErrMessage(String emailId) {
        return String.format("Invalid email address: '%s'. Please use a valid email format (e.g., user@company.com).", emailId);
    }

    /**
     * Extracts a valid email address from the given string using a regular expression.
     *
     * @param email The input string potentially containing an email address.
     * @return The extracted email address if found, otherwise returns the original input.
     */
    public static String emailExtractor(String email) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        Pattern pattern = Pattern.compile(emailPattern);
        if (email != null) {
            Matcher matcher = pattern.matcher(email);
            if (matcher.find()) {
                return matcher.group();
            }
            return email;
        }
        return null;
    }
}
