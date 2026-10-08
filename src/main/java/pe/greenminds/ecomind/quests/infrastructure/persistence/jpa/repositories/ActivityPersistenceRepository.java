package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.ActivityPersistenceEntity;

import java.util.List;

@Repository
public interface ActivityPersistenceRepository extends JpaRepository<ActivityPersistenceEntity, Long> {
    List<ActivityPersistenceEntity>
    findByQuestIdOrderByOrderAsc(Long questId);

    boolean existsByQuestIdAndOrder(
            Long questId,
            Integer order
    );

    void deleteByQuestId(Long questId);

    Integer countByQuestId(Long questId);
}
