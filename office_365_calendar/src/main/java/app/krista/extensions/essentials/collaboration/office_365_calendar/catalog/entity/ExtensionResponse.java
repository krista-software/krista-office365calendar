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

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity;

import app.krista.extension.impl.anno.*;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "2329f537-825e-4037-8a7e-cdc1f223419c")
@Entity(name = "Extension Response", id = "localDomainEntity_418023aa-8802-4794-85ae-dcbb7b272aa6", primaryKey = "Message", supportStore = false, options = {})
public class ExtensionResponse {

    // Debug constructor
    public ExtensionResponse() {
    }

    @Field.Text(name = "Message", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'A detailed message describing the extension\\'s response'")}, options = {},
            description = "Human-readable summary message describing the outcome of the extension operation. Contains success confirmation or a descriptive error message explaining what went wrong during execution.")
    public String message;

    @Field(name = "Technical Detailed Error Report", type = "RichText", required = false, attributes = {@Attribute(name = "visualWidth", value = "L"), @Attribute(name = "toolTip", value = "'Provides error details to assist the technical team in troubleshooting'")}, options = {},
            description = "Rich-text formatted technical error report containing stack traces, exception details, and diagnostic information. Intended for developers and support teams to troubleshoot failures. Empty or null when the operation succeeds.")
    public String technicalDetailedErrorReport;

    @Field.PickOne(name = "Response Type", values = {}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Specifies the type of exception to aid in categorizing and handling errors'")}, options = {},
            description = "Classification of the exception or response outcome. Used to categorize errors for routing and handling. Possible values include SYSTEM_ERROR, INPUT_ERROR, and LOGIC_ERROR. Null when the operation succeeds.")
    public String responseType;

    @Field(name = "Time Taken In Seconds", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Indicates the total duration of the operation, measured in seconds'")}, options = {},
            description = "Total wall-clock duration of the extension operation measured in seconds as a decimal number. Useful for performance monitoring and SLA tracking. Example: 2.35 means the operation took 2.35 seconds.")
    public Double timeTakenInSeconds;

}

