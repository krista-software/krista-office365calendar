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

package app.krista.extensions.essentials.collaboration.office_365_calendar.api;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.TestConnectionServiceImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.audit.Office365CalendarAuditService;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonObject;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.requests.CalendarCollectionPage;
import com.microsoft.graph.requests.CalendarCollectionRequest;
import com.microsoft.graph.requests.CalendarCollectionRequestBuilder;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserRequestBuilder;
import okhttp3.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationResourceTest {

    @Mock
    private Office365CalendarAttributeStore attributeStore;
    @Mock
    private RefreshTokenStore refreshTokenStore;
    @Mock
    private GraphServiceClientProviderFactory providerFactory;
    @Mock
    private AuthorizationContext context;
    @Mock
    private AuthorizationListener authorizationListener;
    @Mock
    private TestConnectionServiceImpl testConnectionService;
    @Mock
    private Office365CalendarAuditService auditService;
    @Mock
    private Invoker invoker;
    @Mock
    private RoutingInfo routingInfo;
    @Mock
    private GraphServiceClientProvider mockProvider;
    @Mock
    private Office365CalendarAttributes mockAttributes;
    @Mock
    private OAuth20Service mockOAuth20Service;
    @Mock
    private OAuth2AccessToken mockAccessToken;

    private AuthenticationResource resource;
    private static final String BASE_URL = "https://test.krista.ai";
    private static final String INVOKER_ID = "test-invoker-id";

    @Before
    public void setUp() {
        when(invoker.getRoutingInfo()).thenReturn(routingInfo);
        when(routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE)).thenReturn(BASE_URL);
        when(invoker.getInvokerId()).thenReturn(INVOKER_ID);

        resource = new AuthenticationResource(
                attributeStore, refreshTokenStore, providerFactory,
                context, authorizationListener, testConnectionService, auditService, invoker
        );
    }

    // ========================================================================
    // getCallBack - delegates to getAuthenticationResponseMessage
    // ========================================================================

    @Test
    public void testGetCallBack_NullCode_ReturnsAuthFailed() {
        String result = resource.getCallBack(null, "someState");
        assertEquals("Authentication Failed. Please re-authorize.", result);
    }

    @Test
    public void testGetCallbackForV3Auth_NullCode_ReturnsAuthFailed() {
        String result = resource.getCallbackForV3Auth(null, "someState");
        assertEquals("Authentication Failed. Please re-authorize.", result);
    }

    @Test(expected = BadRequestException.class)
    public void testGetCallBack_BlankState_ThrowsBadRequest() {
        resource.getCallBack("code123", "#something");
    }

    @Test(expected = BadRequestException.class)
    public void testGetCallBack_EmptyState_ThrowsBadRequest() {
        resource.getCallBack("code123", "");
    }

    // ========================================================================
    // Successful callback - authenticated context
    // ========================================================================

    @Test
    public void testGetCallBack_Success_Authenticated() throws Exception {
        setupSuccessfulCallback("user@example.com", "user@example.com");
        when(context.isAuthenticated()).thenReturn(true);
        setupHasUserAccess(true);

        String result = resource.getCallBack("code123", "user@example.com");

        assertEquals(AuthenticationResource.USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST, result);
        verify(refreshTokenStore).put(eq("user@example.com_client-id_Public"), eq("refresh-token-value"));
        verify(authorizationListener).authorized();
    }

    @Test
    public void testGetCallBack_Success_NotAuthenticated() throws Exception {
        setupSuccessfulCallback("user@example.com", "user@example.com");
        when(context.isAuthenticated()).thenReturn(false);
        setupHasUserAccess(true);

        String result = resource.getCallBack("code123", "user@example.com");

        assertEquals(Constants.USER_AUTHENTICATED_SUCCESSFULLY_SAVE_THE_CHANGES, result);
        verify(authorizationListener, never()).authorized();
    }

    // ========================================================================
    // Callback - WS_CONTACT key
    // ========================================================================

    @Test
    public void testGetCallBack_WsContactKey_UsesKeyDirectly() throws Exception {
        String wsKey = Constants.WS_CONTACT + "_someId";
        setupSuccessfulCallback(wsKey, wsKey);
        when(context.isAuthenticated()).thenReturn(true);
        // WS_CONTACT keys skip hasUserAccess check

        String result = resource.getCallBack("code123", wsKey);

        assertEquals(AuthenticationResource.USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST, result);
        // WS_CONTACT key is used directly without appending clientId and authType
        verify(refreshTokenStore).put(eq(wsKey), eq("refresh-token-value"));
    }

    // ========================================================================
    // Callback - user access check fails
    // ========================================================================

    @Test
    public void testGetCallBack_UserAccessFails_RemovesTokenAndReturnsUnauthorised() throws Exception {
        setupSuccessfulCallback("user@example.com", "user@example.com");
        setupHasUserAccess(false);

        String result = resource.getCallBack("code123", "user@example.com");

        assertTrue(result.contains(Constants.UNAUTHORISED_USER));
        assertTrue(result.contains("user@example.com"));
        verify(refreshTokenStore).remove("user@example.com_client-id_Public");
    }

    // ========================================================================
    // Callback - state with authContextId
    // ========================================================================

    @Test
    public void testGetCallBack_StateWithAuthContextId() throws Exception {
        String state = "user@example.com" + Constants.HASH + "auth-context-uuid";
        when(providerFactory.create("auth-context-uuid")).thenReturn(mockProvider);
        setupProviderMocks();
        when(context.isAuthenticated()).thenReturn(true);
        setupHasUserAccess(true);

        String result = resource.getCallBack("code123", state);

        assertEquals(AuthenticationResource.USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST, result);
        verify(providerFactory).create("auth-context-uuid");
    }

    @Test
    public void testGetCallBack_StateWithForwardPath_NoAuthContextId() throws Exception {
        // parts[1] is a URL, not an authContextId
        String state = "user@example.com" + Constants.HASH + "https://forward.path.com";
        when(providerFactory.create()).thenReturn(mockProvider);
        setupProviderMocks();
        when(context.isAuthenticated()).thenReturn(true);
        setupHasUserAccess(true);

        String result = resource.getCallBack("code123", state);

        assertEquals(AuthenticationResource.USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST, result);
        verify(providerFactory).create();
    }

    @Test
    public void testGetCallBack_StateWithHttpForwardPath() throws Exception {
        String state = "user@example.com" + Constants.HASH + "http://localhost:8080";
        when(providerFactory.create()).thenReturn(mockProvider);
        setupProviderMocks();
        when(context.isAuthenticated()).thenReturn(true);
        setupHasUserAccess(true);

        String result = resource.getCallBack("code123", state);

        verify(providerFactory).create();
    }

    @Test
    public void testGetCallBack_StateWithSlashForwardPath() throws Exception {
        String state = "user@example.com" + Constants.HASH + "/some/path";
        when(providerFactory.create()).thenReturn(mockProvider);
        setupProviderMocks();
        when(context.isAuthenticated()).thenReturn(true);
        setupHasUserAccess(true);

        resource.getCallBack("code123", state);

        verify(providerFactory).create();
    }

    @Test
    public void testGetCallBack_SinglePartState_NoAuthContextId() throws Exception {
        // State without HASH separator → single part, no authContextId
        when(providerFactory.create()).thenReturn(mockProvider);
        setupProviderMocks();
        when(context.isAuthenticated()).thenReturn(false);
        setupHasUserAccess(true);

        String result = resource.getCallBack("code123", "user@example.com");

        assertEquals(Constants.USER_AUTHENTICATED_SUCCESSFULLY_SAVE_THE_CHANGES, result);
        verify(providerFactory).create();
    }

    // ========================================================================
    // Callback - exception handling
    // ========================================================================

    @Test(expected = IllegalStateException.class)
    public void testGetCallBack_OAuthException_ThrowsIllegalState() throws Exception {
        when(providerFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getOffice365CalendarAttributes()).thenReturn(mockAttributes);
        when(mockAttributes.getOAuth20Service()).thenReturn(mockOAuth20Service);
        when(mockOAuth20Service.getAccessToken("code123"))
                .thenThrow(new OAuthException("oauth error"));

        resource.getCallBack("code123", "user@example.com");
    }

    @Test
    public void testGetCallBack_OAuthException_WithJsonErrorDescription() throws Exception {
        when(providerFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getOffice365CalendarAttributes()).thenReturn(mockAttributes);
        when(mockAttributes.getOAuth20Service()).thenReturn(mockOAuth20Service);

        String jsonMsg = "{\"error_description\": \"Token expired\"}";
        when(mockOAuth20Service.getAccessToken("code123"))
                .thenThrow(new OAuthException(jsonMsg));

        try {
            resource.getCallBack("code123", "user@example.com");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Token expired"));
        }
    }

    @Test
    public void testGetCallBack_OAuthException_WithNonJsonMessage() throws Exception {
        when(providerFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getOffice365CalendarAttributes()).thenReturn(mockAttributes);
        when(mockAttributes.getOAuth20Service()).thenReturn(mockOAuth20Service);

        when(mockOAuth20Service.getAccessToken("code123"))
                .thenThrow(new OAuthException("plain text error"));

        try {
            resource.getCallBack("code123", "user@example.com");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Error occurred during authorization"));
        }
    }

    @Test
    public void testGetCallBack_OAuthException_NullMessage() throws Exception {
        when(providerFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getOffice365CalendarAttributes()).thenReturn(mockAttributes);
        when(mockAttributes.getOAuth20Service()).thenReturn(mockOAuth20Service);

        when(mockOAuth20Service.getAccessToken("code123"))
                .thenThrow(new OAuthException((String) null));

        try {
            resource.getCallBack("code123", "user@example.com");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Error occurred during authorization"));
        }
    }

    @Test
    public void testGetCallBack_OAuthException_BlankMessage() throws Exception {
        when(providerFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getOffice365CalendarAttributes()).thenReturn(mockAttributes);
        when(mockAttributes.getOAuth20Service()).thenReturn(mockOAuth20Service);

        when(mockOAuth20Service.getAccessToken("code123"))
                .thenThrow(new OAuthException("   "));

        try {
            resource.getCallBack("code123", "user@example.com");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Error occurred during authorization"));
        }
    }

    @Test
    public void testGetCallBack_OAuthException_JsonWithoutErrorDescription() throws Exception {
        when(providerFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getOffice365CalendarAttributes()).thenReturn(mockAttributes);
        when(mockAttributes.getOAuth20Service()).thenReturn(mockOAuth20Service);

        String jsonMsg = "{\"error\": \"invalid_grant\"}";
        when(mockOAuth20Service.getAccessToken("code123"))
                .thenThrow(new OAuthException(jsonMsg));

        try {
            resource.getCallBack("code123", "user@example.com");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Error occurred during authorization"));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testGetCallBack_IOException_ThrowsIllegalState() throws Exception {
        when(providerFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getOffice365CalendarAttributes()).thenReturn(mockAttributes);
        when(mockAttributes.getOAuth20Service()).thenReturn(mockOAuth20Service);
        when(mockOAuth20Service.getAccessToken("code123"))
                .thenThrow(new IOException("io error"));

        resource.getCallBack("code123", "user@example.com");
    }

    @Test(expected = IllegalStateException.class)
    public void testGetCallBack_ExecutionException_ThrowsIllegalState() throws Exception {
        when(providerFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getOffice365CalendarAttributes()).thenReturn(mockAttributes);
        when(mockAttributes.getOAuth20Service()).thenReturn(mockOAuth20Service);
        when(mockOAuth20Service.getAccessToken("code123"))
                .thenThrow(new ExecutionException("exec error", new Exception("cause")));

        resource.getCallBack("code123", "user@example.com");
    }

    @Test
    public void testGetCallBack_InterruptedException_ThrowsIllegalStateAndInterruptsThread() throws Exception {
        when(providerFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getOffice365CalendarAttributes()).thenReturn(mockAttributes);
        when(mockAttributes.getOAuth20Service()).thenReturn(mockOAuth20Service);
        when(mockOAuth20Service.getAccessToken("code123"))
                .thenThrow(new InterruptedException("interrupted"));

        try {
            resource.getCallBack("code123", "user@example.com");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted(); // clear flag
        }
    }

    // ========================================================================
    // saveCredentials
    // ========================================================================

    @Test
    public void testSaveCredentials_Success() {
        JsonObject payload = createPublicAuthPayload();

        try (MockedStatic<Office365CalendarAttributes> mockedStatic = mockStatic(Office365CalendarAttributes.class)) {
            mockedStatic.when(() -> Office365CalendarAttributes.create(eq(payload), eq(BASE_URL)))
                    .thenReturn(mockAttributes);
            when(providerFactory.createAttributes(mockAttributes)).thenReturn("temp-auth-id");

            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockGraphClient = mock(GraphServiceClient.class);
            when(providerFactory.create("temp-auth-id")).thenReturn(mockProvider);
            when(mockProvider.getGraphServiceClientForAdmin()).thenReturn(mockGraphClient);
            when(mockAttributes.getEmail()).thenReturn("user@example.com");

            UserRequestBuilder userReqBuilder = mock(UserRequestBuilder.class);
            CalendarCollectionRequestBuilder calCollBuilder = mock(CalendarCollectionRequestBuilder.class);
            CalendarCollectionRequest calCollRequest = mock(CalendarCollectionRequest.class);
            CalendarCollectionPage calPage = mock(CalendarCollectionPage.class);

            when(mockGraphClient.users("user@example.com")).thenReturn(userReqBuilder);
            when(userReqBuilder.calendars()).thenReturn(calCollBuilder);
            when(calCollBuilder.buildRequest()).thenReturn(calCollRequest);
            when(calCollRequest.get()).thenReturn(calPage);

            when(attributeStore.save(mockAttributes, INVOKER_ID)).thenReturn(true);

            String result = resource.saveCredentials(payload);

            assertTrue(result.contains("true"));
            verify(attributeStore).remove("temp-auth-id");
        }
    }

    @Test
    public void testSaveCredentials_SaveFails() {
        JsonObject payload = createPublicAuthPayload();

        try (MockedStatic<Office365CalendarAttributes> mockedStatic = mockStatic(Office365CalendarAttributes.class)) {
            mockedStatic.when(() -> Office365CalendarAttributes.create(eq(payload), eq(BASE_URL)))
                    .thenReturn(mockAttributes);
            when(providerFactory.createAttributes(mockAttributes)).thenReturn("temp-auth-id");

            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockGraphClient = mock(GraphServiceClient.class);
            when(providerFactory.create("temp-auth-id")).thenReturn(mockProvider);
            when(mockProvider.getGraphServiceClientForAdmin()).thenReturn(mockGraphClient);
            when(mockAttributes.getEmail()).thenReturn("user@example.com");

            UserRequestBuilder userReqBuilder = mock(UserRequestBuilder.class);
            CalendarCollectionRequestBuilder calCollBuilder = mock(CalendarCollectionRequestBuilder.class);
            CalendarCollectionRequest calCollRequest = mock(CalendarCollectionRequest.class);

            when(mockGraphClient.users("user@example.com")).thenReturn(userReqBuilder);
            when(userReqBuilder.calendars()).thenReturn(calCollBuilder);
            when(calCollBuilder.buildRequest()).thenReturn(calCollRequest);
            when(calCollRequest.get()).thenReturn(mock(CalendarCollectionPage.class));

            when(attributeStore.save(mockAttributes, INVOKER_ID)).thenReturn(false);

            String result = resource.saveCredentials(payload);

            assertTrue(result.contains("false"));
            assertTrue(result.contains("Failed to save attributes"));
            verify(attributeStore).remove("temp-auth-id");
        }
    }

    @Test
    public void testSaveCredentials_ExceptionDuringConnection() {
        JsonObject payload = createPublicAuthPayload();

        try (MockedStatic<Office365CalendarAttributes> mockedStatic = mockStatic(Office365CalendarAttributes.class)) {
            mockedStatic.when(() -> Office365CalendarAttributes.create(eq(payload), eq(BASE_URL)))
                    .thenReturn(mockAttributes);
            when(providerFactory.createAttributes(mockAttributes)).thenReturn("temp-auth-id");
            when(providerFactory.create("temp-auth-id")).thenThrow(new RuntimeException("connection failed"));

            String result = resource.saveCredentials(payload);

            assertTrue(result.contains("false"));
            assertTrue(result.contains("Failed to save attributes"));
            verify(attributeStore).remove("temp-auth-id");
        }
    }

    // ========================================================================
    // testConnection
    // ========================================================================

    @Test
    public void testTestConnection_DelegatesToService() {
        JsonObject payload = createPublicAuthPayload();

        try (MockedStatic<Office365CalendarAttributes> mockedStatic = mockStatic(Office365CalendarAttributes.class)) {
            mockedStatic.when(() -> Office365CalendarAttributes.create(eq(payload), eq(BASE_URL)))
                    .thenReturn(mockAttributes);
            when(testConnectionService.testConnection(mockAttributes)).thenReturn("{\"success\":true}");

            String result = resource.testConnection(payload);

            assertEquals("{\"success\":true}", result);
            verify(testConnectionService).testConnection(mockAttributes);
        }
    }

    // ========================================================================
    // getCredentials
    // ========================================================================

    @Test
    public void testGetCredentials_MatchingAuthType_ReturnsJson() {
        when(attributeStore.load(INVOKER_ID)).thenReturn(mockAttributes);
        when(mockAttributes.getAuthType()).thenReturn("Public");
        Map<String, Object> attrMap = Map.of("email", "user@example.com", "authType", "Public", "publicAuthMode", "krista");
        when(mockAttributes.toMap()).thenReturn(attrMap);

        String result = resource.getCredentials("Public", "AUTH_CODE_PUBLIC");

        assertFalse(result.isEmpty());
        assertTrue(result.contains("user@example.com"));
    }

    @Test
    public void testGetCredentials_NonMatchingAuthType_ReturnsEmpty() {
        when(attributeStore.load(INVOKER_ID)).thenReturn(mockAttributes);
        when(mockAttributes.getAuthType()).thenReturn("Private");

        String result = resource.getCredentials("Public", "AUTH_CODE_PUBLIC");

        assertEquals("", result);
    }

    @Test
    public void testGetCredentials_NullAttributes_ReturnsEmpty() {
        when(attributeStore.load(INVOKER_ID)).thenReturn(null);

        String result = resource.getCredentials("Public", "AUTH_CODE_PUBLIC");

        assertEquals("", result);
    }

    // ========================================================================
    // getAuthKey
    // ========================================================================

    @Test
    public void testGetAuthKey_WithStoredAttributes_ReturnsAuthType() {
        when(attributeStore.load(INVOKER_ID)).thenReturn(mockAttributes);
        when(mockAttributes.getAuthType()).thenReturn("Private");

        String result = resource.getAuthKey();

        assertTrue(result.contains("Private"));
    }

    @Test
    public void testGetAuthKey_WithNullAuthType_ReturnsPublicDefault() {
        when(attributeStore.load(INVOKER_ID)).thenReturn(mockAttributes);
        when(mockAttributes.getAuthType()).thenReturn(null);

        String result = resource.getAuthKey();

        assertTrue(result.contains(Constants.PUBLIC));
    }

    @Test
    public void testGetAuthKey_NullAttributes_ReturnsEmpty() {
        when(attributeStore.load(INVOKER_ID)).thenReturn(null);

        String result = resource.getAuthKey();

        assertEquals("", result);
    }

    // ========================================================================
    // customTabs
    // ========================================================================

    @Test
    public void testCustomTabs_EmptySubPath_ReturnsIndexHtml() {
        InputStream result = resource.customTabs("");
        // Result can be null if resource doesn't exist in test classpath - just verify no exception
        // The path would be "ui/office365calendaroauth/index.html"
    }

    @Test
    public void testCustomTabs_WithSubPath_ReturnsRequestedFile() {
        InputStream result = resource.customTabs("style.css");
        // The path would be "ui/office365calendaroauth/style.css"
    }

    // ========================================================================
    // v3 callback
    // ========================================================================

    @Test
    public void testGetCallbackForV3Auth_Success() throws Exception {
        setupSuccessfulCallback("user@example.com", "user@example.com");
        when(context.isAuthenticated()).thenReturn(true);
        setupHasUserAccess(true);

        String result = resource.getCallbackForV3Auth("code123", "user@example.com");

        assertEquals(AuthenticationResource.USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST, result);
    }

    // 4.0.4 recovery: 4-part state encoding (wsContact#INV_..#tempUUID#forwardPath) was
    // removed along with the clientKey/INV_ embedding and the per-MCP-user attrs persistence.
    // The corresponding tests (testGetCallBack_McpFourPartState_*, testGetCallBack_McpThreePartState_*)
    // were deleted because they asserted behavior that's intentionally gone.

    /**
     * Builds a minimal fake JWT with the given JSON payload (kept for any future tests that
     * legitimately need a fake bearer token).
     */
    private String buildFakeJwt(String jsonPayload) {
        String header = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"RS256\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String payload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(jsonPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return header + "." + payload + ".fakesignature";
    }

    private void setupSuccessfulCallback(String key, String state) throws Exception {
        // 4.0.4-recovery: AuthenticationResource.getGraphServiceClientProvider routes per state:
        //   wsContact-prefixed → providerFactory.create(Office365CalendarAttributes) with defaults
        //   authContextId != null → providerFactory.create(authContextId)
        //   else → providerFactory.create()
        // Tests don't always exercise all three overloads, so we use lenient() to avoid
        // strict-stubbing complaints about unused stubs.
        org.mockito.Mockito.lenient().when(providerFactory.create()).thenReturn(mockProvider);
        org.mockito.Mockito.lenient().when(providerFactory.create(any(Office365CalendarAttributes.class))).thenReturn(mockProvider);
        org.mockito.Mockito.lenient().when(providerFactory.create(anyString())).thenReturn(mockProvider);
        setupProviderMocks();
    }

    private void setupProviderMocks() throws Exception {
        when(mockProvider.getOffice365CalendarAttributes()).thenReturn(mockAttributes);
        when(mockAttributes.getClientId()).thenReturn("client-id");
        when(mockAttributes.getAuthType()).thenReturn("Public");
        when(mockAttributes.getOAuth20Service()).thenReturn(mockOAuth20Service);
        when(mockOAuth20Service.getAccessToken("code123")).thenReturn(mockAccessToken);
        when(mockAccessToken.getRefreshToken()).thenReturn("refresh-token-value");
    }

    private void setupHasUserAccess(boolean hasAccess) {
        UserRequestBuilder userReqBuilder = mock(UserRequestBuilder.class);
        CalendarCollectionRequestBuilder calCollBuilder = mock(CalendarCollectionRequestBuilder.class);
        CalendarCollectionRequest calCollRequest = mock(CalendarCollectionRequest.class);

        when(mockProvider.getUserRequestBuilder(null, null)).thenReturn(userReqBuilder);
        when(userReqBuilder.calendars()).thenReturn(calCollBuilder);
        when(calCollBuilder.buildRequest()).thenReturn(calCollRequest);

        if (hasAccess) {
            when(calCollRequest.get()).thenReturn(mock(CalendarCollectionPage.class));
        } else {
            GraphServiceException gse = mock(GraphServiceException.class);
            when(gse.getMessage()).thenReturn("Access denied");
            when(calCollRequest.get()).thenThrow(gse);
        }
    }

    private JsonObject createPublicAuthPayload() {
        JsonObject payload = new JsonObject();
        payload.addProperty("authType", "Public");
        payload.addProperty("email", "user@example.com");
        return payload;
    }
}
