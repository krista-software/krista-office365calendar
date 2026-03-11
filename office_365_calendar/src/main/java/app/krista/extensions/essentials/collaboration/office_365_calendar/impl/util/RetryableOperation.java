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
