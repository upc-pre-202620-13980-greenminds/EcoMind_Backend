package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementAwardRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.AchievementAwardPersistenceEntity;

@Repository
public class AchievementAwardRepositoryImpl implements AchievementAwardRepository {
  private final EntityManager entities;
  private final JdbcTemplate jdbc;
  public AchievementAwardRepositoryImpl(EntityManager entities, JdbcTemplate jdbc) {
    this.entities = entities;
    this.jdbc = jdbc;
  }

  public void addIfAbsent(AchievementAward award) {
    // Unique constraint preserves the first award, including source event and date, on retries.
    // Avoid a missing-row gap lock followed by insert for different beneficiaries in MySQL.
    jdbc.update("""
        INSERT IGNORE INTO achievement_awards
          (id, achievement_id, scope, beneficiary_id, source_event_id, awarded_at)
        VALUES (?, ?, ?, ?, ?, ?)
        """, award.id().toString(), award.achievementId().toString(), award.scope().name(),
        award.beneficiaryId(), award.sourceEventId().toString(), Timestamp.from(award.awardedAt()));
  }

  public List<AchievementAward> findByBeneficiary(AchievementScope scope, Long beneficiaryId, int page, int size) {
    return entities.createQuery("from AchievementAwardPersistenceEntity where scope = :scope and beneficiaryId = :beneficiary order by awardedAt desc, id", AchievementAwardPersistenceEntity.class)
        .setParameter("scope", scope.name()).setParameter("beneficiary", beneficiaryId)
        .setFirstResult(Math.multiplyExact(page, size)).setMaxResults(size).getResultList().stream()
        .map(row -> new AchievementAward(UUID.fromString(row.getId()), UUID.fromString(row.getAchievementId()),
            AchievementScope.valueOf(row.getScope()), row.getBeneficiaryId(), UUID.fromString(row.getSourceEventId()), row.getAwardedAt())).toList();
  }
}
