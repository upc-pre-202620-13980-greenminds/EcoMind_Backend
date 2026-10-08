package pe.greenminds.ecomind.gamification.domain.services;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Streak;

import java.time.LocalDate;

public class StreakService {
    public Streak registerDailyActivity(Streak streak, LocalDate date) {
        return streak.recordDailyActivity(date);
    }

    public Streak confirmProtection(Streak streak, LocalDate date) {
        return streak.protect(date);
    }

    public Streak confirmUnavailable(Streak streak, LocalDate date) {
        return streak.resetForMissedDay(date);
    }
}
