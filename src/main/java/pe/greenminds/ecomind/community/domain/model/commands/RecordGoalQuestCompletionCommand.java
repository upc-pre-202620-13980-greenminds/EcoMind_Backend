package pe.greenminds.ecomind.community.domain.model.commands;

import java.time.Instant;
import java.util.List;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalTopic;

public record RecordGoalQuestCompletionCommand(
        String sourceKey,
        List<Long> userIds,
        CommunityGoalTopic topic,
        Instant completedAt) {

    public RecordGoalQuestCompletionCommand {
        userIds = List.copyOf(userIds);
    }
}