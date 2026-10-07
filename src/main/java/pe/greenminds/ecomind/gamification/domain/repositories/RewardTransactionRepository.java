package pe.greenminds.ecomind.gamification.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pe.greenminds.ecomind.gamification.domain.model.entities.RewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSourceType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

public interface RewardTransactionRepository {
  Optional<RewardTransaction> findByOrigin(
      RewardSourceType sourceType, UUID sourceExecutionId, UserId beneficiary);

  RewardTransaction save(RewardTransaction rewardTransaction);

  List<RewardTransaction> findRecentByUser(UserId userId);
}
