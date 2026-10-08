package pe.greenminds.ecomind.gamification.domain.repositories;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementRepository {
    void add(Achievement achievement);

    Optional<Achievement> findById(UUID id);

    List<Achievement> findActive(AchievementScope scope);

    List<Achievement> search(AchievementScope scope, int page, int size);
}
