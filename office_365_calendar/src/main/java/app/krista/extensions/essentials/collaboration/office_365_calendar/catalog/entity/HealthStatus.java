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
@Entity(name = "Health Status", id = "localDomainEntity_72b8095a-12e4-4b21-bfdd-f2009756081d", primaryKey = "Extension Name", supportStore = false, options = {})
public class HealthStatus {

    public HealthStatus() {
    }

    @Field.Text(name = "Extension Name", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Name of the extension reporting health status'")}, options = {},
            description = "Registered name of the Krista extension instance reporting its health status. Used to identify which extension the health metrics belong to. Example: 'Office 365 Calendar'.")
    public String extensionName;

    @Field(name = "Current Memory Usage MB", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Current memory usage in megabytes'")}, options = {},
            description = "Amount of heap memory currently consumed by the extension process, measured in megabytes (MB). Calculated as total memory minus available memory. Useful for detecting memory leaks or high consumption.")
    public Double currentMemoryUsageMB;

    @Field(name = "Available Memory MB", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Available memory in megabytes'")}, options = {},
            description = "Amount of free heap memory available to the extension process, measured in megabytes (MB). Low values may indicate the extension is under memory pressure and could experience performance degradation.")
    public Double availableMemoryMB;

    @Field(name = "Total Memory MB", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Total memory allocated in megabytes'")}, options = {},
            description = "Maximum heap memory allocated to the extension process by the JVM, measured in megabytes (MB). Represents the upper bound of memory the extension can use before triggering OutOfMemoryError.")
    public Double totalMemoryMB;

    @Field(name = "CPU Usage Percentage", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Current CPU usage as a percentage'")}, options = {},
            description = "Current CPU utilization of the extension process expressed as a percentage (0.0 to 100.0). High values may indicate compute-intensive operations or potential performance bottlenecks.")
    public Double cPUUsagePercentage;

    @Field(name = "Active Threads", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Number of active threads'")}, options = {},
            description = "Number of currently active threads in the extension's JVM thread pool. Includes both daemon and non-daemon threads. A sudden increase may indicate thread leaks or high concurrency load.")
    public Double activeThreads;

    @Field(name = "Uptime Hours", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'System uptime in hours'")}, options = {},
            description = "Continuous running time of the extension process since its last restart, measured in hours as a decimal number. Example: 48.5 means the extension has been running for 48 hours and 30 minutes.")
    public Double uptimeHours;

    @Field.Text(name = "System Status", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Overall system status (HEALTHY, DEGRADED, UNHEALTHY)'")}, options = {},
            description = "Overall operational health status of the extension. Valid values: 'HEALTHY' (all systems operational), 'DEGRADED' (partial functionality impaired), or 'UNHEALTHY' (critical failure detected requiring immediate attention).")
    public String systemStatus;

    @Field.Text(name = "Database Connection Status", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Status of database connectivity'")}, options = {},
            description = "Current connectivity status of the extension's database connection pool. Indicates whether the extension can successfully communicate with its backing data store. Null if no database is configured.")
    public String databaseConnectionStatus;

    @Field(name = "Active Connections", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Number of active database connections'")}, options = {},
            description = "Number of currently active and in-use database connections in the connection pool. High values relative to the pool maximum may indicate connection exhaustion risk. Null if no database is configured.")
    public Double activeConnections;

    @Field.Text(name = "Last Health Check Time", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Timestamp of the last health check'")}, options = {},
            description = "Unix epoch timestamp in milliseconds indicating when the most recent health check was performed. Stored as a string representation of the millisecond value. Example: '1700000000000'.")
    public String lastHealthCheckTime;

    /**
     * Builder method to populate HealthStatus with system metrics.
     * This method allows fluent configuration of all health status fields.
     *
     * @param extensionName Name of the extension
     * @param usedMemory Used memory in MB
     * @param availableMemory Available memory in MB
     * @param maxMemory Total memory in MB
     * @param cpuUsage CPU usage percentage
     * @param threadCount Number of active threads
     * @param uptimeHours Uptime in hours
     * @param systemStatus Overall system status
     * @return This HealthStatus instance for method chaining
     */
    public HealthStatus buildWithMetrics(String extensionName, double usedMemory, double availableMemory,
                                       double maxMemory, double cpuUsage, double threadCount,
                                       double uptimeHours, String systemStatus) {
        this.extensionName = extensionName;
        this.currentMemoryUsageMB = usedMemory;
        this.availableMemoryMB = availableMemory;
        this.totalMemoryMB = maxMemory;
        this.cPUUsagePercentage = cpuUsage;
        this.activeThreads = threadCount;
        this.uptimeHours = uptimeHours;
        this.systemStatus = systemStatus;
        this.databaseConnectionStatus = null;
        this.activeConnections = null;
        this.lastHealthCheckTime = String.valueOf(System.currentTimeMillis());
        return this;
    }

}

