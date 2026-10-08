package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.GamificationOutboxPersistenceEntity;

import java.time.Instant;
import java.util.List;

public interface GamificationOutboxPersistenceRepository
        extends JpaRepository<GamificationOutboxPersistenceEntity, String> {
    List<GamificationOutboxPersistenceEntity>
            findTop50ByDeliveredAtIsNullAndNextAttemptAtLessThanEqualOrderByOccurredAt(Instant at);
}
