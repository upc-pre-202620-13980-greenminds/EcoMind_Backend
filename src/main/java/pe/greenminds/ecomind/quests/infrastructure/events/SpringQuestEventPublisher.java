package pe.greenminds.ecomind.quests.infrastructure.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.quests.application.outboundservices.QuestEventPublisher;
import pe.greenminds.ecomind.quests.interfaces.acl.events.*;

@Component
public class SpringQuestEventPublisher implements QuestEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringQuestEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(QuestCompletedIntegrationEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publish(MinigameCompletedIntegrationEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publish(CollaborativeQuestCompletedIntegrationEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publish(FamilyPlanCompletedIntegrationEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override public void publish(CollaborativeQuestInvitationSentIntegrationEvent event) { applicationEventPublisher.publishEvent(event); }
    @Override public void publish(CollaborativeQuestInvitationAcceptedIntegrationEvent event) { applicationEventPublisher.publishEvent(event); }
    @Override public void publish(CollaborativeQuestInvitationRejectedIntegrationEvent event) { applicationEventPublisher.publishEvent(event); }
    @Override public void publish(CollaborativeQuestStartedIntegrationEvent event) { applicationEventPublisher.publishEvent(event); }
    @Override public void publish(FamilyPlanActivatedIntegrationEvent event) { applicationEventPublisher.publishEvent(event); }
}
