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
                throw new IllegalStateException(Constants.ERROR_OCCURRED_WHILE_UPLOADING_ATTACHMENT_, ioException.getCause());
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

    private static java.io.File getFileObject(File file) throws IOException {
        MediaClient mediaClient = new MediaClient(Constants.MEDIA_URL);
        String mediaURI = MediaClient.createMediaUrl(file.getMediaId());
        return mediaClient.download(mediaURI, file.getFileName());
    }

    private static String getFileType(java.io.File file) throws IOException {
        return Files.probeContentType(file.toPath()) != null ? Files.probeContentType(file.toPath()) : Constants.APPLICATION_X_BINARY;
    }

    private static byte[] readContentOfTheFile(java.io.File ioFile) throws IOException {
        byte[] bFile = new byte[(int) ioFile.length()];
        try (FileInputStream fileInputStream = new FileInputStream(ioFile)) {
            //convert file into array of bytes
            fileInputStream.read(bFile);
            return bFile;
        } catch (IOException exception) {
            throw new IOException(Constants.ERROR_OCCURRED_WHILE_READING_FILE_, exception.getCause());
        }
    }
}
