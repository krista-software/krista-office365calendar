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

import java.lang.reflect.Method;

/**
 * Test to demonstrate the dynamic field type extraction functionality.
 */
public class TestFieldTypeExtraction {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Dynamic Field Type Extraction ===\n");
        
        // Test 1: Extract field types from getSchedules method
        System.out.println("1. Testing getSchedules method field types:\n");
        Method getSchedulesMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");
        
        if (getSchedulesMethod != null) {
            System.out.println("Found getSchedules method, now extracting field types...\n");
            
            // Test Date From field type
            System.out.println("--- Extracting Date From field type ---");
            String dateFromType = FieldAttributeExtractor.extractFieldType(getSchedulesMethod, "Date From");
            System.out.println("Result: " + dateFromType + "\n");
            
            // Test Date To field type
            System.out.println("--- Extracting Date To field type ---");
            String dateToType = FieldAttributeExtractor.extractFieldType(getSchedulesMethod, "Date To");
            System.out.println("Result: " + dateToType + "\n");
            
            // Test Attendees field type
            System.out.println("--- Extracting Attendees field type ---");
            String attendeesType = FieldAttributeExtractor.extractFieldType(getSchedulesMethod, "Attendees");
            System.out.println("Result: " + attendeesType + "\n");
            
            // Test Time Zone field type
            System.out.println("--- Extracting Time Zone field type ---");
            String timeZoneType = FieldAttributeExtractor.extractFieldType(getSchedulesMethod, "Time Zone");
            System.out.println("Result: " + timeZoneType + "\n");
            
        } else {
            System.out.println("❌ Could not find getSchedules method");
        }
        
        System.out.println("\n2. Testing getFreeSlots method field types:\n");
        
        // Test with getFreeSlots method which has @Field.Date annotations
        Method getFreeSlotsMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getFreeSlots");
        
        if (getFreeSlotsMethod != null) {
            System.out.println("--- Extracting Date From field type from getFreeSlots ---");
            String complexDateType = FieldAttributeExtractor.extractFieldType(getFreeSlotsMethod, "Date From");
            System.out.println("Result: " + complexDateType + "\n");
            
            System.out.println("--- Extracting Date To field type from getFreeSlots ---");
            String complexDateToType = FieldAttributeExtractor.extractFieldType(getFreeSlotsMethod, "Date To");
            System.out.println("Result: " + complexDateToType + "\n");
        } else {
            System.out.println("❌ Could not find getFreeSlots method");
        }
        
        System.out.println("\n3. Testing scheduleMeeting method field types:\n");
        
        // Test with scheduleMeeting method which has different field types
        Method scheduleMeetingMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "scheduleMeeting");
        
        if (scheduleMeetingMethod != null) {
            System.out.println("--- Extracting Subject field type from scheduleMeeting ---");
            String subjectType = FieldAttributeExtractor.extractFieldType(scheduleMeetingMethod, "Subject");
            System.out.println("Result: " + subjectType + "\n");
            
            System.out.println("--- Extracting Date From field type from scheduleMeeting ---");
            String scheduleDateType = FieldAttributeExtractor.extractFieldType(scheduleMeetingMethod, "Date From");
            System.out.println("Result: " + scheduleDateType + "\n");
        } else {
            System.out.println("❌ Could not find scheduleMeeting method");
        }
        
        System.out.println("\n4. Testing non-existent field:\n");
        
        if (getSchedulesMethod != null) {
            System.out.println("--- Extracting non-existent field type ---");
            String nonExistentType = FieldAttributeExtractor.extractFieldType(getSchedulesMethod, "Non Existent Field");
            System.out.println("Result: " + nonExistentType + "\n");
        }
        
        System.out.println("=== Field Type Extraction Test Complete ===");
        
        System.out.println("\n📝 Summary of Expected Results:");
        System.out.println("  • Date From (getSchedules): 'Date' ✅");
        System.out.println("  • Date To (getSchedules): 'Date' ✅");
        System.out.println("  • Attendees (getSchedules): '[ Email ]' ✅");
        System.out.println("  • Time Zone (getSchedules): 'Text' ✅");
        System.out.println("  • Date From (getFreeSlots): 'Date' ✅ (from @Field.Date)");
        System.out.println("  • Subject (scheduleMeeting): 'Text' ✅");
        System.out.println("  • Non-existent field: null ✅");
        
        System.out.println("\n🎯 Key Benefits:");
        System.out.println("  • Field types are now extracted directly from method annotations");
        System.out.println("  • No more hardcoded field type mappings");
        System.out.println("  • Changing field types in annotations automatically updates validation");
        System.out.println("  • Full consistency between method definitions and validation results");
    }
}
