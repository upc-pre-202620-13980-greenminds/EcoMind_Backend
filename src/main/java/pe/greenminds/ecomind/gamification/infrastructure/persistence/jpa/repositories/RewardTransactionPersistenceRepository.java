package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.RewardTransactionPersistenceEntity;

public interface RewardTransactionPersistenceRepository
    extends JpaRepository<RewardTransactionPersistenceEntity, String> {
  // A locking read sees grants committed while waiting for the beneficiary lock in MySQL.
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<RewardTransactionPersistenceEntity>
      findBySourceTypeAndSourceExecutionIdAndBeneficiaryTypeAndBeneficiaryId(
          String sourceType, String sourceExecutionId, String beneficiaryType, Long beneficiaryId);

  List<RewardTransactionPersistenceEntity>
      findTop100ByBeneficiaryTypeAndBeneficiaryIdOrderByOccurredAtDescIdDesc(
          String beneficiaryType, Long beneficiaryId);
}
