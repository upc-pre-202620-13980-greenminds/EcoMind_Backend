package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.EventPersistenceEntity;

public interface EventPersistenceRepository extends JpaRepository<EventPersistenceEntity,Long>{List<EventPersistenceEntity> findByCommunityId(Long communityId);}
