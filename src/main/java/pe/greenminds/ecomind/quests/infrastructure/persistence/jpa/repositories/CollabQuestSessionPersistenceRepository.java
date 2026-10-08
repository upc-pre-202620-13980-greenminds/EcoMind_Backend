package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.CollabQuestSessionPersistenceEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollabQuestSessionPersistenceRepository
        extends JpaRepository<CollabQuestSessionPersistenceEntity, Long> {
    Optional<CollabQuestSessionPersistenceEntity> findByQuestIdAndOwnerId(
            Long questId,
            Long ownerId
    );

    Optional<CollabQuestSessionPersistenceEntity> findFirstByQuestIdAndOwnerIdAndStatusInOrderByIdDesc(
            Long questId,
            Long ownerId,
            List<CollabQuestStatus> statuses
    );

    List<CollabQuestSessionPersistenceEntity> findByQuestId(Long questId);

    List<CollabQuestSessionPersistenceEntity> findByOwnerId(Long ownerId);

    void deleteByQuestId(Long questId);
}
