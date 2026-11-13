package pe.greenminds.ecomind.community.domain.repositories;

import java.util.Optional;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoalEnrollment;

public interface CommunityGoalParticipationRepository {
    Optional<CommunityGoalEnrollment> findByUserId(Long userId);

    boolean enroll(Long userId, Long goalId);

    long countByGoalId(Long goalId);

    void remove(Long userId, Long goalId);

    void releaseGoal(Long goalId);

    boolean registerCompletion(String receiptKey);
}