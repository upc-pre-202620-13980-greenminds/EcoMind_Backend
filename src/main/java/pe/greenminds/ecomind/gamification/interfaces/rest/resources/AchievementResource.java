package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.util.UUID;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;

public record AchievementResource(UUID id, String code, String name, String description,
    String scope, String metric, long target, boolean active) {
  public static AchievementResource from(Achievement achievement) {
    return new AchievementResource(achievement.id(), achievement.code(), achievement.name(),
        achievement.description(), achievement.scope().name(), achievement.metric().name(),
        achievement.target(), achievement.active());
  }
}
