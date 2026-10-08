package pe.greenminds.ecomind.quests.domain.services;

import pe.greenminds.ecomind.quests.interfaces.events.CollaborativeQuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.FamilyPlanCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.MinigameCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.QuestCompletedIntegrationEvent;

public interface QuestEventPublisher {
    void publish(QuestCompletedIntegrationEvent event);
    void publish(MinigameCompletedIntegrationEvent event);
    void publish(CollaborativeQuestCompletedIntegrationEvent event);
    void publish(FamilyPlanCompletedIntegrationEvent event);
}
