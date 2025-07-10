import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.MeetingsArea;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.FieldAttributeExtractor;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Test to demonstrate the dynamic field name extraction functionality.
 * This shows how field names are now extracted directly from method annotations
 * instead of being hardcoded in validators.
 */
public class TestFieldNameExtraction {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Dynamic Field Name Extraction ===\n");
        
        // Test 1: Extract field names from getSchedules method
        System.out.println("1. Testing getSchedules method field name mapping:\n");
        Method getSchedulesMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");
        
        if (getSchedulesMethod != null) {
            System.out.println("Found getSchedules method, creating field name to resource mapping...\n");
            
            // Create the field name to resource mapping
            Map<String, Validator.ValidationResource> mapping = 
                FieldAttributeExtractor.createFieldNameToResourceMapping(getSchedulesMethod);
            
            System.out.println("Complete field name to resource mapping:");
            for (Map.Entry<String, Validator.ValidationResource> entry : mapping.entrySet()) {
                System.out.println("  • Field Name: '" + entry.getKey() + "' → Resource: " + entry.getValue());
            }
            
            System.out.println("\n--- Testing reverse lookup (Resource → Field Name) ---");
            
            // Test finding field names for specific resources
            String dateFromFieldName = findFieldNameForResource(Validator.ValidationResource.FROM_DATE, mapping);
            System.out.println("FROM_DATE resource → Field Name: '" + dateFromFieldName + "'");
            
            String dateToFieldName = findFieldNameForResource(Validator.ValidationResource.TO_DATE, mapping);
            System.out.println("TO_DATE resource → Field Name: '" + dateToFieldName + "'");
            
            String attendeesFieldName = findFieldNameForResource(Validator.ValidationResource.ATTENDEES, mapping);
            System.out.println("ATTENDEES resource → Field Name: '" + attendeesFieldName + "'");
            
            String eventIdFieldName = findFieldNameForResource(Validator.ValidationResource.EVENT_ID, mapping);
            System.out.println("EVENT_ID resource → Field Name: '" + eventIdFieldName + "'");
            
        } else {
            System.out.println("❌ Could not find getSchedules method");
        }
        
        System.out.println("\n2. Testing getFreeSlots method field name mapping:\n");
        
        // Test with getFreeSlots method
        Method getFreeSlotsMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getFreeSlots");
        
        if (getFreeSlotsMethod != null) {
            Map<String, Validator.ValidationResource> freeSlotsMapping = 
                FieldAttributeExtractor.createFieldNameToResourceMapping(getFreeSlotsMethod);
            
            System.out.println("getFreeSlots field name to resource mapping:");
            for (Map.Entry<String, Validator.ValidationResource> entry : freeSlotsMapping.entrySet()) {
                System.out.println("  • Field Name: '" + entry.getKey() + "' → Resource: " + entry.getValue());
            }
        } else {
            System.out.println("❌ Could not find getFreeSlots method");
        }
        
        System.out.println("\n3. Testing scheduleMeeting method field name mapping:\n");
        
        // Test with scheduleMeeting method
        Method scheduleMeetingMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "scheduleMeeting");
        
        if (scheduleMeetingMethod != null) {
            Map<String, Validator.ValidationResource> scheduleMeetingMapping = 
                FieldAttributeExtractor.createFieldNameToResourceMapping(scheduleMeetingMethod);
            
            System.out.println("scheduleMeeting field name to resource mapping (first 10 fields):");
            int count = 0;
            for (Map.Entry<String, Validator.ValidationResource> entry : scheduleMeetingMapping.entrySet()) {
                if (count < 10) {
                    System.out.println("  • Field Name: '" + entry.getKey() + "' → Resource: " + entry.getValue());
                    count++;
                } else {
                    break;
                }
            }
            if (scheduleMeetingMapping.size() > 10) {
                System.out.println("  ... and " + (scheduleMeetingMapping.size() - 10) + " more fields");
            }
        } else {
            System.out.println("❌ Could not find scheduleMeeting method");
        }
        
        System.out.println("\n=== Field Name Extraction Test Complete ===");
        
        System.out.println("\n📝 Summary of Expected Results:");
        System.out.println("  • FROM_DATE → 'Date From' ✅");
        System.out.println("  • TO_DATE → 'Date To' ✅");
        System.out.println("  • ATTENDEES → 'Attendees' ✅");
        System.out.println("  • EVENT_ID → 'Time Zone' ✅ (from getSchedules method)");
        
        System.out.println("\n🎯 Key Benefits:");
        System.out.println("  • Field names are now extracted directly from method annotations");
        System.out.println("  • No more hardcoded field name mappings in validators");
        System.out.println("  • Changing field names in annotations automatically updates validation");
        System.out.println("  • ValidationResult now shows the actual field names from method definitions");
        System.out.println("  • Complete consistency between method definitions and validation results");
        
        System.out.println("\n💡 Impact on ValidationResult:");
        System.out.println("  • Before: ValidationResult used validator.getFetchFieldName() (hardcoded)");
        System.out.println("  • After: ValidationResult uses dynamically extracted field names");
        System.out.println("  • Result: Field names in validation errors match exactly the method annotations");
    }
    
    /**
     * Helper method to find field name for a given validation resource.
     * This simulates the findFieldNameForResource method in ValidationOrchestrator.
     */
    private static String findFieldNameForResource(Validator.ValidationResource resource, 
                                                  Map<String, Validator.ValidationResource> fieldNameToResourceMapping) {
        for (Map.Entry<String, Validator.ValidationResource> entry : fieldNameToResourceMapping.entrySet()) {
            if (resource.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
