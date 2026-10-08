package pe.greenminds.ecomind.gamification.interfaces.rest.transform;

import pe.greenminds.ecomind.gamification.domain.model.commands.ShareAchievementCommand;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.ShareAchievementResource;

public final class ShareAchievementCommandFromResourceAssembler {
    private ShareAchievementCommandFromResourceAssembler() {}

    public static ShareAchievementCommand toCommandFromResource(
            ShareAchievementResource resource, Long authenticatedUser) {
        return new ShareAchievementCommand(
                resource.requestId(),
                resource.awardId(),
                authenticatedUser,
                resource.communityId());
    }
}
