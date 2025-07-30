package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors;

import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Factory class for creating and managing {@link GraphServiceClientProvider} instances
 * for Office 365 calendar integration.
 * <p>
 * This class encapsulates the logic for constructing client providers with the appropriate
 * dependencies and context, supporting both default and custom attribute-based providers.
 * It is typically used to obtain authenticated Microsoft Graph API clients for various
 * calendar operations, either with default or user-supplied credentials.
 * </p>
 */
@Service
public final class GraphServiceClientProviderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphServiceClientProviderFactory.class);

    private final RefreshTokenStore refreshTokenStore;
    private final Office365CalendarAttributeStore office365CalendarAttributeStore;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final GraphServiceClientProvider defaultClientProvider;

    /**
     * Constructs a new factory with the required dependencies and a default client provider.
     *
     * @param refreshTokenStore the store for managing refresh tokens
     * @param office365CalendarAttributeStore the store for Office 365 calendar attributes
     * @param office365CalendarAttributes the default Office 365 calendar attributes
     * @param requestContext the request context
     * @param authorizationContext the authorization context
     */
    @Inject
    public GraphServiceClientProviderFactory(RefreshTokenStore refreshTokenStore, Office365CalendarAttributeStore office365CalendarAttributeStore, Office365CalendarAttributes office365CalendarAttributes, RequestContext requestContext, AuthorizationContext authorizationContext) {
        this(refreshTokenStore, office365CalendarAttributeStore, requestContext, authorizationContext,
                new GraphServiceClientProvider(refreshTokenStore, office365CalendarAttributes, requestContext, authorizationContext, null));

        LOGGER.info("→ GraphServiceClientProviderFactory: Successfully initialized with default client provider for tenant: {}",
                office365CalendarAttributes.getTenantId());
    }

    /**
     * Constructs a new factory with the required dependencies and a specified default client provider.
     *
     * @param refreshTokenStore the store for managing refresh tokens
     * @param office365CalendarAttributeStore the store for Office 365 calendar attributes
     * @param requestContext the request context
     * @param authorizationContext the authorization context
     * @param defaultClientProvider the default client provider instance
     */
    public GraphServiceClientProviderFactory(RefreshTokenStore refreshTokenStore, Office365CalendarAttributeStore office365CalendarAttributeStore, RequestContext requestContext, AuthorizationContext authorizationContext, GraphServiceClientProvider defaultClientProvider) {
        LOGGER.info("→ GraphServiceClientProviderFactory: Initializing with custom default client provider");

        this.refreshTokenStore = refreshTokenStore;
        LOGGER.debug("→ RefreshTokenStore set");

        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        LOGGER.debug("→ Office365CalendarAttributeStore set");

        this.requestContext = requestContext;
        LOGGER.debug("→ RequestContext set with invokeAsUser: {}", requestContext.invokeAsUser());

        this.authorizationContext = authorizationContext;
        LOGGER.debug("→ AuthorizationContext set");

        this.defaultClientProvider = defaultClientProvider;
        LOGGER.debug("→ Default GraphServiceClientProvider set");

        LOGGER.info("→ GraphServiceClientProviderFactory: Initialization completed successfully");
    }

    /**
     * Creates a new {@link GraphServiceClientProvider} using the provided Office 365 calendar attributes.
     * The attributes are saved to the attribute store and a new provider is constructed with the resulting context ID.
     *
     * @param office365CalendarAttributes the Office 365 calendar attributes to use
     * @return a new GraphServiceClientProvider instance
     * @throws IOException if an error occurs while saving attributes
     */
    public GraphServiceClientProvider create(Office365CalendarAttributes office365CalendarAttributes) throws IOException {
        LOGGER.info("→ create: Creating GraphServiceClientProvider with custom attributes for tenant: {}",
                office365CalendarAttributes.getTenantId());
        LOGGER.debug("→ Client ID: {}, Email: {}", office365CalendarAttributes.getClientId(), office365CalendarAttributes.getEmail());

        String authContextId = office365CalendarAttributeStore.save(office365CalendarAttributes);
        LOGGER.debug("→ Saved attributes to store with authContextId: {}", authContextId);

        GraphServiceClientProvider provider = new GraphServiceClientProvider(refreshTokenStore, office365CalendarAttributes, requestContext, authorizationContext, authContextId);
        LOGGER.info("→ create: Successfully created GraphServiceClientProvider with authContextId: {}", authContextId);

        return provider;
    }

    /**
     * Creates a new {@link GraphServiceClientProvider} using the provided authContextId.
     *
     * @param authContextId the authentication context ID
     * @return a GraphServiceClientProvider instance
     */
    public GraphServiceClientProvider create(String authContextId) {
        LOGGER.info("→ create: Creating GraphServiceClientProvider with authContextId: {}", authContextId);

        final Office365CalendarAttributes attributes = office365CalendarAttributeStore.load(authContextId);
        return new GraphServiceClientProvider(refreshTokenStore, attributes, requestContext, authorizationContext, authContextId);
    }

    /**
     * Creates and saves attributes, returning the authContextId.
     *
     * @param attributes the Office 365 calendar attributes to save
     * @return the authentication context ID
     */
    public String createAttributes(Office365CalendarAttributes attributes) {
        LOGGER.info("→ createAttributes: Saving attributes for email: {}", attributes.getEmail());
        return office365CalendarAttributeStore.save(attributes);
    }

    /**
     * Returns the default {@link GraphServiceClientProvider} instance.
     *
     * @return the default client provider
     */
    public GraphServiceClientProvider create() {
        LOGGER.info("→ create: Returning default GraphServiceClientProvider");
        LOGGER.debug("→ Default provider tenant: {}", defaultClientProvider.getOffice365CalendarAttributes().getTenantId());

        return defaultClientProvider;
    }
}
