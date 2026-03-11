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


package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.RemediationAction;
import app.krista.extension.executor.RemediationActions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Builder for constructing {@link ExtensionResponse} objects in a flexible and readable manner.
 * Allows step-by-step configuration of all ExtensionResponse fields.
 */
public class ExtensionResponseBuilder {

    private ExtensionResponse.Result result = ExtensionResponse.Result.SUCCESS;
    private Map<String, Object> values;
    private ExtensionResponse.Error error;
    private RemediationActions remediationActions;
    private Map<String, Object> state;

    public ExtensionResponseBuilder result(ExtensionResponse.Result result) {
        this.result = result;
        return this;
    }

    public ExtensionResponseBuilder values(Map<String, Object> values) {
        this.values = values;
        return this;
    }

    public ExtensionResponseBuilder error(Exception cause, String message, ExtensionResponse.Error.ExceptionType exceptionType) {
        this.error = new ExtensionResponse.Error(
                message,
                System.currentTimeMillis(),
                exceptionType,
                Arrays.toString(cause.getStackTrace())
        );
        return this;
    }

    public ExtensionResponseBuilder error(String message, ExtensionResponse.Error.ExceptionType exceptionType) {
        this.error = new ExtensionResponse.Error(
                message,
                System.currentTimeMillis(),
                exceptionType,
                ""
        );
        return this;
    }

    public ExtensionResponseBuilder remediationActions(List<RemediationAction> actions, String subCatalogRequestName) {
        this.remediationActions = new RemediationActions(actions, subCatalogRequestName);
        return this;
    }

    public ExtensionResponseBuilder remediationActions(RemediationActions remediationActions) {
        this.remediationActions = remediationActions;
        return this;
    }

    public ExtensionResponseBuilder state(Map<String, Object> state) {
        this.state = state;
        return this;
    }

    public ExtensionResponse build() {
        return new ExtensionResponse(result, values, error, remediationActions, state);
    }
}
