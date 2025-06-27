package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.MeetingsArea;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for FieldAttributeExtractor class.
 * Tests the dynamic extraction of field attributes from method parameters.
 */
public class FieldAttributeExtractorTest {

    @Test
    public void testExtractFieldAttributes_GetSchedulesMethod() {
        // Get the getSchedules method from MeetingsArea
        Method getSchedulesMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");
        assertNotNull("getSchedules method should be found", getSchedulesMethod);

        // Test Date From field attributes
        Map<String, Object> dateFromAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Date From");
        assertNotNull(dateFromAttributes);
        assertEquals("Date From should have includeTimeOfDay=true", "true", dateFromAttributes.get("includeTimeOfDay"));

        // Test Date To field attributes
        Map<String, Object> dateToAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Date To");
        assertNotNull(dateToAttributes);
        assertEquals("Date To should have includeTimeOfDay=true", "true", dateToAttributes.get("includeTimeOfDay"));

        // Test Attendees field attributes (should be empty for Field.Desc)
        Map<String, Object> attendeesAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Attendees");
        assertNotNull(attendeesAttributes);
        assertTrue("Attendees field should have no attributes", attendeesAttributes.isEmpty());

        // Test Time Zone field attributes (should be empty for basic Field)
        Map<String, Object> timeZoneAttributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Time Zone");
        assertNotNull(timeZoneAttributes);
        assertTrue("Time Zone field should have no attributes", timeZoneAttributes.isEmpty());
    }

    @Test
    public void testExtractFieldAttributes_NonExistentField() {
        Method getSchedulesMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");
        assertNotNull(getSchedulesMethod);

        // Test with a field name that doesn't exist
        Map<String, Object> attributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, "Non Existent Field");
        assertNotNull(attributes);
        assertTrue("Non-existent field should return empty attributes", attributes.isEmpty());
    }

    @Test
    public void testExtractFieldAttributes_NullInputs() {
        // Test with null method
        Map<String, Object> attributes = FieldAttributeExtractor.extractFieldAttributes(null, "Date From");
        assertNotNull(attributes);
        assertTrue("Null method should return empty attributes", attributes.isEmpty());

        // Test with null field name
        Method getSchedulesMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");
        attributes = FieldAttributeExtractor.extractFieldAttributes(getSchedulesMethod, null);
        assertNotNull(attributes);
        assertTrue("Null field name should return empty attributes", attributes.isEmpty());
    }

    @Test
    public void testGetMethodByName() {
        // Test finding existing method
        Method method = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");
        assertNotNull("getSchedules method should be found", method);
        assertEquals("getSchedules", method.getName());

        // Test finding non-existent method
        method = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "nonExistentMethod");
        assertNull("Non-existent method should return null", method);

        // Test with null class
        method = FieldAttributeExtractor.getMethodByName(null, "getSchedules");
        assertNull("Null class should return null", method);

        // Test with null method name
        method = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, null);
        assertNull("Null method name should return null", method);
    }

    @Test
    public void testCreateFieldNameToResourceMapping() {
        Method getSchedulesMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getSchedules");
        assertNotNull(getSchedulesMethod);

        Map<String, Validator.ValidationResource> mapping =
            FieldAttributeExtractor.createFieldNameToResourceMapping(getSchedulesMethod);

        assertNotNull(mapping);
        assertFalse("Mapping should not be empty", mapping.isEmpty());

        // Verify specific mappings
        assertEquals(Validator.ValidationResource.FROM_DATE, mapping.get("Date From"));
        assertEquals(Validator.ValidationResource.TO_DATE, mapping.get("Date To"));
        assertEquals(Validator.ValidationResource.ATTENDEES, mapping.get("Attendees"));

        // Time Zone might map to a different resource or be null depending on implementation
        // We'll just check that the mapping contains the expected fields
        assertTrue("Mapping should contain Date From", mapping.containsKey("Date From"));
        assertTrue("Mapping should contain Date To", mapping.containsKey("Date To"));
        assertTrue("Mapping should contain Attendees", mapping.containsKey("Attendees"));
    }

    @Test
    public void testCreateFieldNameToResourceMapping_NullMethod() {
        Map<String, Validator.ValidationResource> mapping =
            FieldAttributeExtractor.createFieldNameToResourceMapping(null);

        assertNotNull(mapping);
        assertTrue("Null method should return empty mapping", mapping.isEmpty());
    }

    @Test
    public void testExtractFieldAttributes_ScheduleMeetingMethod() {
        // Test with a different method that has more complex field attributes
        Method scheduleMeetingMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "scheduleMeeting");
        assertNotNull("scheduleMeeting method should be found", scheduleMeetingMethod);

        // Test Date From field attributes (should have includeTimeOfDay=true)
        Map<String, Object> dateFromAttributes = FieldAttributeExtractor.extractFieldAttributes(scheduleMeetingMethod, "Date From");
        assertNotNull(dateFromAttributes);
        // The scheduleMeeting method uses @Field.Date annotation with includeTimeOfDay = true
        assertEquals("Date From should have includeTimeOfDay=true", "true", dateFromAttributes.get("includeTimeOfDay"));
    }

    @Test
    public void testExtractFieldAttributes_GetFreeSlotsMethod() {
        // Test with getFreeSlots method which has different date field configurations
        Method getFreeSlotsMethod = FieldAttributeExtractor.getMethodByName(MeetingsArea.class, "getFreeSlots");
        assertNotNull("getFreeSlots method should be found", getFreeSlotsMethod);

        // Test Date From field attributes
        Map<String, Object> dateFromAttributes = FieldAttributeExtractor.extractFieldAttributes(getFreeSlotsMethod, "Date From");
        assertNotNull(dateFromAttributes);

        // The getFreeSlots method has additional attributes like showHowManyDaysInViewer and allowPast
        assertEquals("Date From should have showHowManyDaysInViewer=60", "60", dateFromAttributes.get("showHowManyDaysInViewer"));
        assertEquals("Date From should have allowPast=false", "false", dateFromAttributes.get("allowPast"));
        assertEquals("Date From should have visualWidth=S", "S", dateFromAttributes.get("visualWidth"));
    }
}
