package pe.greenminds.ecomind.quests.application.internal.submission;

import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

@Component
public class ActivitySubmissionHandlerRegistry {
    private final EnumMap<ActivityType, ActivitySubmissionHandler> handlers =
            new EnumMap<>(ActivityType.class);

    public ActivitySubmissionHandlerRegistry(List<ActivitySubmissionHandler> handlers) {
        for (var handler : handlers) {
            var previous = this.handlers.put(handler.supportedType(), handler);
            if (previous != null) {
                throw new IllegalStateException(
                        "Multiple submission handlers registered for " + handler.supportedType()
                );
            }
        }
    }

    public Optional<ActivitySubmissionHandler> findByType(ActivityType activityType) {
        return Optional.ofNullable(handlers.get(activityType));
    }
}
