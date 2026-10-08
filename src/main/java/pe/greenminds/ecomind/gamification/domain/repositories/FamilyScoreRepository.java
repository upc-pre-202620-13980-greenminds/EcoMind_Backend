package pe.greenminds.ecomind.gamification.domain.repositories;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.FamilyScore;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;

import java.util.Optional;

public interface FamilyScoreRepository {
    FamilyScore lockForReward(FamilyId familyId);

    void save(FamilyScore score);

    Optional<FamilyScore> findByFamilyId(FamilyId familyId);
}
