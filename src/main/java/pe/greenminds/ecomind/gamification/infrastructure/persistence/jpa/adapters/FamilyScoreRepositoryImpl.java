package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.FamilyScore;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.repositories.FamilyScoreRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.FamilyScorePersistenceEntity;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.FamilyScorePersistenceRepository;

@Repository
public class FamilyScoreRepositoryImpl implements FamilyScoreRepository {
  private final FamilyScorePersistenceRepository rows;
  private final JdbcTemplate jdbc;
  private final EntityManager entities;

  public FamilyScoreRepositoryImpl(FamilyScorePersistenceRepository rows, JdbcTemplate jdbc, EntityManager entities) {
    this.rows = rows;
    this.jdbc = jdbc;
    this.entities = entities;
  }

  public FamilyScore lockForReward(FamilyId familyId) {
    jdbc.update("INSERT INTO family_scores (family_id, total_ecopoints, version) VALUES (?, 0, 0) ON CONFLICT DO NOTHING", familyId.value());
    return toDomain(entities.find(FamilyScorePersistenceEntity.class, familyId.value(), LockModeType.PESSIMISTIC_WRITE));
  }

  public void save(FamilyScore score) {
    var row = rows.findById(score.getFamilyId().value()).orElseThrow();
    row.setTotalEcopoints(score.getTotalEcopoints());
    rows.save(row);
  }

  public Optional<FamilyScore> findByFamilyId(FamilyId familyId) {
    return rows.findById(familyId.value()).map(FamilyScoreRepositoryImpl::toDomain);
  }

  private static FamilyScore toDomain(FamilyScorePersistenceEntity row) {
    return new FamilyScore(new FamilyId(row.getFamilyId()), row.getTotalEcopoints());
  }
}
