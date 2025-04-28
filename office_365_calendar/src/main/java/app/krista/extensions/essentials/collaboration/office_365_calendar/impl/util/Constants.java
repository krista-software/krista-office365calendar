package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Constants {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String MEDIA_URL = "http://platform:9991";
    public static final String NEW_EVENT = "New Event";
    public static final String DONT_REMIND_ME = "Don't remind me";
    public static final String NUMBER_OF_MEETINGS_SHOULD_BE_GREATER_THAN_0 = "Number of meetings should be greater than 0.";
    public static final String NONE = "None";
    public static final String INVALID_STATE_PARAMETERS = "Invalid state parameters!";
    public static final String PLEASE_PROVIDE_SUPPORTED_TIME_ZONE = "Please provide supported time zone.";
    public static final String ERROR_OCCURRED_DURING_AUTHORIZATION = "Error occurred during authorization ";
    public static final String ERROR_OCCURRED_WHILE_READING_FILE = "Error occurred while reading file ";
    public static final String ERROR_OCCURRED_WHILE_UPLOADING_ATTACHMENT = "Error occurred while uploading attachment ";
    public static final String MICROSOFT_GRAPH_FILE_ATTACHMENT = "#microsoft.graph.fileAttachment";
    public static final String APPLICATION_X_BINARY = "application/x-binary";
    public static final String THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE = " The To date must be greater than the From date.";
    public static final String FAILED_TO_UPDATE_EVENT = "Failed to update event ";
    public static final String MEETING_ID = "meeting Id";
    public static final String FAILED_TO_ADD_ATTENDEES = "Failed to add attendees ";
    public static final String FAILED_TO_SCHEDULE_MEETING = "Failed to schedule Meeting ";
    public static final String FAILED_TO_REMOVE_ATTENDEE = "Failed to remove attendee ";
    public static final String FAILED_TO_CANCEL_EVENT = "Failed to cancel event ";
    public static final String FAILED_TO_GET_TENTATIVE_INVITATIONS = "Failed to get tentative invitations ";
    public static final String FAILED_TO_DECLINE_INVITATION = "Failed to decline invitation ";
    public static final String FAILED_TO_ACCEPT_INVITATION = "Failed to accept invitation ";
    public static final String YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ATTENDEE = "Attendee";
    public static final String AVAILABILITY = "Availability";
    public static final String PT = "PT";
    public static final String SEARCH_RESPONSE_IS_EMPTY = "Search Response is Empty!";
    public static final String SCHEDULES = "Schedules";
    public static final String FAILED_TO_GET_SCHEDULES = "Failed to get schedules ";
    public static final String FAILED_TO_GET_ALL_UNACCEPTED_EVENTS = "Failed to get all unaccepted events ";
    public static final String TEXT = "text";
    public static final String HTML = "html";
    public static final String YYYY_DD_MM = "yyyy-dd-MM";
    public static final String STATE = "state";
    public static final String CALLBACK_URI = "/calendar/callback";
    public static final String FAILED_TO_GET_ACCOUNT = "Failed to get account";
    public static final String FAILED_TO_GET_EVENT_ATTACHMENTS = "Failed to get event attachments " ;
    public static final String RETRY_WAS_INTERRUPTED = "Retry was interrupted " ;
    public static final String TIMEOUT_OCCURRED = "Timeout occurred. " ;
    public static final String ERROR_OCCURRED_WHILE_PROCESSING_ATTACHMENT = "Error occurred while processing attachment ";
    private static final String SPACE = " ";
    private static final String OPEN_ID = "openid";
    private static final String OFFLINE_ACCESS = "offline_access";
    private static final String CALENDARS_READWRITE = "Calendars.ReadWrite";
    private static final String CALENDARS_READWRITE_SHARED = "Calendars.ReadWrite.Shared";
    private static final String USER_READ = "User.Read";
    public static final String WS_CONTACT = "wsContact";
    public static final String IGNORED_EXCEPTION = "Ignored exception";
    public static final String REQUIRED_SCOPE = OPEN_ID + SPACE
            + OFFLINE_ACCESS + SPACE
            + CALENDARS_READWRITE + SPACE
            + CALENDARS_READWRITE_SHARED + SPACE
            + USER_READ;

    public static final String AUTHORIZATION_PROMPT = "Authorize yourself and click 'Validate Attributes' before saving changes to proceed further";
    public static final String FAILED_TO_GET_USER_EVENTS = "Failed to get user events. ";
    public static final String UTC = "UTC";
    public static final String START_DATE_TIME = "startDateTime";
    public static final String END_DATE_TIME = "endDateTime";
    public static final String TOP = "$top";
    public static final String ORDER_BY = "$orderby";

    private Constants() {
    }
}
