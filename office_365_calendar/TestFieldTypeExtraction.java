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

        // Test 1: Extract field types from getSchedules method
        Method getSchedulesMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");

        if (getSchedulesMethod != null) {
            // Test Date From field type
            String dateFromType = FieldAttributeExtractor.extractFieldType(getSchedulesMethod, "Date From");

            // Test Date To field type
            String dateToType = FieldAttributeExtractor.extractFieldType(getSchedulesMethod, "Date To");

            // Test Attendees field type
            String attendeesType = FieldAttributeExtractor.extractFieldType(getSchedulesMethod, "Attendees");

            // Test Time Zone field type
            String timeZoneType = FieldAttributeExtractor.extractFieldType(getSchedulesMethod, "Time Zone");
        }

        // Test 2: Test with getFreeSlots method which has @Field.Date annotations
        Method getFreeSlotsMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getFreeSlots");

        if (getFreeSlotsMethod != null) {
            String complexDateType = FieldAttributeExtractor.extractFieldType(getFreeSlotsMethod, "Date From");
            String complexDateToType = FieldAttributeExtractor.extractFieldType(getFreeSlotsMethod, "Date To");
        }

        // Test 3: Test with scheduleMeeting method which has different field types
        Method scheduleMeetingMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "scheduleMeeting");

        if (scheduleMeetingMethod != null) {
            String subjectType = FieldAttributeExtractor.extractFieldType(scheduleMeetingMethod, "Subject");
            String scheduleDateType = FieldAttributeExtractor.extractFieldType(scheduleMeetingMethod, "Date From");
        }

        // Test 4: Testing non-existent field
        if (getSchedulesMethod != null) {
            String nonExistentType = FieldAttributeExtractor.extractFieldType(getSchedulesMethod, "Non Existent Field");
        }
    }
}
