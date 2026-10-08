package pe.greenminds.ecomind.gamification.domain.model.events;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.StreakProtectionRequest;

public record DailyStreakAtRiskEvent(StreakProtectionRequest request) {}
