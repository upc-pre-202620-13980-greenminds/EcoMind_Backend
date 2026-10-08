package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Repository;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.StreakProtectionRequest;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.StreakProtectionStatus;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.StreakProtectionRequestRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.StreakProtectionRequestPersistenceEntity;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StreakProtectionRequestRepositoryImpl implements StreakProtectionRequestRepository {
    private final EntityManager entities;

    public StreakProtectionRequestRepositoryImpl(EntityManager entities) {
        this.entities = entities;
    }

    public Optional<StreakProtectionRequest> find(UserId user, LocalDate date) {
        return entities
                .createQuery(
                        "from StreakProtectionRequestPersistenceEntity where userId=:user and"
                                + " streakDate=:date",
                        StreakProtectionRequestPersistenceEntity.class)
                .setParameter("user", user.value())
                .setParameter("date", date)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst()
                .map(this::map);
    }

    public Optional<StreakProtectionRequest> find(UUID id) {
        return Optional.ofNullable(
                        entities.find(
                                StreakProtectionRequestPersistenceEntity.class, id.toString()))
                .map(this::map);
    }

    public Optional<StreakProtectionRequest> lock(UUID id) {
        return Optional.ofNullable(
                        entities.find(
                                StreakProtectionRequestPersistenceEntity.class,
                                id.toString(),
                                LockModeType.PESSIMISTIC_WRITE))
                .map(this::map);
    }

    public boolean hasPendingBefore(UserId user, LocalDate date) {
        return entities.createQuery(
                                "select count(r) from StreakProtectionRequestPersistenceEntity r"
                                        + " where userId=:user and streakDate<:date and"
                                        + " status='PENDING'",
                                Long.class)
                        .setParameter("user", user.value())
                        .setParameter("date", date)
                        .getSingleResult()
                > 0;
    }

    public StreakProtectionRequest save(StreakProtectionRequest r) {
        var row = entities.find(StreakProtectionRequestPersistenceEntity.class, r.id().toString());
        if (row == null) {
            row = new StreakProtectionRequestPersistenceEntity();
            row.setId(r.id().toString());
            row.setUserId(r.userId().value());
            row.setStreakDate(r.streakDate());
            row.setCreatedAt(r.createdAt());
            row.setStatus(r.status().name());
            entities.persist(row);
        }
        row.setStatus(r.status().name());
        row.setResolvedAt(r.resolvedAt());
        return r;
    }

    private StreakProtectionRequest map(StreakProtectionRequestPersistenceEntity r) {
        return new StreakProtectionRequest(
                UUID.fromString(r.getId()),
                new UserId(r.getUserId()),
                r.getStreakDate(),
                StreakProtectionStatus.valueOf(r.getStatus()),
                r.getCreatedAt(),
                r.getResolvedAt());
    }
}
