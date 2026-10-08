package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementMilestoneRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.AchievementMilestoneLockPersistenceEntity;

import java.util.UUID;

@Repository
public class AchievementMilestoneRepositoryImpl implements AchievementMilestoneRepository {
    private final JdbcTemplate jdbc;
    private final EntityManager entities;

    public AchievementMilestoneRepositoryImpl(JdbcTemplate jdbc, EntityManager entities) {
        this.jdbc = jdbc;
        this.entities = entities;
    }

    public void lock(String beneficiary) {
        jdbc.update(
                "INSERT INTO achievement_milestone_locks (beneficiary) VALUES (?) ON CONFLICT DO"
                        + " NOTHING",
                beneficiary);
        entities.find(
                AchievementMilestoneLockPersistenceEntity.class,
                beneficiary,
                LockModeType.PESSIMISTIC_WRITE);
    }

    public void record(AchievementMetric metric, String beneficiary, UUID execution) {
        jdbc.update(
                "INSERT INTO achievement_milestones (id,metric,beneficiary,execution_id) VALUES"
                        + " (?,?,?,?) ON CONFLICT DO NOTHING",
                UUID.randomUUID().toString(),
                metric.name(),
                beneficiary,
                execution.toString());
    }

    public long count(AchievementMetric metric, String beneficiary) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM achievement_milestones WHERE metric=? AND beneficiary=?",
                Long.class,
                metric.name(),
                beneficiary);
    }
}
