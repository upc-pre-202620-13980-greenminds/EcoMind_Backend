package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.MinigameAttemptPersistenceEntity;

import java.util.List;

@Repository
public interface MinigameAttemptPersistenceRepository
        extends JpaRepository<MinigameAttemptPersistenceEntity, Long> {
    boolean existsByUserIdAndStatus(Long userId, MinigameAttemptStatus status);

    List<MinigameAttemptPersistenceEntity> findByUserIdAndMinigameIdOrderByStartDateDesc(
            Long userId,
            Long minigameId
    );

    @Modifying
    void deleteByMinigameId(Long minigameId);
}
