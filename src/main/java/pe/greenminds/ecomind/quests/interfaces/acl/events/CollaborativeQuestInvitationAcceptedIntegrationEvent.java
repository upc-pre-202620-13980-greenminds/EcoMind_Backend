package pe.greenminds.ecomind.quests.interfaces.acl.events;
import java.time.OffsetDateTime;
import java.util.UUID;
public record CollaborativeQuestInvitationAcceptedIntegrationEvent(UUID eventId, Long memberId, Long sessionId, Long userId, Long ownerUserId, OffsetDateTime occurredAt) {}
