package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors;

import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Service for creating OAuth20Service instances for Office 365 Calendar authentication.
 */
public class OAuthService {
    private final Office365CalendarAttributes office365CalendarAttributes;

    public OAuthService(Office365CalendarAttributes office365CalendarAttributes) {
        this.office365CalendarAttributes = office365CalendarAttributes;
    }

    public OAuth20Service getOAuth20Service() {
        return office365CalendarAttributes.getOAuth20Service();
    }
}
