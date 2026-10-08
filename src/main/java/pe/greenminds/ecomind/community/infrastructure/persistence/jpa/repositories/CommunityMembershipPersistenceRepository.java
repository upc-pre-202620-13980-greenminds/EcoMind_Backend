package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityMembershipPersistenceEntity;

public interface CommunityMembershipPersistenceRepository extends JpaRepository<CommunityMembershipPersistenceEntity,Long> {
  List<CommunityMembershipPersistenceEntity> findByUserId(Long userId);
  List<CommunityMembershipPersistenceEntity> findByCommunityId(Long communityId);
  Optional<CommunityMembershipPersistenceEntity> findByCommunityIdAndUserId(Long communityId,Long userId);
  boolean existsByUserIdAndCommunityIdNotAndRole(Long userId,Long communityId,String role);
  long countByCommunityId(Long communityId);
}
