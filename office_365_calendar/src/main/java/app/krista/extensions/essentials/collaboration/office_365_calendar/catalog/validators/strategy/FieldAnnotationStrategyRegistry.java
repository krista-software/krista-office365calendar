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
        System.out.println("      → FieldAnnotationStrategyRegistry.findStrategy()");
        System.out.println("      → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);
        System.out.println("      → Checking " + strategies.size() + " available strategies...");

        for (int i = 0; i < strategies.size(); i++) {
            FieldAnnotationStrategy strategy = strategies.get(i);
            System.out.println("      → Testing strategy " + (i + 1) + "/" + strategies.size() + ": " + strategy.getStrategyName());

            if (strategy.canHandle(parameter, fieldName)) {
                System.out.println("      → ✓ Found matching strategy: " + strategy.getStrategyName() + " (strategy " + (i + 1) + ")");
                return Optional.of(strategy);
            } else {
                System.out.println("      → ✗ Strategy " + strategy.getStrategyName() + " cannot handle this parameter");
            }
        }

        System.out.println("      → ⚠ No matching strategy found for field: " + fieldName + " after checking all " + strategies.size() + " strategies");
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
        System.out.println("      → FieldAnnotationStrategyRegistry.extractAttributes()");
        System.out.println("      → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Optional<FieldAnnotationStrategy> strategyOpt = findStrategy(parameter, fieldName);
        if (strategyOpt.isPresent()) {
            FieldAnnotationStrategy strategy = strategyOpt.get();
            System.out.println("      → Using " + strategy.getStrategyName() + " strategy to extract attributes");
            Map<String, Object> result = strategy.extractAttributes(parameter, fieldName);
            System.out.println("      → FieldAnnotationStrategyRegistry returning attributes: " + result);
            return result;
        } else {
            System.out.println("      → No strategy found, returning empty map");
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
        System.out.println("      → FieldAnnotationStrategyRegistry.extractFieldType()");
        System.out.println("      → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Optional<FieldAnnotationStrategy> strategyOpt = findStrategy(parameter, fieldName);
        if (strategyOpt.isPresent()) {
            FieldAnnotationStrategy strategy = strategyOpt.get();
            System.out.println("      → Using " + strategy.getStrategyName() + " strategy to extract field type");
            String result = strategy.extractFieldType(parameter, fieldName);
            System.out.println("      → FieldAnnotationStrategyRegistry returning field type: " + result);
            return result;
        } else {
            System.out.println("      → No strategy found, returning null");
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
        System.out.println("      → FieldAnnotationStrategyRegistry.mapToValidationResource()");
        System.out.println("      → Parameter: " + parameter.getType().getSimpleName() + ", Field Name: " + fieldName);

        Optional<FieldAnnotationStrategy> strategyOpt = findStrategy(parameter, fieldName);
        if (strategyOpt.isPresent()) {
            FieldAnnotationStrategy strategy = strategyOpt.get();
            System.out.println("      → Using " + strategy.getStrategyName() + " strategy to map to validation resource");
            Validator.ValidationResource result = strategy.mapToValidationResource(parameter, fieldName);
            System.out.println("      → FieldAnnotationStrategyRegistry returning validation resource: " + result);
            return result;
        } else {
            System.out.println("      → No strategy found, returning null");
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
