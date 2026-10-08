package pe.greenminds.ecomind.quests.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.domain.model.events.CollaborativeQuestCompletedEvent;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.application.outboundservices.QuestEventPublisher;
import pe.greenminds.ecomind.quests.interfaces.acl.events.CollaborativeQuestCompletedIntegrationEvent;

import java.util.List;

@Service
public class CollaborativeQuestCompletedEventHandler {
    private final QuestRepository questRepository;
    private final CollabQuestMemberRepository collabQuestMemberRepository;
    private final QuestEventPublisher questEventPublisher;

    public CollaborativeQuestCompletedEventHandler(
            QuestRepository questRepository,
            CollabQuestMemberRepository collabQuestMemberRepository,
            QuestEventPublisher questEventPublisher
    ) {
        this.questRepository = questRepository;
        this.collabQuestMemberRepository = collabQuestMemberRepository;
        this.questEventPublisher = questEventPublisher;
    }

    @EventListener
    public void on(CollaborativeQuestCompletedEvent event) {
        questRepository.findById(event.questId()).ifPresent(quest -> {
            var participantIds = collabQuestMemberRepository.findBySessionIdAndStatusIn(
                            event.sessionId(), List.of(CollabMemberStatus.ACCEPTED))
                    .stream().map(member -> member.getUserId()).distinct().toList();
            questEventPublisher.publish(new CollaborativeQuestCompletedIntegrationEvent(
                    IntegrationEventId.forCompletion("collaborative-session", event.sessionId()),
                    event.sessionId(), quest.getId(), quest.getVersionGroupId(),
                    quest.getVersionNumber(), participantIds, quest.getGems(),
                    quest.getEcopoints(), event.completedAt()
            ));
        });
    }
}
