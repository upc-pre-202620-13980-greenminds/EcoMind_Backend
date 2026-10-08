package pe.greenminds.ecomind.quests.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.domain.model.events.QuestCompletedEvent;
import pe.greenminds.ecomind.quests.domain.model.events.IntegrationEventId;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.domain.services.QuestEventPublisher;
import pe.greenminds.ecomind.quests.interfaces.events.QuestCompletedIntegrationEvent;

@Service
public class QuestCompletedEventHandler {
    private final QuestRepository questRepository;
    private final QuestEventPublisher questEventPublisher;

    public QuestCompletedEventHandler(
            QuestRepository questRepository,
            QuestEventPublisher questEventPublisher
    ) {
        this.questRepository = questRepository;
        this.questEventPublisher = questEventPublisher;
    }

    @EventListener
    public void on(QuestCompletedEvent event) {
        if (event.collaborativeSessionId() != null) {
            return;
        }
        questRepository.findById(event.questId()).ifPresent(quest ->
                questEventPublisher.publish(new QuestCompletedIntegrationEvent(
                        IntegrationEventId.forCompletion("quest-user", event.questUserId()),
                        event.questUserId(), quest.getId(), quest.getVersionGroupId(),
                        quest.getVersionNumber(), event.userId(), quest.getCategory(),
                        quest.getType(), quest.getGems(), quest.getEcopoints(), event.completedAt()
                ))
        );
    }
}
