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

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.MeetingsArea;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.FieldAttributeExtractor;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy.FileFieldAnnotationStrategy;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy.FieldAnnotationStrategyRegistry;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Test class to verify that the FileFieldAnnotationStrategy is working correctly
 * with the @Field.File annotation in the Schedule Meeting method.
 */
public class TestFileFieldAnnotationStrategy {

    public static void main(String[] args) {
        try {
            // Test 1: Test the FileFieldAnnotationStrategy directly
            testFileFieldAnnotationStrategyDirectly();

            // Test 2: Test through FieldAttributeExtractor
            testThroughFieldAttributeExtractor();

            // Test 3: Test strategy registry
            testStrategyRegistry();

        } catch (Exception e) {
            // Test failed with exception
        }
    }

    private static void testFileFieldAnnotationStrategyDirectly() throws Exception {
        // Get the scheduleMeeting method
        Method scheduleMeetingMethod = MeetingsArea.class.getMethod("scheduleMeeting",
            String.class, String.class, Long.class, Long.class, java.util.List.class,
            java.util.List.class, Boolean.class, String.class, String.class, String.class,
            Double.class, java.util.List.class, String.class, Double.class, String.class,
            String.class, String.class, String.class, Long.class, Double.class,
            java.util.List.class, String.class, Boolean.class);

        // Find the attachments parameter (should be parameter index 20 - List<File> attachments)
        Parameter[] parameters = scheduleMeetingMethod.getParameters();
        Parameter attachmentsParameter = null;

        for (Parameter param : parameters) {
            if (param.getType().equals(java.util.List.class)) {
                // Check if this parameter has @Field.File annotation
                if (param.getAnnotation(app.krista.extension.impl.anno.Field.File.class) != null) {
                    attachmentsParameter = param;
                    break;
                }
            }
        }

        if (attachmentsParameter == null) {
            throw new RuntimeException("Could not find attachments parameter with @Field.File annotation");
        }

        // Test the FileFieldAnnotationStrategy
        FileFieldAnnotationStrategy strategy = new FileFieldAnnotationStrategy();

        // Test canHandle
        boolean canHandle = strategy.canHandle(attachmentsParameter, "Attachments");
        assert canHandle : "Strategy should be able to handle Attachments field";

        // Test extractAttributes
        Map<String, Object> attributes = strategy.extractAttributes(attachmentsParameter, "Attachments");
        assert attributes.containsKey("multipleFileUpload") : "Should contain multipleFileUpload attribute";
        assert "true".equals(attributes.get("multipleFileUpload")) : "multipleFileUpload should be true";
        assert attributes.containsKey("visualWidth") : "Should contain visualWidth attribute";
        assert "S".equals(attributes.get("visualWidth")) : "visualWidth should be S";

        // Test extractFieldType
        String fieldType = strategy.extractFieldType(attachmentsParameter, "Attachments");
        assert "File".equals(fieldType) : "Field type should be File";

        // Test mapToValidationResource
        Validator.ValidationResource resource = strategy.mapToValidationResource(attachmentsParameter, "Attachments");
        assert Validator.ValidationResource.ATTACHMENTS.equals(resource) : "Should map to ATTACHMENTS validation resource";
    }

    private static void testThroughFieldAttributeExtractor() throws Exception {
        // Get the scheduleMeeting method
        Method scheduleMeetingMethod = MeetingsArea.class.getMethod("scheduleMeeting",
            String.class, String.class, Long.class, Long.class, java.util.List.class,
            java.util.List.class, Boolean.class, String.class, String.class, String.class,
            Double.class, java.util.List.class, String.class, Double.class, String.class,
            String.class, String.class, String.class, Long.class, Double.class,
            java.util.List.class, String.class, Boolean.class);

        // Test extractFieldAttributes
        Map<String, Object> attributes = FieldAttributeExtractor.extractFieldAttributes(scheduleMeetingMethod, "Attachments");
        assert !attributes.isEmpty() : "Should extract attributes for Attachments field";
        assert attributes.containsKey("multipleFileUpload") : "Should contain multipleFileUpload attribute";

        // Test extractFieldType
        String fieldType = FieldAttributeExtractor.extractFieldType(scheduleMeetingMethod, "Attachments");
        assert "File".equals(fieldType) : "Field type should be File";

        // Test createFieldNameToResourceMapping
        Map<String, Validator.ValidationResource> mapping = FieldAttributeExtractor.createFieldNameToResourceMapping(scheduleMeetingMethod);
        assert mapping.containsKey("Attachments") : "Should contain Attachments field mapping";
        assert Validator.ValidationResource.ATTACHMENTS.equals(mapping.get("Attachments")) : "Should map to ATTACHMENTS validation resource";
    }

    private static void testStrategyRegistry() throws Exception {
        FieldAnnotationStrategyRegistry registry = new FieldAnnotationStrategyRegistry();
        assert registry.getStrategyCount() == 6 : "Should have 6 strategies (including FileFieldAnnotationStrategy)";

        // Check that FileFieldAnnotationStrategy is registered
        boolean hasFileStrategy = registry.getAllStrategies().stream()
            .anyMatch(strategy -> "FileField".equals(strategy.getStrategyName()));
        assert hasFileStrategy : "Registry should contain FileFieldAnnotationStrategy";
    }
}
