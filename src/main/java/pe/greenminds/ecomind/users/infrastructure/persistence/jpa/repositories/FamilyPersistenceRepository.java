package pe.greenminds.ecomind.users.infrastructure.persistence.jpa.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.entities.FamilyPersistenceEntity;

public interface FamilyPersistenceRepository extends JpaRepository<FamilyPersistenceEntity, Long> {

  Optional<FamilyPersistenceEntity> findByMembersUserId(Long userId);

  Optional<FamilyPersistenceEntity> findByMembersId(Long familyMemberId);

  boolean existsByMembersUserId(Long userId);
}
