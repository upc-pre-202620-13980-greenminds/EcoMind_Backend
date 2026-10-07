package pe.greenminds.ecomind.gamification.domain.model.aggregates;

import java.util.Objects;
import java.util.UUID;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;

/** Immutable catalog definition. Thresholds are configured, never inferred from reward amounts. */
public record Achievement(UUID id, String code, String name, String description,
    AchievementScope scope, AchievementMetric metric, long target, boolean active) {
  public Achievement {
    Objects.requireNonNull(id);
    Objects.requireNonNull(scope);
    Objects.requireNonNull(metric);
    if (code == null || !code.matches("[A-Z0-9_]{1,80}"))
      throw new IllegalArgumentException("Achievement code must contain uppercase letters, digits or underscores");
    if (name == null || name.isBlank() || name.length() > 120)
      throw new IllegalArgumentException("Achievement name is required, up to 120 characters");
    if (description == null || description.isBlank() || description.length() > 500)
      throw new IllegalArgumentException("Achievement description is required, up to 500 characters");
    if (target <= 0) throw new IllegalArgumentException("Achievement target must be positive");
    if (scope == AchievementScope.COMMUNITY)
      throw new IllegalArgumentException("Community criteria require the Community integration contract");
    if (scope == AchievementScope.FAMILY && metric != AchievementMetric.ECOPOINTS)
      throw new IllegalArgumentException("Family achievements support ecopoints only");
  }

  public boolean isSatisfiedBy(long value) { return active && value >= target; }
}
