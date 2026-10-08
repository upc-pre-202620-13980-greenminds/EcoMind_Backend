package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;
import java.util.List; import org.springframework.data.jpa.repository.JpaRepository; import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.PostPersistenceEntity;
public interface PostPersistenceRepository extends JpaRepository<PostPersistenceEntity,Long>{List<PostPersistenceEntity> findByCommunityIdOrderByCreatedAtDesc(Long communityId);}
