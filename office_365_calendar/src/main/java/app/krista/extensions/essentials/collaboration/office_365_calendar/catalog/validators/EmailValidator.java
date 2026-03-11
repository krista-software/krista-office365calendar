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
