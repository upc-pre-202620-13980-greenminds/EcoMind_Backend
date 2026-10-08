package pe.greenminds.ecomind.quests.domain.services;

import pe.greenminds.ecomind.quests.interfaces.events.CollaborativeQuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.FamilyPlanCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.MinigameCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.QuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.CollaborativeQuestInvitationSentIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.CollaborativeQuestInvitationAcceptedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.CollaborativeQuestInvitationRejectedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.CollaborativeQuestStartedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.FamilyPlanActivatedIntegrationEvent;

public interface QuestEventPublisher {
    void publish(QuestCompletedIntegrationEvent event);
    void publish(MinigameCompletedIntegrationEvent event);
    void publish(CollaborativeQuestCompletedIntegrationEvent event);
    void publish(FamilyPlanCompletedIntegrationEvent event);
    void publish(CollaborativeQuestInvitationSentIntegrationEvent event);
    void publish(CollaborativeQuestInvitationAcceptedIntegrationEvent event);
    void publish(CollaborativeQuestInvitationRejectedIntegrationEvent event);
    void publish(CollaborativeQuestStartedIntegrationEvent event);
    void publish(FamilyPlanActivatedIntegrationEvent event);
}
