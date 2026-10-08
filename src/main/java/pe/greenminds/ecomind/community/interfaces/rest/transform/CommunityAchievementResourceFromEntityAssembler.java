package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityAchievement;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CommunityAchievementResource;

public final class CommunityAchievementResourceFromEntityAssembler {
    private CommunityAchievementResourceFromEntityAssembler() {
    }

    public static CommunityAchievementResource toResourceFromEntity(CommunityAchievement communityAchievement) {
        return new CommunityAchievementResource(communityAchievement.id(), communityAchievement.communityId(),
                communityAchievement.title(), communityAchievement.description(), communityAchievement.communityGoalId());
    }
}
