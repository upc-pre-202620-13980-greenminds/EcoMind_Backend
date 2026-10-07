package pe.greenminds.ecomind.quests.application.internal.submission;

import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;

import java.util.Map;

@Component
public class CheckboxActivitySubmissionHandler implements ActivitySubmissionHandler {
    @Override
    public ActivityType supportedType() {
        return ActivityType.CHECKBOX;
    }

    @Override
    public void submit(
            ActivityUser activityUser,
            Map<String, Object> data
    ) {
        var checked = data.get("checked");
        if (!(checked instanceof Boolean checkedValue)) {
            throw new IllegalArgumentException("'checked' must be boolean");
        }

        activityUser.updateProgress(checkedValue ? 100.0 : 0.0);
    }
}
