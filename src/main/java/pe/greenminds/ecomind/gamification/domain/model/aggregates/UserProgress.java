package pe.greenminds.ecomind.gamification.domain.model.aggregates;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

/** Gamification-owned score and daily streak. */
public class UserProgress {

  private final UserId userId;
  private long totalEcopoints;
  private long totalExperience;
  private int currentStreak;
  private int longestStreak;
  private LocalDate lastActivityDate;

  public UserProgress(
      UserId userId,
      long totalEcopoints,
      long totalExperience,
      int currentStreak,
      int longestStreak,
      LocalDate lastActivityDate) {
    if (totalEcopoints < 0 || totalExperience < 0 || currentStreak < 0
        || longestStreak < currentStreak) {
      throw new IllegalArgumentException("Progress values are invalid");
    }
    this.userId = java.util.Objects.requireNonNull(userId);
    this.totalEcopoints = totalEcopoints;
    this.totalExperience = totalExperience;
    this.currentStreak = currentStreak;
    this.longestStreak = longestStreak;
    this.lastActivityDate = lastActivityDate;
  }

  public static UserProgress empty(UserId userId) {
    return new UserProgress(userId, 0, 0, 0, 0, null);
  }

  public void applyReward(Reward reward, LocalDate activityDate, boolean countsForDailyStreak) {
    totalEcopoints = Math.addExact(totalEcopoints, reward.ecopoints());
    totalExperience = Math.addExact(totalExperience, reward.experience());
    if (!countsForDailyStreak ||
        (lastActivityDate != null && !activityDate.isAfter(lastActivityDate))) {
      return;
    }
    boolean consecutive = lastActivityDate != null
        && ChronoUnit.DAYS.between(lastActivityDate, activityDate) == 1;
    currentStreak = consecutive ? Math.addExact(currentStreak, 1) : 1;
    longestStreak = Math.max(longestStreak, currentStreak);
    lastActivityDate = activityDate;
  }

  public UserId getUserId() { return userId; }
  public long getTotalEcopoints() { return totalEcopoints; }
  public long getTotalExperience() { return totalExperience; }
  public int getCurrentStreak() { return currentStreak; }
  public int getLongestStreak() { return longestStreak; }
  public LocalDate getLastActivityDate() { return lastActivityDate; }
}
