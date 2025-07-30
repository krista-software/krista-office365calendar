package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors;

import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

@Service
public class GraphServiceClientProviderFactory {

    private final RefreshTokenStore refreshTokenStore;
    private final Office365CalendarAttributeStore office365CalendarAttributeStore;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;


    @Inject
    public GraphServiceClientProviderFactory(RefreshTokenStore refreshTokenStore,
                                             Office365CalendarAttributeStore office365CalendarAttributeStore,
                                             RequestContext requestContext,
                                             AuthorizationContext authorizationContext) {
        this.refreshTokenStore = refreshTokenStore;
        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
    }

    public String createAttributes(Office365CalendarAttributes attributes) {
        return office365CalendarAttributeStore.save(attributes);
    }

    @NotNull
    private GraphServiceClientProvider getGraphServiceClientProvider(Office365CalendarAttributes attributes,
                                                                     String authContextId) {
        return new GraphServiceClientProvider(refreshTokenStore, attributes, requestContext, authorizationContext,
                authContextId);
    }

    public GraphServiceClientProvider create(String authContextId) {
        final Office365CalendarAttributes attributes = office365CalendarAttributeStore.load(authContextId);
        return getGraphServiceClientProvider(attributes, authContextId);
    }

    public GraphServiceClientProvider create() {
        return create(requestContext);
    }

    public GraphServiceClientProvider create(RequestContext context) {
        String key = context.getInvokerId();
        Office365CalendarAttributes attributes = office365CalendarAttributeStore.load(key);
        return getGraphServiceClientProvider(attributes, null);
    }
}
