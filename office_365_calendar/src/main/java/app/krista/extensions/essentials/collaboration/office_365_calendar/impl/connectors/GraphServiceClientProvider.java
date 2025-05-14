package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
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

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
     * @param refreshTokenStore the store for managing refresh tokens
     * @param office365CalendarAttributes Office 365 calendar configuration and credentials
     * @param requestContext the request context
     * @param authorizationContext the authorization context
     */
    @Inject
    public GraphServiceClientProvider(RefreshTokenStore refreshTokenStore, Office365CalendarAttributes office365CalendarAttributes, RequestContext requestContext, AuthorizationContext authorizationContext) {
        this(refreshTokenStore, office365CalendarAttributes, requestContext, authorizationContext, null);
    }

    /**
     * Constructs a new GraphServiceClientProvider with an optional authentication context ID.
     *
     * @param refreshTokenStore the store for managing refresh tokens
     * @param office365CalendarAttributes Office 365 calendar configuration and credentials
     * @param requestContext the request context
     * @param authorizationContext the authorization context
     * @param authContextId optional authentication context ID
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
        return office365CalendarAttributes;
    }

    /**
     * Returns a GraphServiceClient for the current user or system, depending on the request context.
     *
     * @return a configured GraphServiceClient instance
     */
    public GraphServiceClient<Request> getGraphServiceClient() {
        return getGraphServiceClient(!requestContext.invokeAsUser());
    }

    /**
     * Returns a GraphServiceClient for system-level (application) authentication.
     *
     * @return a configured GraphServiceClient instance for system operations
     */
    public GraphServiceClient<Request> getGraphServiceClientForSystem() {
        return getGraphServiceClientWithClientCredentials();
    }

    /**
     * Returns a GraphServiceClient for admin-level authentication.
     *
     * @return a configured GraphServiceClient instance for admin operations
     */
    public GraphServiceClient<Request> getGraphServiceClientForAdmin() {
        return getGraphServiceClient(true);
    }


    private GraphServiceClient<Request> getGraphServiceClientWithClientCredentials() {
        try {
            IClientCredential clientCredential = ClientCredentialFactory
                    .createFromSecret(office365CalendarAttributes.getClientSecret());
            String authority = AUTHORITY + office365CalendarAttributes.getTenantId();

            ConfidentialClientApplication cca = ConfidentialClientApplication.builder(
                            office365CalendarAttributes.getClientId(),
                            clientCredential)
                    .authority(authority)
                    .build();

            ClientCredentialParameters parameters = ClientCredentialParameters.builder(
                            Collections.singleton(GraphServiceClientProvider.CLIENT_CREDENTIAL_SCOPES))
                    .build();

            IAuthenticationResult result = cca.acquireToken(parameters).get();

            return GraphServiceClient.builder()
                    .authenticationProvider(new GraphServiceClientAuthenticationProvider(result.accessToken()))
                    .buildClient();
        } catch (Exception cause) {
            LOGGER.error("Error creating GraphServiceClient with client credentials", cause);
            throw new RuntimeException("Error creating GraphServiceClient with client credentials", cause);
        }
    }

    /**
     * Returns a GraphServiceClient for the current user or system, depending on the request context.
     *
     * @return a configured GraphServiceClient instance
     */
    private GraphServiceClient<Request> getGraphServiceClient(boolean useEmail) {
        try {
            String userId = getUserId(useEmail);
            String refreshToken = refreshTokenStore.get(userId);
            if (refreshToken == null) {
                throw createMustAuthorizationException(userId, false);
            }
            return getGraphServiceClient(userId, refreshToken);
        } catch (RuntimeException cause) {
            throw cause;
        }
    }

    /**
     * Returns a GraphServiceClient for the specified user, using the provided refresh token.
     *
     * @param userId the user ID
     * @param refreshToken the refresh token for the user
     * @return a configured GraphServiceClient instance
     */
    private GraphServiceClient<Request> getGraphServiceClient(String userId, String refreshToken) {
        try {

            String[] scopes = REQUIRED_SCOPE.split(SCOPE_SEPARATOR);
            Set<String> scopeSet = Arrays.stream(scopes).collect(Collectors.toSet());
            RefreshTokenParameters refreshTokenStoreParameters = RefreshTokenParameters.builder(scopeSet, refreshToken).build();
            IClientCredential clientCredential = ClientCredentialFactory.createFromSecret(office365CalendarAttributes.getClientSecret());
            String authority = AUTHORITY + office365CalendarAttributes.getTenantId();
            ConfidentialClientApplication confidentialClientApplication = ConfidentialClientApplication.builder(office365CalendarAttributes.getClientId(), clientCredential).authority(authority).build();
            IAuthenticationResult authenticationResult = confidentialClientApplication.acquireToken(refreshTokenStoreParameters).get();
            final String cachedTokenContent = confidentialClientApplication.tokenCache().serialize();
            updateRefreshToken(userId, Constants.GSON.fromJson(cachedTokenContent, JsonObject.class));
            return GraphServiceClient.builder().authenticationProvider(new GraphServiceClientAuthenticationProvider(authenticationResult.accessToken())).buildClient();
        } catch (IOException | ExecutionException cause) {
            throw createMustAuthorizationException(userId, true);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw createMustAuthorizationException(userId, true);
        }
    }

    /**
     * Updates the refresh token for the specified user in the refresh token store.
     *
     * @param userId the user ID
     * @param jsonTokenCache the token cache as a JsonObject
     */
    private void updateRefreshToken(String userId, JsonObject jsonTokenCache) {
        JsonObject refreshToken = jsonTokenCache.get("RefreshToken").getAsJsonObject();
        String firstKey = refreshToken.keySet().iterator().next();
        JsonObject tokenData = refreshToken.getAsJsonObject(firstKey);
        String secret = tokenData.get("secret").getAsString();
        refreshTokenStore.put(userId, secret);
        System.out.println("Updated Refresh Token.");
    }

    /**
     * Creates a MustAuthorizeException for the specified user, indicating that re-authorization is required.
     *
     * @param userId the user ID
     * @param reAuthentication whether re-authentication is required
     * @return a MustAuthorizeException with details
     */
    private MustAuthorizeException createMustAuthorizationException(String userId, boolean reAuthentication) {
        LOGGER.debug(reAuthentication ? "Got error for Authentication. So sending for re authentication" : "Got error for Authentication. So sending for authentication");
        List<NamedValuedField> details = new ArrayList<>();
        NamedValuedField userIdField = new NamedValuedField("userId", "Text", userId, new HashMap<>(), new HashMap<>());
        details.add(userIdField);
        if (authContextId != null) {
            NamedValuedField contextIdField = new NamedValuedField("authContextId", "Text", authContextId, new HashMap<>(), new HashMap<>());
            details.add(contextIdField);
        }
        return new MustAuthorizeException(reAuthentication ? "Refresh Token is no longer valid. Please reauthorize yourself " : Constants.AUTHORIZATION_PROMPT, details);
    }

    /**
     * Returns the user ID based on the context and parameters.
     *
     * @param calledFromValidateAttributes whether called from attribute validation
     * @return the user ID
     * @throws IllegalStateException if the user ID cannot be determined
     */
    private String getUserId(boolean calledFromValidateAttributes) {
        String userId = calledFromValidateAttributes || !requestContext.invokeAsUser() ? office365CalendarAttributes.getEmail() : authorizationContext.getAuthorizedAccount().getAccountId();
        if (userId == null) {
            throw new IllegalStateException(FAILED_TO_GET_ACCOUNT);
        }
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
        try {
            if (useEmail != null) {
                return useEmail ? getGraphServiceClientForUser(useEmail, accountID).users(office365CalendarAttributes.getEmail())
                        : getGraphServiceClientForUser(useEmail, null).me();
            }
            return requestContext.invokeAsUser() ? getGraphServiceClientForUser(null, null).me() : getGraphServiceClientForUser(null, null).users(office365CalendarAttributes.getEmail());
        } catch (IOException cause) {
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
        if (useEmail != null) {
            return getGraphServiceClientForUsers(useEmail, useEmail ? null : accountID);
        } else {
            return getGraphServiceClientForUsers(!requestContext.invokeAsUser(), null);
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
        try {
            String userId = getUserId(useEmail);
            String refreshToken = refreshTokenStore.get(userId);
            if (refreshToken == null) {
                throw createMustAuthorizationException(userId, false);
            }
            return getGraphServiceClient(userId, refreshToken);
        } catch (RuntimeException cause) {
            throw cause;
        }
    }

    /**
     * Gets the user ID based on the specified parameters.
     *
     * @param calledFromValidateAttributes A boolean indicating whether the method is called from validateAttributes.
     * @param accountID The account ID of the user; used if 'calledFromValidateAttributes' is false.
     * @return The user ID.
     * @throws IllegalStateException If the user ID cannot be determined.
     */
    private String getUserId(boolean calledFromValidateAttributes, String accountID) {
        String userId;
        if (calledFromValidateAttributes) {
            userId = office365CalendarAttributes.getEmail();
        } else {
            userId = (accountID != null) ? accountID : authorizationContext.getAuthorizedAccount().getAccountId();
        }
        if (userId == null) {
            throw new IllegalStateException(Constants.FAILED_TO_GET_ACCOUNT);
        }
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
            if (accessToken == null || accessToken.isEmpty() || accessToken.isBlank()) {
                throw new IllegalArgumentException("Empty access token provided " + accessToken);
            }
            this.accessToken = accessToken;
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
            return CompletableFuture.completedFuture(accessToken);
        }
    }

}
