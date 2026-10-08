package pe.greenminds.ecomind.gamification.application.queryservices;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementShareRequest;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetAchievementByIdQuery;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetAchievementShareStatusQuery;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetCommunityAchievementsQuery;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetFamilyAchievementsQuery;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetUserAchievementsQuery;
import pe.greenminds.ecomind.gamification.domain.model.queries.SearchAchievementsQuery;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.util.List;
import java.util.UUID;

public interface AchievementQueryService {
    default Result<Achievement, ApplicationError> handle(GetAchievementByIdQuery q) {
        return find(q.achievementId());
    }

    default List<Achievement> handle(SearchAchievementsQuery q) {
        return search(q.scope(), q.page(), q.size());
    }

    default List<AchievementAward> handle(GetUserAchievementsQuery q) {
        return forUser(q.userId(), q.page(), q.size());
    }

    default Result<List<AchievementAward>, ApplicationError> handle(GetFamilyAchievementsQuery q) {
        return forFamily(q.familyId(), q.requestedBy(), q.page(), q.size());
    }

    default Result<List<AchievementAward>, ApplicationError> handle(
            GetCommunityAchievementsQuery q) {
        return forCommunity(q.communityId(), q.requestedBy(), q.page(), q.size());
    }

    default Result<AchievementShareRequest, ApplicationError> handle(
            GetAchievementShareStatusQuery q) {
        return shareStatus(q.requestId(), q.requestedBy());
    }

    Result<List<AchievementAward>, ApplicationError> forCommunity(
            Long communityId, UserId requester, int page, int size);

    Result<AchievementShareRequest, ApplicationError> shareStatus(UUID requestId, UserId requester);

    List<Achievement> search(AchievementScope scope, int page, int size);

    Result<Achievement, ApplicationError> find(UUID id);

    List<AchievementAward> forUser(UserId userId, int page, int size);

    Result<List<AchievementAward>, ApplicationError> forFamily(
            FamilyId familyId, UserId requester, int page, int size);
}
