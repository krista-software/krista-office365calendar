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

package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

/**
 * Configuration class for exponential backoff retry mechanism.
 * <p>
 * This record encapsulates all the parameters needed to configure retry behavior
 * for operations that may fail due to transient network issues or service unavailability.
 * It follows Java 21 best practices by using a record for immutable data.
 * </p>
 * 
 * @param maxRetryAttempts   Maximum number of retry attempts (including the initial attempt)
 * @param initialDelayMs     Initial delay in milliseconds before the first retry
 * @param backoffMultiplier  Multiplier for exponential backoff (e.g., 2.0 doubles the delay each time)
 * @param maxDelayMs         Maximum delay in milliseconds between retry attempts
 */
public record RetryConfiguration(
        int maxRetryAttempts,
        long initialDelayMs,
        double backoffMultiplier,
        long maxDelayMs
) {
    
    /**
     * Creates a RetryConfiguration with default values from Constants.
     * 
     * @return a RetryConfiguration with default retry settings
     */
    public static RetryConfiguration defaultConfiguration() {
        return new RetryConfiguration(
                Constants.DEFAULT_MAX_RETRY_ATTEMPTS,
                Constants.DEFAULT_INITIAL_DELAY_MS,
                Constants.DEFAULT_BACKOFF_MULTIPLIER,
                Constants.DEFAULT_MAX_DELAY_MS
        );
    }
    
    /**
     * Creates a RetryConfiguration with custom max retry attempts and default other values.
     * 
     * @param maxRetryAttempts the maximum number of retry attempts
     * @return a RetryConfiguration with specified max retry attempts
     */
    public static RetryConfiguration withMaxRetries(int maxRetryAttempts) {
        return new RetryConfiguration(
                maxRetryAttempts,
                Constants.DEFAULT_INITIAL_DELAY_MS,
                Constants.DEFAULT_BACKOFF_MULTIPLIER,
                Constants.DEFAULT_MAX_DELAY_MS
        );
    }
    
    /**
     * Validates the configuration parameters.
     * 
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public RetryConfiguration {
        if (maxRetryAttempts < 1) {
            throw new IllegalArgumentException("maxRetryAttempts must be at least 1");
        }
        if (initialDelayMs < 0) {
            throw new IllegalArgumentException("initialDelayMs must be non-negative");
        }
        if (backoffMultiplier <= 0) {
            throw new IllegalArgumentException("backoffMultiplier must be positive");
        }
        if (maxDelayMs < initialDelayMs) {
            throw new IllegalArgumentException("maxDelayMs must be greater than or equal to initialDelayMs");
        }
    }
    
    /**
     * Calculates the delay for a specific retry attempt.
     * 
     * @param attemptNumber the attempt number (1-based)
     * @return the delay in milliseconds for this attempt
     */
    public long calculateDelay(int attemptNumber) {
        if (attemptNumber <= 1) {
            return 0; // No delay for the first attempt
        }
        
        // Calculate exponential backoff: initialDelay * (multiplier ^ (attempt - 2))
        double delay = initialDelayMs * Math.pow(backoffMultiplier, attemptNumber - 2);
        
        // Cap at maxDelayMs
        return Math.min((long) delay, maxDelayMs);
    }
}
