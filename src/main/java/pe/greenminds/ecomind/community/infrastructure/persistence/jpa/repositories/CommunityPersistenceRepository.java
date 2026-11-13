package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityPersistenceEntity;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPersistenceRepository extends JpaRepository<CommunityPersistenceEntity,Long> {
  List<CommunityPersistenceEntity> findByTypeAndLocalityIgnoreCase(CommunityType type,String locality);
  List<CommunityPersistenceEntity> findByType(CommunityType type);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select community from CommunityPersistenceEntity community where community.id = :id")
  Optional<CommunityPersistenceEntity> findByIdForUpdate(@Param("id") Long id);
}
