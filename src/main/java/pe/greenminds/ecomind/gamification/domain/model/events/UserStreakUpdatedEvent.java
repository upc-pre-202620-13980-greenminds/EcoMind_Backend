package pe.greenminds.ecomind.gamification.domain.model.events;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.LocalDate;

public record UserStreakUpdatedEvent(
        UserId userId, int currentStreak, int longestStreak, LocalDate activityDate) {}
