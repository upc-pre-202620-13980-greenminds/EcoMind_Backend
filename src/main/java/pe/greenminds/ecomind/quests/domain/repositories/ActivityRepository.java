package pe.greenminds.ecomind.quests.domain.repositories;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Activity;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository {
    Optional<Activity> findById(Long id);
    List<Activity> findByQuestsIdOrderByOrderAsc(Long questId);

    boolean existsByQuestIdAndOrder(Long questId, Integer order);

    Activity save(Activity activity);
    void deleteById(Long id);
    void deleteByQuestId(Long questId);

    Integer countByQuestId(Long questId);
}
