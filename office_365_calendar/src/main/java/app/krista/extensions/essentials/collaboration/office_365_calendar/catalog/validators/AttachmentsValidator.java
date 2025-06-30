package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Validator implementation for validating attachments within the Office 365 calendar extension.
 * <p>
 * This class validates that attachment files are only PDF files by checking file extensions.
 * It provides user-facing messages for fetch, confirmation, and error steps.
 * </p>
 */
// Update this Validator
public class AttachmentsValidator implements Validator {

    private static final Gson gson = new Gson();

    @Override
    public Boolean validate(String attachmentsStr, Map<ValidationResource, String> context) {
        try {
            if (attachmentsStr == null || attachmentsStr.trim().isEmpty()) {
                // Attachments are optional, so null/empty is considered valid
                return true;
            }

            // Try to parse as JSON array of file objects
            Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
            List<Map<String, Object>> attachments = gson.fromJson(attachmentsStr, listType);

            if (attachments == null || attachments.isEmpty()) {
                // Empty list is valid
                return true;
            }

            // Check if all attachments are PDF files
            for (Map<String, Object> attachment : attachments) {
                if (attachment == null) {
                    continue;
                }

                // Get the file name from the attachment
                String fileName = null;
                if (attachment.containsKey("name")) {
                    fileName = (String) attachment.get("name");
                } else if (attachment.containsKey("fileName")) {
                    fileName = (String) attachment.get("fileName");
                } else if (attachment.containsKey("originalName")) {
                    fileName = (String) attachment.get("originalName");
                }

                if (fileName == null || fileName.trim().isEmpty()) {
                    return false; // No file name found
                }

                // Check if the file extension is PDF
                String lowerCaseFileName = fileName.toLowerCase();
                if (!lowerCaseFileName.endsWith(".pdf")) {
                    return false; // Non-PDF file found
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return "Attachments";
    }

    @Override
    public String getFetchStepMessage() {
        return "Please attach only PDF files. Other file types are not allowed.";
    }

    @Override
    public String getConfirmationStepMessage(String attachmentsStr, Map<ValidationResource, String> context) {
        return String.format("The provided attachments contain non-PDF files: %s. Only PDF files are allowed for meeting attachments.", attachmentsStr);
    }

    @Override
    public String getErrMessage(String attachmentsStr) {
        return String.format("Invalid attachments: %s. Only PDF files are allowed.", attachmentsStr);
    }
}
