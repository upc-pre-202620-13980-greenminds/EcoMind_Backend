package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardBeneficiary;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardHistoryEntry;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSource;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSourceType;
import pe.greenminds.ecomind.gamification.domain.repositories.RewardHistoryRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.RewardTransactionPersistenceEntity;

import java.util.List;
import java.util.UUID;

@Repository
public class RewardHistoryRepositoryImpl implements RewardHistoryRepository {
    private final EntityManager entities;

    public RewardHistoryRepositoryImpl(EntityManager entities) {
        this.entities = entities;
    }

    public List<RewardHistoryEntry> find(
            RewardBeneficiary beneficiary, RankingPeriod period, int offset, int limit) {
        return entities
                .createQuery(
                        "from RewardTransactionPersistenceEntity where beneficiaryType=:type and"
                            + " beneficiaryId=:id and occurredAt>=:from and occurredAt<:to order by"
                            + " occurredAt desc,id desc",
                        RewardTransactionPersistenceEntity.class)
                .setParameter("type", beneficiary.type().name())
                .setParameter("id", beneficiary.id())
                .setParameter("from", period.from())
                .setParameter("to", period.to())
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(
                        r ->
                                new RewardHistoryEntry(
                                        UUID.fromString(r.getId()),
                                        new RewardSource(
                                                RewardSourceType.valueOf(r.getSourceType()),
                                                UUID.fromString(r.getSourceExecutionId())),
                                        beneficiary,
                                        new Reward(
                                                r.getBaseEcopoints(),
                                                r.getBaseExperience(),
                                                r.getBaseGems()),
                                        new Reward(
                                                r.getEcopoints(), r.getExperience(), r.getGems()),
                                        r.getOccurredAt()))
                .toList();
    }
}
