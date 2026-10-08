package pe.greenminds.ecomind.gamification.interfaces.rest.transform;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.AchievementAwardResource;

public final class AchievementAwardResourceFromEntityAssembler {
    private AchievementAwardResourceFromEntityAssembler() {}

    public static AchievementAwardResource toResourceFromEntity(AchievementAward entity) {
        return new AchievementAwardResource(
                entity.id(),
                entity.achievementId(),
                entity.scope().name(),
                entity.beneficiaryId(),
                entity.sourceEventId(),
                entity.awardedAt(),
                entity.communityId());
    }
}
