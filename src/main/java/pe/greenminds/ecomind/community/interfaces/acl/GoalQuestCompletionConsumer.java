package pe.greenminds.ecomind.community.interfaces.acl;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.community.application.commandservices.CommunityGoalCommandService;
import pe.greenminds.ecomind.community.domain.model.commands.RecordGoalQuestCompletionCommand;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalTopic;
import pe.greenminds.ecomind.quests.interfaces.acl.events.QuestFinishedForGoalsIntegrationEvent;

@Component
public class GoalQuestCompletionConsumer {

    private final CommunityGoalCommandService commands;

    public GoalQuestCompletionConsumer(CommunityGoalCommandService commands) {
        this.commands = commands;
    }

    @EventListener
    public void on(QuestFinishedForGoalsIntegrationEvent event) {
        commands.handle(new RecordGoalQuestCompletionCommand(
                event.sourceKey(),
                event.userIds(),
                CommunityGoalTopic.fromValue(event.topic()),
                event.completedAt()));
    }
}