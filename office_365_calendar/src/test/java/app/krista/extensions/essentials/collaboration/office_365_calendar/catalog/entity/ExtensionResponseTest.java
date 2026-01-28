package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for ExtensionResponse entity class.
 * Tests entity creation and field assignments.
 */
public class ExtensionResponseTest {

    private ExtensionResponse extensionResponse;

    @Before
    public void setUp() {
        extensionResponse = new ExtensionResponse();
    }

    @Test
    public void testDefaultConstructor() {
        // Act
        ExtensionResponse response = new ExtensionResponse();

        // Assert
        assertNotNull("ExtensionResponse should be created", response);
        assertNull("Message should be null by default", response.message);
        assertNull("Technical detailed error report should be null by default", response.technicalDetailedErrorReport);
        assertNull("Response type should be null by default", response.responseType);
        assertNull("Time taken in seconds should be null by default", response.timeTakenInSeconds);
    }

    @Test
    public void testFieldAssignments_SuccessResponse() {
        // Act
        extensionResponse.message = "Operation completed successfully";
        extensionResponse.technicalDetailedErrorReport = "";
        extensionResponse.responseType = "SUCCESS";
        extensionResponse.timeTakenInSeconds = 0.5;

        // Assert
        assertEquals("Message should be set", "Operation completed successfully", extensionResponse.message);
        assertEquals("Technical error report should be empty", "", extensionResponse.technicalDetailedErrorReport);
        assertEquals("Response type should be SUCCESS", "SUCCESS", extensionResponse.responseType);
        assertEquals("Time taken should be 0.5 seconds", Double.valueOf(0.5), extensionResponse.timeTakenInSeconds);
    }

    @Test
    public void testFieldAssignments_ErrorResponse() {
        // Act
        extensionResponse.message = "Operation failed";
        extensionResponse.technicalDetailedErrorReport = "NullPointerException at line 42";
        extensionResponse.responseType = "SYSTEM_ERROR";
        extensionResponse.timeTakenInSeconds = 1.25;

        // Assert
        assertEquals("Message should be set", "Operation failed", extensionResponse.message);
        assertEquals("Technical error report should be set", "NullPointerException at line 42", extensionResponse.technicalDetailedErrorReport);
        assertEquals("Response type should be SYSTEM_ERROR", "SYSTEM_ERROR", extensionResponse.responseType);
        assertEquals("Time taken should be 1.25 seconds", Double.valueOf(1.25), extensionResponse.timeTakenInSeconds);
    }

    @Test
    public void testFieldAssignments_InputError() {
        // Act
        extensionResponse.message = "Invalid input provided";
        extensionResponse.technicalDetailedErrorReport = "Field 'email' is required but was null";
        extensionResponse.responseType = "INPUT_ERROR";
        extensionResponse.timeTakenInSeconds = 0.1;

        // Assert
        assertEquals("Message should be set", "Invalid input provided", extensionResponse.message);
        assertEquals("Technical error report should be set", "Field 'email' is required but was null", extensionResponse.technicalDetailedErrorReport);
        assertEquals("Response type should be INPUT_ERROR", "INPUT_ERROR", extensionResponse.responseType);
        assertEquals("Time taken should be 0.1 seconds", Double.valueOf(0.1), extensionResponse.timeTakenInSeconds);
    }

    @Test
    public void testFieldAssignments_LongMessage() {
        // Arrange
        String longMessage = "This is a very long message that contains detailed information about the operation. ".repeat(10);
        String longErrorReport = "Stack trace: ".repeat(50);

        // Act
        extensionResponse.message = longMessage;
        extensionResponse.technicalDetailedErrorReport = longErrorReport;
        extensionResponse.responseType = "SYSTEM_ERROR";
        extensionResponse.timeTakenInSeconds = 5.0;

        // Assert
        assertEquals("Long message should be set", longMessage, extensionResponse.message);
        assertEquals("Long error report should be set", longErrorReport, extensionResponse.technicalDetailedErrorReport);
        assertEquals("Response type should be SYSTEM_ERROR", "SYSTEM_ERROR", extensionResponse.responseType);
        assertEquals("Time taken should be 5.0 seconds", Double.valueOf(5.0), extensionResponse.timeTakenInSeconds);
    }

    @Test
    public void testFieldAssignments_ZeroTimeTaken() {
        // Act
        extensionResponse.message = "Instant operation";
        extensionResponse.technicalDetailedErrorReport = "";
        extensionResponse.responseType = "SUCCESS";
        extensionResponse.timeTakenInSeconds = 0.0;

        // Assert
        assertEquals("Time taken should be 0.0 seconds", Double.valueOf(0.0), extensionResponse.timeTakenInSeconds);
    }

    @Test
    public void testFieldAssignments_LargeTimeTaken() {
        // Act
        extensionResponse.message = "Long running operation";
        extensionResponse.technicalDetailedErrorReport = "";
        extensionResponse.responseType = "SUCCESS";
        extensionResponse.timeTakenInSeconds = 3600.0; // 1 hour

        // Assert
        assertEquals("Time taken should be 3600.0 seconds", Double.valueOf(3600.0), extensionResponse.timeTakenInSeconds);
    }

    @Test
    public void testFieldAssignments_NullValues() {
        // Act
        extensionResponse.message = null;
        extensionResponse.technicalDetailedErrorReport = null;
        extensionResponse.responseType = null;
        extensionResponse.timeTakenInSeconds = null;

        // Assert
        assertNull("Message should be null", extensionResponse.message);
        assertNull("Technical error report should be null", extensionResponse.technicalDetailedErrorReport);
        assertNull("Response type should be null", extensionResponse.responseType);
        assertNull("Time taken should be null", extensionResponse.timeTakenInSeconds);
    }

    @Test
    public void testFieldAssignments_EmptyStrings() {
        // Act
        extensionResponse.message = "";
        extensionResponse.technicalDetailedErrorReport = "";
        extensionResponse.responseType = "";
        extensionResponse.timeTakenInSeconds = 0.0;

        // Assert
        assertEquals("Message should be empty", "", extensionResponse.message);
        assertEquals("Technical error report should be empty", "", extensionResponse.technicalDetailedErrorReport);
        assertEquals("Response type should be empty", "", extensionResponse.responseType);
        assertEquals("Time taken should be 0.0", Double.valueOf(0.0), extensionResponse.timeTakenInSeconds);
    }

    @Test
    public void testFieldAssignments_SpecialCharacters() {
        // Act
        extensionResponse.message = "Error: <script>alert('test')</script>";
        extensionResponse.technicalDetailedErrorReport = "Stack trace:\n\tat com.example.Test.method(Test.java:42)\n\tat com.example.Main.main(Main.java:10)";
        extensionResponse.responseType = "SYSTEM_ERROR";
        extensionResponse.timeTakenInSeconds = 0.75;

        // Assert
        assertEquals("Message with special characters should be set", "Error: <script>alert('test')</script>", extensionResponse.message);
        assertTrue("Technical error report should contain newlines", extensionResponse.technicalDetailedErrorReport.contains("\n"));
        assertEquals("Response type should be SYSTEM_ERROR", "SYSTEM_ERROR", extensionResponse.responseType);
    }

    @Test
    public void testFieldAssignments_HealthCheckSuccess() {
        // Act
        extensionResponse.message = "Health check completed successfully";
        extensionResponse.technicalDetailedErrorReport = "";
        extensionResponse.responseType = "SUCCESS";
        extensionResponse.timeTakenInSeconds = 0.123;

        // Assert
        assertEquals("Message should indicate success", "Health check completed successfully", extensionResponse.message);
        assertEquals("Technical error report should be empty for success", "", extensionResponse.technicalDetailedErrorReport);
        assertEquals("Response type should be SUCCESS", "SUCCESS", extensionResponse.responseType);
        assertTrue("Time taken should be positive", extensionResponse.timeTakenInSeconds > 0);
    }

    @Test
    public void testFieldAssignments_HealthCheckWithIssues() {
        // Act
        extensionResponse.message = "Health check completed with issues";
        extensionResponse.technicalDetailedErrorReport = "";
        extensionResponse.responseType = "SUCCESS";
        extensionResponse.timeTakenInSeconds = 0.234;

        // Assert
        assertEquals("Message should indicate issues", "Health check completed with issues", extensionResponse.message);
        assertEquals("Response type should still be SUCCESS", "SUCCESS", extensionResponse.responseType);
    }
}

