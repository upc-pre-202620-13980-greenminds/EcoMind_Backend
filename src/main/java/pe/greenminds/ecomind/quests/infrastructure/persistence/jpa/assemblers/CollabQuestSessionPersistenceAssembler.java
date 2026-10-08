package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.CollabQuestSessionPersistenceEntity;

public final class CollabQuestSessionPersistenceAssembler {
    private CollabQuestSessionPersistenceAssembler() {
    }

    public static CollabQuestSession toDomainFromPersistence(
            CollabQuestSessionPersistenceEntity entity
    ) {
        return new CollabQuestSession(
                entity.getId(),
                entity.getQuestId(),
                entity.getOwnerId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }

    public static CollabQuestSessionPersistenceEntity toPersistenceFromDomain(
            CollabQuestSession collabQuestSession
    ) {
        var entity = new CollabQuestSessionPersistenceEntity();
        entity.setId(collabQuestSession.getId());
        entity.setQuestId(collabQuestSession.getQuestId());
        entity.setOwnerId(collabQuestSession.getOwnerId());
        entity.setStatus(collabQuestSession.getStatus());
        entity.setStartDate(collabQuestSession.getStartDate());
        entity.setEndDate(collabQuestSession.getEndDate());
        return entity;
    }
}
