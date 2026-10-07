package pe.greenminds.ecomind.gamification.domain.model.aggregates;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;

/** Scope and beneficiary together identify one recipient, even when numeric IDs overlap. */
public record AchievementAward(UUID id, UUID achievementId, AchievementScope scope,
    Long beneficiaryId, UUID sourceEventId, Instant awardedAt) {
  public AchievementAward {
    Objects.requireNonNull(id);
    Objects.requireNonNull(achievementId);
    Objects.requireNonNull(scope);
    Objects.requireNonNull(sourceEventId);
    Objects.requireNonNull(awardedAt);
    if (beneficiaryId == null || beneficiaryId <= 0)
      throw new IllegalArgumentException("Beneficiary id must be positive");
  }
}
