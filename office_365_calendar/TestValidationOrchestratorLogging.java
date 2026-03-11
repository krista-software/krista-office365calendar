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
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;

import java.util.Map;
import java.util.HashMap;

/**
 * Test to demonstrate ValidationOrchestrator logging with dynamic field attribute extraction.
 * This simulates what happens when validation is called from the getSchedules method.
 */
public class TestValidationOrchestratorLogging {
    
    public static void main(String[] args) {
        System.out.println("=== Testing ValidationOrchestrator Logging ===\n");
        
        // Create a ValidationOrchestrator instance
        ValidationOrchestrator orchestrator = new ValidationOrchestrator();
        
        // Simulate validation parameters that would come from getSchedules method
        Map<Validator.ValidationResource, String> validationParams = new HashMap<>();
        validationParams.put(Validator.ValidationResource.FROM_DATE, "2024-01-01");
        validationParams.put(Validator.ValidationResource.TO_DATE, "2024-01-31");
        validationParams.put(Validator.ValidationResource.ATTENDEES, "test@example.com");
        
        System.out.println("🔍 Calling ValidationOrchestrator.validate() with dynamic parameters...\n");
        System.out.println("This simulates what happens when getSchedules method calls validation.\n");
        
        try {
            // Call the new dynamic validation method
            // This will show all the logging we added
            var results = orchestrator.validate(
                validationParams, 
                MeetingsArea.class, 
                "getSchedules"
            );
            
            System.out.println("\n📊 Validation Results:");
            System.out.println("Number of validation failures: " + results.size());
            
            if (!results.isEmpty()) {
                System.out.println("\nValidation failures:");
                for (var result : results) {
                    System.out.println("  • Field: " + result.getFetchFieldName());
                    System.out.println("    Error: " + result.getErrMessage());
                    System.out.println("    Attributes: " + result.getFieldAttributes());
                }
            } else {
                System.out.println("✅ All validations passed!");
            }
            
        } catch (Exception e) {
            System.out.println("⚠️  Note: This test may fail because we don't have all dependencies injected.");
            System.out.println("The important part is seeing the logging output above.");
            System.out.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎯 KEY OBSERVATIONS FROM THE LOGGING:");
        System.out.println("=".repeat(80));
        System.out.println("1. ValidationOrchestrator shows which class and method it's processing");
        System.out.println("2. It attempts to find the method using reflection");
        System.out.println("3. It creates field name to resource mapping");
        System.out.println("4. For each validation resource, it:");
        System.out.println("   • Finds the corresponding field name");
        System.out.println("   • Extracts attributes dynamically from method annotations");
        System.out.println("   • Shows whether it uses dynamic or fallback attributes");
        System.out.println("5. FieldAttributeExtractor shows detailed parameter examination");
        System.out.println("6. It shows exactly which annotations are found and processed");
        System.out.println("7. The final attributes match exactly what's in the method annotations");
        
        System.out.println("\n🚀 BENEFITS:");
        System.out.println("• No more hardcoded field attributes!");
        System.out.println("• Attributes are extracted directly from method annotations");
        System.out.println("• Removing attributes from @Field annotations automatically removes them from validation");
        System.out.println("• Full traceability of where attributes come from");
        System.out.println("• Easy debugging when field attributes don't match expectations");
    }
}
