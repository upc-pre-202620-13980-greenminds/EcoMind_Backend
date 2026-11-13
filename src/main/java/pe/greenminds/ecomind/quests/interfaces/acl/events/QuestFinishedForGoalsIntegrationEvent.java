package pe.greenminds.ecomind.quests.interfaces.acl.events;

import java.time.Instant;
import java.util.List;

public record QuestFinishedForGoalsIntegrationEvent(
        String sourceKey,
        List<Long> userIds,
        String topic,
        Instant completedAt) {

    public QuestFinishedForGoalsIntegrationEvent {
        userIds = List.copyOf(userIds);
    }
}