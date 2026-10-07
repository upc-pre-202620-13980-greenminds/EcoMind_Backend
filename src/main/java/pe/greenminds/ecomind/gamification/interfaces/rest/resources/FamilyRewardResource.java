package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;
import pe.greenminds.ecomind.gamification.domain.model.entities.FamilyRewardTransaction;

public record FamilyRewardResource(UUID id, String sourceType, UUID sourceExecutionId,
    Long familyId, long ecopoints, Instant occurredAt) {
  public static FamilyRewardResource from(FamilyRewardTransaction transaction) {
    return new FamilyRewardResource(transaction.id(), "FAMILY_PLAN", transaction.sourceExecutionId(),
        transaction.familyId().value(), transaction.ecopoints(), transaction.occurredAt());
  }
}
