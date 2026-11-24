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

        // Create a ValidationOrchestrator instance
        ValidationOrchestrator orchestrator = new ValidationOrchestrator();

        // Simulate validation parameters that would come from getSchedules method
        Map<Validator.ValidationResource, String> validationParams = new HashMap<>();
        validationParams.put(Validator.ValidationResource.FROM_DATE, "2024-01-01");
        validationParams.put(Validator.ValidationResource.TO_DATE, "2024-01-31");
        validationParams.put(Validator.ValidationResource.ATTENDEES, "test@example.com");

        try {
            // Call the new dynamic validation method
            var results = orchestrator.validate(
                validationParams,
                MeetingsArea.class,
                "getSchedules"
            );

        } catch (Exception e) {
            // Exception during test execution
        }
    }
}
