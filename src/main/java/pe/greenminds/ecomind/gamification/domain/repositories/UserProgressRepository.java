package pe.greenminds.ecomind.gamification.domain.repositories;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.UserProgress;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository {
    /** Creates the empty row if needed and locks it until the caller's transaction commits. */
    UserProgress lockForReward(UserId userId);

    UserProgress save(UserProgress progress);

    Optional<UserProgress> findByUserId(UserId userId);

    List<UserId> findActiveStreakUsers();
}
