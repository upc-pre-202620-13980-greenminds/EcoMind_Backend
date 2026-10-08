package pe.greenminds.ecomind.quests.domain.model.events;
import java.time.OffsetDateTime;
public record CollaborativeQuestInvitationSentEvent(Long memberId, Long sessionId, Long invitedUserId, Long ownerUserId, OffsetDateTime occurredAt) {}
