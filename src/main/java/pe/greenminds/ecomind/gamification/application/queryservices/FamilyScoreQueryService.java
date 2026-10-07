package pe.greenminds.ecomind.gamification.application.queryservices;

import java.util.List;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.FamilyScore;
import pe.greenminds.ecomind.gamification.domain.model.entities.FamilyRewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface FamilyScoreQueryService {
  Result<FamilyScore, ApplicationError> getScore(FamilyId familyId, UserId requestedBy);
  Result<List<FamilyRewardTransaction>, ApplicationError> getRewards(FamilyId familyId, UserId requestedBy);
}
