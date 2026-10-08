package pe.greenminds.ecomind.gamification.domain.model.commands;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.StreakProtectionStatus;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.LocalDate;
import java.util.UUID;

public record ResolveStreakProtectionCommand(
        UUID requestId, UserId userId, LocalDate streakDate, StreakProtectionStatus status) {}
