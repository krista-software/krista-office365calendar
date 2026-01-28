package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for HealthStatus entity class.
 * Tests entity creation, field assignments, and the buildWithMetrics helper method.
 */
public class HealthStatusTest {

    private HealthStatus healthStatus;

    @Before
    public void setUp() {
        healthStatus = new HealthStatus();
    }

    @Test
    public void testDefaultConstructor() {
        // Act
        HealthStatus status = new HealthStatus();

        // Assert
        assertNotNull("HealthStatus should be created", status);
        assertNull("Extension name should be null by default", status.extensionName);
        assertNull("Current memory usage should be null by default", status.currentMemoryUsageMB);
        assertNull("Available memory should be null by default", status.availableMemoryMB);
        assertNull("Total memory should be null by default", status.totalMemoryMB);
        assertNull("CPU usage should be null by default", status.cPUUsagePercentage);
        assertNull("Active threads should be null by default", status.activeThreads);
        assertNull("Uptime hours should be null by default", status.uptimeHours);
        assertNull("System status should be null by default", status.systemStatus);
        assertNull("Database connection status should be null by default", status.databaseConnectionStatus);
        assertNull("Active connections should be null by default", status.activeConnections);
        assertNull("Last health check time should be null by default", status.lastHealthCheckTime);
    }

    @Test
    public void testBuildWithMetrics_AllFields() {
        // Arrange
        String extensionName = "Office 365 Calendar Extension";
        double usedMemory = 512.5;
        double availableMemory = 1536.5;
        double maxMemory = 2048.0;
        double cpuUsage = 45.75;
        double threadCount = 25.0;
        double uptimeHours = 12.5;
        String systemStatus = "HEALTHY";

        // Act
        HealthStatus result = healthStatus.buildWithMetrics(
                extensionName, usedMemory, availableMemory, maxMemory,
                cpuUsage, threadCount, uptimeHours, systemStatus
        );

        // Assert
        assertNotNull("Result should not be null", result);
        assertSame("Should return the same instance", healthStatus, result);
        assertEquals("Extension name should match", extensionName, result.extensionName);
        assertEquals("Used memory should match", Double.valueOf(usedMemory), result.currentMemoryUsageMB);
        assertEquals("Available memory should match", Double.valueOf(availableMemory), result.availableMemoryMB);
        assertEquals("Max memory should match", Double.valueOf(maxMemory), result.totalMemoryMB);
        assertEquals("CPU usage should match", Double.valueOf(cpuUsage), result.cPUUsagePercentage);
        assertEquals("Thread count should match", Double.valueOf(threadCount), result.activeThreads);
        assertEquals("Uptime hours should match", Double.valueOf(uptimeHours), result.uptimeHours);
        assertEquals("System status should match", systemStatus, result.systemStatus);
        assertNull("Database connection status should be null", result.databaseConnectionStatus);
        assertNull("Active connections should be null", result.activeConnections);
        assertNotNull("Last health check time should be set", result.lastHealthCheckTime);
    }

    @Test
    public void testBuildWithMetrics_HealthyStatus() {
        // Act
        HealthStatus result = healthStatus.buildWithMetrics(
                "Test Extension", 100.0, 900.0, 1000.0,
                25.0, 10.0, 5.0, "HEALTHY"
        );

        // Assert
        assertEquals("System status should be HEALTHY", "HEALTHY", result.systemStatus);
    }

    @Test
    public void testBuildWithMetrics_DegradedStatus() {
        // Act
        HealthStatus result = healthStatus.buildWithMetrics(
                "Test Extension", 800.0, 200.0, 1000.0,
                80.0, 50.0, 10.0, "DEGRADED (SLOW RESPONSE)"
        );

        // Assert
        assertEquals("System status should be DEGRADED", "DEGRADED (SLOW RESPONSE)", result.systemStatus);
    }

    @Test
    public void testBuildWithMetrics_UnhealthyStatus() {
        // Act
        HealthStatus result = healthStatus.buildWithMetrics(
                "Test Extension", 950.0, 50.0, 1000.0,
                95.0, 100.0, 20.0, "UNHEALTHY"
        );

        // Assert
        assertEquals("System status should be UNHEALTHY", "UNHEALTHY", result.systemStatus);
    }

    @Test
    public void testBuildWithMetrics_ZeroValues() {
        // Act
        HealthStatus result = healthStatus.buildWithMetrics(
                "Test Extension", 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, "HEALTHY"
        );

        // Assert
        assertEquals("Used memory should be 0", Double.valueOf(0.0), result.currentMemoryUsageMB);
        assertEquals("Available memory should be 0", Double.valueOf(0.0), result.availableMemoryMB);
        assertEquals("Max memory should be 0", Double.valueOf(0.0), result.totalMemoryMB);
        assertEquals("CPU usage should be 0", Double.valueOf(0.0), result.cPUUsagePercentage);
        assertEquals("Thread count should be 0", Double.valueOf(0.0), result.activeThreads);
        assertEquals("Uptime hours should be 0", Double.valueOf(0.0), result.uptimeHours);
    }

    @Test
    public void testBuildWithMetrics_HighValues() {
        // Act
        HealthStatus result = healthStatus.buildWithMetrics(
                "Test Extension", 16384.0, 16384.0, 32768.0,
                100.0, 500.0, 720.0, "HEALTHY"
        );

        // Assert
        assertEquals("Used memory should handle large values", Double.valueOf(16384.0), result.currentMemoryUsageMB);
        assertEquals("Available memory should handle large values", Double.valueOf(16384.0), result.availableMemoryMB);
        assertEquals("Max memory should handle large values", Double.valueOf(32768.0), result.totalMemoryMB);
        assertEquals("CPU usage should be 100", Double.valueOf(100.0), result.cPUUsagePercentage);
        assertEquals("Thread count should handle large values", Double.valueOf(500.0), result.activeThreads);
        assertEquals("Uptime hours should handle large values", Double.valueOf(720.0), result.uptimeHours);
    }

    @Test
    public void testBuildWithMetrics_LastHealthCheckTimeIsSet() {
        // Arrange
        long beforeTime = System.currentTimeMillis();

        // Act
        HealthStatus result = healthStatus.buildWithMetrics(
                "Test Extension", 100.0, 900.0, 1000.0,
                25.0, 10.0, 5.0, "HEALTHY"
        );

        // Assert
        assertNotNull("Last health check time should be set", result.lastHealthCheckTime);
        long healthCheckTime = Long.parseLong(result.lastHealthCheckTime);
        assertTrue("Health check time should be recent", healthCheckTime >= beforeTime);
        assertTrue("Health check time should not be in the future", healthCheckTime <= System.currentTimeMillis());
    }

    @Test
    public void testFieldAssignments() {
        // Act
        healthStatus.extensionName = "Test Extension";
        healthStatus.currentMemoryUsageMB = 256.0;
        healthStatus.availableMemoryMB = 768.0;
        healthStatus.totalMemoryMB = 1024.0;
        healthStatus.cPUUsagePercentage = 50.0;
        healthStatus.activeThreads = 20.0;
        healthStatus.uptimeHours = 8.0;
        healthStatus.systemStatus = "HEALTHY";
        healthStatus.databaseConnectionStatus = "Connected";
        healthStatus.activeConnections = 5.0;
        healthStatus.lastHealthCheckTime = "1234567890";

        // Assert
        assertEquals("Extension name should be set", "Test Extension", healthStatus.extensionName);
        assertEquals("Current memory usage should be set", Double.valueOf(256.0), healthStatus.currentMemoryUsageMB);
        assertEquals("Available memory should be set", Double.valueOf(768.0), healthStatus.availableMemoryMB);
        assertEquals("Total memory should be set", Double.valueOf(1024.0), healthStatus.totalMemoryMB);
        assertEquals("CPU usage should be set", Double.valueOf(50.0), healthStatus.cPUUsagePercentage);
        assertEquals("Active threads should be set", Double.valueOf(20.0), healthStatus.activeThreads);
        assertEquals("Uptime hours should be set", Double.valueOf(8.0), healthStatus.uptimeHours);
        assertEquals("System status should be set", "HEALTHY", healthStatus.systemStatus);
        assertEquals("Database connection status should be set", "Connected", healthStatus.databaseConnectionStatus);
        assertEquals("Active connections should be set", Double.valueOf(5.0), healthStatus.activeConnections);
        assertEquals("Last health check time should be set", "1234567890", healthStatus.lastHealthCheckTime);
    }
}

