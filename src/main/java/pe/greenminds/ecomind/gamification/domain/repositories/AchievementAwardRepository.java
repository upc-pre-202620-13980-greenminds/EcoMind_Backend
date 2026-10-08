package pe.greenminds.ecomind.gamification.domain.repositories;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementAwardRepository {
    boolean addIfAbsent(AchievementAward award);

    Optional<AchievementAward> findById(UUID id);

    List<AchievementAward> findByCommunity(Long communityId, int page, int size);

    List<AchievementAward> findByBeneficiary(
            AchievementScope scope, Long beneficiaryId, int page, int size);
}
