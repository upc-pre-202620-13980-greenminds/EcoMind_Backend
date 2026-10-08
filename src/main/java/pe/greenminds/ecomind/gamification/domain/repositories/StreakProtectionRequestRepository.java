package pe.greenminds.ecomind.gamification.domain.repositories;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.StreakProtectionRequest;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface StreakProtectionRequestRepository {
    Optional<StreakProtectionRequest> find(UUID id);

    Optional<StreakProtectionRequest> find(UserId user, LocalDate date);

    Optional<StreakProtectionRequest> lock(UUID id);

    boolean hasPendingBefore(UserId user, LocalDate date);

    StreakProtectionRequest save(StreakProtectionRequest request);
}
