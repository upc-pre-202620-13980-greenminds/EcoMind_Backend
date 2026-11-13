package pe.greenminds.ecomind.quests.application.internal.submission;

import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;

import java.util.Map;

@Component
public class WriteActivitySubmissionHandler implements ActivitySubmissionHandler {
    @Override
    public ActivityType supportedType() {
        return ActivityType.WRITE;
    }

    @Override
    public void submit(ActivityUser activityUser, Map<String, Object> data) {
        if (data == null || !(data.get("text") instanceof String text)) {
            throw new IllegalArgumentException("'text' must be a string");
        }
        activityUser.submitWrittenResponse(text);
    }
}
