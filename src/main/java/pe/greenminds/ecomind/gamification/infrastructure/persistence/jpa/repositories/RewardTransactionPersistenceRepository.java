package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.RewardTransactionPersistenceEntity;

import java.util.List;
import java.util.Optional;

public interface RewardTransactionPersistenceRepository
        extends JpaRepository<RewardTransactionPersistenceEntity, String> {
    long countByBeneficiaryTypeAndBeneficiaryIdAndSourceTypeIn(
            String beneficiaryType, Long beneficiaryId, List<String> sourceTypes);

    @Query("select distinct r.beneficiaryId from RewardTransactionPersistenceEntity r"
            + " where r.beneficiaryType = 'USER' and r.sourceType in :sourceTypes"
            + " order by r.beneficiaryId")
    List<Long> findUsersWithCompletedQuests(List<String> sourceTypes);

    // Read the recorded grant after acquiring the beneficiary lock, including concurrent retries.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RewardTransactionPersistenceEntity>
            findBySourceTypeAndSourceExecutionIdAndBeneficiaryTypeAndBeneficiaryId(
                    String sourceType,
                    String sourceExecutionId,
                    String beneficiaryType,
                    Long beneficiaryId);

    List<RewardTransactionPersistenceEntity>
            findTop100ByBeneficiaryTypeAndBeneficiaryIdOrderByOccurredAtDescIdDesc(
                    String beneficiaryType, Long beneficiaryId);
}
