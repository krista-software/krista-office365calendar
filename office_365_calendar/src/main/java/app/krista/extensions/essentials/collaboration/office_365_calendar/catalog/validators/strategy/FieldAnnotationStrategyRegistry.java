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

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy;

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for managing field annotation strategies.
 * <p>
 * This class maintains a collection of {@link FieldAnnotationStrategy} implementations
 * and provides methods to find the appropriate strategy for a given parameter and field name.
 * </p>
 * <p>
 * The registry follows the Strategy pattern to eliminate if-statement chains in the
 * FieldAttributeExtractor class, making it easier to add new annotation types in the future.
 * </p>
 */
public class FieldAnnotationStrategyRegistry {

    private final List<FieldAnnotationStrategy> strategies;

    /**
     * Creates a new registry with all available field annotation strategies.
     */
    public FieldAnnotationStrategyRegistry() {
        this.strategies = List.of(
            new BasicFieldAnnotationStrategy(),
            new DateFieldAnnotationStrategy(),
            new TextFieldAnnotationStrategy(),
            new BooleanFieldAnnotationStrategy(),
            new DescFieldAnnotationStrategy(),
            new FileFieldAnnotationStrategy()
        );
    }

    /**
     * Finds the appropriate strategy for the given parameter and field name.
     *
     * @param parameter the method parameter to find a strategy for
     * @param fieldName the field name to match against
     * @return an Optional containing the matching strategy, or empty if no strategy can handle the parameter
     */
    public Optional<FieldAnnotationStrategy> findStrategy(Parameter parameter, String fieldName) {
        for (int i = 0; i < strategies.size(); i++) {
            FieldAnnotationStrategy strategy = strategies.get(i);

            if (strategy.canHandle(parameter, fieldName)) {
                return Optional.of(strategy);
            }
        }

        return Optional.empty();
    }

    /**
     * Extracts field attributes using the appropriate strategy.
     *
     * @param parameter the method parameter containing the annotation
     * @param fieldName the field name to extract attributes for
     * @return a map containing the extracted attributes, or empty map if no strategy found
     */
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        Optional<FieldAnnotationStrategy> strategyOpt = findStrategy(parameter, fieldName);
        if (strategyOpt.isPresent()) {
            FieldAnnotationStrategy strategy = strategyOpt.get();
            Map<String, Object> result = strategy.extractAttributes(parameter, fieldName);
            return result;
        } else {
            return Map.of();
        }
    }

    /**
     * Extracts field type using the appropriate strategy.
     *
     * @param parameter the method parameter containing the annotation
     * @param fieldName the field name to extract type for
     * @return the field type string, or null if no strategy found
     */
    public String extractFieldType(Parameter parameter, String fieldName) {
        Optional<FieldAnnotationStrategy> strategyOpt = findStrategy(parameter, fieldName);
        if (strategyOpt.isPresent()) {
            FieldAnnotationStrategy strategy = strategyOpt.get();
            String result = strategy.extractFieldType(parameter, fieldName);
            return result;
        } else {
            return null;
        }
    }

    /**
     * Maps field to validation resource using the appropriate strategy.
     *
     * @param parameter the method parameter containing the annotation
     * @param fieldName the field name to map
     * @return the corresponding ValidationResource, or null if no strategy found
     */
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        Optional<FieldAnnotationStrategy> strategyOpt = findStrategy(parameter, fieldName);
        if (strategyOpt.isPresent()) {
            FieldAnnotationStrategy strategy = strategyOpt.get();
            Validator.ValidationResource result = strategy.mapToValidationResource(parameter, fieldName);
            return result;
        } else {
            return null;
        }
    }

    /**
     * Gets all registered strategies.
     *
     * @return an immutable list of all registered strategies
     */
    public List<FieldAnnotationStrategy> getAllStrategies() {
        return List.copyOf(strategies);
    }

    /**
     * Gets the number of registered strategies.
     *
     * @return the count of registered strategies
     */
    public int getStrategyCount() {
        return strategies.size();
    }
}
