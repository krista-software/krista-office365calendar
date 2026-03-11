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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for EmailValidator static methods.
 * Tests the utility methods for email extraction and validation.
 */
public class EmailValidatorTest {

    // Tests for emailExtractor method

    @Test
    public void shouldReturnNullForNullInput() {
        // Act
        String result = EmailValidator.emailExtractor(null);

        // Assert
        assertNull(result);
    }

    @Test
    public void shouldReturnOriginalStringForEmptyInput() {
        // Arrange
        String input = "";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals(input, result);
    }

    @Test
    public void shouldExtractValidEmailFromSimpleString() {
        // Arrange
        String input = "user@company.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user@company.com", result);
    }

    @Test
    public void shouldExtractEmailFromStringWithSurroundingText() {
        // Arrange
        String input = "Contact John Doe at john.doe@company.com for more information";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("john.doe@company.com", result);
    }

    @Test
    public void shouldExtractFirstEmailWhenMultipleEmailsPresent() {
        // Arrange
        String input = "Contact john@company.com or jane@company.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("john@company.com", result);
    }

    @Test
    public void shouldExtractEmailWithNumbersInUsername() {
        // Arrange
        String input = "user123@company.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user123@company.com", result);
    }

    @Test
    public void shouldExtractEmailWithDotsInUsername() {
        // Arrange
        String input = "first.last@company.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("first.last@company.com", result);
    }

    @Test
    public void shouldExtractEmailWithPlusSignInUsername() {
        // Arrange
        String input = "user+tag@company.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user+tag@company.com", result);
    }

    @Test
    public void shouldExtractEmailWithUnderscoreInUsername() {
        // Arrange
        String input = "user_name@company.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user_name@company.com", result);
    }

    @Test
    public void shouldExtractEmailWithHyphenInUsername() {
        // Arrange
        String input = "user-name@company.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user-name@company.com", result);
    }

    @Test
    public void shouldExtractEmailWithSubdomain() {
        // Arrange
        String input = "user@mail.company.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user@mail.company.com", result);
    }

    @Test
    public void shouldExtractEmailWithMultipleSubdomains() {
        // Arrange
        String input = "user@mail.internal.company.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user@mail.internal.company.com", result);
    }

    @Test
    public void shouldExtractEmailWithCountryCodeTLD() {
        // Arrange
        String input = "user@company.co.uk";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user@company.co.uk", result);
    }

    @Test
    public void shouldExtractEmailWithLongTLD() {
        // Arrange
        String input = "user@company.museum";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user@company.museum", result);
    }

    @Test
    public void shouldReturnOriginalStringWhenNoEmailPatternFound() {
        // Arrange
        String input = "This is just plain text without email";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals(input, result);
    }

    @Test
    public void shouldReturnOriginalStringForInvalidEmailFormat() {
        // Arrange
        String input = "invalid@";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals(input, result);
    }

    @Test
    public void shouldReturnOriginalStringForEmailWithoutDomain() {
        // Arrange
        String input = "user@";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals(input, result);
    }

    @Test
    public void shouldReturnOriginalStringForEmailWithoutUsername() {
        // Arrange
        String input = "@company.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals(input, result);
    }

    @Test
    public void shouldHandleStringWithOnlyAtSymbol() {
        // Arrange
        String input = "@";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals(input, result);
    }

    @Test
    public void shouldExtractEmailFromStringWithSpecialCharacters() {
        // Arrange
        String input = "Email: <user@company.com> (primary contact)";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user@company.com", result);
    }

    @Test
    public void shouldHandleWhitespaceAroundEmail() {
        // Arrange
        String input = "   user@company.com   ";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user@company.com", result);
    }

    @Test
    public void shouldExtractEmailWithNumbersInDomain() {
        // Arrange
        String input = "user@company123.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user@company123.com", result);
    }

    @Test
    public void shouldExtractEmailWithHyphensInDomain() {
        // Arrange
        String input = "user@my-company.com";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("user@my-company.com", result);
    }

    @Test
    public void shouldHandleComplexEmailPattern() {
        // Arrange
        String input = "Please contact first.last+tag@sub.company-name.co.uk for assistance";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("first.last+tag@sub.company-name.co.uk", result);
    }

    @Test
    public void shouldHandleEmailInParentheses() {
        // Arrange
        String input = "John Smith (john.smith@company.com)";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("john.smith@company.com", result);
    }

    @Test
    public void shouldHandleEmailInBrackets() {
        // Arrange
        String input = "Contact [support@company.com] for help";

        // Act
        String result = EmailValidator.emailExtractor(input);

        // Assert
        assertEquals("support@company.com", result);
    }
}
