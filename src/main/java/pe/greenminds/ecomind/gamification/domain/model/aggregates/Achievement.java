package pe.greenminds.ecomind.gamification.domain.model.aggregates;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementCriterion;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;

import java.util.Objects;
import java.util.UUID;

/** Immutable catalog definition. Thresholds are configured, never inferred from reward amounts. */
public record Achievement(
        UUID id,
        String code,
        String name,
        String description,
        AchievementScope scope,
        AchievementMetric metric,
        long target,
        boolean active,
        UUID cosmeticId) {
    public Achievement(
            UUID id,
            String code,
            String name,
            String description,
            AchievementScope scope,
            AchievementMetric metric,
            long target,
            boolean active) {
        this(id, code, name, description, scope, metric, target, active, null);
    }

    public Achievement {
        Objects.requireNonNull(id);
        Objects.requireNonNull(scope);
        Objects.requireNonNull(metric);
        if (code == null || !code.matches("[A-Z0-9_]{1,80}"))
            throw new IllegalArgumentException(
                    "Achievement code must contain uppercase letters, digits or underscores");
        if (name == null || name.isBlank() || name.length() > 120)
            throw new IllegalArgumentException(
                    "Achievement name is required, up to 120 characters");
        if (description == null || description.isBlank() || description.length() > 500)
            throw new IllegalArgumentException(
                    "Achievement description is required, up to 500 characters");
        if (target <= 0) throw new IllegalArgumentException("Achievement target must be positive");
        if (cosmeticId != null && scope != AchievementScope.INDIVIDUAL)
            throw new IllegalArgumentException("Cosmetic rewards require an individual owner");
        if (scope == AchievementScope.COMMUNITY
                && metric != AchievementMetric.COMPLETED_COMMUNITY_GOALS)
            throw new IllegalArgumentException(
                    "Community achievements require a validated community criterion");
        if (scope == AchievementScope.INDIVIDUAL
                && metric == AchievementMetric.COMPLETED_FAMILY_PLANS)
            throw new IllegalArgumentException("Family plan criteria require a family beneficiary");
        if (scope == AchievementScope.FAMILY
                && metric != AchievementMetric.ECOPOINTS
                && metric != AchievementMetric.COMPLETED_FAMILY_PLANS)
            throw new IllegalArgumentException(
                    "Family achievements require ecopoints or completed family plans");
    }

    public AchievementCriterion criterion() {
        return new AchievementCriterion(metric, target);
    }

    public boolean isSatisfiedBy(long value) {
        return active && criterion().isSatisfiedBy(value);
    }
}
