package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.MinigamePersistenceEntity;

@Repository
public interface MinigamePersistenceRepository
        extends JpaRepository<MinigamePersistenceEntity, Long> {
}
