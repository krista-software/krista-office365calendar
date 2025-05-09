package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EventIdValidator implements Validator {

    private final MeetingDaoImpl meetingDao;

    private static final Logger logger = LoggerFactory.getLogger(EventIdValidator.class);

    public EventIdValidator(MeetingDaoImpl meetingDao) {
        this.meetingDao = meetingDao;
    }

    @Override
    public Boolean validate(String eventId, Map<ValidationResource, String> context) {
        try {
            Meeting meeting = meetingDao.getMeetingById(eventId);
            return meeting != null;
        } catch (MustAuthorizeException cause) {
            logger.error("Exception thrown while authorizing user : {} {}", cause.getMessage(), cause);
            throw cause;
        } catch (Exception cause) {
            logger.error("Exception thrown while validating Event ID: {}", cause.getMessage(), cause);
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return Constants.EVENT_ID;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Event ID.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Event ID: %s does not exist.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid Event ID: %s", resourceId);
    }
}

