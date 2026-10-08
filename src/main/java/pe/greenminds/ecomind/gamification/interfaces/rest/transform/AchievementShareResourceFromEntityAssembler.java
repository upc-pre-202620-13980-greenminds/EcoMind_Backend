package pe.greenminds.ecomind.gamification.interfaces.rest.transform;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementShareRequest;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.AchievementShareResource;

public final class AchievementShareResourceFromEntityAssembler {
    private AchievementShareResourceFromEntityAssembler() {}

    public static AchievementShareResource toResourceFromEntity(AchievementShareRequest r) {
        return new AchievementShareResource(
                r.id(),
                r.awardId(),
                r.requestedBy(),
                r.communityId(),
                r.status().name(),
                r.publicationId(),
                r.createdAt(),
                r.confirmedAt());
    }
}
