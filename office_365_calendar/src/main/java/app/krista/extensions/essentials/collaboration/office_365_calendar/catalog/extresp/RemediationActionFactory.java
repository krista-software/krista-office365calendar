package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp;

import app.krista.extension.executor.RemediationAction;
import app.krista.extension.executor.impl.AskAPersonAction;
import app.krista.extension.executor.impl.InformAPersonAction;
import app.krista.model.field.NamedField;
import app.krista.model.field.NamedValuedField;

import java.util.List;

/**
 * Utility class providing static factory methods for creating {@link RemediationAction} instances
 * for use in Office 365 calendar extension workflows.
 * <p>
 * This class simplifies the creation of common remediation actions, such as prompting the active user
 * for input or informing participants, by encapsulating the construction logic for different action types.
 * </p>
 */

public class RemediationActionFactory {

    public static RemediationAction createAskAction(String message,
                                                    List<NamedField> fields) {
        return AskAPersonAction.create(message, RemediationAction.RecipientType.ACTIVE_USER, fields);
    }

    public static RemediationAction createInformAction(String message,
                                                       List<NamedValuedField> fields) {
        return InformAPersonAction.create(message, RemediationAction.RecipientType.ACTIVE_USER, fields);
    }

    public static RemediationAction createInformActionALLParticipants(String message,
                                                                      List<NamedValuedField> fields) {
        return InformAPersonAction.create(message, RemediationAction.RecipientType.ALL_PARTICIPANTS, fields);
    }
}