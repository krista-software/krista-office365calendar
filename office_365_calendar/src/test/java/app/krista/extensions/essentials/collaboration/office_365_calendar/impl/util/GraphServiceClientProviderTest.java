package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GraphServiceClientProviderTest {

    @Mock
    private RefreshTokenStore refreshTokenStore;
    @Mock
    private Office365CalendarAttributes attributes;
    @Mock
    private RequestContext requestContext;
    @Mock
    private AuthorizationContext authorizationContext;
    @Mock
    private GraphServiceClient<Request> mockClient;
    @Mock
    private Account mockAccount;

    private GraphServiceClientProvider clientProvider;

    private final String testUserId = "test-user";
    private final String email = "test@example.com";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        String clientId = "client-id";
        when(attributes.getClientId()).thenReturn(clientId);
        String clientSecret = "client-secret";
        when(attributes.getClientSecret()).thenReturn(clientSecret);
        String tenantId = "tenant-id";
        when(attributes.getTenantId()).thenReturn(tenantId);
        when(attributes.getEmail()).thenReturn(email);

        when(authorizationContext.getAuthorizedAccount()).thenReturn(mockAccount);
        when(mockAccount.getAccountId()).thenReturn(testUserId);

        clientProvider = spy(new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        ));
    }

    @Test
    public void testGetUserId_whenInvokeAsUserTrue_returnsAccountId() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        String userId = clientProvider.getUserId(false);
        assertEquals(testUserId, userId);
    }

    @Test
    public void testGetUserId_whenInvokeAsUserFalse_returnsEmail() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        String userId = clientProvider.getUserId(true);
        assertEquals(email, userId);
    }

    @Test
    public void testGetGraphServiceClient_whenRefreshTokenExists_returnsClient() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        String testRefreshToken = "test-refresh-token";
        when(refreshTokenStore.get(testUserId)).thenReturn(testRefreshToken);

        doReturn(mockClient).when(clientProvider).getGraphServiceClient(eq(testUserId), eq(testRefreshToken));
        GraphServiceClient<Request> client = clientProvider.getGraphServiceClient();
        assertEquals(mockClient, client);
    }

    @Test
    public void testGetGraphServiceClient_whenRefreshTokenMissing_throwsMustAuthorizeException() {
        when(requestContext.invokeAsUser()).thenReturn(true);

        when(mockAccount.getAccountId()).thenReturn(testUserId);
        when(authorizationContext.getAuthorizedAccount()).thenReturn(mockAccount);

        when(refreshTokenStore.get(testUserId)).thenReturn(null);

        MustAuthorizeException exception = assertThrows(
                MustAuthorizeException.class,
                () -> clientProvider.getGraphServiceClient()
        );
        assertTrue(exception.getMessage().contains("Authorize yourself"));
    }


    @Test
    public void testUpdateRefreshToken_storesNewToken() {
        JsonObject tokenCache = new JsonObject();
        JsonObject refreshToken = new JsonObject();
        JsonObject tokenEntry = new JsonObject();
        tokenEntry.addProperty("secret", "new-token");
        refreshToken.add("some-key", tokenEntry);
        tokenCache.add("RefreshToken", refreshToken);

        clientProvider.updateRefreshToken("user-1", tokenCache);

        verify(refreshTokenStore).put("user-1", "new-token");
    }


    @Test
    public void testGetGraphServiceClient_userContext_tokenMissing_throwsException() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(authorizationContext.getAuthorizedAccount()).thenReturn(mockAccount);
        when(mockAccount.getAccountId()).thenReturn(testUserId);
        when(refreshTokenStore.get(testUserId)).thenReturn(null);

        assertThrows(MustAuthorizeException.class, () -> clientProvider.getGraphServiceClient());
    }

}
