package pe.greenminds.ecomind.gamification.domain.model.aggregates;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.StreakProtectionStatus;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record StreakProtectionRequest(
        UUID id,
        UserId userId,
        LocalDate streakDate,
        StreakProtectionStatus status,
        Instant createdAt,
        Instant resolvedAt) {
    public StreakProtectionRequest(
            UUID id,
            UserId userId,
            LocalDate streakDate,
            StreakProtectionStatus status,
            Instant createdAt) {
        this(
                id,
                userId,
                streakDate,
                status,
                createdAt,
                status == StreakProtectionStatus.PENDING ? null : createdAt);
    }

    public StreakProtectionRequest {
        Objects.requireNonNull(id);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(streakDate);
        Objects.requireNonNull(status);
        Objects.requireNonNull(createdAt);
        if ((status != StreakProtectionStatus.PENDING) != (resolvedAt != null)
                || (resolvedAt != null && resolvedAt.isBefore(createdAt)))
            throw new IllegalArgumentException("Invalid protection resolution time");
    }

    public StreakProtectionRequest resolve(
            UserId user, LocalDate date, StreakProtectionStatus result, Instant resolvedAt) {
        if (!userId.equals(user)
                || !streakDate.equals(date)
                || result == StreakProtectionStatus.PENDING)
            throw new IllegalArgumentException("Protection result does not match its request");
        if (status != StreakProtectionStatus.PENDING && status != result)
            throw new IllegalStateException("Conflicting protection result");
        if (status != StreakProtectionStatus.PENDING) return this;
        return new StreakProtectionRequest(id, userId, streakDate, result, createdAt, resolvedAt);
    }
}
