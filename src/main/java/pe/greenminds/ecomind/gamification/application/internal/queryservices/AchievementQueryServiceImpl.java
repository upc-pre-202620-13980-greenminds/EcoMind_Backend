package pe.greenminds.ecomind.gamification.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.outboundservices.CommunityServiceClient;
import pe.greenminds.ecomind.gamification.application.outboundservices.UsersServiceClient;
import pe.greenminds.ecomind.gamification.application.queryservices.AchievementQueryService;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementShareRequest;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementAwardRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementShareRequestRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AchievementQueryServiceImpl implements AchievementQueryService {
    private final AchievementRepository catalog;
    private final AchievementAwardRepository awards;
    private final UsersServiceClient users;
    private final CommunityServiceClient community;
    private final AchievementShareRequestRepository shares;

    public AchievementQueryServiceImpl(
            AchievementRepository catalog,
            AchievementAwardRepository awards,
            UsersServiceClient users,
            CommunityServiceClient community,
            AchievementShareRequestRepository shares) {
        this.catalog = catalog;
        this.awards = awards;
        this.users = users;
        this.community = community;
        this.shares = shares;
    }

    public List<Achievement> search(AchievementScope scope, int page, int size) {
        validatePage(page, size);
        return catalog.search(scope, page, size);
    }

    public Result<Achievement, ApplicationError> find(UUID id) {
        return catalog.findById(id)
                .<Result<Achievement, ApplicationError>>map(Result::success)
                .orElseGet(
                        () ->
                                Result.failure(
                                        ApplicationError.notFound("ACHIEVEMENT", id.toString())));
    }

    public List<AchievementAward> forUser(UserId userId, int page, int size) {
        validatePage(page, size);
        return awards.findByBeneficiary(AchievementScope.INDIVIDUAL, userId.value(), page, size);
    }

    public Result<List<AchievementAward>, ApplicationError> forFamily(
            FamilyId familyId, UserId requester, int page, int size) {
        validatePage(page, size);
        if (!users.isFamilyMember(familyId, requester)) {
            return Result.failure(
                    ApplicationError.forbidden(
                            "FAMILY_ACHIEVEMENT_ACCESS_FORBIDDEN",
                            "Only current family members can access family achievements."));
        }
        return Result.success(
                awards.findByBeneficiary(AchievementScope.FAMILY, familyId.value(), page, size));
    }

    public Result<List<AchievementAward>, ApplicationError> forCommunity(
            UUID id, UserId user, int page, int size) {
        validatePage(page, size);
        if (!community.isMember(id, user.value()))
            return Result.failure(
                    ApplicationError.forbidden(
                            "COMMUNITY_ACHIEVEMENT_ACCESS_FORBIDDEN",
                            "Community membership is required"));
        return Result.success(awards.findByCommunity(id, page, size));
    }

    public Result<AchievementShareRequest, ApplicationError> shareStatus(UUID id, UserId user) {
        var request = shares.find(id);
        if (request.isEmpty())
            return Result.failure(ApplicationError.notFound("SHARE_REQUEST", id.toString()));
        if (!request.get().requestedBy().equals(user.value()))
            return Result.failure(
                    ApplicationError.forbidden(
                            "SHARE_ACCESS_FORBIDDEN", "Only the requester can access this share"));
        return Result.success(request.get());
    }

    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid page or size (maximum size: 100)");
    }
}
