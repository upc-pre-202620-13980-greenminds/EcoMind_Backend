package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;

import pe.greenminds.ecomind.gamification.domain.model.entities.FamilyRewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.repositories.FamilyRewardTransactionRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.RewardTransactionPersistenceEntity;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.RewardTransactionPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FamilyRewardTransactionRepositoryImpl implements FamilyRewardTransactionRepository {
    private final RewardTransactionPersistenceRepository rows;

    public FamilyRewardTransactionRepositoryImpl(RewardTransactionPersistenceRepository rows) {
        this.rows = rows;
    }

    public Optional<FamilyRewardTransaction> findByExecution(UUID executionId, FamilyId familyId) {
        return rows.findBySourceTypeAndSourceExecutionIdAndBeneficiaryTypeAndBeneficiaryId(
                        "FAMILY_PLAN", executionId.toString(), "FAMILY", familyId.value())
                .map(FamilyRewardTransactionRepositoryImpl::toDomain);
    }

    public void save(FamilyRewardTransaction transaction) {
        var row = new RewardTransactionPersistenceEntity();
        row.setId(transaction.id().toString());
        row.setSourceType("FAMILY_PLAN");
        row.setSourceExecutionId(transaction.sourceExecutionId().toString());
        row.setBeneficiaryType("FAMILY");
        row.setBeneficiaryId(transaction.familyId().value());
        row.setFamilyScoreId(transaction.familyId().value());
        row.setBaseEcopoints(transaction.ecopoints());
        row.setEcopoints(transaction.ecopoints());
        row.setOccurredAt(transaction.occurredAt());
        // Families receive ecopoints only; XP and gems remain zero.
        rows.save(row);
    }

    public List<FamilyRewardTransaction> findRecent(FamilyId familyId) {
        return rows
                .findTop100ByBeneficiaryTypeAndBeneficiaryIdOrderByOccurredAtDescIdDesc(
                        "FAMILY", familyId.value())
                .stream()
                .map(FamilyRewardTransactionRepositoryImpl::toDomain)
                .toList();
    }

    private static FamilyRewardTransaction toDomain(RewardTransactionPersistenceEntity row) {
        return new FamilyRewardTransaction(
                UUID.fromString(row.getId()),
                UUID.fromString(row.getSourceExecutionId()),
                new FamilyId(row.getBeneficiaryId()),
                row.getEcopoints(),
                row.getOccurredAt());
    }
}
