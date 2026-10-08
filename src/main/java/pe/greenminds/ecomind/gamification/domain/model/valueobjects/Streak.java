package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

import java.time.LocalDate;
import java.util.Objects;

public record Streak(
        int current, int longest, LocalDate lastActivityDate, LocalDate lastProtectedDate) {
    public Streak {
        if (current < 0 || longest < current) throw new IllegalArgumentException("Invalid streak");
    }

    public LocalDate continuityDate() {
        return lastProtectedDate != null
                        && (lastActivityDate == null || lastProtectedDate.isAfter(lastActivityDate))
                ? lastProtectedDate
                : lastActivityDate;
    }

    public Streak recordDailyActivity(LocalDate date) {
        Objects.requireNonNull(date);
        if (lastActivityDate != null && !date.isAfter(lastActivityDate)) return this;
        var continuity = continuityDate();
        boolean consecutive =
                continuity != null
                        && (date.equals(continuity.plusDays(1)) || date.equals(lastProtectedDate));
        int next = consecutive ? Math.addExact(current, 1) : 1;
        return new Streak(next, Math.max(longest, next), date, lastProtectedDate);
    }

    public Streak protect(LocalDate date) {
        if (date.equals(lastProtectedDate)) return this;
        if (current == 0 || continuityDate() == null || !date.equals(continuityDate().plusDays(1)))
            throw new IllegalStateException(
                    "Protection requires the next missing day of an active streak");
        return new Streak(current, longest, lastActivityDate, date);
    }

    public Streak resetForMissedDay(LocalDate date) {
        return continuityDate() != null && continuityDate().isBefore(date)
                ? new Streak(0, longest, lastActivityDate, lastProtectedDate)
                : this;
    }
}
