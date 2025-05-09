package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ValidationOrchestrator {

    private final Map<Validator.ValidationResource, Validator> validators = new HashMap<>();

    @Inject
    public ValidationOrchestrator(MeetingDaoImpl meetingDao) {
        validators.put(Validator.ValidationResource.EMAIL, new EmailValidator());
        validators.put(Validator.ValidationResource.EVENT_ID, new EventIdValidator(meetingDao));
    }

    public record ValidationResult(String confirmStepMessage, String fetchFieldName, String fetchStepMessage,
                                   String errMessage) {
    }

    public List<ValidationResult> validate(Map<Validator.ValidationResource, String> resources) {
        List<ValidationResult> results = new ArrayList<>();
        for (Map.Entry<Validator.ValidationResource, String> entry : resources.entrySet()) {
            Validator validator = validators.get(entry.getKey());
            if (validator != null && !validator.validate(entry.getValue(), resources)) {
                results.add(new ValidationResult(validator.getConfirmationStepMessage(entry.getValue(), resources),
                        validator.getFetchFieldName(), validator.getFetchStepMessage(),
                        validator.getErrMessage(entry.getValue())));
            }
        }
        return results;
    }
}
