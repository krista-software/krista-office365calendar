package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

/**
 * Functional interface representing an operation that can be retried.
 * <p>
 * This interface is designed to work with the exponential backoff retry mechanism.
 * Operations that implement this interface can throw exceptions that will be caught
 * and potentially retried based on the retry configuration.
 * </p>
 * 
 * @param <T> the return type of the operation
 */
@FunctionalInterface
public interface RetryableOperation<T> {
    
    /**
     * Executes the operation that may fail and need to be retried.
     * 
     * @return the result of the operation
     * @throws Exception if the operation fails
     */
    T execute() throws Exception;
}
