package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity.EventDetails;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import app.krista.model.base.File;
import com.google.gson.reflect.TypeToken;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Utility and service class for converting between Krista and Microsoft Graph types
 * related to files and events in the Office 365 calendar integration.
 * <p>
 * CatalogTypes provides methods to:
 * - Convert Meeting objects to EventDetails entities
 * - Convert lists of Krista File objects to Microsoft Graph Attachment objects and vice versa
 * - Handle file content reading, file type detection, and temporary file management for attachments
 * <p>
 * This class is typically used by service and DAO layers to facilitate interoperability
 * between Krista's internal models and Microsoft Graph API models, especially for
 * event and attachment management.
 */

@Service
public final class CatalogTypes {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogTypes.class);
    private final KristaMediaClient kristaMediaClient;

    @Inject
    public CatalogTypes(KristaMediaClient kristaMediaClient) {
        this.kristaMediaClient = kristaMediaClient;
    }

    /**
     * This function will return {@link EventDetails} entity from {@link Meeting}
     *
     * @param meeting {@link Meeting} object
     * @return {@link EventDetails} entity
     */
    public static EventDetails fromEvent(Meeting meeting) {
        if (meeting == null) {
            return null;
        }
        EventDetails eventDetails = new EventDetails();
        eventDetails.eventId = meeting.getId();
        eventDetails.title = meeting.getTitle();
        eventDetails.details = meeting.getDetails();
        eventDetails.dateFrom = meeting.getDateFrom();
        eventDetails.dateTo = meeting.getDateTo();
        eventDetails.contacts = meeting.getContacts();
        eventDetails.location = meeting.getLocation();
        eventDetails.timeZone = meeting.getTimeZone();
        eventDetails.organizer = meeting.getOrganizer();
        return eventDetails;
    }

    private static List<File> getAttachmentsByParsingIntoJsonMapper(List<File> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }

        // Hack to resolve Gson conversation
        Type fileListType = new TypeToken<ArrayList<File>>() {
        }.getType();
        attachments = Constants.GSON.fromJson(Constants.GSON.toJson(attachments), fileListType);
        return attachments;
    }

    /**
     * This function will return the type of file extension
     *
     * @param file {@link java.io.File}
     * @return file extension string
     * @throws IOException if there is error in input file
     */
    private static String getFileType(java.io.File file) throws IOException {
        return Files.probeContentType(file.toPath()) != null ? Files.probeContentType(file.toPath()) : Constants.APPLICATION_X_BINARY;
    }

    /**
     * This function will read the java file contents and return byte array of file
     *
     * @param ioFile input {@link java.io.File}
     * @return byte array
     * @throws IOException if there is error in input ioFile
     */
    private static byte[] readContentOfTheFile(java.io.File ioFile) throws IOException {
        byte[] bFile = new byte[(int) ioFile.length()];
        try (FileInputStream fileInputStream = new FileInputStream(ioFile)) {
            //convert file into array of bytes
            final int read = fileInputStream.read(bFile);
            if (read > 0) {
                return bFile;
            } else {
                throw new IOException(Constants.ERROR_OCCURRED_WHILE_READING_FILE);
            }
        }
    }

    /**
     * This function will return list of krista files to list of microsoft attachments
     *
     * @param attachments list of {@link File}
     * @return list of {@link Attachment}
     */
    public List<Attachment> toAttachment(List<File> attachments) {
        List<Attachment> attachmentsList = new LinkedList<>();
        List<File> filesToAttach = getAttachmentsByParsingIntoJsonMapper(attachments);
        for (File file : filesToAttach) {
            FileAttachment fileAttachments = new FileAttachment();
            fileAttachments.name = file.getFileName();
            try {
                java.io.File ioFile = getFileObject(file);
                fileAttachments.contentType = getFileType(ioFile);
                fileAttachments.contentBytes = readContentOfTheFile(ioFile);
                fileAttachments.oDataType = Constants.MICROSOFT_GRAPH_FILE_ATTACHMENT;
            } catch (IOException ioException) {
                throw new IllegalStateException(Constants.ERROR_OCCURRED_WHILE_UPLOADING_ATTACHMENT, ioException.getCause());
            }
            attachmentsList.add(fileAttachments);
        }
        return attachmentsList;
    }

    /**
     * This function will return java file for given krista file
     *
     * @param file {@link File}
     * @return {@link java.io.File}
     * @throws IOException if there is any error in input file
     */
    private java.io.File getFileObject(File file) throws IOException {
        return kristaMediaClient.toJavaFile(file);
    }

    /**
     * This function will convert a list of Microsoft Graph FileAttachments to a
     * list of Krista Files
     *
     * @param fileAttachments List of Microsoft Graph {@link FileAttachment}
     * @return List of {@link File} Krista File objects
     */
    public List<File> toKristaFilList(List<FileAttachment> fileAttachments) {
        if (fileAttachments == null || fileAttachments.isEmpty()) {
            return List.of();
        }

        List<File> files = new ArrayList<>();
        for (FileAttachment attachment : fileAttachments) {
            File file = toKristaFile(attachment);
            if (file != null) {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * This function will convert a Microsoft Graph FileAttachment to a Krista File
     *
     * @param fileAttachment Microsoft Graph {@link FileAttachment}
     * @return {@link File} Krista File object
     */
    private File toKristaFile(FileAttachment fileAttachment) {
        if (fileAttachment == null || fileAttachment.contentBytes == null) {
            return null;
        }
        java.io.File tempFile;
        try {
            java.io.File tempDir = new java.io.File(System.getProperty("java.io.tmpdir"));

            String originalName = fileAttachment.name != null ? fileAttachment.name : UUID.randomUUID().toString();
            String safeFileName = originalName.replaceAll("[^a-zA-Z0-9.\\-]", "_");

            tempFile = new java.io.File(tempDir, safeFileName);

            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                outputStream.write(fileAttachment.contentBytes);
            } catch (IOException cause) {
                LOGGER.error("Error while writing attachment to temp file: {}", cause.getMessage(), cause);
                return null;
            } finally {
                tempFile.deleteOnExit();
            }
            return kristaMediaClient.toKristaFile(tempFile);
        } catch (IOException cause) {
            throw new IllegalStateException(Constants.ERROR_OCCURRED_WHILE_PROCESSING_ATTACHMENT, cause);
        }
    }
}
