package pe.greenminds.ecomind.quests.application.outboundservices;

import pe.greenminds.ecomind.quests.interfaces.acl.events.*;

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
