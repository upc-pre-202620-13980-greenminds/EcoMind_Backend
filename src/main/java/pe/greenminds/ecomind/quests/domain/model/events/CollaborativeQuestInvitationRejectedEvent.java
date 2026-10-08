package pe.greenminds.ecomind.quests.domain.model.events;
import java.time.OffsetDateTime;
public record CollaborativeQuestInvitationRejectedEvent(Long memberId, Long sessionId, Long userId, Long ownerUserId, OffsetDateTime occurredAt) {}
