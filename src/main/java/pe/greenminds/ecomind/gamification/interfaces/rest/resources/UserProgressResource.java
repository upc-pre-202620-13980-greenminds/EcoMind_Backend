package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.time.LocalDate;

public record UserProgressResource(
        Long userId,
        long totalEcopoints,
        int currentStreak,
        int longestStreak,
        LocalDate lastActivityDate,
        LocalDate lastProtectedDate) {}
