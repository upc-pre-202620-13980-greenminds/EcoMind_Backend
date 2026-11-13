package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoalEnrollment;
import pe.greenminds.ecomind.community.domain.repositories.CommunityGoalParticipationRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.CommunityGoalCompletionReceiptPersistenceRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.CommunityGoalEnrollmentPersistenceRepository;

@Repository
public class CommunityGoalParticipationRepositoryImpl implements CommunityGoalParticipationRepository {

    private final CommunityGoalEnrollmentPersistenceRepository enrollments;
    private final CommunityGoalCompletionReceiptPersistenceRepository receipts;

    public CommunityGoalParticipationRepositoryImpl(
            CommunityGoalEnrollmentPersistenceRepository enrollments,
            CommunityGoalCompletionReceiptPersistenceRepository receipts) {
        this.enrollments = enrollments;
        this.receipts = receipts;
    }

    @Override
    public Optional<CommunityGoalEnrollment> findByUserId(Long userId) {
        return enrollments.findById(userId)
                .map(entity -> new CommunityGoalEnrollment(
                        entity.getUserId(),
                        entity.getGoalId(),
                        entity.getJoinedAt()));
    }

    @Override
    public boolean enroll(Long userId, Long goalId) {
        return enrollments.enroll(userId, goalId) == 1;
    }

    @Override
    public long countByGoalId(Long goalId) {
        return enrollments.countByGoalId(goalId);
    }

    @Override
    public void remove(Long userId, Long goalId) {
        enrollments.remove(userId, goalId);
    }

    @Override
    public void releaseGoal(Long goalId) {
        enrollments.releaseGoal(goalId);
    }

    @Override
    public boolean registerCompletion(String receiptKey) {
        return receipts.register(receiptKey) == 1;
    }
}