package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

public enum AchievementMetric {
    ECOPOINTS,
    /** Legacy spelling of ECOPOINTS, normalized when loading a definition. */
    @Deprecated
    EXPERIENCE,
    LONGEST_STREAK,
    COMPLETED_COMMUNITY_GOALS,
    COMPLETED_FAMILY_PLANS
}
