package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity.EventDetails;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.MediaClient;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import app.krista.model.base.File;
import com.google.gson.reflect.TypeToken;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class CatalogTypes {

    private CatalogTypes() {
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
        return eventDetails;
    }

    /**
     * This function will return list of krista files to list of microsoft attachments
     *
     * @param attachments list of {@link File}
     * @return list of {@link Attachment}
     */
    public static List<Attachment> toAttachment(List<File> attachments) {
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
     * This function will return java file for given krista file
     *
     * @param file {@link File}
     * @return {@link java.io.File}
     * @throws IOException if there is any error in input file
     */
    private static java.io.File getFileObject(File file) throws IOException {
        MediaClient mediaClient = MediaClient.getInstance();
        String mediaURI = MediaClient.createMediaUrl(file.getMediaId());
        return mediaClient.download(mediaURI, file.getFileName());
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
}
