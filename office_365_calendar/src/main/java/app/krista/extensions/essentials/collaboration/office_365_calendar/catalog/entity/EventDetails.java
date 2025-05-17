package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity;

import java.util.List;

import app.krista.extension.impl.anno.*;

/**
 * Represents the details of a calendar event within the Collaboration domain of the Essentials ecosystem.
 * <p>
 * This entity is used to store and manage information about events, including their identifiers,
 * location, title, contacts, description, date range, time zone, and organizer.
 * <p>
 * The class is annotated for use with the organization's domain-driven and entity frameworks,
 * supporting features such as search ability and rich text fields.
 * <p>
 * Fields:
 * eventId - Unique identifier for the event.
 * location - Location where the event takes place.
 * title - Title of the event (searchable).
 * contacts - List of contacts associated with the event.
 * details - Rich text description of the event.
 * dateFrom - Start date and time of the event (epoch millis).
 * dateTo - End date and time of the event (epoch millis).
 * timeZone - Time zone of the event.
 * organizer - Organizer of the event.
 * <p>
 * Annotations are used to define domain, entity, and field metadata for integration with
 * the Krista platform's catalog and search infrastructure.
 */

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "2ab5b937-0a80-4bb4-894a-cd72104f864a")
@Entity(name = "Event Details", id = "localDomainEntity_b13aad88-786e-440a-8131-84351124a9cf", primaryKey = "Event Id", supportStore = false)
public class EventDetails {

    @Field(name = "Event Id", type = "Text", required = false)
    public String eventId;

    @Field(name = "Location", type = "Text", required = false)
    public String location;

    @Searchable
    @Field(name = "Title", type = "Text", required = false)
    public String title;

    @Field.Desc(name = "Contacts", type = "[ Text ]", required = false)
    public List<String> contacts;

    @Field(name = "Details", type = "RichText", required = false)
    public String details;

    @Field(name = "Date From", type = "Date", required = false, attributes = @Attribute(name = "includeTimeOfDay", value = "true"))
    public Long dateFrom;

    @Field(name = "Date To", type = "Date", required = false, attributes = @Attribute(name = "includeTimeOfDay", value = "true"))
    public Long dateTo;

    @Field.Text(name = "Time Zone", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String timeZone;

    @Field.Text(name = "Organizer", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String organizer;
}
