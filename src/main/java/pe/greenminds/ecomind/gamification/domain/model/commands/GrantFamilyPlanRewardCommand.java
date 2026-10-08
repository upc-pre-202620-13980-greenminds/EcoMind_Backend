package pe.greenminds.ecomind.gamification.domain.model.commands;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Validated plan completion with its configured additional family reward, not the sum of quests.
 */
public record GrantFamilyPlanRewardCommand(
        UUID sourceExecutionId, FamilyId familyId, long ecopoints, Instant occurredAt) {
    public GrantFamilyPlanRewardCommand {
        Objects.requireNonNull(sourceExecutionId);
        Objects.requireNonNull(familyId);
        Objects.requireNonNull(occurredAt);
        if (ecopoints < 0) throw new IllegalArgumentException("Ecopoints cannot be negative");
    }
}
