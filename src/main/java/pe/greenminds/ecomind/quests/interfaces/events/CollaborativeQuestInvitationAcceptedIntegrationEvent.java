package pe.greenminds.ecomind.quests.interfaces.events;
import java.time.OffsetDateTime;
import java.util.UUID;
public record CollaborativeQuestInvitationAcceptedIntegrationEvent(UUID eventId, Long memberId, Long sessionId, Long userId, Long ownerUserId, OffsetDateTime occurredAt) {}
