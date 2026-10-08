package pe.greenminds.ecomind.gamification.domain.repositories;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardBeneficiary;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardHistoryEntry;

import java.util.List;

public interface RewardHistoryRepository {
    List<RewardHistoryEntry> find(
            RewardBeneficiary beneficiary, RankingPeriod period, int offset, int limit);
}
