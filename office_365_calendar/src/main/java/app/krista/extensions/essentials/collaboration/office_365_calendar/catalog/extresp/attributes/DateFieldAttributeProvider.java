package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.attributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides default attributes for Date field types.
 * This class encapsulates all the default configuration for date fields.
 */
public class DateFieldAttributeProvider implements FieldAttributeProvider {
    
    @Override
    public Map<String, Object> getDefaultAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("visualWidth", "S");
        attributes.put("dateFormat", "MM/dd/yyyy");
        attributes.put("showCalendar", true);
        attributes.put("allowPastDates", true);
        attributes.put("allowFutureDates", true);
        return attributes;
    }
    
    @Override
    public String getFieldType() {
        return "date";
    }
}
