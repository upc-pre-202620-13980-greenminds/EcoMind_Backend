package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.UserProgress;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.UserProgressRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.UserProgressPersistenceEntity;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.UserProgressPersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserProgressRepositoryImpl implements UserProgressRepository {
    private final UserProgressPersistenceRepository persistenceRepository;
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    public UserProgressRepositoryImpl(
            UserProgressPersistenceRepository persistenceRepository,
            EntityManager entityManager,
            JdbcTemplate jdbcTemplate) {
        this.persistenceRepository = persistenceRepository;
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserProgress lockForReward(UserId userId) {
        // The unique progress row is the per-user serialization point, including the first grant.
        jdbcTemplate.update(
                """
                INSERT INTO user_progresses
                  (user_id, total_ecopoints, current_streak, longest_streak, version)
                VALUES (?, 0, 0, 0, 0)
                ON CONFLICT DO NOTHING
                """,
                userId.value());
        var entity =
                entityManager.find(
                        UserProgressPersistenceEntity.class,
                        userId.value(),
                        LockModeType.PESSIMISTIC_WRITE);
        return toDomain(entity);
    }

    @Override
    public UserProgress save(UserProgress progress) {
        var entity = persistenceRepository.findById(progress.getUserId().value()).orElseThrow();
        entity.setTotalEcopoints(progress.getTotalEcopoints());
        entity.setCurrentStreak(progress.getCurrentStreak());
        entity.setLongestStreak(progress.getLongestStreak());
        entity.setLastActivityDate(progress.getLastActivityDate());
        entity.setLastProtectedDate(progress.getLastProtectedDate());
        return toDomain(persistenceRepository.save(entity));
    }

    @Override
    public Optional<UserProgress> findByUserId(UserId userId) {
        return persistenceRepository
                .findById(userId.value())
                .map(UserProgressRepositoryImpl::toDomain);
    }

    public List<UserId> findActiveStreakUsers() {
        return entityManager
                .createQuery(
                        "select userId from UserProgressPersistenceEntity where currentStreak>0"
                                + " order by userId",
                        Long.class)
                .getResultList()
                .stream()
                .map(UserId::new)
                .toList();
    }

    private static UserProgress toDomain(UserProgressPersistenceEntity entity) {
        var progress =
                new UserProgress(
                        new UserId(entity.getUserId()),
                        entity.getTotalEcopoints(),
                        entity.getCurrentStreak(),
                        entity.getLongestStreak(),
                        entity.getLastActivityDate());
        progress.restoreProtectedDate(entity.getLastProtectedDate());
        return progress;
    }
}
