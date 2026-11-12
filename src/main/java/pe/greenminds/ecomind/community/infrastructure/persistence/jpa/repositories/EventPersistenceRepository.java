package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.EventPersistenceEntity;

public interface EventPersistenceRepository extends JpaRepository<EventPersistenceEntity, Long> {

    List<EventPersistenceEntity> findByCommunityId(Long communityId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from EventPersistenceEntity event where event.id = :id")
    Optional<EventPersistenceEntity> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from EventPersistenceEntity event where event.communityId = :communityId order by event.id")
    List<EventPersistenceEntity> findByCommunityIdForUpdate(@Param("communityId") Long communityId);
}
