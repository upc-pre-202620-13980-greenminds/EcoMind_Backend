package pe.greenminds.ecomind.quests.interfaces.acl.events;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
public record CollaborativeQuestStartedIntegrationEvent(UUID eventId, Long sessionId, Long questId, Long ownerUserId, List<Long> participantUserIds, OffsetDateTime occurredAt) {
    public CollaborativeQuestStartedIntegrationEvent { participantUserIds = List.copyOf(participantUserIds); }
}
