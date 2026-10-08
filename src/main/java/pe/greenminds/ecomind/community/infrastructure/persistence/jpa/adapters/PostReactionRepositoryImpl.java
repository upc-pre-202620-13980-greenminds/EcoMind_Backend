package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.community.domain.model.aggregates.PostReaction;
import pe.greenminds.ecomind.community.domain.repositories.PostReactionRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers.PostReactionPersistenceAssembler;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.PostReactionPersistenceRepository;

@Repository
public class PostReactionRepositoryImpl implements PostReactionRepository {
    private final PostReactionPersistenceRepository repository;

    public PostReactionRepositoryImpl(PostReactionPersistenceRepository r) {
        repository = r;
    }

    public PostReaction save(PostReaction r) {
        var existing = repository.findByPostIdAndUserId(r.postId(), r.userId());
        if (existing.isPresent()) {
            existing.get().changeType(r.reactionType());
            return PostReactionPersistenceAssembler.toDomain(existing.get());
        }
        return PostReactionPersistenceAssembler.toDomain(repository.save(PostReactionPersistenceAssembler.toEntity(r)));
    }

    public List<PostReaction> findByPostId(Long id) {
        return repository.findByPostId(id).stream().map(PostReactionPersistenceAssembler::toDomain).toList();
    }

    public Optional<PostReaction> findByPostIdAndUserId(Long p, Long u) {
        return repository.findByPostIdAndUserId(p, u).map(PostReactionPersistenceAssembler::toDomain);
    }

    public long countByPostId(Long id) {
        return repository.countByPostId(id);
    }

    public void delete(PostReaction r) {
        repository.findByPostIdAndUserId(r.postId(), r.userId()).ifPresent(repository::delete);
    }
}
