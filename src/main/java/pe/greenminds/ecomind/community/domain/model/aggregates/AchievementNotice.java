package pe.greenminds.ecomind.community.domain.model.aggregates;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AchievementNotice(UUID awardId, UUID achievementId, Long userId, Instant occurredAt) {
    public AchievementNotice {
        Objects.requireNonNull(awardId);
        Objects.requireNonNull(achievementId);
        Objects.requireNonNull(occurredAt);
        occurredAt = occurredAt.truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        if (userId == null || userId <= 0)
            throw new IllegalArgumentException("User must be positive");
    }
}
