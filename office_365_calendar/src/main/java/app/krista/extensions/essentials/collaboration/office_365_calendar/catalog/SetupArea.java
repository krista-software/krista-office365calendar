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

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.*;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.SetupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Setup Area for Office 365 Calendar Extension.
 * Provides health check and system monitoring capabilities.
 */
@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "2329f537-825e-4037-8a7e-cdc1f223419c")
public class SetupArea {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetupArea.class);

    private final SetupService setupService;

    @Inject
    public SetupArea(SetupService setupService) {
        this.setupService = setupService;
    }

    /**
     * Health Check catalog request for Office 365 Calendar Extension.
     * This request verifies the health status of the extension by calling the health check service.
     * It provides comprehensive system metrics including CPU usage, memory usage, thread count,
     * and uptime information.
     *
     * @return ExtensionResponse containing health status and system metrics
     */
    @CatalogRequest(
            id = "localDomainRequest_ae1f0d9f-5c92-45bf-877d-ad0fb4e59f3d",
            name = "Health Check",
            description = "This 'Health Check' request verifies the health status of Office 365 Calendar by calling the health check API.",
            area = "Setup",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Boolean(name = "Is Healthy", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    @Field.Desc(name = "Health Status", type = "Entity(Health Status)",required = false)
    @Field.Desc(name = "Extension Response", type = "Entity(Extension Response)",required = false)
    public ExtensionResponse healthCheck() {
        LOGGER.info("→ Health Check request initiated");
        return setupService.checkHealth();
    }
}

