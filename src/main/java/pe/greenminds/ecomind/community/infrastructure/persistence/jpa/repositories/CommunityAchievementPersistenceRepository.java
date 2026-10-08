package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;
import java.util.List; import org.springframework.data.jpa.repository.JpaRepository; import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityAchievementPersistenceEntity;
public interface CommunityAchievementPersistenceRepository extends JpaRepository<CommunityAchievementPersistenceEntity,Long>{List<CommunityAchievementPersistenceEntity> findByCommunityId(Long id);}
