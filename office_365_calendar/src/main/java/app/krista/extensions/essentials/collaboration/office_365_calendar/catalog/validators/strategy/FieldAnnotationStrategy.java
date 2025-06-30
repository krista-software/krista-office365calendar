package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy;

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Strategy interface for handling different field annotation types.
 * <p>
 * This interface defines the contract for extracting field attributes, types, and validation resources
 * from different annotation types (@Field, @Field.Date, @Field.Text, etc.).
 * </p>
 * <p>
 * Each concrete strategy implementation handles a specific annotation type, eliminating the need
 * for if-statement chains in the FieldAttributeExtractor class.
 * </p>
 */
public interface FieldAnnotationStrategy {

    /**
     * Checks if this strategy can handle the given parameter's annotations.
     *
     * @param parameter the method parameter to check
     * @param fieldName the field name to match against
     * @return true if this strategy can handle the parameter, false otherwise
     */
    boolean canHandle(Parameter parameter, String fieldName);

    /**
     * Extracts field attributes from the parameter's annotation.
     *
     * @param parameter the method parameter containing the annotation
     * @param fieldName the field name to extract attributes for
     * @return a map containing the extracted attributes, or empty map if no attributes found
     */
    Map<String, Object> extractAttributes(Parameter parameter, String fieldName);

    /**
     * Extracts the field type from the parameter's annotation.
     *
     * @param parameter the method parameter containing the annotation
     * @param fieldName the field name to extract type for
     * @return the field type string, or null if not found
     */
    String extractFieldType(Parameter parameter, String fieldName);

    /**
     * Maps the field to its corresponding validation resource.
     *
     * @param parameter the method parameter containing the annotation
     * @param fieldName the field name to map
     * @return the corresponding ValidationResource, or null if no mapping exists
     */
    Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName);

    /**
     * Gets the annotation type that this strategy handles.
     *
     * @return the annotation class this strategy is designed for
     */
    Class<? extends Annotation> getAnnotationType();

    /**
     * Gets a human-readable name for this strategy (for logging purposes).
     *
     * @return the strategy name
     */
    String getStrategyName();
}
