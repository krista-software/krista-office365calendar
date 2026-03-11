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

package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Constants {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Gson GSON_OBJECT = new Gson();
    public static final String NEW_EVENT = "New Event";
    public static final String DONT_REMIND_ME = "Don't remind me";
    public static final String NUMBER_OF_MEETINGS_SHOULD_BE_GREATER_THAN_0 = "Please enter a number greater than 0 for the number of meetings to retrieve.";
    public static final String NONE = "None";
    public static final String INVALID_STATE_PARAMETERS = "The request contains invalid parameters. Please check your input and try again.";
    public static final String PLEASE_PROVIDE_SUPPORTED_TIME_ZONE = "Please enter a valid time zone (e.g., 'America/New_York', 'Europe/London', 'UTC').";
    public static final String ERROR_OCCURRED_DURING_AUTHORIZATION = "Unable to authenticate with Office 365. Please check your credentials and try again. ";
    public static final String ERROR_OCCURRED_WHILE_READING_FILE = "Unable to read the file. Please ensure the file exists and is accessible. ";
    public static final String ERROR_OCCURRED_WHILE_UPLOADING_ATTACHMENT = "Unable to upload the attachment. Please check the file size and format, then try again. ";
    public static final String MICROSOFT_GRAPH_FILE_ATTACHMENT = "#microsoft.graph.fileAttachment";
    public static final String APPLICATION_X_BINARY = "application/x-binary";
    public static final String THE_TO_DATE_MUST_BE_GREATER_THAN_THE_FROM_DATE = "The end date must be after the start date. Please adjust your date selection.";
    public static final String FAILED_TO_UPDATE_EVENT = "Unable to update the calendar event. Please verify you have permission to modify this event and try again. ";
    public static final String MEETING_ID = "meeting Id";
    public static final String FAILED_TO_ADD_ATTENDEES = "Unable to add attendees to the meeting. Please check the email addresses and try again. ";
    public static final String FAILED_TO_SCHEDULE_MEETING = "Unable to create the meeting. Please check your calendar permissions and try again. ";
    public static final String FAILED_TO_REMOVE_ATTENDEE = "Unable to remove the attendee from the meeting. Please verify you have permission to modify this event. ";
    public static final String FAILED_TO_CANCEL_EVENT = "Unable to cancel the event. Please verify you have permission to cancel this event and try again. ";
    public static final String FAILED_TO_GET_TENTATIVE_INVITATIONS = "Unable to retrieve tentative invitations. Please check your calendar access and try again. ";
    public static final String FAILED_TO_DECLINE_INVITATION = "Unable to decline the meeting invitation. Please try again or decline directly in your calendar. ";
    public static final String FAILED_TO_ACCEPT_INVITATION = "Unable to accept the meeting invitation. Please try again or accept directly in your calendar. ";
    public static final String YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ATTENDEE = "Attendee";
    public static final String AVAILABILITY = "Availability";
    public static final String PT = "PT";
    public static final String SEARCH_RESPONSE_IS_EMPTY = "No events found matching your search criteria. Try using different keywords or expanding your search terms.";
    public static final String SCHEDULES = "Schedules";
    public static final String FAILED_TO_GET_SCHEDULES = "Unable to retrieve calendar schedules. Please check your permissions and try again. ";
    public static final String FAILED_TO_GET_ALL_UNACCEPTED_EVENTS = "Unable to retrieve pending invitations. Please check your calendar access and try again. ";
    public static final String TEXT = "text";
    public static final String HTML = "html";
    public static final String YYYY_DD_MM = "yyyy-dd-MM";
    public static final String STATE = "state";
    public static final String CALLBACK_URI = "/calendar/callback";
    public static final String FAILED_TO_GET_ACCOUNT = "Unable to access your Office 365 account. Please verify your authentication and try again.";
    public static final String FAILED_TO_GET_EVENT_ATTACHMENTS = "Unable to retrieve event attachments. Please check your permissions and try again. ";
    public static final String ERROR_OCCURRED_WHILE_PROCESSING_ATTACHMENT = "Unable to process the attachment. Please check the file format and size, then try again. ";
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

    // OAuth Flow Constants
    public static final String PUBLIC = "Public";
    public static final String PRIVATE = "Private";
    public static final String HASH = "#";
    public static final String UNDER_SCORE = "_";
    public static final String AUTH_CONTEXT_ID = "authContextId";
    public static final String UNSUPPORTED_AUTH = "Invalid authentication type. Please choose either 'Public' or 'Private' authentication mode.";
    public static final String LOCAL_EXTN_URL = "https://extension.local.eng.krista.app";
    public static final String LOCAL_EXTN_REPLACE_URL = "http://localhost:8765";
    public static final String DEFAULT_CALLBACK_PATH = "/extension/api/rest/v3/oauth/callback";
    public static final String EXTENSION_FORWARD_PATH = "/rest/calendar/callback";
    public static final String ORG_AUTHORITY = "https://login.microsoftonline.com/organizations/";
    public static final String AUTHORITY = "https://login.microsoftonline.com/";
    public static final String AUTH_URL_QUERY_PARAMS = "&access_type=offline&approval_prompt=force&prompt=select_account";
    public static final String AUTHORIZATION_PROMPT = "Please complete the authorization process and click 'Validate Attributes' before saving your changes.";
    public static final String FAILED_TO_GET_USER_EVENTS = "Unable to retrieve calendar events. Please check your permissions and try again. ";
    public static final String UTC = "UTC";
    public static final String START_DATE_TIME = "startDateTime";
    public static final String END_DATE_TIME = "endDateTime";
    public static final String TOP = "$top";
    public static final String ORDER_BY = "$orderby";
    public static final String EMAIL = "email";
    public static final String ATTACHMENTS = "Attachments";
    public static final String VALIDATION_RESULTS = "Validation Results";
    public static final String STATE_ID = "stateId";
    public static final String EVENT_ID = "Event Id";
    public static final String DELEGATE_EMAIL = "Delegate Email";
    public static final String ORGANIZER_EMAIL = "Organizer Email";
    public static final String RE_ENTER = "re-enter";
    public static final String INVALID_INPUT = "The information provided is not valid. Would you like to correct it and try again?";
    public static final String HANDLE_RE_ENTER_GET_ATTACHMENT_FOR_GIVEN_USER = "handleReEnterGetAttachmentForGivenUser";
    public static final String HANDLE_RE_ENTER_GET_ATTACHMENT = "handleReEnterGetAttachments";
    public static final String CONFIRM_RE_ENTER_GET_ATTACHMENTS = "confirmReenterGetAttachments";
    public static final String FAILED_TO_GET_EVENTS_FOR_USER = "Unable to retrieve calendar events for the specified user. Please verify the email address and permissions. ";
    public static final String USER_EMAIL = "User Email";
    public static final String FROM_DATE = "Date From";
    public static final String TO_DATE = "Date To";
    public static final String EVENTS = "Events";
    public static final String FAILED_TO_CANCEL_EVENT_VALIDATION = "Cannot cancel event due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_RE_ENTER_CANCEL_EVENT = "handleReEnterCancelEvent";
    public static final String CONFIRM_RE_ENTER_CANCEL_EVENT = "confirmReenterCancelEvent";
    public static final String EVENT_DETAILS = "Event Details";
    public static final String FAILED_TO_GET_UPCOMING_MEETINGS = "Unable to retrieve upcoming meetings. Please check your calendar access and try again. ";
    public static final String NUMBER_OF_MEETINGS = "numberOfMeetings";
    public static final String HANDLE_RE_ENTER_GET_UPCOMING_MEETINGS = "handleReEnterGetUpcomingMeetings";
    public static final String CONFIRM_RE_ENTER_GET_UPCOMING_MEETINGS = "confirmReenterGetUpcomingMeetings";
    public static final String FAILED_TO_GET_EVENT = "Unable to retrieve the event details. Please verify the event ID and try again. ";
    public static final String HANDLE_RE_ENTER_GET_EVENT = "handleReEnterGetEvent";
    public static final String CONFIRM_RE_ENTER_GET_EVENT = "confirmReenterGetEvent";
    public static final String HANDLE_REENTER_DECLINE_INVITATION = "handleReenterDeclineInvitation";
    public static final String CONFIRM_REENTER_DECLINE_INVITATION = "confirmReenterDeclineInvitation";
    public static final String FAILED_TO_GET_SCHEDULES_VALIDATION = "Cannot retrieve schedules due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_GET_SCHEDULES = "handleReenterGetSchedules";
    public static final String CONFIRM_REENTER_GET_SCHEDULES = "confirmReenterGetSchedules";
    public static final String TIME_ZONE = "Time Zone";
    public static final String ATTENDEES = "attendees";
    public static final String FAILED_TO_GET_FREE_SLOTS_VALIDATION = "Cannot find available time slots due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_GET_FREE_SLOTS = "handleReenterGetFreeSlots";
    public static final String CONFIRM_REENTER_GET_FREE_SLOTS = "confirmReenterGetFreeSlots";
    public static final String REQUIRED_ATTENDEE = "Required Attendee";
    public static final String OPTIONAL_ATTENDEE = "Optional Attendee";
    public static final String IS_ORGANIZER_OPTIONAL = "Is Organizer Optional";
    public static final String MEETING_DURATION = "Meeting Duration";
    public static final String FAILED_TO_FORWARD_EVENT_VALIDATION = "Cannot forward event due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_FORWARD_EVENT = "handleReenterForwardEvent";
    public static final String CONFIRM_REENTER_FORWARD_EVENT = "confirmReenterForwardEvent";
    public static final String ATTENDEE_EMAIL = "Attendee Email";
    public static final String FAILED_TO_ACCEPT_INVITATION_VALIDATION = "Cannot accept invitation due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_ACCEPT_INVITATION = "handleReenterAcceptInvitation";
    public static final String CONFIRM_REENTER_ACCEPT_INVITATION = "confirmReenterAcceptInvitation";
    public static final String FAILED_TO_TENTATIVE_INVITATION_VALIDATION = "Cannot mark invitation as tentative due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_TENTATIVE_INVITATION = "handleReenterTentativeInvitation";
    public static final String CONFIRM_REENTER_TENTATIVE_INVITATION = "confirmReenterTentativeInvitation";
    public static final String FAILED_TO_SCHEDULE_MEETING_VALIDATION = "Cannot create meeting due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_SCHEDULE_MEETING = "handleReenterScheduleMeeting";
    public static final String CONFIRM_REENTER_SCHEDULE_MEETING = "confirmReenterScheduleMeeting";
    public static final String TITLE = "Title";
    public static final String LOCATION = "Location";
    public static final String DATE_TO = "Date To";
    public static final String REQUIRED_ATTENDEE_EMAIL = "Required Attendee Email";
    public static final String OPTIONAL_ATTENDEE_EMAIL = "Optional Attendee Email";
    public static final String IS_ALL_DAY = "Is All Day";
    public static final String REMINDER = "Reminder";
    public static final String RECURRENCE = "Recurrence";
    public static final String RECURRENCE_INTERVAL = "Recurrence Interval";
    public static final String DAYS_OF_WEEK = "Days Of Week";
    public static final String RECURRENCE_TYPE = "Recurrence Type";
    public static final String DAY_OF_MONTH = "Day Of Month";
    public static final String REPEAT_INDEX = "Repeat Index";
    public static final String REPEAT_DAY = "Repeat Day";
    public static final String REPEAT_MONTH = "Repeat Month";
    public static final String END_RECURRENCE = "End Recurrence";
    public static final String END_DATE = "End Date";
    public static final String NUMBERED_OCCURRENCES = "Numbered Occurrences";
    public static final String ATTACHMENTS_FIELD = "Attachments";
    public static final String DETAILS = "Details";
    public static final String IS_TEAMS_MEETING = "Is Teams Meeting";
    public static final String FAILED_TO_ADD_ATTENDEES_VALIDATION = "Cannot add attendees due to invalid information. Please check the email addresses and try again. ";
    public static final String HANDLE_REENTER_ADD_ATTENDEES = "handleReenterAddAttendees";
    public static final String CONFIRM_REENTER_ADD_ATTENDEES = "confirmReenterAddAttendees";
    public static final String FAILED_TO_REMOVE_ATTENDEE_VALIDATION = "Cannot remove attendee due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_REMOVE_ATTENDEE = "handleReenterRemoveAttendee";
    public static final String CONFIRM_REENTER_REMOVE_ATTENDEE = "confirmReenterRemoveAttendee";
    public static final String FAILED_TO_UPDATE_EVENT_VALIDATION = "Cannot update event due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_UPDATE_EVENT = "handleReenterUpdateEvent";
    public static final String CONFIRM_REENTER_UPDATE_EVENT = "confirmReenterUpdateEvent";
    public static final String FAILED_TO_GET_EVENT_VALIDATION = "Cannot retrieve event due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_GET_EVENT = "handleReenterGetEvent";
    public static final String CONFIRM_REENTER_GET_EVENT = "confirmReenterGetEvent";
    public static final String FAILED_TO_GET_EVENTS_FOR_USER_VALIDATION = "Cannot retrieve events for user due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_GET_EVENTS_FOR_USER = "handleReenterGetEventsForUser";
    public static final String CONFIRM_REENTER_GET_EVENTS_FOR_USER = "confirmReenterGetEventsForUser";
    public static final String FAILED_TO_GET_ATTACHMENTS_FOR_USER_VALIDATION = "Cannot retrieve attachments for user due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_GET_ATTACHMENTS_FOR_USER = "handleReenterGetAttachmentsForUser";
    public static final String CONFIRM_REENTER_GET_ATTACHMENTS_FOR_USER = "confirmReenterGetAttachmentsForUser";
    public static final String FAILED_TO_GET_ATTACHMENTS_VALIDATION = "Cannot retrieve attachments due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_GET_ATTACHMENTS = "handleReenterGetAttachments";
    public static final String CONFIRM_REENTER_GET_ATTACHMENTS = "confirmReenterGetAttachments";
    public static final String FROM = "From";
    public static final String TO = "To";
    public static final String EVENT_ID_FIELD = "Event ID";
    public static final String FAILED_TO_GET_NEXT_UPCOMING_MEETINGS_VALIDATION = "Cannot retrieve upcoming meetings due to invalid information. Please check your input and try again. ";
    public static final String HANDLE_REENTER_GET_NEXT_UPCOMING_MEETINGS = "handleReenterGetNextUpcomingMeetings";
    public static final String CONFIRM_REENTER_GET_NEXT_UPCOMING_MEETINGS = "confirmReenterGetNextUpcomingMeetings";
    public static final String NUMBER_OF_MEETING = "Number of meeting";

    // Allow Retry Feature Constant
    public static final String ENABLE_ERROR_HANDLING = "Allow Retry";

    // Retry Configuration Constants
    public static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    public static final long DEFAULT_INITIAL_DELAY_MS = 1000L; // 1 second
    public static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;
    public static final long DEFAULT_MAX_DELAY_MS = 30000L; // 30 seconds
    public static final String RETRY_ATTEMPT_MESSAGE = "Retry attempt {} of {} for operation: {}";
    public static final String RETRY_EXHAUSTED_MESSAGE = "All retry attempts exhausted for operation: {}";
    public static final String RETRY_SUCCESS_MESSAGE = "Operation succeeded on retry attempt {} of {}: {}";

    // Additional constants for OAuth flow
    public static final String FAILED_TO_SAVE_ATTRIBUTES = "Unable to save configuration settings. Please try again or contact support if the issue persists.";
    public static final String UNAUTHORISED_USER = "Authentication failed. Please verify your credentials and try again.";
    public static final String USER_AUTHENTICATED_SUCCESSFULLY_SAVE_THE_CHANGES = "Authentication successful! Please save your changes to complete the setup.";
    public static final String EMPTY_STRING = "";

    // Health Check and Setup Constants
    public static final String SUCCESS = "SUCCESS";
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
    public static final String INPUT_ERROR = "INPUT_ERROR";
    public static final String REGEX_PATTERN = "^[a-zA-Z0-9_-]+$";
    public static final java.time.Instant START_TIME = java.time.Instant.now();
    public static final java.util.List<String> timeZones = java.util.Arrays.asList(java.util.TimeZone.getAvailableIDs());

    private Constants() {
    }
}