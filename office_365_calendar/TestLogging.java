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
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;

/**
 * Test to demonstrate the logging functionality in the dynamic field attribute extraction.
 */
public class TestLogging {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Dynamic Field Attribute Extraction with Logging ===\n");
        
        // Create a mock ValidationOrchestrator (we can't inject dependencies easily in this test)
        // But we can test the FieldAttributeExtractor directly
        
        System.out.println("1. Testing FieldAttributeExtractor with detailed logging:\n");
        
        // Get the getSchedules method
        Method getSchedulesMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");
        
        if (getSchedulesMethod != null) {
            System.out.println("Found getSchedules method, now extracting field attributes...\n");
            
            // Test Date From field (should have includeTimeOfDay=true)
            System.out.println("--- Extracting Date From field attributes ---");
            Map<String, Object> dateFromAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Date From");
            System.out.println("Result: " + dateFromAttributes + "\n");
            
            // Test Date To field (should have includeTimeOfDay=true)
            System.out.println("--- Extracting Date To field attributes ---");
            Map<String, Object> dateToAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Date To");
            System.out.println("Result: " + dateToAttributes + "\n");
            
            // Test Attendees field (should be empty)
            System.out.println("--- Extracting Attendees field attributes ---");
            Map<String, Object> attendeesAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Attendees");
            System.out.println("Result: " + attendeesAttributes + "\n");
            
            // Test Time Zone field (should be empty)
            System.out.println("--- Extracting Time Zone field attributes ---");
            Map<String, Object> timeZoneAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Time Zone");
            System.out.println("Result: " + timeZoneAttributes + "\n");
            
        } else {
            System.out.println("❌ Could not find getSchedules method");
        }
        
        System.out.println("\n2. Testing with getFreeSlots method (more complex attributes):\n");
        
        // Test with getFreeSlots method which has more complex date attributes
        Method getFreeSlotsMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getFreeSlots");
        
        if (getFreeSlotsMethod != null) {
            System.out.println("--- Extracting Date From field attributes from getFreeSlots ---");
            Map<String, Object> complexDateAttributes = FieldAttributeExtractor.extractFieldAttributes(getFreeSlotsMethod, "Date From");
            System.out.println("Result: " + complexDateAttributes + "\n");
        } else {
            System.out.println("❌ Could not find getFreeSlots method");
        }
        
        System.out.println("\n3. Testing field name to resource mapping:\n");
        
        if (getSchedulesMethod != null) {
            System.out.println("--- Creating field name to resource mapping ---");
            Map<String, Validator.ValidationResource> mapping = 
                FieldAttributeExtractor.createFieldNameToResourceMapping(getSchedulesMethod);
            System.out.println("Mapping result: " + mapping + "\n");
        }
        
        System.out.println("=== Logging Test Complete ===");
        
        System.out.println("\n📝 Note: When ValidationOrchestrator.validate() is called with dynamic parameters,");
        System.out.println("you will see detailed logging showing:");
        System.out.println("  • Method discovery and field mapping");
        System.out.println("  • Field attribute extraction process");
        System.out.println("  • Whether dynamic or fallback attributes are used");
        System.out.println("  • Validation results for each resource");
    }
}
