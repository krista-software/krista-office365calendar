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

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entityrequests;

import app.krista.extension.executor.SearchQuery;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import java.util.List;
import java.util.Map;

/**
 * Abstract contract every entity-store implementation must satisfy.
 * Mirrors the Jira extension pattern so additional Office 365 Calendar entities
 * (e.g. Calendar, Attendee) can be plugged in by extending this class and
 * registering them in {@link ServiceRegistry}.
 */
public abstract class EntityImplementationService {

    private final MeetingDaoImpl meetingDao;

    protected EntityImplementationService(MeetingDaoImpl meetingDao) {
        this.meetingDao = meetingDao;
    }

    protected MeetingDaoImpl getMeetingDao() {
        return meetingDao;
    }

    public abstract EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField);

    public abstract EntityValue update(EntityType entityType, EntityValue entity);

    public abstract List<EntityValue> search(EntityType entityType, SearchQuery searchQuery, Long pageIndex, Integer pageSize);

    public abstract EntityValue get(EntityType entityType, String primaryKey);

    /**
     * Ensures the supplied attribute map carries every required key — throws
     * {@link IllegalArgumentException} on the first missing one. Used by create()
     * implementations to surface missing-field errors with a clear message.
     */
    protected void validateInputs(Map<String, Object> entityAttributeField, List<String> keys) {
        for (String key : keys) {
            if (entityAttributeField == null || !entityAttributeField.containsKey(key)
                    || entityAttributeField.get(key) == null) {
                throw new IllegalArgumentException("Please provide " + key);
            }
        }
    }
}
