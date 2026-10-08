package pe.greenminds.ecomind.gamification.application.queryservices;

import pe.greenminds.ecomind.gamification.domain.model.queries.GetRewardTransactionsQuery;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPage;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardHistoryEntry;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface RewardQueryService {
    Result<RankingPage<RewardHistoryEntry>, ApplicationError> handle(
            GetRewardTransactionsQuery query);
}
