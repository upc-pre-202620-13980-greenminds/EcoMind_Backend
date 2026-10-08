package pe.greenminds.ecomind.gamification.domain.model.queries;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;

public record SearchAchievementsQuery(AchievementScope scope, int page, int size) {}
