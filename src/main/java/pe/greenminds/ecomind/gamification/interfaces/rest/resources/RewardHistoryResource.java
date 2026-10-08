package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/** Preserves the public history JSON without serializing domain value objects. */
public record RewardHistoryResource(
        UUID id,
        SourceResource source,
        BeneficiaryResource beneficiary,
        RewardResource baseReward,
        RewardResource grantedReward,
        Instant occurredAt) {
    public record SourceResource(String type, UUID executionId) {}

    public record BeneficiaryResource(String type, Long id) {}

    public record RewardResource(long ecopoints, int gems) {}
}
