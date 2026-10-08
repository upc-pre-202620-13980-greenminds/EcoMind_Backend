package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityGoalPersistenceEntity;

public interface CommunityGoalPersistenceRepository extends JpaRepository<CommunityGoalPersistenceEntity,Long>{
    List<CommunityGoalPersistenceEntity> findByCommunityId(Long id);
    boolean existsByCommunityIdAndStatus(Long id,String status);
}
