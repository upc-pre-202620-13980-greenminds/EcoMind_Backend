package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.RewardTransactionPersistenceEntity;

public interface RewardTransactionPersistenceRepository
    extends JpaRepository<RewardTransactionPersistenceEntity, String> {
  Optional<RewardTransactionPersistenceEntity>
      findBySourceTypeAndSourceExecutionIdAndBeneficiaryTypeAndBeneficiaryId(
          String sourceType, String sourceExecutionId, String beneficiaryType, Long beneficiaryId);

  List<RewardTransactionPersistenceEntity>
      findTop100ByBeneficiaryTypeAndBeneficiaryIdOrderByOccurredAtDescIdDesc(
          String beneficiaryType, Long beneficiaryId);
}
