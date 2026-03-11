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

package app.krista.extensions.essentials.collaboration.office_365_calendar.service;

import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity.ExtensionResponse;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity.HealthStatus;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.invoker.InvokerConfiguration;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants.*;
import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.TelemetryConstants.*;

/**
 * Service class for handling Office 365 Calendar extension setup operations.
 * Contains all business logic for health checks and configuration management.
 * <p>
 * This service follows the same pattern as the Krista Appliance HealthCheck service,
 * providing comprehensive system metrics and health status information.
 */
@Service
public class SetupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetupService.class);

    private final TelemetryMetrics telemetryMetrics;
    private final Invoker invoker;
    private final InvokerConfiguration invokerConfiguration;
    private final RequestContext requestContext;

    @Inject
    public SetupService(TelemetryMetrics telemetryMetrics,
                       Invoker invoker, InvokerConfiguration invokerConfiguration, RequestContext requestContext) {
        this.telemetryMetrics = telemetryMetrics;
        this.invoker = invoker;
        this.invokerConfiguration = invokerConfiguration;
        this.requestContext = requestContext;
    }
    /**
     * Performs a comprehensive health check of the Office 365 Calendar extension system.
     * This method follows the same pattern as the Krista Appliance HealthCheck service.
     *
     * @return ExtensionResponse containing health status and detailed system metrics
     */
    public app.krista.extension.executor.ExtensionResponse checkHealth() {
        long startTime = System.currentTimeMillis();
        try {
            telemetryMetrics.incrementCounter(HEALTH_CHECK_REQUEST_COUNT);
            Map<String, Object> response = healthCheckData(startTime);
            return new ExtensionResponseBuilder().success(response).build();
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            telemetryMetrics.incrementCounter(HEALTH_CHECK_FAILURE_COUNT);
            telemetryMetrics.incrementCounter(HEALTH_CHECK_ERROR_TYPE, 1,
                    Map.of("error_type", e.getClass().getSimpleName()));
            telemetryMetrics.recordDuration(HEALTH_CHECK_LATENCY_MS, endTime - startTime);

            ExtensionResponse extensionResponseEntity = new ExtensionResponse();
            extensionResponseEntity.message = "Failed to perform health check: " + e.getMessage();
            extensionResponseEntity.technicalDetailedErrorReport = e.getMessage();
            extensionResponseEntity.responseType = SYSTEM_ERROR;
            extensionResponseEntity.timeTakenInSeconds = (endTime - startTime) / 1000.0;

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("Is Healthy", false);
            responseData.put("Extension Response", extensionResponseEntity);
            return new ExtensionResponseBuilder().success(responseData).build();
        }
    }

    /**
     * Collects comprehensive health data including system metrics and configuration validation.
     */
    private Map<String, Object> healthCheckData(long startTime) {
        Map<String, Object> healthData = new HashMap<>();

        Result result = getResult();

        double cpuUsage = getApproximateCpuUsage();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadBean.getThreadCount();

        Duration uptime = Duration.between(START_TIME, Instant.now());
        double uptimeHours = uptime.toMillis() / (1000.0 * 60 * 60);
        uptimeHours = Math.round(uptimeHours * 100.0) / 100.0;

        // Get configuration attributes from invoker
        Map<String, Object> attributes = invoker.getAttributes();
        String timeZone = (String) attributes.get("timeZone");
        boolean configValid = validateConfiguration(timeZone);

        String systemStatus = determineSystemStatus(cpuUsage, result.usedMemory(), result.maxMemory(), configValid);

        telemetryMetrics.incrementCounter(OFFICE365_CALENDAR_SYSTEM_STATUS, 1, Map.of("status", systemStatus));

        long endTime = System.currentTimeMillis();
        telemetryMetrics.incrementCounter(HEALTH_CHECK_SUCCESS_COUNT);
        telemetryMetrics.recordDuration(HEALTH_CHECK_LATENCY_MS, endTime - startTime);

        HealthStatus healthStatus = getHealthStatus((double) result.usedMemory(), (double) result.availableMemory(), (double) result.maxMemory(), cpuUsage, (double) threadCount, uptimeHours, systemStatus);
        // Build response
        boolean isHealthy = systemStatus.equals("HEALTHY");
        healthData.put("Health Status", healthStatus);
        healthData.put("Is Healthy", isHealthy);

        ExtensionResponse extensionResponseEntity = new ExtensionResponse();
        extensionResponseEntity.message = isHealthy ? "Health check completed successfully" : "Health check completed with issues";
        extensionResponseEntity.technicalDetailedErrorReport = "";
        extensionResponseEntity.responseType = SUCCESS;
        extensionResponseEntity.timeTakenInSeconds = (endTime - startTime) / 1000.0;

        healthData.put("Extension Response", extensionResponseEntity);

        return healthData;
    }

    @NotNull
    private static Result getResult() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        long availableMemory = maxMemory - usedMemory;
        return new Result(usedMemory, maxMemory, availableMemory);
    }

    private record Result(long usedMemory, long maxMemory, long availableMemory) {
    }

    private static HealthStatus getHealthStatus(double usedMemory, double availableMemory, double maxMemory, double cpuUsage, double threadCount, double uptimeHours, String systemStatus) {
        // Create HealthStatus entity using the new buildWithMetrics method
        return new HealthStatus().buildWithMetrics(
                "Office 365 Calendar Extension",
                usedMemory,
                availableMemory,
                maxMemory,
                cpuUsage,
                threadCount,
                uptimeHours,
                systemStatus
        );
    }

    /**
     * Determines the overall system status based on various metrics.
     * This method follows the same logic as the Krista Appliance implementation.
     */
    private String determineSystemStatus(double cpuUsage, long usedMemory, long maxMemory, boolean configValid) {
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

        if (!configValid) { return "UNHEALTHY"; }
        if (cpuUsage > 90 || memoryUsagePercent > 90) {return "UNHEALTHY"; }
        if (cpuUsage > 75 || memoryUsagePercent > 75) { return "DEGRADED (SLOW RESPONSE)"; }

        return "HEALTHY";
    }

    /**
     * Gets an approximate CPU usage percentage.
     * This method follows the same implementation as the Krista Appliance service.
     */
    private double getApproximateCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {

                double cpuUsage = sunOsBean.getProcessCpuLoad() * 100;
                // If process CPU load is available and valid, use it
                if (cpuUsage >= 0 && cpuUsage <= 100) {
                    return Math.round(cpuUsage * 100.0) / 100.0; // Round to 2 decimal places
                }

                // Try system CPU load as fallback
                double systemCpuUsage = sunOsBean.getCpuLoad() * 100;
                if (systemCpuUsage >= 0 && systemCpuUsage <= 100) {
                    return Math.round(systemCpuUsage * 100.0) / 100.0;
                }
            }

            // Fallback to load average approach
            double load = osBean.getSystemLoadAverage();
            int processors = Runtime.getRuntime().availableProcessors();

            if (load < 0) {
                throw new RuntimeException("System load average not available - CPU monitoring failed");
            }

            // Convert load average to percentage based on available processors
            double usage = (load / processors) * 100;
            return Math.min(Math.round(usage * 100.0) / 100.0, 100.0); // Cap at 100%

        } catch (Exception e) {
            LOGGER.error("CPU monitoring failed", e);
            throw new RuntimeException("CPU monitoring unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Validates current configuration for health check using direct validation logic
     */
    private boolean validateConfiguration(String timeZone) {
        try {
            if (timeZone != null && !timeZone.isBlank() && !Constants.timeZones.contains(timeZone)) {
                LOGGER.warn("Health check failed: {}", Constants.PLEASE_PROVIDE_SUPPORTED_TIME_ZONE);
                return false;
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Configuration validation failed: {}", e.getMessage(), e);
            return false;
        }
    }
}

