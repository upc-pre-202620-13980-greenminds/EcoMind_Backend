package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityGoalEnrollmentPersistenceEntity;

public interface CommunityGoalEnrollmentPersistenceRepository extends JpaRepository<CommunityGoalEnrollmentPersistenceEntity, Long> {

    long countByGoalId(Long goalId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO community_goal_enrollments
                (user_id, goal_id, joined_at, created_at, updated_at)
            VALUES
                (:userId, :goalId, clock_timestamp(), clock_timestamp(), clock_timestamp())
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int enroll(@Param("userId") Long userId, @Param("goalId") Long goalId);

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from CommunityGoalEnrollmentPersistenceEntity enrollment
            where enrollment.userId = :userId and enrollment.goalId = :goalId
            """)
    int remove(@Param("userId") Long userId, @Param("goalId") Long goalId);

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from CommunityGoalEnrollmentPersistenceEntity enrollment
            where enrollment.goalId = :goalId
            """)
    int releaseGoal(@Param("goalId") Long goalId);
}