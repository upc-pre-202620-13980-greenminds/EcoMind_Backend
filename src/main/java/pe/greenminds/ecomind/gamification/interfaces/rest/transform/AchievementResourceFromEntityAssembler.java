package pe.greenminds.ecomind.gamification.interfaces.rest.transform;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.AchievementResource;

public final class AchievementResourceFromEntityAssembler {
    private AchievementResourceFromEntityAssembler() {}

    public static AchievementResource toResourceFromEntity(Achievement entity) {
        return new AchievementResource(
                entity.id(),
                entity.code(),
                entity.name(),
                entity.description(),
                entity.scope().name(),
                entity.metric().name(),
                entity.target(),
                entity.active());
    }
}
