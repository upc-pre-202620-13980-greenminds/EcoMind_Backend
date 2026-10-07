package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.QuestUserPersistenceEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface QuestUserPersistenceRepository extends JpaRepository<QuestUserPersistenceEntity, Long> {
    Optional<QuestUserPersistenceEntity> findByUserIdAndQuestId(
            Long userId,
            Long questId
    );

    Optional<QuestUserPersistenceEntity> findFirstByUserIdAndQuestIdOrderByIdDesc(
            Long userId,
            Long questId
    );

    Optional<QuestUserPersistenceEntity> findFirstByUserIdAndQuestIdOrderByIdAsc(
            Long userId,
            Long questId
    );

    Optional<QuestUserPersistenceEntity> findFirstByUserIdAndQuestIdAndStatusInOrderByIdDesc(
            Long userId,
            Long questId,
            List<QuestStatus> statuses
    );

    List<QuestUserPersistenceEntity> findByQuestId(Long questId);

    List<QuestUserPersistenceEntity> findByQuestIdAndStatusIn(
            Long questId,
            List<QuestStatus> statuses
    );

    @Query("""
    SELECT qu FROM QuestUserPersistenceEntity qu
    JOIN QuestPersistenceEntity q ON q.id = qu.questId
    WHERE q.questType = :questType
      AND q.assignedDate < :assignedDate
      AND qu.status IN :statuses
    """)
    List<QuestUserPersistenceEntity> findDailyQuestUsersBeforeDateAndStatusIn(
            @Param("questType") QuestType questType,
            @Param("assignedDate") LocalDate assignedDate,
            @Param("statuses") List<QuestStatus> statuses
    );

    @Query("""
    SELECT qu FROM QuestUserPersistenceEntity qu
    JOIN QuestPersistenceEntity q ON q.id = qu.questId
    WHERE qu.userId = :userId
      AND q.questType = :questType
      AND q.assignedDate < :assignedDate
      AND qu.status IN :statuses
    """)
    List<QuestUserPersistenceEntity> findDailyQuestUsersByUserIdBeforeDateAndStatusIn(
            @Param("userId") Long userId,
            @Param("questType") QuestType questType,
            @Param("assignedDate") LocalDate assignedDate,
            @Param("statuses") List<QuestStatus> statuses
    );

    @Query("""
    SELECT COUNT(DISTINCT qu.questId) FROM QuestUserPersistenceEntity qu
    JOIN QuestPersistenceEntity q ON q.id = qu.questId
    WHERE qu.userId IN :userIds
      AND qu.status = :status
      AND q.questType IN :questTypes
    """)
    long countByUserIdsAndStatusAndQuestTypes(
            @Param("userIds") List<Long> userIds,
            @Param("status") QuestStatus status,
            @Param("questTypes") List<QuestType> questTypes
    );

    boolean existsByUserIdAndQuestId(
            Long userId,
            Long questId
    );

    boolean existsByUserIdAndQuestIdAndStatusIn(
            Long userId,
            Long questId,
            List<QuestStatus> statuses
    );

    boolean existsByUserIdAndQuestIdAndStatusAndIdNot(
            Long userId,
            Long questId,
            QuestStatus status,
            Long excludedQuestUserId
    );

    void deleteByQuestId(Long questId);

    List<QuestUserPersistenceEntity> findByUserIdAndStatus(
            Long userId,
            QuestStatus status
    );
}
