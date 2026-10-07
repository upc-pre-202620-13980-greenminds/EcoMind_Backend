package pe.greenminds.ecomind.gamification.application.internal.queryservices;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.gamification.application.queryservices.AchievementQueryService;
import pe.greenminds.ecomind.gamification.application.outboundservices.UsersServiceClient;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementAwardRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
@Transactional(readOnly = true)
public class AchievementQueryServiceImpl implements AchievementQueryService {
  private final AchievementRepository catalog;
  private final AchievementAwardRepository awards;
  private final UsersServiceClient users;

  public AchievementQueryServiceImpl(AchievementRepository catalog, AchievementAwardRepository awards, UsersServiceClient users) {
    this.catalog = catalog;
    this.awards = awards;
    this.users = users;
  }

  public List<Achievement> search(AchievementScope scope, int page, int size) {
    validatePage(page, size);
    return catalog.search(scope, page, size);
  }

  public Result<Achievement, ApplicationError> find(UUID id) {
    return catalog.findById(id).<Result<Achievement, ApplicationError>>map(Result::success)
        .orElseGet(() -> Result.failure(ApplicationError.notFound("ACHIEVEMENT", id.toString())));
  }

  public List<AchievementAward> forUser(UserId userId, int page, int size) {
    validatePage(page, size);
    return awards.findByBeneficiary(AchievementScope.INDIVIDUAL, userId.value(), page, size);
  }

  public Result<List<AchievementAward>, ApplicationError> forFamily(FamilyId familyId, UserId requester, int page, int size) {
    validatePage(page, size);
    if (!users.isFamilyMember(familyId, requester)) {
      return Result.failure(ApplicationError.forbidden("FAMILY_ACHIEVEMENT_ACCESS_FORBIDDEN",
          "Only current family members can access family achievements."));
    }
    return Result.success(awards.findByBeneficiary(AchievementScope.FAMILY, familyId.value(), page, size));
  }

  private static void validatePage(int page, int size) {
    if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE)
      throw new IllegalArgumentException("Invalid page or size (maximum size: 100)");
  }
}
