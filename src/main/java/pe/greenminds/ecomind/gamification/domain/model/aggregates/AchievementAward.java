package pe.greenminds.ecomind.gamification.domain.model.aggregates;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Scope and beneficiary together identify one recipient, even when numeric IDs overlap. */
public record AchievementAward(
        UUID id,
        UUID achievementId,
        AchievementScope scope,
        Long beneficiaryId,
        UUID sourceEventId,
        Instant awardedAt,
        UUID communityId) {
    public AchievementAward(
            UUID id,
            UUID achievementId,
            AchievementScope scope,
            Long beneficiaryId,
            UUID sourceEventId,
            Instant awardedAt) {
        this(id, achievementId, scope, beneficiaryId, sourceEventId, awardedAt, null);
    }

    public AchievementAward {
        Objects.requireNonNull(id);
        Objects.requireNonNull(achievementId);
        Objects.requireNonNull(scope);
        Objects.requireNonNull(sourceEventId);
        Objects.requireNonNull(awardedAt);
        if (scope == AchievementScope.COMMUNITY
                ? communityId == null || beneficiaryId != null
                : communityId != null || beneficiaryId == null || beneficiaryId <= 0)
            throw new IllegalArgumentException("Beneficiary id must be positive");
    }
}
