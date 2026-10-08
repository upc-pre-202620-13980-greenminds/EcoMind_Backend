package pe.greenminds.ecomind.quests.infrastructure.events.spring;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.quests.interfaces.events.CollaborativeQuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.FamilyPlanCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.MinigameCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.events.QuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.domain.services.QuestEventPublisher;

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
}
