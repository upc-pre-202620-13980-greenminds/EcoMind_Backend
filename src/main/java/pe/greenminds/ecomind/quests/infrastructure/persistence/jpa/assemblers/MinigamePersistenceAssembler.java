package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Minigame;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.MinigamePersistenceEntity;

public final class MinigamePersistenceAssembler {
    private MinigamePersistenceAssembler() {
    }

    public static Minigame toDomainFromPersistence(MinigamePersistenceEntity entity) {
        return new Minigame(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getUrl(),
                entity.getCompletionRules()
        );
    }

    public static MinigamePersistenceEntity toPersistenceFromDomain(Minigame minigame) {
        var entity = new MinigamePersistenceEntity();
        entity.setId(minigame.getId());
        entity.setName(minigame.getName());
        entity.setDescription(minigame.getDescription());
        entity.setUrl(minigame.getUrl());
        entity.setCompletionRules(minigame.getCompletionRules());
        return entity;
    }
}
