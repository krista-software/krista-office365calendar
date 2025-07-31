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
        System.out.println("GraphServiceClientProviderFactory constructor - START");
        this.refreshTokenStore = refreshTokenStore;
        System.out.println("GraphServiceClientProviderFactory constructor - refreshTokenStore set");
        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        System.out.println("GraphServiceClientProviderFactory constructor - office365CalendarAttributeStore set");
        this.requestContext = requestContext;
        System.out.println("GraphServiceClientProviderFactory constructor - requestContext set");
        this.authorizationContext = authorizationContext;
        System.out.println("GraphServiceClientProviderFactory constructor - authorizationContext set");
        System.out.println("GraphServiceClientProviderFactory constructor - END");
    }

    public String createAttributes(Office365CalendarAttributes attributes) {
        System.out.println("createAttributes method - START");
        String result = office365CalendarAttributeStore.save(attributes);
        System.out.println("createAttributes method - END with result: " + result);
        return result;
    }

    @NotNull
    private GraphServiceClientProvider getGraphServiceClientProvider(Office365CalendarAttributes attributes,
                                                                     String authContextId) {
        System.out.println("getGraphServiceClientProvider method - START with authContextId: " + authContextId);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(refreshTokenStore, attributes, requestContext, authorizationContext,
                authContextId);
        System.out.println("getGraphServiceClientProvider method - END");
        return provider;
    }

    public GraphServiceClientProvider create(String authContextId) {
        System.out.println("create method (with authContextId) - START with authContextId: " + authContextId);
        final Office365CalendarAttributes attributes = office365CalendarAttributeStore.load(authContextId);
        System.out.println("create method (with authContextId) - loaded attributes");
        GraphServiceClientProvider result = getGraphServiceClientProvider(attributes, authContextId);
        System.out.println("create method (with authContextId) - END");
        return result;
    }

    public GraphServiceClientProvider create() {
        System.out.println("create method (no params) - START");
        GraphServiceClientProvider result = create(requestContext);
        System.out.println("create method (no params) - END");
        return result;
    }

    public GraphServiceClientProvider create(RequestContext context) {
        System.out.println("create method (with RequestContext) - START");
        String key = context.getInvokerId();
        System.out.println("create method (with RequestContext) - invokerId: " + key);
        Office365CalendarAttributes attributes = office365CalendarAttributeStore.load(key);
        System.out.println("create method (with RequestContext) - loaded attributes");
        GraphServiceClientProvider result = getGraphServiceClientProvider(attributes, null);
        System.out.println("create method (with RequestContext) - END");
        return result;
    }
}
