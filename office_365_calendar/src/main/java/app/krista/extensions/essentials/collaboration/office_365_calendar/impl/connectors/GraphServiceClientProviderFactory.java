package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors;

import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.jvnet.hk2.annotations.Service;

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
        this.refreshTokenStore = refreshTokenStore;
        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.defaultClientProvider = defaultClientProvider;
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
        String authContextId = office365CalendarAttributeStore.save(office365CalendarAttributes);
        return new GraphServiceClientProvider(refreshTokenStore, office365CalendarAttributes, requestContext, authorizationContext, authContextId);
    }

    /**
     * Returns the default {@link GraphServiceClientProvider} instance.
     *
     * @return the default client provider
     */
    public GraphServiceClientProvider create() {
        return defaultClientProvider;
    }
}
