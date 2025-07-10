package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

/**
 * Utility class for executing operations with exponential backoff retry mechanism.
 * <p>
 * This class provides methods to execute operations that may fail due to transient
 * network issues or service unavailability. It implements exponential backoff
 * with configurable parameters and proper exception handling.
 * </p>
 * <p>
 * The retry mechanism will only retry on specific types of exceptions that are
 * considered transient (network timeouts, connection issues, etc.) and will not
 * retry on authentication or authorization failures.
 * </p>
 */
public final class ExponentialBackoffRetryExecutor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExponentialBackoffRetryExecutor.class);
    
    /**
     * Predicate to determine if an exception should trigger a retry.
     * Only retries on transient network-related exceptions.
     */
    private static final Predicate<Exception> SHOULD_RETRY = exception -> {
        // Check the exception type and its causes
        Throwable current = exception;
        while (current != null) {
            // Retry on network-related exceptions
            if (current instanceof SocketTimeoutException ||
                current instanceof UnknownHostException ||
                current instanceof java.net.ConnectException ||
                current instanceof java.net.SocketException) {
                return true;
            }
            
            // Retry on ExecutionException if the cause is retryable
            if (current instanceof ExecutionException && current.getCause() != null) {
                current = current.getCause();
                continue;
            }
            
            // Check for specific Microsoft Graph service exceptions that indicate transient issues
            String message = current.getMessage();
            if (message != null) {
                String lowerMessage = message.toLowerCase();
                if (lowerMessage.contains("timeout") ||
                    lowerMessage.contains("connection reset") ||
                    lowerMessage.contains("service unavailable") ||
                    lowerMessage.contains("too many requests") ||
                    lowerMessage.contains("rate limit")) {
                    return true;
                }
            }
            
            current = current.getCause();
        }
        
        return false;
    };
    
    private ExponentialBackoffRetryExecutor() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Executes an operation with exponential backoff retry using default configuration.
     * 
     * @param operation the operation to execute
     * @param operationName a descriptive name for the operation (used in logging)
     * @param <T> the return type of the operation
     * @return the result of the operation
     * @throws Exception if all retry attempts are exhausted
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation, String operationName) throws Exception {
        return executeWithRetry(operation, operationName, RetryConfiguration.defaultConfiguration());
    }
    
    /**
     * Executes an operation with exponential backoff retry using custom configuration.
     * 
     * @param operation the operation to execute
     * @param operationName a descriptive name for the operation (used in logging)
     * @param config the retry configuration
     * @param <T> the return type of the operation
     * @return the result of the operation
     * @throws Exception if all retry attempts are exhausted
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation, String operationName, RetryConfiguration config) throws Exception {
        LOGGER.debug("→ executeWithRetry: Starting operation '{}' with config: maxRetries={}, initialDelay={}ms, multiplier={}, maxDelay={}ms",
                operationName, config.maxRetryAttempts(), config.initialDelayMs(), config.backoffMultiplier(), config.maxDelayMs());
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= config.maxRetryAttempts(); attempt++) {
            try {
                // Calculate and apply delay for retry attempts (not for the first attempt)
                if (attempt > 1) {
                    long delay = config.calculateDelay(attempt);
                    LOGGER.debug("→ executeWithRetry: " + Constants.RETRY_ATTEMPT_MESSAGE, attempt, config.maxRetryAttempts(), operationName);
                    LOGGER.debug("→ executeWithRetry: Waiting {}ms before retry attempt {}", delay, attempt);
                    
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                }
                
                // Execute the operation
                LOGGER.debug("→ executeWithRetry: Executing operation '{}' - attempt {} of {}", operationName, attempt, config.maxRetryAttempts());
                T result = operation.execute();
                
                if (attempt > 1) {
                    LOGGER.info("→ executeWithRetry: " + Constants.RETRY_SUCCESS_MESSAGE, attempt, config.maxRetryAttempts(), operationName);
                } else {
                    LOGGER.debug("→ executeWithRetry: Operation '{}' succeeded on first attempt", operationName);
                }
                
                return result;
                
            } catch (InterruptedException e) {
                LOGGER.warn("→ executeWithRetry: Operation '{}' interrupted on attempt {}", operationName, attempt);
                Thread.currentThread().interrupt();
                throw e;
            } catch (Exception e) {
                lastException = e;
                
                // Check if we should retry this exception
                if (!SHOULD_RETRY.test(e)) {
                    LOGGER.debug("→ executeWithRetry: Exception not retryable for operation '{}': {}", operationName, e.getMessage());
                    throw e;
                }
                
                // Check if we have more attempts left
                if (attempt >= config.maxRetryAttempts()) {
                    LOGGER.error("→ executeWithRetry: " + Constants.RETRY_EXHAUSTED_MESSAGE + " - {}", operationName, e.getMessage(), e);
                    break;
                } else {
                    LOGGER.warn("→ executeWithRetry: Attempt {} of {} failed for operation '{}': {} - will retry", 
                            attempt, config.maxRetryAttempts(), operationName, e.getMessage());
                }
            }
        }
        
        // All attempts exhausted, throw the last exception
        LOGGER.error("→ executeWithRetry: All retry attempts exhausted for operation '{}'", operationName);
        throw lastException;
    }
}
