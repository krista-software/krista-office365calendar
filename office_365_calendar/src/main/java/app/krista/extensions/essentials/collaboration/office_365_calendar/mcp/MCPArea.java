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

package app.krista.extensions.essentials.collaboration.office_365_calendar.mcp;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;

import javax.inject.Inject;

/**
 * Catalog entry point for the MCP "Ask Agent" natural language query
 * for the Office 365 Calendar extension.
 */
@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "2329f537-825e-4037-8a7e-cdc1f223419c")
public class MCPArea {

    private final CalendarMcpQueryService mcpQueryService;

    @Inject
    public MCPArea(CalendarMcpQueryService mcpQueryService) {
        this.mcpQueryService = mcpQueryService;
    }

    @CatalogRequest(
            id = "localDomainRequest_e1c7f3b4-8d29-4a6c-b5e0-9f2d87c14a3e",
            name = "Ask Agent",
            description = "Accepts natural language requests, enables the AI agent to reason and invoke available tools as needed, and returns the generated response.",
            area = "MCP",
            type = CatalogRequest.Type.QUERY_SYSTEM, tool = true)
    @Field.Desc(name = "Response", type = "[ FreeForm ]", required = false,
            description = "List of Office 365 Calendar events matching the query")
    public ExtensionResponse askAgent(
            @Field(name = "Query", type = "Paragraph", required = true, description = "",
                    attributes = {@Attribute(name = "visualWidth", value = "L")},
                    options = {}) String query) {
        return mcpQueryService.query(query);
    }
}
