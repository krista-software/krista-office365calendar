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
        System.out.println("=== Testing FileFieldAnnotationStrategy ===\n");

        try {
            // Test 1: Test the FileFieldAnnotationStrategy directly
            testFileFieldAnnotationStrategyDirectly();
            
            // Test 2: Test through FieldAttributeExtractor
            testThroughFieldAttributeExtractor();
            
            // Test 3: Test strategy registry
            testStrategyRegistry();
            
            System.out.println("\n=== All FileFieldAnnotationStrategy tests completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testFileFieldAnnotationStrategyDirectly() throws Exception {
        System.out.println("1. Testing FileFieldAnnotationStrategy directly:\n");
        
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
        
        System.out.println("Found attachments parameter: " + attachmentsParameter.getType().getSimpleName());
        
        // Test the FileFieldAnnotationStrategy
        FileFieldAnnotationStrategy strategy = new FileFieldAnnotationStrategy();
        
        // Test canHandle
        boolean canHandle = strategy.canHandle(attachmentsParameter, "Attachments");
        System.out.println("Can handle 'Attachments': " + canHandle);
        assert canHandle : "Strategy should be able to handle Attachments field";
        
        // Test extractAttributes
        Map<String, Object> attributes = strategy.extractAttributes(attachmentsParameter, "Attachments");
        System.out.println("Extracted attributes: " + attributes);
        assert attributes.containsKey("multipleFileUpload") : "Should contain multipleFileUpload attribute";
        assert "true".equals(attributes.get("multipleFileUpload")) : "multipleFileUpload should be true";
        assert attributes.containsKey("visualWidth") : "Should contain visualWidth attribute";
        assert "S".equals(attributes.get("visualWidth")) : "visualWidth should be S";
        
        // Test extractFieldType
        String fieldType = strategy.extractFieldType(attachmentsParameter, "Attachments");
        System.out.println("Extracted field type: " + fieldType);
        assert "File".equals(fieldType) : "Field type should be File";
        
        // Test mapToValidationResource
        Validator.ValidationResource resource = strategy.mapToValidationResource(attachmentsParameter, "Attachments");
        System.out.println("Mapped validation resource: " + resource);
        assert Validator.ValidationResource.ATTACHMENTS.equals(resource) : "Should map to ATTACHMENTS validation resource";
        
        System.out.println("✓ FileFieldAnnotationStrategy direct test passed!\n");
    }

    private static void testThroughFieldAttributeExtractor() throws Exception {
        System.out.println("2. Testing through FieldAttributeExtractor:\n");
        
        // Get the scheduleMeeting method
        Method scheduleMeetingMethod = MeetingsArea.class.getMethod("scheduleMeeting", 
            String.class, String.class, Long.class, Long.class, java.util.List.class, 
            java.util.List.class, Boolean.class, String.class, String.class, String.class, 
            Double.class, java.util.List.class, String.class, Double.class, String.class, 
            String.class, String.class, String.class, Long.class, Double.class, 
            java.util.List.class, String.class, Boolean.class);
        
        // Test extractFieldAttributes
        Map<String, Object> attributes = FieldAttributeExtractor.extractFieldAttributes(scheduleMeetingMethod, "Attachments");
        System.out.println("FieldAttributeExtractor.extractFieldAttributes result: " + attributes);
        assert !attributes.isEmpty() : "Should extract attributes for Attachments field";
        assert attributes.containsKey("multipleFileUpload") : "Should contain multipleFileUpload attribute";
        
        // Test extractFieldType
        String fieldType = FieldAttributeExtractor.extractFieldType(scheduleMeetingMethod, "Attachments");
        System.out.println("FieldAttributeExtractor.extractFieldType result: " + fieldType);
        assert "File".equals(fieldType) : "Field type should be File";
        
        // Test createFieldNameToResourceMapping
        Map<String, Validator.ValidationResource> mapping = FieldAttributeExtractor.createFieldNameToResourceMapping(scheduleMeetingMethod);
        System.out.println("FieldAttributeExtractor.createFieldNameToResourceMapping result: " + mapping);
        assert mapping.containsKey("Attachments") : "Should contain Attachments field mapping";
        assert Validator.ValidationResource.ATTACHMENTS.equals(mapping.get("Attachments")) : "Should map to ATTACHMENTS validation resource";
        
        System.out.println("✓ FieldAttributeExtractor test passed!\n");
    }

    private static void testStrategyRegistry() throws Exception {
        System.out.println("3. Testing FieldAnnotationStrategyRegistry:\n");
        
        FieldAnnotationStrategyRegistry registry = new FieldAnnotationStrategyRegistry();
        System.out.println("Registry created with " + registry.getStrategyCount() + " strategies");
        assert registry.getStrategyCount() == 6 : "Should have 6 strategies (including FileFieldAnnotationStrategy)";
        
        // Check that FileFieldAnnotationStrategy is registered
        boolean hasFileStrategy = registry.getAllStrategies().stream()
            .anyMatch(strategy -> "FileField".equals(strategy.getStrategyName()));
        assert hasFileStrategy : "Registry should contain FileFieldAnnotationStrategy";
        
        System.out.println("✓ FieldAnnotationStrategyRegistry test passed!\n");
    }
}
