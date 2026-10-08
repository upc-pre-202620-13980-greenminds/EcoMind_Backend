package pe.greenminds.ecomind.quests.domain.model.events;
import java.time.OffsetDateTime;
public record CollaborativeQuestStartedEvent(Long sessionId, Long questId, Long ownerUserId, OffsetDateTime occurredAt) {}
