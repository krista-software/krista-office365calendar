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
import app.krista.extension.impl.anno.EntityRequest;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Entry point invoked by the Krista platform for entity-store requests against
 * catalog entities declared with {@code supportStore = true}. Delegates by
 * entity name to the appropriate {@link EntityImplementationService} via
 * {@link ServiceRegistry}.
 */
public final class EntityRequests {

    private final ServiceRegistry serviceRegistry;

    @Inject
    public EntityRequests(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @EntityRequest(EntityRequest.Type.CREATE)
    public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField) {
        return resolve(entityType).create(entityType, entityAttributeField);
    }

    @EntityRequest(EntityRequest.Type.UPDATE)
    public EntityValue update(EntityType entityType, EntityValue entity) {
        return resolve(entityType).update(entityType, entity);
    }

    @EntityRequest(EntityRequest.Type.SEARCH)
    public List<EntityValue> search(EntityType entityType, SearchQuery conditions, Long pageIndex, Integer pageSize) {
        return resolve(entityType).search(entityType, conditions, pageIndex, pageSize);
    }

    @EntityRequest(EntityRequest.Type.GET)
    public EntityValue get(EntityType entityType, String primaryKey) {
        return resolve(entityType).get(entityType, primaryKey);
    }

    private EntityImplementationService resolve(EntityType entityType) {
        EntityImplementationService impl = serviceRegistry.getServiceMap().get(entityType.getEntityName());
        if (impl == null) {
            throw new IllegalArgumentException("No entity store implementation registered for entity: " + entityType.getEntityName());
        }
        return impl;
    }
}
