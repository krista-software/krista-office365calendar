package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.ExponentialBackoffRetryExecutor;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.RetryConfiguration;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.field.NamedValuedField;
import com.google.gson.JsonObject;
import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserRequestBuilder;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants.PRIVATE;
import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants.PUBLIC;

/**
 * Provides authenticated Microsoft Graph API clients for Office 365 calendar integration.
 * Typical usage involves obtaining a GraphServiceClient for the current user, for an admin, or for system-level operations,
 * as well as handling token refresh and authorization exceptions.
 */
public final class GraphServiceClientProvider {

    public static final String REQUIRED_SCOPE = Constants.REQUIRED_SCOPE;
    public static final String CLIENT_CREDENTIAL_SCOPES = "https://graph.microsoft.com/.default";
    public static final String SCOPE_SEPARATOR = " ";
    public static final String AUTHORITY = "https://login.microsoftonline.com/";
    public static final String FAILED_TO_GET_ACCOUNT = "Failed to get account";
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphServiceClientProvider.class);
    private final RefreshTokenStore refreshTokenStore;
    private final Office365CalendarAttributes office365CalendarAttributes;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final String authContextId;

    /**
     * Constructs a new GraphServiceClientProvider with required dependencies.
     *
     * @param refreshTokenStore           the store for managing refresh tokens
     * @param office365CalendarAttributes Office 365 calendar configuration and credentials
     * @param requestContext              the request context
     * @param authorizationContext        the authorization context
     */

    public GraphServiceClientProvider(RefreshTokenStore refreshTokenStore, Office365CalendarAttributes office365CalendarAttributes, RequestContext requestContext, AuthorizationContext authorizationContext) {
        this(refreshTokenStore, office365CalendarAttributes, requestContext, authorizationContext, null);
        LOGGER.info("→ GraphServiceClientProvider: Initialized successfully with default auth context");
    }

    /**
     * Constructs a new GraphServiceClientProvider with an optional authentication context ID.
     *
     * @param refreshTokenStore           the store for managing refresh tokens
     * @param office365CalendarAttributes Office 365 calendar configuration and credentials
     * @param requestContext              the request context
     * @param authorizationContext        the authorization context
     * @param authContextId               optional authentication context ID
     */


    public GraphServiceClientProvider(RefreshTokenStore refreshTokenStore, Office365CalendarAttributes office365CalendarAttributes, RequestContext requestContext, AuthorizationContext authorizationContext, String authContextId) {
        this.refreshTokenStore = refreshTokenStore;

        this.office365CalendarAttributes = office365CalendarAttributes;

        this.requestContext = requestContext;

        this.authorizationContext = authorizationContext;

        this.authContextId = authContextId;

    }

    /**
     * Returns the Office 365 calendar attributes used for authentication and configuration.
     *
     * @return the Office365CalendarAttributes instance
     */
    public Office365CalendarAttributes getOffice365CalendarAttributes() {
        LOGGER.debug("→ getOffice365CalendarAttributes: Returning calendar attributes for tenant: {}",
                office365CalendarAttributes.getTenantId());
        return office365CalendarAttributes;
    }

    /**
     * Returns a GraphServiceClient for the current user or system, depending on the request context.
     *
     * @return a configured GraphServiceClient instance
     */
    public GraphServiceClient<Request> getGraphServiceClient() {
        LOGGER.debug("→ getGraphServiceClient: Starting client creation");
        boolean useEmail = !requestContext.invokeAsUser();
        LOGGER.info("→ getGraphServiceClient: Creating client for context - invokeAsUser: {}, useEmail: {}",
                requestContext.invokeAsUser(), useEmail);

        GraphServiceClient<Request> client = getGraphServiceClient(useEmail);
        LOGGER.info("→ getGraphServiceClient: Successfully created GraphServiceClient");
        return client;
    }

    /**
     * Returns a GraphServiceClient for system-level (application) authentication.
     *
     * @return a configured GraphServiceClient instance for system operations
     */
    public GraphServiceClient<Request> getGraphServiceClientForSystem() {
        LOGGER.info("→ getGraphServiceClientForSystem: Creating system-level GraphServiceClient with client credentials");

        GraphServiceClient<Request> client = getGraphServiceClientWithClientCredentials();
        LOGGER.info("→ getGraphServiceClientForSystem: Successfully created system GraphServiceClient");
        return client;
    }

    /**
     * Returns a GraphServiceClient for system-level (application) authentication with custom retry configuration.
     *
     * @param retryConfig the retry configuration to use
     * @return a configured GraphServiceClient instance for system operations
     */
    public GraphServiceClient<Request> getGraphServiceClientForSystem(RetryConfiguration retryConfig) {
        LOGGER.info("→ getGraphServiceClientForSystem: Creating system-level GraphServiceClient with client credentials and retry config: maxRetries={}", retryConfig.maxRetryAttempts());

        GraphServiceClient<Request> client = getGraphServiceClientWithClientCredentials(retryConfig);
        LOGGER.info("→ getGraphServiceClientForSystem: Successfully created system GraphServiceClient with custom retry config");
        return client;
    }

    /**
     * Returns a GraphServiceClient for admin-level authentication.
     *
     * @return a configured GraphServiceClient instance for admin operations
     */
    public GraphServiceClient<Request> getGraphServiceClientForAdmin() {
        LOGGER.info("→ getGraphServiceClientForAdmin: Creating admin-level GraphServiceClient");

        GraphServiceClient<Request> client = getGraphServiceClient(true);
        LOGGER.info("→ getGraphServiceClientForAdmin: Successfully created admin GraphServiceClient");
        return client;
    }


    private GraphServiceClient<Request> getGraphServiceClientWithClientCredentials() {
        GraphServiceClient<Request> result = getGraphServiceClientWithClientCredentials(RetryConfiguration.defaultConfiguration());
        return result;
    }

    private GraphServiceClient<Request> getGraphServiceClientWithClientCredentials(RetryConfiguration retryConfig) {
        LOGGER.debug("→ getGraphServiceClientWithClientCredentials: Creating client with client credentials and retry config: maxRetries={}", retryConfig.maxRetryAttempts());

        try {
            return ExponentialBackoffRetryExecutor.executeWithRetry(() -> {
                LOGGER.debug("→ getGraphServiceClientWithClientCredentials: Executing client credentials token acquisition with retry");

                IClientCredential clientCredential = ClientCredentialFactory
                        .createFromSecret(office365CalendarAttributes.getClientSecret());
                LOGGER.debug("→ Created client credential from secret");

                String authority = AUTHORITY + office365CalendarAttributes.getTenantId();
                LOGGER.debug("→ Built authority URL: {}", authority);

                ConfidentialClientApplication cca = ConfidentialClientApplication.builder(
                                office365CalendarAttributes.getClientId(),
                                clientCredential)
                        .authority(authority)
                        .build();
                LOGGER.debug("→ Created ConfidentialClientApplication for client ID: {}", office365CalendarAttributes.getClientId());

                ClientCredentialParameters parameters = ClientCredentialParameters.builder(
                                Collections.singleton(GraphServiceClientProvider.CLIENT_CREDENTIAL_SCOPES))
                        .build();
                LOGGER.debug("→ Created client credential parameters with scopes: {}", CLIENT_CREDENTIAL_SCOPES);

                // This is the critical network operation that may fail and needs retry
                IAuthenticationResult result = cca.acquireToken(parameters).get();
                LOGGER.debug("→ Acquired access token successfully");

                GraphServiceClient<Request> client = GraphServiceClient.builder()
                        .authenticationProvider(new GraphServiceClientAuthenticationProvider(result.accessToken()))
                        .buildClient();
                LOGGER.debug("→ Created GraphServiceClient with client credentials successfully");

                return client;
            }, "GraphServiceClient creation with client credentials", retryConfig);

        } catch (Exception cause) {
            LOGGER.error("→ getGraphServiceClientWithClientCredentials: Error creating GraphServiceClient with client credentials - {}", cause.getMessage(), cause);
            throw new RuntimeException("Unable to connect to Office 365 using the provided credentials. Please verify your client ID and secret are correct.", cause);
        }
    }

    /**
     * Returns a GraphServiceClient for the current user or system, depending on the request context.
     *
     * @return a configured GraphServiceClient instance
     */
    private GraphServiceClient<Request> getGraphServiceClient(boolean useEmail) {
        LOGGER.debug("→ getGraphServiceClient(useEmail={}): Creating GraphServiceClient", useEmail);

        try {
            String userId = getUserId(useEmail);
            String refreshTokenStoreKey = getRefTokenStoreKey(userId);
            LOGGER.debug("→ Retrieved userId: {}", userId);
            String s = refreshTokenStore.get(refreshTokenStoreKey);
            String refreshToken = refreshTokenStore.get(refreshTokenStoreKey);
            if (refreshToken == null) {
                LOGGER.warn("→ No refresh token found for userId: {}, creating authorization exception", userId);
                throw createMustAuthorizationException(refreshTokenStoreKey, false);
            }
            LOGGER.debug("→ Retrieved refresh token for userId: {}", userId);

            GraphServiceClient<Request> client = getGraphServiceClient(userId, refreshToken);
            LOGGER.debug("→ getGraphServiceClient(useEmail={}): Successfully created GraphServiceClient for userId: {}", useEmail, userId);
            return client;
        } catch (RuntimeException cause) {
            LOGGER.error("→ getGraphServiceClient(useEmail={}): Runtime exception occurred - {}", useEmail, cause.getMessage(), cause);
            throw cause;
        }
    }

    @NotNull
    private String getRefTokenStoreKey(String userId) {
        if (userId.startsWith(Constants.WS_CONTACT)) {
            return userId;
        } else {
            String key = userId + Constants.UNDER_SCORE + office365CalendarAttributes.getClientId() + Constants.UNDER_SCORE + office365CalendarAttributes.getAuthType();
            return key;
        }
    }

    /**
     * Returns a GraphServiceClient for the specified user, using the provided refresh token.
     * Uses exponential backoff retry mechanism for transient network failures.
     *
     * @param userId       the user ID
     * @param refreshToken the refresh token for the user
     * @return a configured GraphServiceClient instance
     */
    public GraphServiceClient<Request> getGraphServiceClient(String userId, String refreshToken) {
        GraphServiceClient<Request> result = getGraphServiceClient(userId, refreshToken, RetryConfiguration.defaultConfiguration());
        return result;
    }

    /**
     * Returns a GraphServiceClient for the specified user, using the provided refresh token and custom retry configuration.
     * Uses exponential backoff retry mechanism for transient network failures.
     *
     * @param userId       the user ID
     * @param refreshToken the refresh token for the user
     * @param retryConfig  the retry configuration to use
     * @return a configured GraphServiceClient instance
     */
    public GraphServiceClient<Request> getGraphServiceClient(String userId, String refreshToken, RetryConfiguration retryConfig) {
        LOGGER.info("→ getGraphServiceClient: Creating GraphServiceClient for userId: {} with retry config: maxRetries={}", userId, retryConfig.maxRetryAttempts());

        try {
            return ExponentialBackoffRetryExecutor.executeWithRetry(() -> {
                LOGGER.debug("→ getGraphServiceClient: Executing token acquisition with retry for userId: {}", userId);

                String[] scopes = REQUIRED_SCOPE.split(SCOPE_SEPARATOR);
                Set<String> scopeSet = Arrays.stream(scopes).collect(Collectors.toSet());
                LOGGER.debug("→ Created scope set with {} scopes: {}", scopeSet.size(), scopeSet);

                RefreshTokenParameters refreshTokenStoreParameters = RefreshTokenParameters.builder(scopeSet, refreshToken).build();
                LOGGER.debug("→ Created refresh token parameters");

                IClientCredential clientCredential = ClientCredentialFactory.createFromSecret(office365CalendarAttributes.getClientSecret());
                LOGGER.debug("→ Created client credential from secret");

                String authority;
                if (PRIVATE.equals(office365CalendarAttributes.getAuthType())) {
                    authority = Constants.AUTHORITY + office365CalendarAttributes.getTenantId();
                } else if (PUBLIC.equals(office365CalendarAttributes.getAuthType())) {
                    authority = Constants.ORG_AUTHORITY;
                } else {
                    throw new IllegalArgumentException(Constants.UNSUPPORTED_AUTH);
                }
                LOGGER.debug("→ Built authority URL: {}", authority);

                ConfidentialClientApplication confidentialClientApplication = ConfidentialClientApplication.builder(
                        office365CalendarAttributes.getClientId(), clientCredential).authority(authority).build();
                LOGGER.debug("→ Created ConfidentialClientApplication for client ID: {}", office365CalendarAttributes.getClientId());

                // This is the critical network operation that may fail and needs retry
                IAuthenticationResult authenticationResult = confidentialClientApplication.acquireToken(refreshTokenStoreParameters).get();
                LOGGER.debug("→ Acquired authentication result successfully");

                final String cachedTokenContent = confidentialClientApplication.tokenCache().serialize();
                updateRefreshToken(userId, Constants.GSON.fromJson(cachedTokenContent, JsonObject.class));
                LOGGER.debug("→ Updated refresh token for userId: {}", userId);
                String accessToken = authenticationResult.accessToken();
                GraphServiceClient<Request> client = GraphServiceClient.builder()
                        .authenticationProvider(new GraphServiceClientAuthenticationProvider(accessToken))
                        .buildClient();
                LOGGER.debug("→ Created GraphServiceClient successfully");

                return client;
            }, "GraphServiceClient creation for user: " + userId, retryConfig);

        } catch (IOException | ExecutionException cause) {
            LOGGER.error("→ getGraphServiceClient: IOException/ExecutionException for userId: {} - {}", userId, cause.getMessage(), cause);
            throw createMustAuthorizationException(userId, true);
        } catch (InterruptedException interruptedException) {
            LOGGER.error("→ getGraphServiceClient: InterruptedException for userId: {} - {}", userId, interruptedException.getMessage(), interruptedException);
            Thread.currentThread().interrupt();
            throw createMustAuthorizationException(userId, true);
        } catch (Exception cause) {
            LOGGER.error("→ getGraphServiceClient: Unexpected exception for userId: {} - {}", userId, cause.getMessage(), cause);
            throw createMustAuthorizationException(userId, true);
        }
    }

    /**
     * Updates the refresh token for the specified user in the refresh token store.
     *
     * @param userId         the user ID
     * @param jsonTokenCache the token cache as a JsonObject
     */
    public void updateRefreshToken(String userId, JsonObject jsonTokenCache) {
        LOGGER.debug("→ updateRefreshToken: Updating refresh token for userId: {}", userId);

        JsonObject refreshToken = jsonTokenCache.get("RefreshToken").getAsJsonObject();
        LOGGER.debug("→ Retrieved RefreshToken object from cache");

        String firstKey = refreshToken.keySet().iterator().next();
        LOGGER.debug("→ Retrieved first key from refresh token: {}", firstKey);

        JsonObject tokenData = refreshToken.getAsJsonObject(firstKey);
        String secret = tokenData.get("secret").getAsString();
        LOGGER.debug("→ Extracted secret from token data");

        refreshTokenStore.put(userId, secret);
        LOGGER.info("→ updateRefreshToken: Successfully updated refresh token for userId: {}", userId);
    }

    /**
     * Creates a MustAuthorizeException for the specified user, indicating that re-authorization is required.
     *
     * @param userId           the user ID
     * @param reAuthentication whether re-authentication is required
     * @return a MustAuthorizeException with details
     */
    private MustAuthorizeException createMustAuthorizationException(String userId, boolean reAuthentication) {
        LOGGER.warn("→ createMustAuthorizationException: Creating authorization exception for userId: {}, reAuthentication: {}", userId, reAuthentication);
        LOGGER.debug(reAuthentication ? "Got error for Authentication. So sending for re authentication" : "Got error for Authentication. So sending for authentication");

        List<NamedValuedField> details = new ArrayList<>();
        NamedValuedField userIdField = new NamedValuedField("userId", "Text", userId, new HashMap<>(), new HashMap<>());
        details.add(userIdField);
        LOGGER.debug("→ Added userId field to exception details");

        if (authContextId != null) {
            NamedValuedField contextIdField = new NamedValuedField("authContextId", "Text", authContextId, new HashMap<>(), new HashMap<>());
            details.add(contextIdField);
            LOGGER.debug("→ Added authContextId field to exception details: {}", authContextId);
        }

        String message = reAuthentication ? "Your session has expired. Please sign in again to continue." : Constants.AUTHORIZATION_PROMPT;
        LOGGER.debug("→ createMustAuthorizationException: Created exception with message: {}", message);

        return new MustAuthorizeException(message, details);
    }

    /**
     * Returns the user ID based on the context and parameters.
     *
     * @param calledFromValidateAttributes whether called from attribute validation
     * @return the user ID
     * @throws IllegalStateException if the user ID cannot be determined
     */
    public String getUserId(boolean calledFromValidateAttributes) {
        LOGGER.debug("→ getUserId: Getting userId with calledFromValidateAttributes: {}, invokeAsUser: {}",
                calledFromValidateAttributes, requestContext.invokeAsUser());

        String userId = calledFromValidateAttributes || !requestContext.invokeAsUser() ?
                office365CalendarAttributes.getEmail() :
                authorizationContext.getAuthorizedAccount().getAccountId();

        if (userId == null) {
            LOGGER.error("→ getUserId: Failed to get account - userId is null");
            throw new IllegalStateException(FAILED_TO_GET_ACCOUNT);
        }

        LOGGER.debug("→ getUserId: Successfully retrieved userId: {}", userId);
        return userId;
    }

    /**
     * Retrieves a UserRequestBuilder based on the provided parameters, allowing interaction with Microsoft Graph API
     * for user-related operations.
     *
     * @param useEmail  A Boolean flag indicating whether to use email for user identification. If true, the 'accountID'
     *                  parameter is used; if false, the current user's information is retrieved.
     * @param accountID The account ID of the user when 'useEmail' is true. Ignored if 'useEmail' is false.
     * @return A UserRequestBuilder instance configured for the specified user or the current user, depending on the
     * 'useEmail' and 'accountID' parameters.
     * @throws IllegalStateException If an IOException occurs while trying to build the request or if there is an issue
     *                               with the underlying state.
     */
    public UserRequestBuilder getUserRequestBuilder(Boolean useEmail, String accountID) {
        LOGGER.debug("→ getUserRequestBuilder: Creating UserRequestBuilder with useEmail: {}, accountID: {}", useEmail, accountID);

        try {
            if (useEmail != null) {
                if (useEmail) {
                    LOGGER.debug("→ Using email mode - creating users() request for: {}", office365CalendarAttributes.getEmail());
                    return getGraphServiceClientForUser(useEmail, accountID).users(office365CalendarAttributes.getEmail());
                } else {
                    LOGGER.debug("→ Using non-email mode - creating me() request");
                    return getGraphServiceClientForUser(useEmail, null).me();
                }
            }

            if (requestContext.invokeAsUser()) {
                LOGGER.debug("→ InvokeAsUser mode - creating me() request");
                return getGraphServiceClientForUser(null, null).me();
            } else {
                LOGGER.debug("→ System mode - creating users() request for: {}", office365CalendarAttributes.getEmail());
                return getGraphServiceClientForUser(null, null).users(office365CalendarAttributes.getEmail());
            }
        } catch (IOException cause) {
            LOGGER.error("→ getUserRequestBuilder: IOException occurred - {}", cause.getMessage(), cause);
            throw new IllegalStateException(cause);
        }
    }
    /**
     * Retrieves a UserRequestBuilder based on the provided parameters, allowing interaction with Microsoft Graph API
     * for user-related operations. This overload supports delegated operations.
     *
     * @param useEmail     A Boolean flag indicating whether to use email for user identification. If true, the 'accountID'
     *                     parameter is used; if false, the current user's information is retrieved.
     * @param accountID    The account ID of the user when 'useEmail' is true. Ignored if 'useEmail' is false.
     * @param delegateEmail The email of the user to delegate to. If provided and not blank, creates a delegated request builder.
     * @return A UserRequestBuilder instance configured for the specified user, delegate, or the current user, depending on the parameters.
     * @throws IllegalStateException If an IOException occurs while trying to build the request or if there is an issue
     *                               with the underlying state.
     */
    public UserRequestBuilder getUserRequestBuilder(Boolean useEmail, String accountID, String delegateEmail) {
        LOGGER.debug("→ getUserRequestBuilder(3-param): Creating UserRequestBuilder with useEmail: {}, accountID: {}, delegateEmail: {}", useEmail, accountID, delegateEmail);

        try {
            // Check for delegated operation first (matches reference implementation pattern)
            if (delegateEmail != null && !delegateEmail.trim().isEmpty()) {
                LOGGER.debug("→ Using delegated mode - creating users() request for delegate: {}", delegateEmail);
                return getGraphServiceClientForUser(false, null).users(delegateEmail);
            }

            // Fall back to the existing 2-parameter logic
            LOGGER.debug("→ No delegate email provided, falling back to standard getUserRequestBuilder logic");
            return getUserRequestBuilder(useEmail, accountID);
        } catch (IOException cause) {
            LOGGER.error("→ getUserRequestBuilder(3-param): IOException occurred - {}", cause.getMessage(), cause);
            throw new IllegalStateException(cause);
        }
    }

    /**
     * Gets a GraphServiceClient for user-related operations based on the specified parameters.
     *
     * @param useEmail  A Boolean flag indicating whether to use email for user identification. If true, the 'accountID'
     *                  parameter is ignored.
     * @param accountID The account ID of the user when 'useEmail' is true; ignored if 'useEmail' is false.
     * @return A GraphServiceClient configured for the specified user or the current user, depending on the 'useEmail'
     * and 'accountID' parameters.
     * @throws IOException If an error occurs while obtaining or configuring the GraphServiceClient.
     */
    public GraphServiceClient<Request> getGraphServiceClientForUser(Boolean useEmail, String accountID) throws IOException {
        LOGGER.debug("→ getGraphServiceClientForUser: Creating client with useEmail: {}, accountID: {}", useEmail, accountID);

        if (useEmail != null) {
            String effectiveAccountID = useEmail ? null : accountID;
            LOGGER.debug("→ Using explicit useEmail flag: {}, effectiveAccountID: {}", useEmail, effectiveAccountID);
            return getGraphServiceClientForUsers(useEmail, effectiveAccountID);
        } else {
            boolean useEmailFlag = !requestContext.invokeAsUser();
            LOGGER.debug("→ Using context-based useEmail flag: {}", useEmailFlag);
            return getGraphServiceClientForUsers(useEmailFlag, null);
        }
    }

    /**
     * Gets a GraphServiceClient for user-related operations using the specified parameters.
     *
     * @param useEmail  A boolean indicating whether to use email for user identification. If false, 'accountID' is used.
     * @param accountID The account ID of the user; ignored if 'useEmail' is true.
     * @return A GraphServiceClient configured for the specified user.
     * @throws IOException If an error occurs while obtaining or configuring the GraphServiceClient.
     */
    private GraphServiceClient<Request> getGraphServiceClientForUsers(boolean useEmail, String accountID) {
        LOGGER.debug("→ getGraphServiceClientForUsers: Creating client with useEmail: {}, accountID: {}", useEmail, accountID);

        try {
            String userId = getUserId(useEmail);
            String refreshTokenStoreKey = getRefTokenStoreKey(userId);
            LOGGER.debug("→ Retrieved userId: {}", userId);
            LOGGER.info("getGraphServiceClientForUsers method - calling refreshTokenStore.getAllKeyValuePairsAsString()");
            String refreshToken = refreshTokenStore.get(refreshTokenStoreKey);
            if (refreshToken == null) {
                LOGGER.warn("→ No refresh token found for userId: {}, creating authorization exception", userId);
                throw createMustAuthorizationException(refreshTokenStoreKey, false);
            }
            LOGGER.debug("→ Retrieved refresh token for userId: {}", userId);

            GraphServiceClient<Request> client = getGraphServiceClient(userId, refreshToken);
            LOGGER.debug("→ getGraphServiceClientForUsers: Successfully created GraphServiceClient for userId: {}", userId);
            return client;
        } catch (RuntimeException cause) {
            LOGGER.error("→ getGraphServiceClientForUsers: Runtime exception occurred - {}", cause.getMessage(), cause);
            throw cause;
        }
    }

    /**
     * Gets the user ID based on the specified parameters.
     *
     * @param calledFromValidateAttributes A boolean indicating whether the method is called from validateAttributes.
     * @param accountID                    The account ID of the user; used if 'calledFromValidateAttributes' is false.
     * @return The user ID.
     * @throws IllegalStateException If the user ID cannot be determined.
     */
    private String getUserId(boolean calledFromValidateAttributes, String accountID) {
        LOGGER.debug("→ getUserId(overloaded): Getting userId with calledFromValidateAttributes: {}, accountID: {}",
                calledFromValidateAttributes, accountID);

        String userId;
        if (calledFromValidateAttributes) {
            userId = office365CalendarAttributes.getEmail();
            LOGGER.debug("→ Using email from attributes: {}", userId);
        } else {
            userId = (accountID != null) ? accountID : authorizationContext.getAuthorizedAccount().getAccountId();
            LOGGER.debug("→ Using accountID or authorized account: {}", userId);
        }

        if (userId == null) {
            LOGGER.error("→ getUserId(overloaded): Failed to get account - userId is null");
            throw new IllegalStateException(Constants.FAILED_TO_GET_ACCOUNT);
        }

        LOGGER.debug("→ getUserId(overloaded): Successfully retrieved userId: {}", userId);
        return userId;
    }

    /**
     * Authentication provider for Microsoft GraphServiceClient using a static access token.
     * <p>
     * This implementation provides the access token for all requests, supporting asynchronous token retrieval.
     * </p>
     */
    public static class GraphServiceClientAuthenticationProvider implements IAuthenticationProvider {

        private final String accessToken;

        /**
         * Constructs a new authentication provider with the given access token.
         *
         * @param accessToken the OAuth access token to use for authentication
         */
        public GraphServiceClientAuthenticationProvider(String accessToken) {
            LOGGER.debug("→ GraphServiceClientAuthenticationProvider: Initializing with access token");

            if (accessToken == null || accessToken.isEmpty() || accessToken.isBlank()) {
                LOGGER.error("→ GraphServiceClientAuthenticationProvider: Empty access token provided: {}", accessToken);
                throw new IllegalArgumentException("Authentication failed: No valid access token was provided. Please sign in again.");
            }

            this.accessToken = accessToken;
            LOGGER.debug("→ GraphServiceClientAuthenticationProvider: Successfully initialized with valid access token");
        }

        /**
         * Returns the authorization token for the given request URL.
         *
         * @param requestUrl the URL of the request
         * @return a CompletableFuture containing the access token
         */
        @NotNull
        @Override
        public CompletableFuture<String> getAuthorizationTokenAsync(@NotNull URL requestUrl) {
            LOGGER.debug("→ getAuthorizationTokenAsync: Providing access token for request URL: {}", requestUrl);
            CompletableFuture<String> result = CompletableFuture.completedFuture(accessToken);
            return result;
        }
    }

}
