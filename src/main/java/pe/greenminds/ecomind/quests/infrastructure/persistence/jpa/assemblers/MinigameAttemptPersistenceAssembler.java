package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.MinigameAttemptPersistenceEntity;

public final class MinigameAttemptPersistenceAssembler {
    private MinigameAttemptPersistenceAssembler() {
    }

    public static MinigameAttempt toDomainFromPersistence(
            MinigameAttemptPersistenceEntity entity
    ) {
        return new MinigameAttempt(
                entity.getId(),
                entity.getUserId(),
                entity.getQuestId(),
                entity.getMinigameId(),
                entity.getScore(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getMetadata(),
                entity.getSuccessful()
        );
    }

    public static MinigameAttemptPersistenceEntity toPersistenceFromDomain(
            MinigameAttempt minigameAttempt
    ) {
        var entity = new MinigameAttemptPersistenceEntity();
        entity.setId(minigameAttempt.getId());
        entity.setUserId(minigameAttempt.getUserId());
        entity.setQuestId(minigameAttempt.getQuestId());
        entity.setMinigameId(minigameAttempt.getMinigameId());
        entity.setScore(minigameAttempt.getScore());
        entity.setStatus(minigameAttempt.getStatus());
        entity.setStartDate(minigameAttempt.getStartDate());
        entity.setEndDate(minigameAttempt.getEndDate());
        entity.setMetadata(minigameAttempt.getMetadata());
        entity.setSuccessful(minigameAttempt.getSuccessful());
        return entity;
    }
}
