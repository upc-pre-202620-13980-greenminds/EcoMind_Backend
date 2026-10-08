package pe.greenminds.ecomind.gamification.application.queryservices;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.FamilyScore;
import pe.greenminds.ecomind.gamification.domain.model.entities.FamilyRewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetFamilyScoreQuery;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.util.List;

public interface FamilyScoreQueryService {
    default Result<FamilyScore, ApplicationError> handle(GetFamilyScoreQuery q) {
        return getScore(q.familyId(), q.requestedBy());
    }

    Result<FamilyScore, ApplicationError> getScore(FamilyId familyId, UserId requestedBy);

    Result<List<FamilyRewardTransaction>, ApplicationError> getRewards(
            FamilyId familyId, UserId requestedBy);
}
