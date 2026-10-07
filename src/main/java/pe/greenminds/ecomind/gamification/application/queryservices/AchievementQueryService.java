package pe.greenminds.ecomind.gamification.application.queryservices;

import java.util.List;
import java.util.UUID;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface AchievementQueryService {
  List<Achievement> search(AchievementScope scope, int page, int size);
  Result<Achievement, ApplicationError> find(UUID id);
  List<AchievementAward> forUser(UserId userId, int page, int size);
  Result<List<AchievementAward>, ApplicationError> forFamily(FamilyId familyId, UserId requester, int page, int size);
}
