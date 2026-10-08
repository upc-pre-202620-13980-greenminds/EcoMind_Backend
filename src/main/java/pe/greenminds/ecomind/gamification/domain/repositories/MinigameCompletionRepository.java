package pe.greenminds.ecomind.gamification.domain.repositories;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.util.UUID;

public interface MinigameCompletionRepository {
    long count(UserId user, UUID minigame, Instant from, Instant to);

    void record(UUID execution, UserId user, UUID minigame, Instant at);
}
