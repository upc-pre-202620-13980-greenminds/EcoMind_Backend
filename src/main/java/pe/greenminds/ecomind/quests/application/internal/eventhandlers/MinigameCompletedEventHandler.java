package pe.greenminds.ecomind.quests.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.domain.model.events.MinigameCompletedEvent;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.application.outboundservices.QuestEventPublisher;
import pe.greenminds.ecomind.quests.interfaces.acl.events.MinigameCompletedIntegrationEvent;

@Service
public class MinigameCompletedEventHandler {
    private final QuestRepository questRepository;
    private final QuestEventPublisher questEventPublisher;

    public MinigameCompletedEventHandler(
            QuestRepository questRepository,
            QuestEventPublisher questEventPublisher
    ) {
        this.questRepository = questRepository;
        this.questEventPublisher = questEventPublisher;
    }

    @EventListener
    public void on(MinigameCompletedEvent event) {
        questRepository.findById(event.questId()).ifPresent(quest ->
                questEventPublisher.publish(new MinigameCompletedIntegrationEvent(
                        IntegrationEventId.forCompletion("minigame-attempt", event.attemptId()),
                        event.attemptId(), quest.getId(), quest.getVersionGroupId(),
                        quest.getVersionNumber(), event.minigameId(), event.userId(), event.score(),
                        quest.getGems(), quest.getEcopoints(), event.completedAt()
                ))
        );
    }
}
