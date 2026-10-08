package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.community.domain.model.aggregates.Post;
import pe.greenminds.ecomind.community.domain.repositories.PostRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers.PostPersistenceAssembler;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.PostPersistenceRepository;

@Repository
public class PostRepositoryImpl implements PostRepository {
    private final PostPersistenceRepository repository;

    public PostRepositoryImpl(PostPersistenceRepository persistenceRepository) {
        repository = persistenceRepository;
    }

    @Override
    public Post save(Post post) {
        return PostPersistenceAssembler.toDomain(repository.save(PostPersistenceAssembler.toEntity(post)));
    }

    @Override
    public Optional<Post> findById(Long id) {
        return repository.findById(id).map(PostPersistenceAssembler::toDomain);
    }

    @Override
    public List<Post> findAll(Long communityId) {
        return (communityId == null ? repository.findAll() : repository.findByCommunityIdOrderByCreatedAtDesc(communityId)).stream()
                .map(PostPersistenceAssembler::toDomain).toList();
    }

    @Override
    public void delete(Post post) {
        repository.deleteById(post.id());
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
