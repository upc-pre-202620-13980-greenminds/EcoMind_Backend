package pe.greenminds.ecomind.gamification.application.queryservices;

import java.util.List;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.UserProgress;
import pe.greenminds.ecomind.gamification.domain.model.entities.RewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

public interface GamificationQueryService {
  UserProgress getUserProgress(UserId userId);

  List<RewardTransaction> getRecentRewards(UserId userId);
}
