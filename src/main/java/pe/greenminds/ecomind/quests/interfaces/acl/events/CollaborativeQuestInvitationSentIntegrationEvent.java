package pe.greenminds.ecomind.quests.interfaces.acl.events;
import java.time.OffsetDateTime;
import java.util.UUID;
public record CollaborativeQuestInvitationSentIntegrationEvent(UUID eventId, Long memberId, Long sessionId, Long invitedUserId, Long ownerUserId, OffsetDateTime occurredAt) {}
