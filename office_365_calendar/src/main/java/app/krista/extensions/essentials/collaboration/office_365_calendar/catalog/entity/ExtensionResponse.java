package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity;

import app.krista.extension.impl.anno.*;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "2329f537-825e-4037-8a7e-cdc1f223419c")
@Entity(name = "Extension Response", id = "localDomainEntity_418023aa-8802-4794-85ae-dcbb7b272aa6", primaryKey = "Message", supportStore = false, options = {})
public class ExtensionResponse {

    // Debug constructor
    public ExtensionResponse() {
    }

    @Field.Text(name = "Message", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'A detailed message describing the extension\\'s response'")}, options = {})
    public String message;

    @Field(name = "Technical Detailed Error Report", type = "RichText", required = false, attributes = {@Attribute(name = "visualWidth", value = "L"), @Attribute(name = "toolTip", value = "'Provides error details to assist the technical team in troubleshooting'")}, options = {})
    public String technicalDetailedErrorReport;

    @Field.PickOne(name = "Response Type", values = {}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Specifies the type of exception to aid in categorizing and handling errors'")}, options = {})
    public String responseType;

    @Field(name = "Time Taken In Seconds", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Indicates the total duration of the operation, measured in seconds'")}, options = {})
    public Double timeTakenInSeconds;

}

