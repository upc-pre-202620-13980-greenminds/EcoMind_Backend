package pe.greenminds.ecomind.gamification.domain.model.entities;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSourceType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

/** Immutable grant identified by canonical execution and beneficiary. */
public record RewardTransaction(
    UUID id,
    RewardSourceType sourceType,
    UUID sourceExecutionId,
    UserId beneficiary,
    Reward baseReward,
    Reward grantedReward,
    Instant occurredAt) {
  public RewardTransaction {
    Objects.requireNonNull(id);
    Objects.requireNonNull(sourceType);
    Objects.requireNonNull(sourceExecutionId);
    Objects.requireNonNull(beneficiary);
    Objects.requireNonNull(baseReward);
    Objects.requireNonNull(grantedReward);
    Objects.requireNonNull(occurredAt);
  }
}
