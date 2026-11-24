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

        // Test 1: Test FieldAttributeExtractor with detailed logging
        Method getSchedulesMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");

        if (getSchedulesMethod != null) {
            // Test Date From field (should have includeTimeOfDay=true)
            Map<String, Object> dateFromAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Date From");

            // Test Date To field (should have includeTimeOfDay=true)
            Map<String, Object> dateToAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Date To");

            // Test Attendees field (should be empty)
            Map<String, Object> attendeesAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Attendees");

            // Test Time Zone field (should be empty)
            Map<String, Object> timeZoneAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Time Zone");
        }

        // Test 2: Test with getFreeSlots method (more complex attributes)
        Method getFreeSlotsMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getFreeSlots");

        if (getFreeSlotsMethod != null) {
            Map<String, Object> complexDateAttributes = FieldAttributeExtractor.extractFieldAttributes(getFreeSlotsMethod, "Date From");
        }

        // Test 3: Test field name to resource mapping
        if (getSchedulesMethod != null) {
            Map<String, Validator.ValidationResource> mapping =
                FieldAttributeExtractor.createFieldNameToResourceMapping(getSchedulesMethod);
        }
    }
}
