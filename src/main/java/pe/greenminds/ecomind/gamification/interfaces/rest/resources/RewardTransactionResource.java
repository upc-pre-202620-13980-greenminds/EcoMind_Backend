package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;
import pe.greenminds.ecomind.gamification.domain.model.entities.RewardTransaction;

public record RewardTransactionResource(
    UUID id,
    String sourceType,
    UUID sourceExecutionId,
    Long beneficiaryId,
    long ecopoints,
    long experience,
    int gems,
    Instant occurredAt) {
  public static RewardTransactionResource from(RewardTransaction transaction) {
    return new RewardTransactionResource(
        transaction.id(), transaction.sourceType().name(), transaction.sourceExecutionId(),
        transaction.beneficiary().value(),
        transaction.grantedReward().ecopoints(), transaction.grantedReward().experience(),
        transaction.grantedReward().gems(), transaction.occurredAt());
  }
}
