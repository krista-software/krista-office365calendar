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

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entityrequests.impl.EventDetailsImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps catalog entity names to their {@link EntityImplementationService}
 * handlers. Add new entities here when introducing them — the dispatcher in
 * {@link EntityRequests} routes lookups through this registry.
 */
@Service
public final class ServiceRegistry {

    private final Map<String, EntityImplementationService> serviceMap = new HashMap<>();

    @Inject
    public ServiceRegistry(MeetingDaoImpl meetingDao) {
        serviceMap.put(Constants.EVENT_DETAILS, new EventDetailsImpl(meetingDao));
    }

    public Map<String, EntityImplementationService> getServiceMap() {
        return serviceMap;
    }
}
