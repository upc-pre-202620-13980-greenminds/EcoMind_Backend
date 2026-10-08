package pe.greenminds.ecomind.gamification.domain.repositories;

import pe.greenminds.ecomind.gamification.domain.model.entities.FamilyRewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FamilyRewardTransactionRepository {
    Optional<FamilyRewardTransaction> findByExecution(UUID executionId, FamilyId familyId);

    void save(FamilyRewardTransaction transaction);

    List<FamilyRewardTransaction> findRecent(FamilyId familyId);
}
