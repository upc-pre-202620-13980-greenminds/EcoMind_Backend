package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityPersistenceEntity;

public interface CommunityPersistenceRepository extends JpaRepository<CommunityPersistenceEntity,Long> {
  List<CommunityPersistenceEntity> findByTypeAndLocalityIgnoreCase(CommunityType type,String locality);
  List<CommunityPersistenceEntity> findByType(CommunityType type);
}
