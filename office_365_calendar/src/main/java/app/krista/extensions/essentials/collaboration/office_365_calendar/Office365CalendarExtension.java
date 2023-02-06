package app.krista.extensions.essentials.collaboration.office_365_calendar;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Validators;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

@Field.Text(value = Office365CalendarAttributes.CLIENT_ID)
@Field.Text(value = Office365CalendarAttributes.CLIENT_SECRET, isSecured = true)
@Field.Text(value = Office365CalendarAttributes.TENANT_ID, isSecured = true)
@Field.Text(value = Office365CalendarAttributes.EMAIL)
@Field.Text(value = Office365CalendarAttributes.TIMEZONE, required = false)
@Extension(version = "2.0.2-rc2")
@StaticResource(path = "docs", file = "docs")
public final class Office365CalendarExtension {

    private final Invoker invoker;
    private final Office365CalendarAttributes office365CalendarAttributes;
    private final GraphServiceClientProviderFactory clientProviderFactory;
    private final Office365CalendarRequestAuthenticator requestAuthenticator;

    @Inject
    public Office365CalendarExtension(Invoker invoker, Office365CalendarAttributes office365CalendarAttributes, GraphServiceClientProviderFactory clientProviderFactory, Office365CalendarAttributeStore office365CalendarAttributeStore) {
        this(invoker, office365CalendarAttributes, clientProviderFactory,
                new Office365CalendarRequestAuthenticator(office365CalendarAttributeStore, office365CalendarAttributes));
    }

    public Office365CalendarExtension(Invoker invoker, Office365CalendarAttributes office365CalendarAttributes, GraphServiceClientProviderFactory clientProviderFactory, Office365CalendarRequestAuthenticator requestAuthenticator) {
        this.invoker = invoker;
        this.office365CalendarAttributes = office365CalendarAttributes;
        this.clientProviderFactory = clientProviderFactory;
        this.requestAuthenticator = requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateConnection(Map<String, Object> connectionAttributes) {
        Office365CalendarAttributes localOffice365CalendarAttributes = Office365CalendarAttributes.create(invoker, connectionAttributes);
        validateAndTestConnection(localOffice365CalendarAttributes);
    }

    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        validateAndTestConnection(office365CalendarAttributes);
    }

    /**
     * @param office365CalendarAttributes This function creates connection with GraphServiceClient to validate the given credentials.
     */
    private void validateAndTestConnection(Office365CalendarAttributes office365CalendarAttributes) {
        try {
            Validators.validateTimeZone(office365CalendarAttributes.getTimeZone());
            if (office365CalendarAttributes.getEmail() != null && !office365CalendarAttributes.getEmail().isBlank()) {
                clientProviderFactory.create(office365CalendarAttributes)
                        .getGraphServiceClientForAdmin()
                        .me().calendar().buildRequest().get();
            }
        } catch (IOException cause) {
            throw new IllegalArgumentException("Exception occurred - " + cause.getMessage(), cause.getCause());
        }
    }

    @InvokerRequest(InvokerRequest.Type.INVOKER_UPDATED)
    public void attributesUpdated(Map<String, Object> oldAttributes, Map<String, Object> newAttributes) {
        office365CalendarAttributes.update(newAttributes);
    }
}
