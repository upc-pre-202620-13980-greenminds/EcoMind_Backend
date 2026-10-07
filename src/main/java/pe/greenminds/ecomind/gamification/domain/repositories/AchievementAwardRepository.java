package pe.greenminds.ecomind.gamification.domain.repositories;

import java.util.List;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;

public interface AchievementAwardRepository {
  void addIfAbsent(AchievementAward award);
  List<AchievementAward> findByBeneficiary(AchievementScope scope, Long beneficiaryId, int page, int size);
}
