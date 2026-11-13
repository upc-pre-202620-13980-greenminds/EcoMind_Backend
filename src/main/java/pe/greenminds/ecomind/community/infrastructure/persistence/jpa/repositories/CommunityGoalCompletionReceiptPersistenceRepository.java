package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityGoalCompletionReceiptPersistenceEntity;

public interface CommunityGoalCompletionReceiptPersistenceRepository extends JpaRepository<CommunityGoalCompletionReceiptPersistenceEntity, String> {

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO community_goal_completion_receipts
                (receipt_key, created_at, updated_at)
            VALUES
                (:receiptKey, clock_timestamp(), clock_timestamp())
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int register(@Param("receiptKey") String receiptKey);
}