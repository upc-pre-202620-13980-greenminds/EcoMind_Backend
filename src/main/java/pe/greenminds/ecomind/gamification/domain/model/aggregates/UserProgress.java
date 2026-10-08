package pe.greenminds.ecomind.gamification.domain.model.aggregates;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Streak;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.services.StreakService;

import java.time.LocalDate;
import java.util.Objects;

/** Gamification-owned score and daily streak. */
public class UserProgress {

    private final UserId userId;
    private long totalEcopoints;
    private Streak streak;

    public UserProgress(
            UserId userId,
            long totalEcopoints,
            int currentStreak,
            int longestStreak,
            LocalDate lastActivityDate) {
        if (totalEcopoints < 0 || currentStreak < 0 || longestStreak < currentStreak) {
            throw new IllegalArgumentException("Progress values are invalid");
        }
        this.userId = Objects.requireNonNull(userId);
        this.totalEcopoints = totalEcopoints;
        this.streak = new Streak(currentStreak, longestStreak, lastActivityDate, null);
    }

    public static UserProgress empty(UserId userId) {
        return new UserProgress(userId, 0, 0, 0, null);
    }

    public void applyReward(Reward reward, LocalDate activityDate, boolean countsForDailyStreak) {
        totalEcopoints = Math.addExact(totalEcopoints, reward.ecopoints());
        if (countsForDailyStreak)
            streak = new StreakService().registerDailyActivity(streak, activityDate);
    }

    public void restoreProtectedDate(LocalDate date) {
        streak = new Streak(streak.current(), streak.longest(), streak.lastActivityDate(), date);
    }

    public LocalDate getLastProtectedDate() {
        return streak.lastProtectedDate();
    }

    public LocalDate getLastContinuityDate() {
        return streak.continuityDate();
    }

    public void protect(LocalDate date) {
        streak = new StreakService().confirmProtection(streak, date);
    }

    public void resetForMissedDay(LocalDate date) {
        streak = new StreakService().confirmUnavailable(streak, date);
    }

    public UserId getUserId() {
        return userId;
    }

    public long getTotalEcopoints() {
        return totalEcopoints;
    }

    public int getCurrentStreak() {
        return streak.current();
    }

    public int getLongestStreak() {
        return streak.longest();
    }

    public LocalDate getLastActivityDate() {
        return streak.lastActivityDate();
    }
}
