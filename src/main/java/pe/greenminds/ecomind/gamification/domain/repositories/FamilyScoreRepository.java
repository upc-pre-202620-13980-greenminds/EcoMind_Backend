package pe.greenminds.ecomind.gamification.domain.repositories;

import java.util.Optional;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.FamilyScore;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;

public interface FamilyScoreRepository {
  FamilyScore lockForReward(FamilyId familyId);
  void save(FamilyScore score);
  Optional<FamilyScore> findByFamilyId(FamilyId familyId);
}
