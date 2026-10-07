package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.MinigameAttemptPersistenceEntity;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MinigameAttemptPersistenceRepository
        extends JpaRepository<MinigameAttemptPersistenceEntity, Long> {
    boolean existsByUserIdAndStatus(Long userId, MinigameAttemptStatus status);

    List<MinigameAttemptPersistenceEntity> findByUserIdAndMinigameIdOrderByStartDateDesc(
            Long userId,
            Long minigameId
    );

    @Query("""
            select attempt
            from MinigameAttemptPersistenceEntity attempt
            where attempt.userId = :userId
                and attempt.minigameId = :minigameId
                and attempt.status = pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus.COMPLETED
                and attempt.endDate >= :since
                and (attempt.givenGems > 0 or attempt.givenEcopoints > 0)
            order by attempt.endDate desc
            """)
    List<MinigameAttemptPersistenceEntity> findRewardedAttemptsSince(
            @Param("userId") Long userId,
            @Param("minigameId") Long minigameId,
            @Param("since") OffsetDateTime since
    );

    @Query("""
            select count(distinct attempt.questId)
            from MinigameAttemptPersistenceEntity attempt
            where attempt.userId in :userIds
                and attempt.status = pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus.COMPLETED
                and (attempt.givenGems > 0 or attempt.givenEcopoints > 0)
            """)
    long countRewardedCompletedByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    void deleteByMinigameId(Long minigameId);
}
