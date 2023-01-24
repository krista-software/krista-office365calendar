package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity;


import app.krista.extension.impl.anno.*;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "7ecfd34e-b2f0-44a7-97f8-71b9a9e517f7")
@Entity(name = "Event Details", id = "localDomainEntity_b13aad88-786e-440a-8131-84351124a9cf", primaryKey = "Event Id")
public class EventDetails {

    @Field(name = "Event Id", type = "Text", required = false)
    public String eventId;

    @Field(name = "Location", type = "Text", required = false)
    public String location;

    @Searchable
    @Field(name = "Title", type = "Text", required = false)
    public String title;

    @Field(name = "Contacts", type = "Checkboxes", required = false)
    public Object contacts;

    @Field(name = "Details", type = "RichText", required = false)
    public String details;

    @Field(name = "Date From", type = "Date", required = false, attributes = @Attribute(name = "includeTimeOfDay", value = "true"))
    public Long dateFrom;

    @Field(name = "Date To", type = "Date", required = false, attributes = @Attribute(name = "includeTimeOfDay", value = "true"))
    public Long dateTo;

}
