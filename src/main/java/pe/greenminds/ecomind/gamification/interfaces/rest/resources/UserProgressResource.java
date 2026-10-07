package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.time.LocalDate;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.UserProgress;

public record UserProgressResource(
    Long userId,
    long totalEcopoints,
    long totalExperience,
    int currentStreak,
    int longestStreak,
    LocalDate lastActivityDate) {
  public static UserProgressResource from(UserProgress progress) {
    return new UserProgressResource(
        progress.getUserId().value(), progress.getTotalEcopoints(),
        progress.getTotalExperience(), progress.getCurrentStreak(),
        progress.getLongestStreak(), progress.getLastActivityDate());
  }
}
