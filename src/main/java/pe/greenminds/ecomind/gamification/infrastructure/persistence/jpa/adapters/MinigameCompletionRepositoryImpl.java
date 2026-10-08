package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.MinigameCompletionRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.MinigameCompletionPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

@Repository
public class MinigameCompletionRepositoryImpl implements MinigameCompletionRepository {
    private final EntityManager entities;

    public MinigameCompletionRepositoryImpl(EntityManager entities) {
        this.entities = entities;
    }

    public long count(UserId user, UUID game, Instant from, Instant to) {
        return entities.createQuery(
                        "select count(m) from MinigameCompletionPersistenceEntity m where"
                                + " userId=:user and minigameId=:game and occurredAt>=:from and"
                                + " occurredAt<=:to",
                        Long.class)
                .setParameter("user", user.value())
                .setParameter("game", game.toString())
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
    }

    public void record(UUID execution, UserId user, UUID game, Instant at) {
        var row = new MinigameCompletionPersistenceEntity();
        row.setExecutionId(execution.toString());
        row.setUserId(user.value());
        row.setMinigameId(game.toString());
        row.setOccurredAt(at);
        entities.persist(row);
    }
}
