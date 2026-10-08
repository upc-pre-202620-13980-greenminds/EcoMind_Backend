package pe.greenminds.ecomind.quests.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.domain.model.events.FamilyPlanCompletedEvent;
import pe.greenminds.ecomind.quests.domain.model.events.IntegrationEventId;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanItemRepository;
import pe.greenminds.ecomind.quests.domain.services.QuestEventPublisher;
import pe.greenminds.ecomind.quests.interfaces.events.FamilyPlanCompletedIntegrationEvent;

import java.util.List;

@Service
public class FamilyPlanCompletedEventHandler {
    private final FamilyPlanItemRepository familyPlanItemRepository;
    private final CollabQuestMemberRepository collabQuestMemberRepository;
    private final QuestEventPublisher questEventPublisher;

    public FamilyPlanCompletedEventHandler(
            FamilyPlanItemRepository familyPlanItemRepository,
            CollabQuestMemberRepository collabQuestMemberRepository,
            QuestEventPublisher questEventPublisher
    ) {
        this.familyPlanItemRepository = familyPlanItemRepository;
        this.collabQuestMemberRepository = collabQuestMemberRepository;
        this.questEventPublisher = questEventPublisher;
    }

    @EventListener
    public void on(FamilyPlanCompletedEvent event) {
        var participantIds = familyPlanItemRepository.findByFamilyPlanId(event.familyPlanId())
                .stream()
                .filter(item -> item.getCollaborativeSessionId() != null)
                .flatMap(item -> collabQuestMemberRepository.findBySessionIdAndStatusIn(
                        item.getCollaborativeSessionId(), List.of(CollabMemberStatus.ACCEPTED)
                ).stream())
                .map(member -> member.getUserId())
                .distinct()
                .toList();
        questEventPublisher.publish(new FamilyPlanCompletedIntegrationEvent(
                IntegrationEventId.forCompletion("family-plan", event.familyPlanId()),
                event.familyPlanId(), event.familyId(), event.ownerUserId(), participantIds,
                event.completedAt()
        ));
    }
}
