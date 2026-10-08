package pe.greenminds.ecomind.gamification.domain.model.events;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementShareRequest;

public record AchievementSharedEvent(AchievementShareRequest request) {}
