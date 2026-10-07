package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.gamification.domain.model.entities.RewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSourceType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.RewardTransactionRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.RewardTransactionPersistenceEntity;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.RewardTransactionPersistenceRepository;

@Repository
public class RewardTransactionRepositoryImpl implements RewardTransactionRepository {
  private final RewardTransactionPersistenceRepository persistenceRepository;

  public RewardTransactionRepositoryImpl(
      RewardTransactionPersistenceRepository persistenceRepository) {
    this.persistenceRepository = persistenceRepository;
  }

  @Override
  public Optional<RewardTransaction> findByOrigin(
      RewardSourceType sourceType, UUID sourceExecutionId, UserId beneficiary) {
    return persistenceRepository
        .findBySourceTypeAndSourceExecutionIdAndBeneficiaryTypeAndBeneficiaryId(
            sourceType.name(), sourceExecutionId.toString(), "USER", beneficiary.value())
        .map(RewardTransactionRepositoryImpl::toDomain);
  }

  @Override
  public RewardTransaction save(RewardTransaction rewardTransaction) {
    var entity = new RewardTransactionPersistenceEntity();
    entity.setId(rewardTransaction.id().toString());
    entity.setSourceType(rewardTransaction.sourceType().name());
    entity.setSourceExecutionId(rewardTransaction.sourceExecutionId().toString());
    entity.setBeneficiaryType("USER");
    entity.setBeneficiaryId(rewardTransaction.beneficiary().value());
    entity.setBaseEcopoints(rewardTransaction.baseReward().ecopoints());
    entity.setBaseExperience(rewardTransaction.baseReward().experience());
    entity.setBaseGems(rewardTransaction.baseReward().gems());
    entity.setEcopoints(rewardTransaction.grantedReward().ecopoints());
    entity.setExperience(rewardTransaction.grantedReward().experience());
    entity.setGems(rewardTransaction.grantedReward().gems());
    entity.setOccurredAt(rewardTransaction.occurredAt());
    return toDomain(persistenceRepository.save(entity));
  }

  @Override
  public List<RewardTransaction> findRecentByUser(UserId userId) {
    return persistenceRepository
        .findTop100ByBeneficiaryTypeAndBeneficiaryIdOrderByOccurredAtDescIdDesc(
            "USER", userId.value())
        .stream().map(RewardTransactionRepositoryImpl::toDomain).toList();
  }

  private static RewardTransaction toDomain(RewardTransactionPersistenceEntity entity) {
    return new RewardTransaction(
        UUID.fromString(entity.getId()), RewardSourceType.valueOf(entity.getSourceType()),
        UUID.fromString(entity.getSourceExecutionId()), new UserId(entity.getBeneficiaryId()),
        new Reward(entity.getBaseEcopoints(), entity.getBaseExperience(), entity.getBaseGems()),
        new Reward(entity.getEcopoints(), entity.getExperience(), entity.getGems()),
        entity.getOccurredAt());
  }
}
