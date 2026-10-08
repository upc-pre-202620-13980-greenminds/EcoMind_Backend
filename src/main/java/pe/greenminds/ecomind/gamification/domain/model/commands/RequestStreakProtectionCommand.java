package pe.greenminds.ecomind.gamification.domain.model.commands;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.time.LocalDate;

public record RequestStreakProtectionCommand(
        UserId userId, LocalDate streakDate, Instant occurredAt) {}
