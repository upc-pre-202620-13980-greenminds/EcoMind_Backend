package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

import java.time.Instant;
import java.util.UUID;

public record RewardHistoryEntry(
        UUID id,
        RewardSource source,
        RewardBeneficiary beneficiary,
        Reward baseReward,
        Reward grantedReward,
        Instant occurredAt) {}
