package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.ActivityUserPersistenceEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityUserPersistenceRepository
        extends JpaRepository<ActivityUserPersistenceEntity, Long> {

    Optional<ActivityUserPersistenceEntity> findByQuestUserIdAndActivityId(
            Long questUserId,
            Long activityId
    );

    List<ActivityUserPersistenceEntity> findByQuestUserId(Long questUserId);

    void deleteByQuestUserId(Long questUserId);

    void deleteByActivityId(Long activityId);

    boolean existsByQuestUserIdAndActivityId(Long questUserId, Long activityId);

    long countByQuestUserId(Long questUserId);
}
