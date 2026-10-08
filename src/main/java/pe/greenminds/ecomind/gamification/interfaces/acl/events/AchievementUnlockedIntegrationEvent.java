package pe.greenminds.ecomind.gamification.interfaces.acl.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Individual achievement notice to Community, not a request to create a publication. */
public record AchievementUnlockedIntegrationEvent(
        UUID eventId, UUID awardId, UUID achievementId, Long userId, Instant occurredAt) {
    public AchievementUnlockedIntegrationEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(awardId);
        Objects.requireNonNull(achievementId);
        Objects.requireNonNull(occurredAt);
        if (userId == null || userId <= 0)
            throw new IllegalArgumentException("User id must be positive");
    }
}
