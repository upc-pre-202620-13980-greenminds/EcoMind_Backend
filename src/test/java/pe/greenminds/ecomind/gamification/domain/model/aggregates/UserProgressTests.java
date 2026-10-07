package pe.greenminds.ecomind.gamification.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

class UserProgressTests {
  private final LocalDate monday = LocalDate.of(2026, 10, 5);

  @Test
  void rewardsAccumulateButOnlyOneEligibleActivityCountsPerDay() {
    var progress = UserProgress.empty(new UserId(42L));
    progress.applyReward(new Reward(10, 5, 0), monday, true);
    progress.applyReward(new Reward(20, 7, 0), monday, true);
    progress.applyReward(new Reward(3, 1, 0), monday.plusDays(1), false);
    progress.applyReward(new Reward(4, 2, 0), monday.plusDays(1), true);

    assertEquals(37, progress.getTotalEcopoints());
    assertEquals(15, progress.getTotalExperience());
    assertEquals(2, progress.getCurrentStreak());
    assertEquals(monday.plusDays(1), progress.getLastActivityDate());
  }

  @Test
  void missedDayRestartsStreakButKeepsRecordAndLateRewardCannotRewindIt() {
    var progress = UserProgress.empty(new UserId(42L));
    progress.applyReward(new Reward(1, 1, 0), monday, true);
    progress.applyReward(new Reward(1, 1, 0), monday.plusDays(1), true);
    progress.applyReward(new Reward(1, 1, 0), monday.plusDays(3), true);
    progress.applyReward(new Reward(1, 1, 0), monday.plusDays(2), true);

    assertEquals(1, progress.getCurrentStreak());
    assertEquals(2, progress.getLongestStreak());
    assertEquals(monday.plusDays(3), progress.getLastActivityDate());
    assertEquals(4, progress.getTotalEcopoints());
  }

  @Test
  void negativeRewardsAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Reward(-1, 0, 0));
  }
}
