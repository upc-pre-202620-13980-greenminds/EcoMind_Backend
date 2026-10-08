package pe.greenminds.ecomind.gamification.domain.model.commands;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateUserStreakCommand(
        UserId userId, LocalDate activityDate, UUID executionId, Instant occurredAt) {}
