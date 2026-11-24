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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import app.krista.extension.authorization.MustAuthorizeException;

/**
 * Validator implementation for validating attachments within the Office 365 calendar extension.
 * <p>
 * This class validates that attachment files have valid file extensions by checking against
 * a predefined set of allowed file types including documents, spreadsheets, presentations,
 * images, and archives. It provides user-facing messages for fetch, confirmation, and error steps.
 * </p>
 */
// Update this Validator
public class AttachmentsValidator implements Validator {

    private static final Gson gson = new Gson();

    private final static Set<String> validFileExtensions = Set.of(
            // Document formats
            "pdf", "doc", "docx", "rtf", "txt",
            // Spreadsheet formats
            "xls", "xlsx", "csv",
            // Presentation formats
            "ppt", "pptx",
            // Image formats
            "png", "jpg", "jpeg", "gif", "bmp", "tif", "tiff",
            // Archive formats
            "zip", "rar"
            // Other common formats - commenting below formats as they are restricted by krista
//            "xml", "html", "htm"
    );

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

            // Check if all attachments have valid file extensions
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

                // Check if the file extension is valid
                String lowerCaseFileName = fileName.toLowerCase();
                String fileExtension = getFileExtension(lowerCaseFileName);
                if (fileExtension == null || !validFileExtensions.contains(fileExtension)) {
                    return false; // Invalid file extension found
                }
            }

            return true;
        } catch (MustAuthorizeException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the file extension from a filename.
     * @param fileName the filename (should be lowercase)
     * @return the file extension without the dot, or null if no extension found
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return null; // No extension or dot at the end
        }

        return fileName.substring(lastDotIndex + 1);
    }

    @Override
    public String getFetchFieldName() {
        return "Attachments";
    }

    @Override
    public String getFetchStepMessage() {
        return "Please attach files with supported formats. Supported: Documents (PDF, DOC, DOCX, RTF, TXT), Spreadsheets (XLS, XLSX, CSV), Presentations (PPT, PPTX), Images (PNG, JPG, JPEG, GIF, BMP, TIF, TIFF), Archives (ZIP, RAR).";
    }

    @Override
    public String getConfirmationStepMessage(String attachmentsStr, Map<ValidationResource, String> context) {
        return "One or more files have unsupported formats. Would you like to attach different files? Supported formats: Documents (PDF, DOC, DOCX, RTF, TXT), Spreadsheets (XLS, XLSX, CSV), Presentations (PPT, PPTX), Images (PNG, JPG, JPEG, GIF, BMP, TIF, TIFF), Archives (ZIP, RAR).";
    }

    @Override
    public String getErrMessage(String attachmentsStr) {
        return "Invalid file attachments. Please use supported file formats: Documents (PDF, DOC, DOCX, RTF, TXT), Spreadsheets (XLS, XLSX, CSV), Presentations (PPT, PPTX), Images (PNG, JPG, JPEG, GIF, BMP, TIF, TIFF), Archives (ZIP, RAR).";
    }
}
