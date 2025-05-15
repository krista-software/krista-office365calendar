package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarExtension;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarRequestAuthenticator;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import com.microsoft.graph.requests.CalendarRequestBuilder;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserRequestBuilder;
import okhttp3.Request;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.*;

public class Office365CalendarExtensionTest {

    private Invoker invoker;
    private Office365CalendarAttributes attributes;
    private GraphServiceClientProviderFactory factory;
    private GraphServiceClientProvider mockClientProvider;
    private GraphServiceClient<?> mockGraphClient;

    private Office365CalendarExtension extension;

    @Before
    public void setup() throws IOException {
        invoker = mock(Invoker.class);
        attributes = mock(Office365CalendarAttributes.class);
        factory = mock(GraphServiceClientProviderFactory.class);
        Office365CalendarAttributeStore attributeStore = mock(Office365CalendarAttributeStore.class);
        Office365CalendarRequestAuthenticator authenticator = new Office365CalendarRequestAuthenticator(attributeStore, attributes);

        mockClientProvider = mock(GraphServiceClientProvider.class);
        mockGraphClient = mock(GraphServiceClient.class);

        when(factory.create(any())).thenReturn(mockClientProvider);
        when(mockClientProvider.getGraphServiceClientForAdmin()).thenReturn((GraphServiceClient<Request>) mockGraphClient);

        extension = new Office365CalendarExtension(invoker, attributes, factory, authenticator);
    }

    @Test
    public void testValidateConnection_withValidAttributes_shouldSucceed() throws IOException {
        Map<String, Object> mockConnectionAttributes = Map.of(
                "clientId", "test-client",
                "clientSecret", "secret",
                "tenantId", "tenant",
                "email", "user@example.com",
                "timeZone", "Asia/Kolkata"
        );

        try (MockedStatic<Office365CalendarAttributes> mocked = Mockito.mockStatic(Office365CalendarAttributes.class)) {
            Office365CalendarAttributes mockLocalAttr = mock(Office365CalendarAttributes.class);

            when(mockLocalAttr.getEmail()).thenReturn("user@example.com");
            when(mockLocalAttr.getTimeZone()).thenReturn("Asia/Kolkata");

            GraphServiceClient<?> mockGraphClient = mock(GraphServiceClient.class);
            UserRequestBuilder userBuilder = mock(UserRequestBuilder.class);
            CalendarRequestBuilder calendarBuilder = mock(CalendarRequestBuilder.class);
            var calendarRequest = mock(com.microsoft.graph.requests.CalendarRequest.class);

            mocked.when(() -> Office365CalendarAttributes.create(invoker, mockConnectionAttributes))
                    .thenReturn(mockLocalAttr);

            when(factory.create(mockLocalAttr)).thenReturn(mockClientProvider);
            when(mockClientProvider.getGraphServiceClientForAdmin()).thenReturn((GraphServiceClient<Request>) mockGraphClient);
            when(mockGraphClient.me()).thenReturn(userBuilder);
            when(userBuilder.calendar()).thenReturn(calendarBuilder);
            when(calendarBuilder.buildRequest()).thenReturn(calendarRequest);

            extension.validateConnection(mockConnectionAttributes);

            verify(factory).create(mockLocalAttr);
            verify(mockClientProvider).getGraphServiceClientForAdmin();
        }
    }

    @Test
    public void testTestConnection_withValidEmail_shouldCallGraphClient() throws IOException {
        when(attributes.getEmail()).thenReturn("test@example.com");
        when(attributes.getTimeZone()).thenReturn("Asia/Kolkata");

        UserRequestBuilder userBuilder = mock(UserRequestBuilder.class);
        CalendarRequestBuilder calendarBuilder = mock(CalendarRequestBuilder.class);
        var calendarRequest = mock(com.microsoft.graph.requests.CalendarRequest.class);

        when(mockGraphClient.me()).thenReturn(userBuilder);
        when(userBuilder.calendar()).thenReturn(calendarBuilder);
        when(calendarBuilder.buildRequest()).thenReturn(calendarRequest);
        when(calendarRequest.get()).thenReturn(null);

        extension.testConnection();

        verify(mockClientProvider).getGraphServiceClientForAdmin();
        verify(calendarRequest).get();
    }


    @Test
    public void testAttributesUpdated_shouldCallUpdate() {
        Map<String, Object> oldAttributes = Map.of("email", "old@example.com");
        Map<String, Object> newAttributes = Map.of("email", "new@example.com");

        extension.attributesUpdated(oldAttributes, newAttributes);

        verify(attributes).update(newAttributes);
    }

}
