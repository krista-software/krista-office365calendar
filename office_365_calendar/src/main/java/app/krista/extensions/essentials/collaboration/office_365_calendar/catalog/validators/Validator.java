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

import java.util.Map;

public interface Validator {

    enum ValidationResource {
        EMAIL,
        EVENT_ID,
        FROM_DATE,
        TO_DATE,
        DATE_RANGE,
        NUMBER_OF_MEETINGS,
        DATE_RANGE_TO,
        ATTENDEES,
        TIME_ZONE,
        RECURRENCE_INTERVAL,
        DAYS_OF_WEEK,
        NUMBERED_OCCURRENCES,
        ATTACHMENTS,
        MEETING_DURATION,
        DELEGATE_EMAIL
    }

    Boolean validate(String resourceId, Map<ValidationResource, String> context);

    String getFetchFieldName();

    String getFetchStepMessage();

    String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context);

    String getErrMessage(String resourceId);
}
