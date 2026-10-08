package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Repository;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementShareRequest;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementShareStatus;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementShareRequestRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.AchievementShareRequestPersistenceEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AchievementShareRequestRepositoryImpl implements AchievementShareRequestRepository {
    private final EntityManager entities;

    public AchievementShareRequestRepositoryImpl(EntityManager entities) {
        this.entities = entities;
    }

    public Optional<AchievementShareRequest> find(UUID id) {
        return Optional.ofNullable(
                        entities.find(
                                AchievementShareRequestPersistenceEntity.class, id.toString()))
                .map(this::map);
    }

    public Optional<AchievementShareRequest> lock(UUID id) {
        return Optional.ofNullable(
                        entities.find(
                                AchievementShareRequestPersistenceEntity.class,
                                id.toString(),
                                LockModeType.PESSIMISTIC_WRITE))
                .map(this::map);
    }

    public AchievementShareRequest save(AchievementShareRequest r) {
        var row = entities.find(AchievementShareRequestPersistenceEntity.class, r.id().toString());
        if (row == null) {
            row = new AchievementShareRequestPersistenceEntity();
            row.setId(r.id().toString());
            row.setAwardId(r.awardId().toString());
            row.setRequestedBy(r.requestedBy());
            row.setCommunityId(r.communityId().toString());
            row.setCreatedAt(r.createdAt());
            row.setStatus(r.status().name());
            entities.persist(row);
        }
        row.setStatus(r.status().name());
        row.setPublicationId(r.publicationId() == null ? null : r.publicationId().toString());
        row.setConfirmedAt(r.confirmedAt());
        return r;
    }

    private AchievementShareRequest map(AchievementShareRequestPersistenceEntity r) {
        return new AchievementShareRequest(
                UUID.fromString(r.getId()),
                UUID.fromString(r.getAwardId()),
                r.getRequestedBy(),
                UUID.fromString(r.getCommunityId()),
                AchievementShareStatus.valueOf(r.getStatus()),
                r.getPublicationId() == null ? null : UUID.fromString(r.getPublicationId()),
                r.getCreatedAt(),
                r.getConfirmedAt());
    }
}
