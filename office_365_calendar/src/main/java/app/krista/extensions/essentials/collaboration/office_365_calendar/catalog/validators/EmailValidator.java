package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return "Please enter valid email.";
    }

    @Override
    public String getConfirmationStepMessage(String emailId, Map<ValidationResource, String> context) {
        return String.format("The provided email: %s does not exist. Please check.", emailId);
    }

    @Override
    public String getErrMessage(String emailId) {
        return String.format("Invalid email: %s", emailId);
    }

    public static String emailExtractor(String email) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        if (matcher.find()) {
            return matcher.group();
        }
        return email;
    }
}
