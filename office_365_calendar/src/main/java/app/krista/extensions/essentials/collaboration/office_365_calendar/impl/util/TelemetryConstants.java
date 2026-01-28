package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

/**
 * Constants for telemetry metrics used throughout the Office 365 Calendar extension.
 * This class centralizes all telemetry-related constants for better maintainability.
 */
public final class TelemetryConstants {

    // Prevent instantiation
    private TelemetryConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // HEALTH CHECK METRICS

    public static final String HEALTH_CHECK_REQUEST_COUNT = "office365_calendar_health_check_request_count";
    public static final String HEALTH_CHECK_SUCCESS_COUNT = "office365_calendar_health_check_success_count";
    public static final String HEALTH_CHECK_FAILURE_COUNT = "office365_calendar_health_check_failure_count";
    public static final String HEALTH_CHECK_LATENCY_MS = "office365_calendar_health_check_latency_ms";
    public static final String HEALTH_CHECK_ERROR_TYPE = "office365_calendar_health_check_error_type";

    public static final String OFFICE365_CALENDAR_SYSTEM_STATUS = "office365_calendar_system_status";

    // SAVE CONFIGURATION METRICS

    public static final String SAVE_CONFIG_REQUEST_COUNT = "office365_calendar_save_config_request_count";
    public static final String SAVE_CONFIG_SUCCESS_COUNT = "office365_calendar_save_config_success_count";
    public static final String SAVE_CONFIG_FAILURE_COUNT = "office365_calendar_save_config_failure_count";
    public static final String SAVE_CONFIG_LATENCY_MS = "office365_calendar_save_config_latency_ms";
    public static final String SAVE_CONFIG_ERROR_TYPE = "office365_calendar_save_config_error_type";

    // TEST CONNECTION METRICS

    public static final String TEST_CONNECTION_REQUEST_COUNT = "office365_calendar_test_connection_request_count";
    public static final String TEST_CONNECTION_SUCCESS_COUNT = "office365_calendar_test_connection_success_count";
    public static final String TEST_CONNECTION_FAILURE_COUNT = "office365_calendar_test_connection_failure_count";
    public static final String TEST_CONNECTION_LATENCY_MS = "office365_calendar_test_connection_latency_ms";
    public static final String TEST_CONNECTION_ERROR_TYPE = "office365_calendar_test_connection_error_type";
}

