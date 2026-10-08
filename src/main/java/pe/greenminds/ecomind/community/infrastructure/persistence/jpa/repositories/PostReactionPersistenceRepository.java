package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;
import java.util.List; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository; import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.PostReactionPersistenceEntity;
public interface PostReactionPersistenceRepository extends JpaRepository<PostReactionPersistenceEntity,Long>{List<PostReactionPersistenceEntity> findByPostId(Long postId);Optional<PostReactionPersistenceEntity> findByPostIdAndUserId(Long postId,Long userId);long countByPostId(Long postId);}
