package pe.greenminds.ecomind.gamification.domain.model.events;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;

import java.util.UUID;

public record AchievementUnlockedEvent(AchievementAward award, UUID cosmeticId) {}
