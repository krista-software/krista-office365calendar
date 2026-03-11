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

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Unit tests for ExponentialBackoffRetryExecutor static methods.
 * Tests the retry logic with exponential backoff functionality.
 */
public class ExponentialBackoffRetryExecutorTest {

    // Tests for executeWithRetry (default config) method

    @Test
    public void shouldReturnResultOnFirstSuccessfulAttempt() throws Exception {
        // Arrange
        String expectedResult = "success";
        RetryableOperation<String> operation = () -> expectedResult;
        String operationName = "testOperation";

        // Act
        String result = ExponentialBackoffRetryExecutor.executeWithRetry(operation, operationName);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    public void shouldRetryAndSucceedOnSecondAttempt() throws Exception {
        // Arrange
        String expectedResult = "success";
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        RetryableOperation<String> operation = () -> {
            int attempt = attemptCount.incrementAndGet();
            if (attempt == 1) {
                throw new java.net.SocketTimeoutException("First attempt failed");
            }
            return expectedResult;
        };
        String operationName = "testOperation";

        // Act
        String result = ExponentialBackoffRetryExecutor.executeWithRetry(operation, operationName);

        // Assert
        assertEquals(expectedResult, result);
        assertEquals(2, attemptCount.get());
    }

    @Test
    public void shouldRetryMaximumTimesAndThenThrowException() {
        // Arrange
        java.net.SocketTimeoutException expectedException = new java.net.SocketTimeoutException("Operation failed");
        AtomicInteger attemptCount = new AtomicInteger(0);

        RetryableOperation<String> operation = () -> {
            attemptCount.incrementAndGet();
            throw expectedException;
        };
        String operationName = "testOperation";

        // Act & Assert
        try {
            ExponentialBackoffRetryExecutor.executeWithRetry(operation, operationName);
            fail("Expected exception to be thrown");
        } catch (Exception e) {
            assertEquals(expectedException, e);
            assertEquals(3, attemptCount.get()); // default maxRetryAttempts = 3 means 3 total attempts
        }
    }

    @Test
    public void shouldHandleNullOperationGracefully() {
        // Arrange
        String operationName = "testOperation";

        // Act & Assert
        try {
            ExponentialBackoffRetryExecutor.executeWithRetry(null, operationName);
            fail("Expected exception to be thrown");
        } catch (Exception e) {
            // Expected behavior - should throw an exception
            assertNotNull(e);
        }
    }

    @Test
    public void shouldHandleNullOperationNameGracefully() throws Exception {
        // Arrange
        String expectedResult = "success";
        RetryableOperation<String> operation = () -> expectedResult;

        // Act
        String result = ExponentialBackoffRetryExecutor.executeWithRetry(operation, null);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    public void shouldHandleEmptyOperationNameGracefully() throws Exception {
        // Arrange
        String expectedResult = "success";
        RetryableOperation<String> operation = () -> expectedResult;

        // Act
        String result = ExponentialBackoffRetryExecutor.executeWithRetry(operation, "");

        // Assert
        assertEquals(expectedResult, result);
    }

    // Tests for executeWithRetry (custom config) method

    @Test
    public void shouldReturnResultOnFirstSuccessfulAttemptWithCustomConfig() throws Exception {
        // Arrange
        String expectedResult = "success";
        RetryableOperation<String> operation = () -> expectedResult;
        String operationName = "testOperation";
        RetryConfiguration customConfig = new RetryConfiguration(5, 500L, 1.5, 10000L);

        // Act
        String result = ExponentialBackoffRetryExecutor.executeWithRetry(operation, operationName, customConfig);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    public void shouldRespectCustomMaxRetryAttempts() {
        // Arrange
        java.net.SocketTimeoutException expectedException = new java.net.SocketTimeoutException("Operation failed");
        AtomicInteger attemptCount = new AtomicInteger(0);

        RetryableOperation<String> operation = () -> {
            attemptCount.incrementAndGet();
            throw expectedException;
        };
        String operationName = "testOperation";
        RetryConfiguration customConfig = new RetryConfiguration(2, 100L, 2.0, 5000L);

        // Act & Assert
        try {
            ExponentialBackoffRetryExecutor.executeWithRetry(operation, operationName, customConfig);
            fail("Expected exception to be thrown");
        } catch (Exception e) {
            assertEquals(expectedException, e);
            assertEquals(2, attemptCount.get()); // maxRetryAttempts = 2 means 2 total attempts
        }
    }

    @Test
    public void shouldHandleNullCustomConfigGracefully() {
        // Arrange
        String expectedResult = "success";
        RetryableOperation<String> operation = () -> expectedResult;
        String operationName = "testOperation";

        // Act & Assert
        try {
            ExponentialBackoffRetryExecutor.executeWithRetry(operation, operationName, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected behavior
            assertNotNull(e);
        } catch (Exception e) {
            fail("Expected NullPointerException but got: " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void shouldSucceedAfterRetriesWithCustomConfig() throws Exception {
        // Arrange
        String expectedResult = "success";
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        RetryableOperation<String> operation = () -> {
            int attempt = attemptCount.incrementAndGet();
            if (attempt < 3) {
                throw new java.net.SocketTimeoutException("Attempt " + attempt + " failed");
            }
            return expectedResult;
        };
        String operationName = "testOperation";
        RetryConfiguration customConfig = new RetryConfiguration(5, 50L, 1.5, 1000L);

        // Act
        String result = ExponentialBackoffRetryExecutor.executeWithRetry(operation, operationName, customConfig);

        // Assert
        assertEquals(expectedResult, result);
        assertEquals(3, attemptCount.get());
    }

    // Tests for RetryConfiguration

    @Test
    public void shouldCreateDefaultConfiguration() {
        // Act
        RetryConfiguration config = RetryConfiguration.defaultConfiguration();

        // Assert
        assertNotNull(config);
        assertEquals(Constants.DEFAULT_MAX_RETRY_ATTEMPTS, config.maxRetryAttempts());
        assertEquals(Constants.DEFAULT_INITIAL_DELAY_MS, config.initialDelayMs());
        assertEquals(Constants.DEFAULT_BACKOFF_MULTIPLIER, config.backoffMultiplier(), 0.001);
        assertEquals(Constants.DEFAULT_MAX_DELAY_MS, config.maxDelayMs());
    }

    @Test
    public void shouldCreateCustomConfiguration() {
        // Arrange
        int maxRetries = 5;
        long initialDelay = 2000L;
        double multiplier = 1.8;
        long maxDelay = 60000L;

        // Act
        RetryConfiguration config = new RetryConfiguration(maxRetries, initialDelay, multiplier, maxDelay);

        // Assert
        assertNotNull(config);
        assertEquals(maxRetries, config.maxRetryAttempts());
        assertEquals(initialDelay, config.initialDelayMs());
        assertEquals(multiplier, config.backoffMultiplier(), 0.001);
        assertEquals(maxDelay, config.maxDelayMs());
    }

    @Test
    public void shouldHandleIntegerReturnType() throws Exception {
        // Arrange
        Integer expectedResult = 42;
        RetryableOperation<Integer> operation = () -> expectedResult;
        String operationName = "integerOperation";

        // Act
        Integer result = ExponentialBackoffRetryExecutor.executeWithRetry(operation, operationName);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    public void shouldHandleBooleanReturnType() throws Exception {
        // Arrange
        Boolean expectedResult = true;
        RetryableOperation<Boolean> operation = () -> expectedResult;
        String operationName = "booleanOperation";

        // Act
        Boolean result = ExponentialBackoffRetryExecutor.executeWithRetry(operation, operationName);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    public void shouldHandleVoidReturnType() throws Exception {
        // Arrange
        AtomicInteger executionCount = new AtomicInteger(0);
        RetryableOperation<Void> operation = () -> {
            executionCount.incrementAndGet();
            return null;
        };
        String operationName = "voidOperation";

        // Act
        Void result = ExponentialBackoffRetryExecutor.executeWithRetry(operation, operationName);

        // Assert
        assertNull(result);
        assertEquals(1, executionCount.get());
    }
}
