package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import com.google.gson.annotations.SerializedName;

/**
 * Response object for authentication operations.
 * Contains success status, error message, and optional authorization URL.
 */
public class AuthenticationResponse {
    @SerializedName("success")
    private final boolean success;
    private final String errorMessage;
    private final String url;

    public AuthenticationResponse(boolean success, String errorMessage, String url) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.url = url;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getUrl() {
        return url;
    }
}
