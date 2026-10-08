package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record RewardTransactionResource(
        UUID id,
        String sourceType,
        UUID sourceExecutionId,
        Long beneficiaryId,
        long ecopoints,
        long experience,
        int gems,
        Instant occurredAt) {}
