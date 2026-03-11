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
