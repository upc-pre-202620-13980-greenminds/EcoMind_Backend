package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.CollabQuestMemberPersistenceEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollabQuestMemberPersistenceRepository
        extends JpaRepository<CollabQuestMemberPersistenceEntity, Long> {
    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);

    Optional<CollabQuestMemberPersistenceEntity> findBySessionIdAndUserId(
            Long sessionId,
            Long userId
    );

    void deleteBySessionId(Long sessionId);

    List<CollabQuestMemberPersistenceEntity> findBySessionIdOrderByIdAsc(Long sessionId);
    List<CollabQuestMemberPersistenceEntity> findByUserIdOrderByIdDesc(Long userId);
    List<CollabQuestMemberPersistenceEntity> findByUserIdAndStatusOrderByIdDesc(
            Long userId, CollabMemberStatus status);

    List<CollabQuestMemberPersistenceEntity> findBySessionIdAndStatusIn(
            Long sessionId,
            List<CollabMemberStatus> statuses
    );

    @Query("""
            select member
            from CollabQuestMemberPersistenceEntity member
            join CollabQuestSessionPersistenceEntity session
                on session.id = member.sessionId
            where member.userId = :userId
                and session.questId = :questId
            """)
    List<CollabQuestMemberPersistenceEntity> findByUserIdAndQuestId(
            @Param("userId") Long userId,
            @Param("questId") Long questId
    );

    @Query("""
            select member
            from CollabQuestMemberPersistenceEntity member
            join CollabQuestSessionPersistenceEntity session
                on session.id = member.sessionId
            where member.userId = :userId
                and session.questId = :questId
                and session.status in :sessionStatuses
            """)
    List<CollabQuestMemberPersistenceEntity> findByUserIdAndQuestIdAndSessionStatusIn(
            @Param("userId") Long userId,
            @Param("questId") Long questId,
            @Param("sessionStatuses") List<CollabQuestStatus> sessionStatuses
    );
}
