package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingTransaction;
import pe.greenminds.ecomind.gamification.domain.repositories.RankingReadRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.RewardTransactionPersistenceEntity;

@Repository
public class RankingReadRepositoryImpl implements RankingReadRepository {
  private final EntityManager entities;
  public RankingReadRepositoryImpl(EntityManager entities) { this.entities = entities; }

  public Map<Long, Long> totals(RankingType type, List<Long> ids) {
    if (ids.isEmpty()) return Map.of();
    String query = type == RankingType.FAMILIES
        ? "select familyId, totalEcopoints from FamilyScorePersistenceEntity where familyId in :ids"
        : "select userId, totalEcopoints from UserProgressPersistenceEntity where userId in :ids";
    var result = new HashMap<Long, Long>();
    for (var row : entities.createQuery(query, Object[].class).setParameter("ids", ids).getResultList()) {
      result.put((Long) row[0], (Long) row[1]);
    }
    return result;
  }

  public List<RankingTransaction> transactions(RankingType type, List<Long> ids,
      RankingPeriod period, int offset, int limit) {
    if (ids.isEmpty()) return List.of();
    return entities.createQuery("""
        from RewardTransactionPersistenceEntity where beneficiaryType = :type
          and beneficiaryId in :ids and occurredAt >= :start and occurredAt < :end
        order by occurredAt, id
        """, RewardTransactionPersistenceEntity.class)
        .setParameter("type", type == RankingType.FAMILIES ? "FAMILY" : "USER")
        .setParameter("ids", ids).setParameter("start", period.from()).setParameter("end", period.to())
        .setFirstResult(offset).setMaxResults(limit).getResultList().stream()
        .map(row -> new RankingTransaction(UUID.fromString(row.getId()), row.getBeneficiaryId(),
            row.getEcopoints(), row.getOccurredAt())).toList();
  }
}
