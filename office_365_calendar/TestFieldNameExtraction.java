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

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Test to demonstrate the dynamic field name extraction functionality.
 * This shows how field names are now extracted directly from method annotations
 * instead of being hardcoded in validators.
 */
public class TestFieldNameExtraction {

    public static void main(String[] args) {

        // Test 1: Extract field names from getSchedules method
        Method getSchedulesMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");

        if (getSchedulesMethod != null) {
            // Create the field name to resource mapping
            Map<String, Validator.ValidationResource> mapping =
                FieldAttributeExtractor.createFieldNameToResourceMapping(getSchedulesMethod);

            // Test finding field names for specific resources
            String dateFromFieldName = findFieldNameForResource(Validator.ValidationResource.FROM_DATE, mapping);
            String dateToFieldName = findFieldNameForResource(Validator.ValidationResource.TO_DATE, mapping);
            String attendeesFieldName = findFieldNameForResource(Validator.ValidationResource.ATTENDEES, mapping);
            String eventIdFieldName = findFieldNameForResource(Validator.ValidationResource.EVENT_ID, mapping);
        }

        // Test 2: Test with getFreeSlots method
        Method getFreeSlotsMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getFreeSlots");

        if (getFreeSlotsMethod != null) {
            Map<String, Validator.ValidationResource> freeSlotsMapping =
                FieldAttributeExtractor.createFieldNameToResourceMapping(getFreeSlotsMethod);
        }

        // Test 3: Test with scheduleMeeting method
        Method scheduleMeetingMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "scheduleMeeting");

        if (scheduleMeetingMethod != null) {
            Map<String, Validator.ValidationResource> scheduleMeetingMapping =
                FieldAttributeExtractor.createFieldNameToResourceMapping(scheduleMeetingMethod);
        }
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
