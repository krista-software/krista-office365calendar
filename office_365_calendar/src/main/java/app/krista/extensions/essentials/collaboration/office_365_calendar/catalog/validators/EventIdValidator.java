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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Validator implementation for verifying the existence of an event ID within the Office 365 calendar extension.
 * <p>
 * This class uses the {@link MeetingDaoImpl} to check if a meeting with the given event ID exists.
 * It provides user-facing messages for fetch, confirmation, and error steps, and handles authorization
 * and validation exceptions appropriately.
 * </p>
 */
public class EventIdValidator implements Validator {

    private final MeetingDaoImpl meetingDao;
    private static final Logger logger = LoggerFactory.getLogger(EventIdValidator.class);

    public EventIdValidator(MeetingDaoImpl meetingDao) {
        this.meetingDao = meetingDao;
    }

    @Override
    public Boolean validate(String eventId, Map<ValidationResource, String> context) {
        try {
            // Extract delegate email from context if available
            String delegateEmail = context.get(ValidationResource.DELEGATE_EMAIL);
            logger.info("→ validate: Validating event ID {} with delegate email: {}", eventId, delegateEmail);

            Meeting meeting;
            if (delegateEmail != null && !delegateEmail.trim().isEmpty()) {
                // Use delegated access
                logger.debug("→ validate: Using delegated access for validation");
                meeting = meetingDao.getMeetingByIdDelegated(eventId, delegateEmail);
            } else {
                // Use current user access (existing behavior)
                logger.debug("→ validate: Using current user access for validation");
                meeting = meetingDao.getMeetingById(eventId);
            }

            boolean isValid = meeting != null;
            logger.debug("→ validate: Event ID validation result: {}", isValid);
            return isValid;
        } catch (MustAuthorizeException cause) {
            logger.error("Exception thrown while authorizing user : {} {}", cause.getMessage(), cause);
            throw cause;
        } catch (Exception cause) {
            logger.error("Exception thrown while validating event id: {}", cause.getMessage(), cause);
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return Constants.EVENT_ID;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid Event ID. You can find the Event ID in your calendar event details or by using the 'Get Meetings' request.";
    }

    @Override
    public String getConfirmationStepMessage(String eventId, Map<ValidationResource, String> context) {
        return String.format("We couldn't find an event with ID '%s'. This event may not exist or you may not have access to it. Would you like to try a different Event ID?", eventId);
    }

    @Override
    public String getErrMessage(String eventId) {
        return String.format("Event ID '%s' not found. Please verify the Event ID and try again. Tip: Use 'Get Meetings' to view available events and their IDs.", eventId);
    }
}

