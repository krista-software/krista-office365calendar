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

package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.OAuthService;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.AuthenticationResponse;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.microsoft.graph.http.GraphServiceException;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

import static app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants.*;

@Service
public class TestConnectionServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestConnectionServiceImpl.class);

    private final GraphServiceClientProviderFactory providerFactory;
    private final Office365CalendarAttributeStore office365CalendarAttributeStore;
    private final String baseRoutingUrl;

    @Inject
    public TestConnectionServiceImpl(GraphServiceClientProviderFactory providerFactory,
                                   Office365CalendarAttributeStore office365CalendarAttributeStore,
                                   Invoker invoker) {
        this.providerFactory = providerFactory;
        this.office365CalendarAttributeStore = office365CalendarAttributeStore;
        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
    }

    public String testConnection(Office365CalendarAttributes office365CalendarAttributes) {
        LOGGER.debug("→ testConnection: Starting connection test");
        LOGGER.info("Testing connection with Microsoft Graph API");
        String authContextId = providerFactory.createAttributes(office365CalendarAttributes);
        LOGGER.debug("→ testConnection: Created authContextId: {}", authContextId);
        String authUrl = null;

        try {
            LOGGER.debug("→ testConnection: Attempting to verify API access");
            LOGGER.info("Verifying API access for email: {}", office365CalendarAttributes.getEmail());
            providerFactory.create(authContextId).getGraphServiceClientForAdmin().me().calendars().buildRequest().get();

            LOGGER.debug("→ testConnection: API access verification successful");
            LOGGER.info("Test connection successful");
            return createTestConnectionResponse(true, null, null);
        } catch (GraphServiceException cause) {
            LOGGER.debug("→ testConnection: GraphServiceException occurred: {}", cause.getMessage());
            LOGGER.error("Graph API connection failed: {}", cause.getMessage());
            return createTestConnectionResponse(false, "An error occurred during test connection.", null);
        } catch (MustAuthorizeException cause) {
            LOGGER.debug("→ testConnection: MustAuthorizeException - authorization required");
            LOGGER.info("Authorization required, generating auth URL");
            String state = createStateParameter(cause, office365CalendarAttributes);
            LOGGER.debug("→ testConnection: Created state parameter: {}", state);
            OAuth20Service oAuth20Service = new OAuthService(office365CalendarAttributes).getOAuth20Service();
            authUrl = oAuth20Service.getAuthorizationUrl(state) + AUTH_URL_QUERY_PARAMS;
            LOGGER.debug("→ testConnection: Generated authorization URL");
            return createTestConnectionResponse(false, AUTHORIZATION_PROMPT, authUrl);
        } finally {
            LOGGER.debug("→ testConnection: Cleanup phase");
            if (authUrl == null) {
                LOGGER.debug("→ testConnection: No auth URL generated, removing temporary authContextId");
                office365CalendarAttributeStore.remove(authContextId);
            }
            LOGGER.debug("→ testConnection: Test connection completed");
        }
    }

    private String createTestConnectionResponse(boolean isSuccess, String errorMessage, String url) {
        LOGGER.debug("→ createTestConnectionResponse: Creating response - success: {}, hasUrl: {}", isSuccess, url != null);
        String result;
        if (isSuccess) {
            LOGGER.debug("→ createTestConnectionResponse: Creating success response");
            result = GSON.toJson(new AuthenticationResponse(true, null, null));
        } else if (url != null) {
            LOGGER.debug("→ createTestConnectionResponse: Creating error response with authorization URL");
            result = GSON.toJson(new AuthenticationResponse(false, errorMessage, url));
        } else {
            LOGGER.debug("→ createTestConnectionResponse: Creating error response without URL");
            result = GSON.toJson(new AuthenticationResponse(false,
                    Objects.requireNonNullElse(errorMessage, "Unknown Error"), null));
        }
        LOGGER.debug("→ createTestConnectionResponse: Response created successfully");
        return result;
    }

    private String createStateParameter(MustAuthorizeException cause, Office365CalendarAttributes office365CalendarAttributes) {
        LOGGER.debug("→ createStateParameter: Creating state parameter from exception details");
        String userId = (String) cause.getDetails().getFirst().getValue();
        LOGGER.debug("→ createStateParameter: Extracted userId: {}", userId);
        String result = createStateParameter(cause, office365CalendarAttributes, userId);
        LOGGER.debug("→ createStateParameter: State parameter created successfully");
        return result;
    }

    private String createStateParameter(MustAuthorizeException cause, Office365CalendarAttributes office365CalendarAttributes, String userId) {
        LOGGER.debug("→ createStateParameter: Building state parameter for userId: {}", userId);
        Optional<NamedValuedField> authContextIdField = cause.getDetails().stream()
                .filter(namedValuedField -> Objects.equals(namedValuedField.getName(), Constants.AUTH_CONTEXT_ID))
                .findFirst();
        LOGGER.debug("→ createStateParameter: AuthContextId field present: {}", authContextIdField.isPresent());
        String state = userId;
        if (authContextIdField.isPresent()) {
            String authContextId = (String) authContextIdField.get().getValue();
            LOGGER.debug("→ createStateParameter: Adding authContextId: {}", authContextId);
            state += Constants.HASH + authContextId;
        }
        if (Constants.PUBLIC.equals(office365CalendarAttributes.getAuthType())) {
            LOGGER.debug("→ createStateParameter: PUBLIC auth type, adding forward path");
            state += Constants.HASH + office365CalendarAttributes.getForwardPath();
        }
        LOGGER.debug("→ createStateParameter: Final state parameter: {}", state);
        return state;
    }
}
