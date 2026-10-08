package pe.greenminds.ecomind.gamification.interfaces.rest.transform;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.UserProgress;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.UserProgressResource;

public final class UserProgressResourceFromEntityAssembler {
    private UserProgressResourceFromEntityAssembler() {}

    public static UserProgressResource toResourceFromEntity(UserProgress entity) {
        return new UserProgressResource(
                entity.getUserId().value(),
                entity.getTotalEcopoints(),
                entity.getCurrentStreak(),
                entity.getLongestStreak(),
                entity.getLastActivityDate(),
                entity.getLastProtectedDate());
    }
}
