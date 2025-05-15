package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class Office365CalendarAttributesTest {

    private static final String ROUTING_URL = "https://my.krista.app";
    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String TENANT_ID = "tenant-id";
    private static final String EMAIL = "user@domain.com";
    private static final String TIMEZONE = "Asia/Kolkata";

    private Map<String, Object> mockAttributes;

    @Before
    public void setup() {
        mockAttributes = new HashMap<>();
        mockAttributes.put(Office365CalendarAttributes.CLIENT_ID, CLIENT_ID);
        mockAttributes.put(Office365CalendarAttributes.CLIENT_SECRET, CLIENT_SECRET);
        mockAttributes.put(Office365CalendarAttributes.TENANT_ID, TENANT_ID);
        mockAttributes.put(Office365CalendarAttributes.EMAIL, EMAIL);
        mockAttributes.put(Office365CalendarAttributes.TIMEZONE, TIMEZONE);
    }

    @Test
    public void constructor_shouldInitializeFieldsCorrectly() {
        Office365CalendarAttributes attrs = new Office365CalendarAttributes(ROUTING_URL, mockAttributes);

        assertEquals(CLIENT_ID, attrs.getClientId());
        assertEquals(CLIENT_SECRET, attrs.getClientSecret());
        assertEquals(TENANT_ID, attrs.getTenantId());
        assertEquals(EMAIL, attrs.getEmail());
        assertEquals(TIMEZONE, attrs.getTimeZone());
        assertEquals(ROUTING_URL + "/rest/calendar/callback", attrs.getCallbackUrl());
    }

    @Test
    public void update_shouldUpdateFieldsCorrectly() {
        Office365CalendarAttributes attrs = new Office365CalendarAttributes(ROUTING_URL, new HashMap<>());

        attrs.update(mockAttributes);

        assertEquals(CLIENT_ID, attrs.getClientId());
        assertEquals(CLIENT_SECRET, attrs.getClientSecret());
        assertEquals(TENANT_ID, attrs.getTenantId());
        assertEquals(EMAIL, attrs.getEmail());
        assertEquals(TIMEZONE, attrs.getTimeZone());
    }

    @Test
    public void create_shouldConstructFromInvokerCorrectly() {
        Invoker invoker = mock(Invoker.class);
        RoutingInfo routingInfo = mock(RoutingInfo.class);

        when(invoker.getRoutingInfo()).thenReturn(routingInfo);
        when(routingInfo.getRoutingURL(eq(HttpProtocol.PROTOCOL_NAME), eq(RoutingInfo.Type.APPLIANCE)))
                .thenReturn(ROUTING_URL);
        when(invoker.getAttributes()).thenReturn(mockAttributes);

        Office365CalendarAttributes attrs = Office365CalendarAttributes.create(invoker, mockAttributes);

        assertEquals(CLIENT_ID, attrs.getClientId());
        assertEquals(EMAIL, attrs.getEmail());
        assertEquals(ROUTING_URL + "/rest/calendar/callback", attrs.getCallbackUrl());
    }

    @Test
    public void getOAuth20Service_shouldReturnServiceInstance() {
        Office365CalendarAttributes attrs = new Office365CalendarAttributes(ROUTING_URL, mockAttributes);

        OAuth20Service service1 = attrs.getOAuth20Service();
        OAuth20Service service2 = attrs.getOAuth20Service();

        assertNotNull(service1);
        assertSame(service1, service2);
    }
}
