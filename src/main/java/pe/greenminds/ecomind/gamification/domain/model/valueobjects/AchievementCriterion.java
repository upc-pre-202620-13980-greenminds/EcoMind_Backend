package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

import java.util.Objects;

public record AchievementCriterion(AchievementMetric metric, long target) {
    public AchievementCriterion {
        Objects.requireNonNull(metric);
        if (target <= 0) throw new IllegalArgumentException("Target must be positive");
    }

    public boolean isSatisfiedBy(long value) {
        return value >= target;
    }
}
