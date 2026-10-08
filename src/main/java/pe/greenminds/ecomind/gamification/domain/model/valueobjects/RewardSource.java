package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record RewardSource(RewardSourceType type, UUID executionId) {
    public RewardSource {
        Objects.requireNonNull(type);
        Objects.requireNonNull(executionId);
    }
}
