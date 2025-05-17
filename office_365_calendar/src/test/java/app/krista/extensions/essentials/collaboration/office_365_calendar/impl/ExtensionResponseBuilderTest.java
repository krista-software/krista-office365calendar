package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.RemediationAction;
import app.krista.extension.executor.RemediationActions;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.ExtensionResponseBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ExtensionResponseBuilderTest {

    @Test
    public void testBuildSuccessResponse() {
        Map<String, Object> values = new HashMap<>();
        values.put("key1", "value1");

        ExtensionResponse response = new ExtensionResponseBuilder()
                .result(ExtensionResponse.Result.SUCCESS)
                .values(values)
                .build();

        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        assertEquals("value1", response.getResponseValue().get("key1"));
        assertNull(response.getError());
        assertNull(response.getRemediationActions());
        assertNull(response.getState());
    }

    @Test
    public void testBuildFailureWithErrorAndException() {
        Exception cause = new RuntimeException("Something went wrong");

        ExtensionResponse response = new ExtensionResponseBuilder()
                .result(ExtensionResponse.Result.FAILURE)
                .error(cause, "Runtime error", ExtensionResponse.Error.ExceptionType.LOGIC_ERROR)
                .build();

        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
        assertNotNull(response.getError());
        assertEquals("Runtime error", response.getError().getErrorMessage());
        assertEquals(ExtensionResponse.Error.ExceptionType.LOGIC_ERROR, response.getError().getExceptionType());
        assertTrue(response.getError().getStackTrace().contains("at"));
    }

    @Test
    public void testBuildFailureWithErrorMessageOnly() {
        ExtensionResponse response = new ExtensionResponseBuilder()
                .result(ExtensionResponse.Result.FAILURE)
                .error("Simple error", ExtensionResponse.Error.ExceptionType.INPUT_ERROR)
                .build();

        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
        assertNotNull(response.getError());
        assertEquals("Simple error", response.getError().getErrorMessage());
        assertEquals(ExtensionResponse.Error.ExceptionType.INPUT_ERROR, response.getError().getExceptionType());
        assertEquals("", response.getError().getStackTrace());
    }

    @Test
    public void testBuildWithRemediationActions() {
        List<RemediationAction> actions = getRemediationActions();
        String subCatalogRequestName = "reauth";

        ExtensionResponse response = new ExtensionResponseBuilder()
                .remediationActions(actions, subCatalogRequestName)
                .build();

        RemediationActions remediationActions = response.getRemediationActions();
        assertNotNull(remediationActions);
        assertEquals("reauth", remediationActions.getSubCatalogRequestName());

        assertEquals(1, remediationActions.getRemediationActionList().size());
        assertEquals("Please reauthenticate", remediationActions.getRemediationActionList().getFirst().getMessage());
    }

    @NotNull
    private static List<RemediationAction> getRemediationActions() {
        RemediationAction mockAction = new RemediationAction() {
            @Override
            public String getMessage() {
                return "Please reauthenticate";
            }

            @Override
            public RecipientType getRecipientType() {
                return RecipientType.ACTIVE_USER;
            }

            @Override
            public int compareTo(RemediationAction o) {
                return this.getMessage().compareTo(o.getMessage());
            }
        };

        return Collections.singletonList(mockAction);
    }


    @Test
    public void testBuildWithState() {
        Map<String, Object> state = new HashMap<>();
        state.put("step", "confirmation");

        ExtensionResponse response = new ExtensionResponseBuilder()
                .state(state)
                .build();

        assertNotNull(response.getState());
        assertEquals("confirmation", response.getState().get("step"));
    }
}
