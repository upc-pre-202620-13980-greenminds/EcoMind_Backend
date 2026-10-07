package pe.greenminds.ecomind.quests.domain.repositories;

import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;

import java.util.List;
import java.util.Optional;

public interface ActivityUserRepository {
    ActivityUser save(ActivityUser activityUser);
    Optional<ActivityUser> findById(Long id);
    Optional<ActivityUser> findByQuestUserIdAndActivityId(Long questUserId, Long activityId);

    List<ActivityUser> findByQuestUserId(Long questUserId);

    void deleteById(Long id);

    void deleteByQuestUserId(Long questUserId);

    void deleteByActivityId(Long activityId);

    boolean existsByQuestUserIdAndActivityId(Long questUserId, Long activityId);

    long countByQuestUserId(Long questUserId);
}
