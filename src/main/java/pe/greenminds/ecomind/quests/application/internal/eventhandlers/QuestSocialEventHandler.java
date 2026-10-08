package pe.greenminds.ecomind.quests.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.domain.model.events.*;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanItemRepository;
import pe.greenminds.ecomind.quests.application.outboundservices.QuestEventPublisher;
import pe.greenminds.ecomind.quests.interfaces.acl.events.*;

import java.util.List;

@Service
public class QuestSocialEventHandler {
    private final CollabQuestMemberRepository memberRepository;
    private final FamilyPlanItemRepository familyPlanItemRepository;
    private final QuestEventPublisher publisher;

    public QuestSocialEventHandler(CollabQuestMemberRepository memberRepository,
                                   FamilyPlanItemRepository familyPlanItemRepository,
                                   QuestEventPublisher publisher) {
        this.memberRepository = memberRepository;
        this.familyPlanItemRepository = familyPlanItemRepository;
        this.publisher = publisher;
    }

    @EventListener
    public void on(CollaborativeQuestInvitationSentEvent event) {
        publisher.publish(new CollaborativeQuestInvitationSentIntegrationEvent(
                IntegrationEventId.forCompletion("collab-invitation-sent", event.memberId()),
                event.memberId(), event.sessionId(), event.invitedUserId(), event.ownerUserId(), event.occurredAt()));
    }

    @EventListener
    public void on(CollaborativeQuestInvitationAcceptedEvent event) {
        publisher.publish(new CollaborativeQuestInvitationAcceptedIntegrationEvent(
                IntegrationEventId.forCompletion("collab-invitation-accepted", event.memberId()),
                event.memberId(), event.sessionId(), event.userId(), event.ownerUserId(), event.occurredAt()));
    }

    @EventListener
    public void on(CollaborativeQuestInvitationRejectedEvent event) {
        publisher.publish(new CollaborativeQuestInvitationRejectedIntegrationEvent(
                IntegrationEventId.forCompletion("collab-invitation-rejected", event.memberId()),
                event.memberId(), event.sessionId(), event.userId(), event.ownerUserId(), event.occurredAt()));
    }

    @EventListener
    public void on(CollaborativeQuestStartedEvent event) {
        publisher.publish(new CollaborativeQuestStartedIntegrationEvent(
                IntegrationEventId.forCompletion("collaborative-session-started", event.sessionId()),
                event.sessionId(), event.questId(), event.ownerUserId(),
                acceptedUsers(event.sessionId()), event.occurredAt()));
    }

    @EventListener
    public void on(FamilyPlanActivatedEvent event) {
        var participants = familyPlanItemRepository.findByFamilyPlanId(event.familyPlanId()).stream()
                .filter(item -> item.getCollaborativeSessionId() != null)
                .flatMap(item -> acceptedUsers(item.getCollaborativeSessionId()).stream())
                .distinct().toList();
        publisher.publish(new FamilyPlanActivatedIntegrationEvent(
                IntegrationEventId.forCompletion("family-plan-activated", event.familyPlanId()),
                event.familyPlanId(), event.familyId(), event.ownerUserId(), participants, event.occurredAt()));
    }

    private List<Long> acceptedUsers(Long sessionId) {
        return memberRepository.findBySessionIdAndStatusIn(
                        sessionId, List.of(CollabMemberStatus.ACCEPTED)).stream()
                .map(member -> member.getUserId()).distinct().toList();
    }
}
