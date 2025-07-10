import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.MeetingsArea;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.FieldAttributeExtractor;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Simple test to verify the dynamic field attribute extraction works correctly.
 */
public class TestValidationOrchestrator {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Dynamic Field Attribute Extraction ===");
        
        // Test 1: Extract field attributes from getSchedules method
        System.out.println("\n1. Testing getSchedules method:");
        Method getSchedulesMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");
        if (getSchedulesMethod != null) {
            System.out.println("✓ Found getSchedules method");
            
            // Test Date From field
            Map<String, Object> dateFromAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Date From");
            System.out.println("Date From attributes: " + dateFromAttributes);
            
            // Test Date To field
            Map<String, Object> dateToAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Date To");
            System.out.println("Date To attributes: " + dateToAttributes);
            
            // Test Attendees field
            Map<String, Object> attendeesAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Attendees");
            System.out.println("Attendees attributes: " + attendeesAttributes);
            
            // Test Time Zone field
            Map<String, Object> timeZoneAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Time Zone");
            System.out.println("Time Zone attributes: " + timeZoneAttributes);
        } else {
            System.out.println("✗ Could not find getSchedules method");
        }
        
        // Test 2: Extract field attributes from getFreeSlots method
        System.out.println("\n2. Testing getFreeSlots method:");
        Method getFreeSlotsMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getFreeSlots");
        if (getFreeSlotsMethod != null) {
            System.out.println("✓ Found getFreeSlots method");
            
            // Test Date From field (should have more attributes)
            Map<String, Object> dateFromAttributes = FieldAttributeExtractor.extractFieldAttributes(getFreeSlotsMethod, "Date From");
            System.out.println("Date From attributes: " + dateFromAttributes);
        } else {
            System.out.println("✗ Could not find getFreeSlots method");
        }
        
        // Test 3: Create field name to resource mapping
        System.out.println("\n3. Testing field name to resource mapping:");
        if (getSchedulesMethod != null) {
            Map<String, Validator.ValidationResource> mapping = 
                FieldAttributeExtractor.createFieldNameToResourceMapping(getSchedulesMethod);
            System.out.println("Field name to resource mapping: " + mapping);
        }
        
        System.out.println("\n=== Test Complete ===");
    }
}
