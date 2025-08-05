package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors;

import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Service
public class GraphServiceClientProviderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphServiceClientProviderFactory.class);
    private final RefreshTokenStore refreshTokenStore;
    private final Office365CalendarAttributeStore office365CalendarAttributeStore;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;


    @Inject
    public GraphServiceClientProviderFactory(RefreshTokenStore refreshTokenStore,
                                             Office365CalendarAttributeStore office365CalendarAttributeStore,
                                             RequestContext requestContext,
                                             AuthorizationContext authorizationContext) {
        LOGGER.debug("→ GraphServiceClientProviderFactory: Initializing with injected dependencies");
        this.refreshTokenStore = refreshTokenStore;
        LOGGER.debug("→ RefreshTokenStore dependency set");
        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        LOGGER.debug("→ Office365CalendarAttributeStore dependency set");
        this.requestContext = requestContext;
        LOGGER.debug("→ RequestContext dependency set");
        this.authorizationContext = authorizationContext;
        LOGGER.debug("→ AuthorizationContext dependency set");
        LOGGER.debug("→ GraphServiceClientProviderFactory: Initialization complete");
    }

    public String createAttributes(Office365CalendarAttributes attributes) {
        LOGGER.debug("→ createAttributes: Saving Office365CalendarAttributes");
        String result = office365CalendarAttributeStore.save(attributes);
        LOGGER.debug("→ createAttributes: Attributes saved with ID: {}", result);
        return result;
    }

    @NotNull
    private GraphServiceClientProvider getGraphServiceClientProvider(Office365CalendarAttributes attributes,
                                                                     String authContextId) {
        LOGGER.debug("→ getGraphServiceClientProvider: Creating provider with authContextId: {}", authContextId);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(refreshTokenStore, attributes, requestContext, authorizationContext,
                authContextId);
        LOGGER.debug("→ getGraphServiceClientProvider: Provider created successfully");
        return provider;
    }

    public GraphServiceClientProvider create(String authContextId) {
        LOGGER.debug("→ create: Creating provider with authContextId: {}", authContextId);
        final Office365CalendarAttributes attributes = office365CalendarAttributeStore.load(authContextId);
        LOGGER.debug("→ create: Attributes loaded successfully");
        GraphServiceClientProvider result = getGraphServiceClientProvider(attributes, authContextId);
        LOGGER.debug("→ create: Provider created and returning");
        return result;
    }

    public GraphServiceClientProvider create() {
        LOGGER.debug("→ create: Creating provider with default request context");
        GraphServiceClientProvider result = create(requestContext);
        LOGGER.debug("→ create: Provider created and returning");
        return result;
    }

    public GraphServiceClientProvider create(RequestContext context) {
        LOGGER.debug("→ create: Creating provider with RequestContext");
        String key = context.getInvokerId();
        LOGGER.debug("→ create: Using invokerId: {}", key);
        Office365CalendarAttributes attributes = office365CalendarAttributeStore.load(key);
        LOGGER.debug("→ create: Attributes loaded successfully");
        GraphServiceClientProvider result = getGraphServiceClientProvider(attributes, null);
        LOGGER.debug("→ create: Provider created and returning");
        return result;
    }
}
