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

package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.ExponentialBackoffRetryExecutor;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.RetryConfiguration;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.RetryableOperation;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;
import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserRequestBuilder;
import okhttp3.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GraphServiceClientProviderTest {

    @Mock private RefreshTokenStore refreshTokenStore;
    @Mock private Office365CalendarAttributes attributes;
    @Mock private RequestContext requestContext;
    @Mock private AuthorizationContext authorizationContext;
    @Mock private Account account;

    private GraphServiceClientProvider provider;
    private GraphServiceClientProvider providerWithAuthCtx;

    private static final String EMAIL = "user@example.com";
    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String TENANT_ID = "tenant-123";
    private static final String AUTH_TYPE_PUBLIC = "Public";
    private static final String AUTH_TYPE_PRIVATE = "Private";
    private static final String REFRESH_TOKEN_KEY = EMAIL + "_" + CLIENT_ID + "_" + AUTH_TYPE_PUBLIC;
    private static final String REFRESH_TOKEN = "test-refresh-token";
    private static final String ACCOUNT_ID = "account-123";

    @Before
    public void setUp() {
        provider = new GraphServiceClientProvider(refreshTokenStore, attributes, requestContext, authorizationContext);
        providerWithAuthCtx = new GraphServiceClientProvider(refreshTokenStore, attributes, requestContext, authorizationContext, "auth-ctx-123");
    }

    private void setupAttributes() {
        when(attributes.getEmail()).thenReturn(EMAIL);
        when(attributes.getClientId()).thenReturn(CLIENT_ID);
        when(attributes.getAuthType()).thenReturn(AUTH_TYPE_PUBLIC);
    }

    private void setupAttributesAndRefreshToken() {
        setupAttributes();
        when(refreshTokenStore.get(REFRESH_TOKEN_KEY)).thenReturn(REFRESH_TOKEN);
    }

    private void setupAccount() {
        when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(ACCOUNT_ID);
    }

    @SuppressWarnings("unchecked")
    private GraphServiceClient<Request> setupMockedRetrySuccess(MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry) {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                any(), anyString(), any(RetryConfiguration.class)
        )).thenReturn(mockClient);
        return mockClient;
    }

    // ========================================================================
    // Constructors
    // ========================================================================

    @Test
    public void testConstructor_FourParams() {
        GraphServiceClientProvider p = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext);
        assertNotNull(p);
        assertSame(attributes, p.getOffice365CalendarAttributes());
    }

    @Test
    public void testConstructor_FiveParams_WithAuthContextId() {
        GraphServiceClientProvider p = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext, "ctx-id");
        assertNotNull(p);
        assertSame(attributes, p.getOffice365CalendarAttributes());
    }

    @Test
    public void testConstructor_FiveParams_NullAuthContextId() {
        GraphServiceClientProvider p = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext, null);
        assertNotNull(p);
    }

    // ========================================================================
    // getOffice365CalendarAttributes
    // ========================================================================

    @Test
    public void testGetOffice365CalendarAttributes_ReturnsAttributes() {
        assertSame(attributes, provider.getOffice365CalendarAttributes());
    }

    @Test
    public void testGetOffice365CalendarAttributes_WithTenantId() {
        when(attributes.getTenantId()).thenReturn(TENANT_ID);
        assertSame(attributes, provider.getOffice365CalendarAttributes());
    }

    @Test
    public void testGetOffice365CalendarAttributes_NullAttributesProvider() {
        GraphServiceClientProvider p = new GraphServiceClientProvider(
                refreshTokenStore, null, requestContext, authorizationContext);
        assertNull(p.getOffice365CalendarAttributes());
    }

    // ========================================================================
    // getUserId(boolean)
    // ========================================================================

    @Test
    public void testGetUserId_CalledFromValidateAttributes_ReturnsEmail() {
        when(attributes.getEmail()).thenReturn(EMAIL);
        assertEquals(EMAIL, provider.getUserId(true));
    }

    @Test
    public void testGetUserId_NotFromValidateAttrs_InvokeAsUserFalse_ReturnsEmail() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(attributes.getEmail()).thenReturn(EMAIL);
        assertEquals(EMAIL, provider.getUserId(false));
    }

    @Test
    public void testGetUserId_NotFromValidateAttrs_InvokeAsUserTrue_ReturnsAccountId() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        setupAccount();
        assertEquals(ACCOUNT_ID, provider.getUserId(false));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetUserId_NullEmail_ThrowsIllegalState() {
        when(attributes.getEmail()).thenReturn(null);
        provider.getUserId(true);
    }

    @Test
    public void testGetUserId_NullEmail_ExceptionMessage() {
        when(attributes.getEmail()).thenReturn(null);
        try {
            provider.getUserId(true);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals(GraphServiceClientProvider.FAILED_TO_GET_ACCOUNT, e.getMessage());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testGetUserId_NullAccountId_ThrowsIllegalState() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(null);
        provider.getUserId(false);
    }

    // ========================================================================
    // getGraphServiceClient() - no params
    // ========================================================================

    @Test
    public void testGetGraphServiceClient_InvokeAsUserFalse_Success() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        setupAttributesAndRefreshToken();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            GraphServiceClient<Request> mockClient = setupMockedRetrySuccess(mockedRetry);

            GraphServiceClient<Request> result = provider.getGraphServiceClient();

            assertSame(mockClient, result);
        }
    }

    @Test
    public void testGetGraphServiceClient_InvokeAsUserTrue_Success() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        setupAttributes();
        setupAccount();
        String accountKey = ACCOUNT_ID + "_" + CLIENT_ID + "_" + AUTH_TYPE_PUBLIC;
        when(refreshTokenStore.get(accountKey)).thenReturn(REFRESH_TOKEN);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            GraphServiceClient<Request> mockClient = setupMockedRetrySuccess(mockedRetry);

            GraphServiceClient<Request> result = provider.getGraphServiceClient();

            assertSame(mockClient, result);
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetGraphServiceClient_NoRefreshToken_ThrowsMustAuthorize() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        setupAttributes();
        when(refreshTokenStore.get(REFRESH_TOKEN_KEY)).thenReturn(null);

        provider.getGraphServiceClient();
    }

    @Test
    public void testGetGraphServiceClient_NoRefreshToken_ExceptionMessage() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        setupAttributes();
        when(refreshTokenStore.get(REFRESH_TOKEN_KEY)).thenReturn(null);

        try {
            provider.getGraphServiceClient();
            fail("Expected MustAuthorizeException");
        } catch (MustAuthorizeException e) {
            assertEquals(Constants.AUTHORIZATION_PROMPT, e.getMessage());
        }
    }

    // ========================================================================
    // getGraphServiceClientForAdmin
    // ========================================================================

    @Test
    public void testGetGraphServiceClientForAdmin_Success() {
        setupAttributesAndRefreshToken();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            GraphServiceClient<Request> mockClient = setupMockedRetrySuccess(mockedRetry);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForAdmin();

            assertSame(mockClient, result);
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetGraphServiceClientForAdmin_NoRefreshToken_ThrowsMustAuthorize() {
        setupAttributes();
        when(refreshTokenStore.get(REFRESH_TOKEN_KEY)).thenReturn(null);

        provider.getGraphServiceClientForAdmin();
    }

    // ========================================================================
    // getGraphServiceClientForSystem
    // ========================================================================

    @Test
    public void testGetGraphServiceClientForSystem_Success() {
        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            GraphServiceClient<Request> mockClient = setupMockedRetrySuccess(mockedRetry);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForSystem();

            assertSame(mockClient, result);
        }
    }

    @Test
    public void testGetGraphServiceClientForSystem_Failure_ThrowsRuntimeException() {
        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenThrow(new Exception("connection failed"));

            try {
                provider.getGraphServiceClientForSystem();
                fail("Expected RuntimeException");
            } catch (RuntimeException e) {
                assertTrue(e.getMessage().contains("Unable to connect to Office 365"));
            }
        }
    }

    @Test
    public void testGetGraphServiceClientForSystem_WithRetryConfig_Success() {
        RetryConfiguration config = RetryConfiguration.withMaxRetries(5);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            GraphServiceClient<Request> mockClient = setupMockedRetrySuccess(mockedRetry);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForSystem(config);

            assertSame(mockClient, result);
        }
    }

    @Test
    public void testGetGraphServiceClientForSystem_WithRetryConfig_Failure() {
        RetryConfiguration config = RetryConfiguration.withMaxRetries(5);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenThrow(new Exception("connection failed"));

            try {
                provider.getGraphServiceClientForSystem(config);
                fail("Expected RuntimeException");
            } catch (RuntimeException e) {
                assertTrue(e.getMessage().contains("Unable to connect to Office 365"));
            }
        }
    }

    // ========================================================================
    // getGraphServiceClientWithClientCredentials - lambda branch coverage
    // (tested via getGraphServiceClientForSystem)
    // ========================================================================

    @SuppressWarnings({"rawtypes", "unchecked"})
    private GraphServiceClient<Request> setupLambdaSuccessChain(
            MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry,
            MockedStatic<ClientCredentialFactory> mockedCCF,
            MockedStatic<ConfidentialClientApplication> mockedCCA,
            MockedStatic<GraphServiceClient> mockedGSC) throws Exception {

        mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                any(), anyString(), any(RetryConfiguration.class)
        )).thenAnswer(invocation -> {
            RetryableOperation<?> op = invocation.getArgument(0);
            return op.execute();
        });

        IClientSecret mockCred = mock(IClientSecret.class);
        mockedCCF.when(() -> ClientCredentialFactory.createFromSecret(any())).thenReturn(mockCred);

        ConfidentialClientApplication.Builder mockCCABuilder = mock(ConfidentialClientApplication.Builder.class);
        ConfidentialClientApplication mockApp = mock(ConfidentialClientApplication.class);
        mockedCCA.when(() -> ConfidentialClientApplication.builder(anyString(), any(IClientCredential.class))).thenReturn(mockCCABuilder);
        doReturn(mockCCABuilder).when(mockCCABuilder).authority(anyString());
        when(mockCCABuilder.build()).thenReturn(mockApp);

        IAuthenticationResult mockAuthResult = mock(IAuthenticationResult.class);
        when(mockAuthResult.accessToken()).thenReturn("mock-access-token");
        when(mockApp.acquireToken(any(ClientCredentialParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(mockAuthResult));

        GraphServiceClient.Builder mockGSCBuilder = mock(GraphServiceClient.Builder.class);
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        mockedGSC.when(GraphServiceClient::builder).thenReturn(mockGSCBuilder);
        when(mockGSCBuilder.authenticationProvider(any(IAuthenticationProvider.class))).thenReturn(mockGSCBuilder);
        when(mockGSCBuilder.buildClient()).thenReturn(mockClient);

        return mockClient;
    }

    private void setupLambdaExecution(
            MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry,
            MockedStatic<ClientCredentialFactory> mockedCCF) {

        mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                any(), anyString(), any(RetryConfiguration.class)
        )).thenAnswer(invocation -> {
            RetryableOperation<?> op = invocation.getArgument(0);
            return op.execute();
        });

        IClientSecret mockCred = mock(IClientSecret.class);
        mockedCCF.when(() -> ClientCredentialFactory.createFromSecret(any())).thenReturn(mockCred);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testGetGraphServiceClientForSystem_LambdaExecution_TenantIdConfigured_Success() throws Exception {
        when(attributes.getClientSecret()).thenReturn(CLIENT_SECRET);
        when(attributes.getTenantId()).thenReturn(TENANT_ID);
        when(attributes.getClientId()).thenReturn(CLIENT_ID);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class);
             MockedStatic<ClientCredentialFactory> mockedCCF = mockStatic(ClientCredentialFactory.class);
             MockedStatic<ConfidentialClientApplication> mockedCCA = mockStatic(ConfidentialClientApplication.class);
             MockedStatic<GraphServiceClient> mockedGSC = mockStatic(GraphServiceClient.class)) {

            GraphServiceClient<Request> mockClient = setupLambdaSuccessChain(mockedRetry, mockedCCF, mockedCCA, mockedGSC);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForSystem();

            assertSame(mockClient, result);
        }
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testGetGraphServiceClientForSystem_LambdaExecution_NullTenantIdDerivedFromEmail_Success() throws Exception {
        when(attributes.getClientSecret()).thenReturn(CLIENT_SECRET);
        when(attributes.getTenantId()).thenReturn(null);
        when(attributes.getEmail()).thenReturn(EMAIL);
        when(attributes.getClientId()).thenReturn(CLIENT_ID);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class);
             MockedStatic<ClientCredentialFactory> mockedCCF = mockStatic(ClientCredentialFactory.class);
             MockedStatic<ConfidentialClientApplication> mockedCCA = mockStatic(ConfidentialClientApplication.class);
             MockedStatic<GraphServiceClient> mockedGSC = mockStatic(GraphServiceClient.class)) {

            GraphServiceClient<Request> mockClient = setupLambdaSuccessChain(mockedRetry, mockedCCF, mockedCCA, mockedGSC);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForSystem();

            assertSame(mockClient, result);
        }
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testGetGraphServiceClientForSystem_LambdaExecution_BlankTenantIdDerivedFromEmail_Success() throws Exception {
        when(attributes.getClientSecret()).thenReturn(CLIENT_SECRET);
        when(attributes.getTenantId()).thenReturn("   ");
        when(attributes.getEmail()).thenReturn(EMAIL);
        when(attributes.getClientId()).thenReturn(CLIENT_ID);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class);
             MockedStatic<ClientCredentialFactory> mockedCCF = mockStatic(ClientCredentialFactory.class);
             MockedStatic<ConfidentialClientApplication> mockedCCA = mockStatic(ConfidentialClientApplication.class);
             MockedStatic<GraphServiceClient> mockedGSC = mockStatic(GraphServiceClient.class)) {

            GraphServiceClient<Request> mockClient = setupLambdaSuccessChain(mockedRetry, mockedCCF, mockedCCA, mockedGSC);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForSystem();

            assertSame(mockClient, result);
        }
    }

    @Test
    public void testGetGraphServiceClientForSystem_NullTenantId_NullEmail_ThrowsRuntimeException() {
        when(attributes.getClientSecret()).thenReturn(CLIENT_SECRET);
        when(attributes.getTenantId()).thenReturn(null);
        when(attributes.getEmail()).thenReturn(null);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class);
             MockedStatic<ClientCredentialFactory> mockedCCF = mockStatic(ClientCredentialFactory.class)) {

            setupLambdaExecution(mockedRetry, mockedCCF);

            try {
                provider.getGraphServiceClientForSystem();
                fail("Expected RuntimeException");
            } catch (RuntimeException e) {
                assertTrue(e.getMessage().contains("Unable to connect to Office 365"));
                assertTrue(e.getCause() instanceof IllegalStateException);
                assertTrue(e.getCause().getMessage().contains("Tenant ID is not configured"));
            }
        }
    }

    @Test
    public void testGetGraphServiceClientForSystem_NullTenantId_EmailWithoutAt_ThrowsRuntimeException() {
        when(attributes.getClientSecret()).thenReturn(CLIENT_SECRET);
        when(attributes.getTenantId()).thenReturn(null);
        when(attributes.getEmail()).thenReturn("no-at-sign");

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class);
             MockedStatic<ClientCredentialFactory> mockedCCF = mockStatic(ClientCredentialFactory.class)) {

            setupLambdaExecution(mockedRetry, mockedCCF);

            try {
                provider.getGraphServiceClientForSystem();
                fail("Expected RuntimeException");
            } catch (RuntimeException e) {
                assertTrue(e.getMessage().contains("Unable to connect to Office 365"));
                assertTrue(e.getCause() instanceof IllegalStateException);
            }
        }
    }

    @Test
    public void testGetGraphServiceClientForSystem_BlankTenantId_NullEmail_ThrowsRuntimeException() {
        when(attributes.getClientSecret()).thenReturn(CLIENT_SECRET);
        when(attributes.getTenantId()).thenReturn("");
        when(attributes.getEmail()).thenReturn(null);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class);
             MockedStatic<ClientCredentialFactory> mockedCCF = mockStatic(ClientCredentialFactory.class)) {

            setupLambdaExecution(mockedRetry, mockedCCF);

            try {
                provider.getGraphServiceClientForSystem();
                fail("Expected RuntimeException");
            } catch (RuntimeException e) {
                assertTrue(e.getMessage().contains("Unable to connect to Office 365"));
                assertTrue(e.getCause() instanceof IllegalStateException);
            }
        }
    }

    // ========================================================================
    // getGraphServiceClient(String userId, String refreshToken)
    // ========================================================================

    @Test
    public void testGetGraphServiceClient_WithUserIdAndToken_Success() {
        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            GraphServiceClient<Request> mockClient = setupMockedRetrySuccess(mockedRetry);

            GraphServiceClient<Request> result = provider.getGraphServiceClient("userId", "refreshToken");

            assertSame(mockClient, result);
        }
    }

    // ========================================================================
    // getGraphServiceClient(String, String, RetryConfiguration) - exceptions
    // ========================================================================

    @Test
    public void testGetGraphServiceClient_WithRetryConfig_Success() {
        RetryConfiguration config = RetryConfiguration.withMaxRetries(2);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            GraphServiceClient<Request> mockClient = setupMockedRetrySuccess(mockedRetry);

            GraphServiceClient<Request> result = provider.getGraphServiceClient("userId", "refreshToken", config);

            assertSame(mockClient, result);
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetGraphServiceClient_IOException_ThrowsMustAuthorize() {
        setupAttributes();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenThrow(new IOException("network error"));

            provider.getGraphServiceClient(REFRESH_TOKEN_KEY, REFRESH_TOKEN);
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetGraphServiceClient_ExecutionException_ThrowsMustAuthorize() {
        setupAttributes();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenThrow(new ExecutionException("exec error", new Exception("cause")));

            provider.getGraphServiceClient(REFRESH_TOKEN_KEY, REFRESH_TOKEN);
        }
    }

    @Test
    public void testGetGraphServiceClient_InterruptedException_ThrowsMustAuthorizeAndInterrupts() {
        setupAttributes();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenThrow(new InterruptedException("interrupted"));

            try {
                provider.getGraphServiceClient(REFRESH_TOKEN_KEY, REFRESH_TOKEN);
                fail("Expected MustAuthorizeException");
            } catch (MustAuthorizeException e) {
                assertTrue(Thread.currentThread().isInterrupted());
                assertEquals("Your session has expired. Please sign in again to continue.", e.getMessage());
            } finally {
                Thread.interrupted(); // clear flag
            }
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetGraphServiceClient_GenericException_ThrowsMustAuthorize() {
        setupAttributes();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenThrow(new Exception("generic error"));

            provider.getGraphServiceClient(REFRESH_TOKEN_KEY, REFRESH_TOKEN);
        }
    }

    @Test
    public void testGetGraphServiceClient_IOException_ExceptionMessageIsSessionExpired() {
        setupAttributes();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenThrow(new IOException("error"));

            try {
                provider.getGraphServiceClient(REFRESH_TOKEN_KEY, REFRESH_TOKEN);
                fail("Expected MustAuthorizeException");
            } catch (MustAuthorizeException e) {
                assertEquals("Your session has expired. Please sign in again to continue.", e.getMessage());
            }
        }
    }

    // ========================================================================
    // extractPlainUserId through exception paths
    // ========================================================================

    @Test
    public void testGetGraphServiceClient_WsContactKey_IOException_PreservesKey() {
        setupAttributes();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenThrow(new IOException("error"));

            try {
                provider.getGraphServiceClient("wsContact_123", REFRESH_TOKEN);
                fail("Expected MustAuthorizeException");
            } catch (MustAuthorizeException e) {
                assertNotNull(e);
            }
        }
    }

    @Test
    public void testGetGraphServiceClient_PlainKey_IOException_ReturnsKeyAsIs() {
        setupAttributes();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenThrow(new IOException("error"));

            try {
                provider.getGraphServiceClient("plainKey", REFRESH_TOKEN);
                fail("Expected MustAuthorizeException");
            } catch (MustAuthorizeException e) {
                assertNotNull(e);
            }
        }
    }

    // ========================================================================
    // MustAuthorizeException with authContextId
    // ========================================================================

    @Test
    public void testGetGraphServiceClient_WithAuthContextId_IOException_IncludesAuthContextInException() {
        setupAttributes();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenThrow(new IOException("error"));

            try {
                providerWithAuthCtx.getGraphServiceClient(REFRESH_TOKEN_KEY, REFRESH_TOKEN);
                fail("Expected MustAuthorizeException");
            } catch (MustAuthorizeException e) {
                assertEquals("Your session has expired. Please sign in again to continue.", e.getMessage());
            }
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetGraphServiceClient_WithAuthContextId_NoRefreshToken() {
        setupAttributes();
        when(refreshTokenStore.get(REFRESH_TOKEN_KEY)).thenReturn(null);

        providerWithAuthCtx.getGraphServiceClientForAdmin();
    }

    // ========================================================================
    // WS_CONTACT key handling in getRefTokenStoreKey
    // ========================================================================

    @Test
    public void testGetGraphServiceClientForAdmin_WsContactKey_UsesKeyDirectly() {
        String wsEmail = Constants.WS_CONTACT + "_someId";
        when(attributes.getEmail()).thenReturn(wsEmail);
        when(refreshTokenStore.get(wsEmail)).thenReturn(REFRESH_TOKEN);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            GraphServiceClient<Request> mockClient = setupMockedRetrySuccess(mockedRetry);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForAdmin();

            assertSame(mockClient, result);
            verify(refreshTokenStore).get(wsEmail);
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetGraphServiceClientForAdmin_WsContactKey_NoRefreshToken() {
        String wsEmail = Constants.WS_CONTACT + "_someId";
        when(attributes.getEmail()).thenReturn(wsEmail);
        when(refreshTokenStore.get(wsEmail)).thenReturn(null);

        provider.getGraphServiceClientForAdmin();
    }

    // ========================================================================
    // updateRefreshToken
    // ========================================================================

    @Test
    public void testUpdateRefreshToken_ParsesAndStores() {
        JsonObject tokenCache = new JsonObject();
        JsonObject refreshTokenObj = new JsonObject();
        JsonObject tokenData = new JsonObject();
        tokenData.addProperty("secret", "new-refresh-token");
        refreshTokenObj.add("firstKey", tokenData);
        tokenCache.add("RefreshToken", refreshTokenObj);

        provider.updateRefreshToken("REDACTED_SECRET", tokenCache);

        verify(refreshTokenStore).put("userId-123", "new-refresh-token");
    }

    @Test
    public void testUpdateRefreshToken_MultipleKeys_UsesFirst() {
        JsonObject tokenCache = new JsonObject();
        JsonObject refreshTokenObj = new JsonObject();
        JsonObject tokenData1 = new JsonObject();
        tokenData1.addProperty("secret", "token-1");
        JsonObject tokenData2 = new JsonObject();
        tokenData2.addProperty("secret", "token-2");
        refreshTokenObj.add("key1", tokenData1);
        refreshTokenObj.add("key2", tokenData2);
        tokenCache.add("RefreshToken", refreshTokenObj);

        provider.updateRefreshToken("userId", tokenCache);

        verify(refreshTokenStore).put(eq("userId"), eq("token-1"));
    }

    // ========================================================================
    // getUserRequestBuilder(Boolean, String) - 2 param
    // ========================================================================

    @Test
    public void testGetUserRequestBuilder_UseEmailTrue_ReturnsUsersBuilder() {
        setupAttributesAndRefreshToken();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            UserRequestBuilder mockBuilder = mock(UserRequestBuilder.class);
            when(mockClient.users(EMAIL)).thenReturn(mockBuilder);

            UserRequestBuilder result = provider.getUserRequestBuilder(true, "someAccountId");

            assertSame(mockBuilder, result);
        }
    }

    @Test
    public void testGetUserRequestBuilder_UseEmailFalse_ReturnsMeBuilder() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        setupAttributes();
        setupAccount();
        String accountKey = ACCOUNT_ID + "_" + CLIENT_ID + "_" + AUTH_TYPE_PUBLIC;
        when(refreshTokenStore.get(accountKey)).thenReturn(REFRESH_TOKEN);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            UserRequestBuilder mockBuilder = mock(UserRequestBuilder.class);
            when(mockClient.me()).thenReturn(mockBuilder);

            UserRequestBuilder result = provider.getUserRequestBuilder(false, "someAccountId");

            assertSame(mockBuilder, result);
        }
    }

    @Test
    public void testGetUserRequestBuilder_NullUseEmail_InvokeAsUserTrue_ReturnsMeBuilder() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        setupAttributes();
        setupAccount();
        String accountKey = ACCOUNT_ID + "_" + CLIENT_ID + "_" + AUTH_TYPE_PUBLIC;
        when(refreshTokenStore.get(accountKey)).thenReturn(REFRESH_TOKEN);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            UserRequestBuilder mockBuilder = mock(UserRequestBuilder.class);
            when(mockClient.me()).thenReturn(mockBuilder);

            UserRequestBuilder result = provider.getUserRequestBuilder(null, null);

            assertSame(mockBuilder, result);
        }
    }

    @Test
    public void testGetUserRequestBuilder_NullUseEmail_InvokeAsUserFalse_ReturnsUsersBuilder() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        setupAttributesAndRefreshToken();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            UserRequestBuilder mockBuilder = mock(UserRequestBuilder.class);
            when(mockClient.users(EMAIL)).thenReturn(mockBuilder);

            UserRequestBuilder result = provider.getUserRequestBuilder(null, null);

            assertSame(mockBuilder, result);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testGetUserRequestBuilder_IOException_ThrowsIllegalState() throws IOException {
        GraphServiceClientProvider spyProvider = spy(provider);
        doThrow(new IOException("test")).when(spyProvider).getGraphServiceClientForUser(any(), any());

        spyProvider.getUserRequestBuilder(true, "id");
    }

    // ========================================================================
    // getUserRequestBuilder(Boolean, String, String) - 3 param
    // ========================================================================

    @Test
    public void testGetUserRequestBuilder3_WithDelegateEmail_ReturnsDelegateBuilder() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        setupAttributes();
        setupAccount();
        String accountKey = ACCOUNT_ID + "_" + CLIENT_ID + "_" + AUTH_TYPE_PUBLIC;
        when(refreshTokenStore.get(accountKey)).thenReturn(REFRESH_TOKEN);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            UserRequestBuilder mockBuilder = mock(UserRequestBuilder.class);
            when(mockClient.users("delegate@example.com")).thenReturn(mockBuilder);

            UserRequestBuilder result = provider.getUserRequestBuilder(false, null, "delegate@example.com");

            assertSame(mockBuilder, result);
        }
    }

    @Test
    public void testGetUserRequestBuilder3_NullDelegate_FallsBackTo2Param() {
        setupAttributesAndRefreshToken();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            UserRequestBuilder mockBuilder = mock(UserRequestBuilder.class);
            when(mockClient.users(EMAIL)).thenReturn(mockBuilder);

            UserRequestBuilder result = provider.getUserRequestBuilder(true, null, null);

            assertSame(mockBuilder, result);
        }
    }

    @Test
    public void testGetUserRequestBuilder3_EmptyDelegate_FallsBackTo2Param() {
        setupAttributesAndRefreshToken();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            UserRequestBuilder mockBuilder = mock(UserRequestBuilder.class);
            when(mockClient.users(EMAIL)).thenReturn(mockBuilder);

            UserRequestBuilder result = provider.getUserRequestBuilder(true, null, "");

            assertSame(mockBuilder, result);
        }
    }

    @Test
    public void testGetUserRequestBuilder3_BlankDelegate_FallsBackTo2Param() {
        setupAttributesAndRefreshToken();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            UserRequestBuilder mockBuilder = mock(UserRequestBuilder.class);
            when(mockClient.users(EMAIL)).thenReturn(mockBuilder);

            UserRequestBuilder result = provider.getUserRequestBuilder(true, null, "   ");

            assertSame(mockBuilder, result);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testGetUserRequestBuilder3_IOException_ThrowsIllegalState() throws IOException {
        GraphServiceClientProvider spyProvider = spy(provider);
        doThrow(new IOException("test")).when(spyProvider).getGraphServiceClientForUser(any(), any());

        spyProvider.getUserRequestBuilder(true, "id", "delegate@example.com");
    }

    // ========================================================================
    // getGraphServiceClientForUser(Boolean, String)
    // ========================================================================

    @Test
    public void testGetGraphServiceClientForUser_UseEmailTrue() throws IOException {
        setupAttributesAndRefreshToken();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForUser(true, "someAccountId");

            assertSame(mockClient, result);
        }
    }

    @Test
    public void testGetGraphServiceClientForUser_UseEmailFalse_WithAccountId() throws IOException {
        when(requestContext.invokeAsUser()).thenReturn(true);
        setupAttributes();
        setupAccount();
        String accountKey = ACCOUNT_ID + "_" + CLIENT_ID + "_" + AUTH_TYPE_PUBLIC;
        when(refreshTokenStore.get(accountKey)).thenReturn(REFRESH_TOKEN);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForUser(false, "specific-account");

            assertSame(mockClient, result);
        }
    }

    @Test
    public void testGetGraphServiceClientForUser_UseEmailFalse_NullAccountId_UsesAuthContext() throws IOException {
        when(requestContext.invokeAsUser()).thenReturn(true);
        setupAttributes();
        setupAccount();
        String accountKey = ACCOUNT_ID + "_" + CLIENT_ID + "_" + AUTH_TYPE_PUBLIC;
        when(refreshTokenStore.get(accountKey)).thenReturn(REFRESH_TOKEN);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForUser(false, null);

            assertSame(mockClient, result);
        }
    }

    @Test
    public void testGetGraphServiceClientForUser_NullUseEmail_InvokeAsUserTrue() throws IOException {
        when(requestContext.invokeAsUser()).thenReturn(true);
        setupAttributes();
        setupAccount();
        String accountKey = ACCOUNT_ID + "_" + CLIENT_ID + "_" + AUTH_TYPE_PUBLIC;
        when(refreshTokenStore.get(accountKey)).thenReturn(REFRESH_TOKEN);

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForUser(null, null);

            assertSame(mockClient, result);
        }
    }

    @Test
    public void testGetGraphServiceClientForUser_NullUseEmail_InvokeAsUserFalse() throws IOException {
        when(requestContext.invokeAsUser()).thenReturn(false);
        setupAttributesAndRefreshToken();

        try (MockedStatic<ExponentialBackoffRetryExecutor> mockedRetry = mockStatic(ExponentialBackoffRetryExecutor.class)) {
            @SuppressWarnings("unchecked")
            GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
            mockedRetry.when(() -> ExponentialBackoffRetryExecutor.executeWithRetry(
                    any(), anyString(), any(RetryConfiguration.class)
            )).thenReturn(mockClient);

            GraphServiceClient<Request> result = provider.getGraphServiceClientForUser(null, null);

            assertSame(mockClient, result);
        }
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetGraphServiceClientForUser_NoRefreshToken_ThrowsMustAuthorize() throws IOException {
        setupAttributes();
        when(refreshTokenStore.get(REFRESH_TOKEN_KEY)).thenReturn(null);

        provider.getGraphServiceClientForUser(true, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetGraphServiceClientForUser_NullAccountId_ThrowsIllegalState() throws IOException {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(null);

        provider.getGraphServiceClientForUser(false, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetGraphServiceClientForUser_NullEmail_ThrowsIllegalState() throws IOException {
        when(attributes.getEmail()).thenReturn(null);

        provider.getGraphServiceClientForUser(true, null);
    }

    // ========================================================================
    // GraphServiceClientAuthenticationProvider - inner class
    // ========================================================================

    @Test
    public void testAuthProvider_ValidToken() {
        GraphServiceClientProvider.GraphServiceClientAuthenticationProvider authProvider =
                new GraphServiceClientProvider.GraphServiceClientAuthenticationProvider("valid-token");
        assertNotNull(authProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthProvider_NullToken_ThrowsIllegalArgument() {
        new GraphServiceClientProvider.GraphServiceClientAuthenticationProvider(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthProvider_EmptyToken_ThrowsIllegalArgument() {
        new GraphServiceClientProvider.GraphServiceClientAuthenticationProvider("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthProvider_BlankToken_ThrowsIllegalArgument() {
        new GraphServiceClientProvider.GraphServiceClientAuthenticationProvider("   ");
    }

    @Test
    public void testAuthProvider_GetAuthorizationTokenAsync_ReturnsToken() throws Exception {
        GraphServiceClientProvider.GraphServiceClientAuthenticationProvider authProvider =
                new GraphServiceClientProvider.GraphServiceClientAuthenticationProvider("my-access-token");

        CompletableFuture<String> future = authProvider.getAuthorizationTokenAsync(
                new URL("https://graph.microsoft.com/v1.0/me"));

        assertEquals("my-access-token", future.get());
    }

    @Test
    public void testAuthProvider_ExceptionMessage() {
        try {
            new GraphServiceClientProvider.GraphServiceClientAuthenticationProvider(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("No valid access token"));
        }
    }
}
