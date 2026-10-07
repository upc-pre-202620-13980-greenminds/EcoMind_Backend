package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.AchievementPersistenceEntity;

@Repository
public class AchievementRepositoryImpl implements AchievementRepository {
  private final EntityManager entities;
  public AchievementRepositoryImpl(EntityManager entities) { this.entities = entities; }

  public void add(Achievement achievement) {
    var row = new AchievementPersistenceEntity();
    row.setId(achievement.id().toString());
    row.setCode(achievement.code());
    row.setName(achievement.name());
    row.setDescription(achievement.description());
    row.setScope(achievement.scope().name());
    row.setMetric(achievement.metric().name());
    row.setTarget(achievement.target());
    row.setActive(achievement.active());
    entities.persist(row);
  }

  public Optional<Achievement> findById(UUID id) {
    return Optional.ofNullable(entities.find(AchievementPersistenceEntity.class, id.toString())).map(AchievementRepositoryImpl::toDomain);
  }

  public List<Achievement> findActive(AchievementScope scope) {
    return entities.createQuery("from AchievementPersistenceEntity where scope = :scope and active = true order by code", AchievementPersistenceEntity.class)
        .setParameter("scope", scope.name()).getResultList().stream().map(AchievementRepositoryImpl::toDomain).toList();
  }

  public List<Achievement> search(AchievementScope scope, int page, int size) {
    String filter = scope == null ? "" : " where scope = :scope";
    var query = entities.createQuery("from AchievementPersistenceEntity" + filter + " order by code", AchievementPersistenceEntity.class);
    if (scope != null) query.setParameter("scope", scope.name());
    return query.setFirstResult(Math.multiplyExact(page, size)).setMaxResults(size)
        .getResultList().stream().map(AchievementRepositoryImpl::toDomain).toList();
  }

  private static Achievement toDomain(AchievementPersistenceEntity row) {
    return new Achievement(UUID.fromString(row.getId()), row.getCode(), row.getName(), row.getDescription(),
        AchievementScope.valueOf(row.getScope()), AchievementMetric.valueOf(row.getMetric()), row.getTarget(), row.isActive());
  }
}
