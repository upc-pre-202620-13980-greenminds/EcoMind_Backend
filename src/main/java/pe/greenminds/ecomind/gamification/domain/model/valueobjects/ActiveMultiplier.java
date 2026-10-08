package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ActiveMultiplier(UUID id, BigDecimal factor, Instant startsAt, Instant expiresAt) {
    public ActiveMultiplier(BigDecimal factor, Instant startsAt, Instant expiresAt) {
        this(UUID.randomUUID(), factor, startsAt, expiresAt);
    }

    public ActiveMultiplier {
        Objects.requireNonNull(id);
        Objects.requireNonNull(factor);
        Objects.requireNonNull(startsAt);
        Objects.requireNonNull(expiresAt);
        if (factor.compareTo(BigDecimal.ONE) < 0 || !startsAt.isBefore(expiresAt))
            throw new IllegalArgumentException("Invalid multiplier");
    }

    public boolean isActiveAt(Instant at) {
        return !at.isBefore(startsAt) && at.isBefore(expiresAt);
    }
}
