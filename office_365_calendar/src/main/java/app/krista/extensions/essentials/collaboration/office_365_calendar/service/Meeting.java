package app.krista.extensions.essentials.collaboration.office_365_calendar.service;

import java.util.List;

public interface Meeting {
    /**
     * Returns the meeting id of the Office 365 calendar event
     *
     * @return meetings id of the meeting
     */
    String getId();

    /**
     * Returns the display name of the location of Office 365 calendar event
     *
     * @return location of the meeting
     */
    String getLocation();

    /**
     * Returns the title of the Office 365 calendar event
     *
     * @return the title of the meeting
     */
    String getTitle();

    /**
     * Returns the list of attendees of the Office 365 calendar event
     *
     * @return {@link List<String>} list of meeting attendees
     */
    List<String> getContacts();

    /**
     * Returns the description of the Office 365 calendar event
     *
     * @return the description of the meeting
     */
    String getDetails();

    /**
     * Returns the start date and time of the Office 365 calendar event
     *
     * @return start date and time of the meeting
     */
    Long getDateFrom();

    /**
     * Returns the end date and time of the Office 365 calendar event
     *
     * @return the end date and time of the meeting
     */
    Long getDateTo();
}
