package pe.greenminds.ecomind.gamification.domain.repositories;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementShareRequest;

import java.util.Optional;
import java.util.UUID;

public interface AchievementShareRequestRepository {
    Optional<AchievementShareRequest> find(UUID id);

    Optional<AchievementShareRequest> lock(UUID id);

    AchievementShareRequest save(AchievementShareRequest request);
}
