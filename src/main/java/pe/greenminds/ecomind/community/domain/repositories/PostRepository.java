package pe.greenminds.ecomind.community.domain.repositories;
import java.util.List;import java.util.Optional;import pe.greenminds.ecomind.community.domain.model.aggregates.Post;
public interface PostRepository{Post save(Post post);Optional<Post> findById(Long id);List<Post> findAll(Long communityId);void delete(Post post);boolean existsById(Long id);}
