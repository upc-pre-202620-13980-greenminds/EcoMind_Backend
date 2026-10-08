package pe.greenminds.ecomind.quests.domain.model.events;
import java.time.OffsetDateTime;
public record CollaborativeQuestInvitationAcceptedEvent(Long memberId, Long sessionId, Long userId, Long ownerUserId, OffsetDateTime occurredAt) {}
