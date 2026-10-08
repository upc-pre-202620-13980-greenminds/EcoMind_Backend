package pe.greenminds.ecomind.gamification.domain.model.entities;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record FamilyRewardTransaction(
        UUID id, UUID sourceExecutionId, FamilyId familyId, long ecopoints, Instant occurredAt) {
    public FamilyRewardTransaction {
        Objects.requireNonNull(id);
        Objects.requireNonNull(sourceExecutionId);
        Objects.requireNonNull(familyId);
        Objects.requireNonNull(occurredAt);
        if (ecopoints < 0) throw new IllegalArgumentException("Ecopoints cannot be negative");
    }
}
