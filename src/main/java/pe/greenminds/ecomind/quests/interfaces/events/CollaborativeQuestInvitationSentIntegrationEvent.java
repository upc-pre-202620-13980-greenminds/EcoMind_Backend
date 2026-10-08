package pe.greenminds.ecomind.quests.interfaces.events;
import java.time.OffsetDateTime;
import java.util.UUID;
public record CollaborativeQuestInvitationSentIntegrationEvent(UUID eventId, Long memberId, Long sessionId, Long invitedUserId, Long ownerUserId, OffsetDateTime occurredAt) {}
