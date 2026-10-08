package pe.greenminds.ecomind.gamification.application.queryservices;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.RewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.UserProgress;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetUserProgressQuery;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.util.List;

public interface GamificationQueryService extends UserProgressQueryService {
    default UserProgress handle(GetUserProgressQuery query) {
        return getUserProgress(query.userId());
    }

    UserProgress getUserProgress(UserId userId);

    List<RewardTransaction> getRecentRewards(UserId userId);
}
