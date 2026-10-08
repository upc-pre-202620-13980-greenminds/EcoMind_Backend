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
    private long totalExperience;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActivityDate;
    private LocalDate lastProtectedDate;

    public UserProgress(
            UserId userId,
            long totalEcopoints,
            long totalExperience,
            int currentStreak,
            int longestStreak,
            LocalDate lastActivityDate) {
        if (totalEcopoints < 0
                || totalExperience < 0
                || currentStreak < 0
                || longestStreak < currentStreak) {
            throw new IllegalArgumentException("Progress values are invalid");
        }
        this.userId = Objects.requireNonNull(userId);
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
        if (countsForDailyStreak)
            applyStreak(new StreakService().registerDailyActivity(streak(), activityDate));
    }

    private Streak streak() {
        return new Streak(currentStreak, longestStreak, lastActivityDate, lastProtectedDate);
    }

    private void applyStreak(Streak streak) {
        currentStreak = streak.current();
        longestStreak = streak.longest();
        lastActivityDate = streak.lastActivityDate();
        lastProtectedDate = streak.lastProtectedDate();
    }

    public void restoreProtectedDate(LocalDate date) {
        lastProtectedDate = date;
    }

    public LocalDate getLastProtectedDate() {
        return lastProtectedDate;
    }

    public LocalDate getLastContinuityDate() {
        return streak().continuityDate();
    }

    public void protect(LocalDate date) {
        applyStreak(new StreakService().confirmProtection(streak(), date));
    }

    public void resetForMissedDay(LocalDate date) {
        applyStreak(new StreakService().confirmUnavailable(streak(), date));
    }

    public UserId getUserId() {
        return userId;
    }

    public long getTotalEcopoints() {
        return totalEcopoints;
    }

    public long getTotalExperience() {
        return totalExperience;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }
}
