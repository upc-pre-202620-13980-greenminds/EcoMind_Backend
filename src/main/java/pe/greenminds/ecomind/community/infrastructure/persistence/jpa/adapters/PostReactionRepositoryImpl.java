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

    public PostReactionRepositoryImpl(PostReactionPersistenceRepository persistenceRepository) {
        repository = persistenceRepository;
    }

    @Override
    public PostReaction save(PostReaction postReaction) {
        var existing = repository.findByPostIdAndUserId(postReaction.postId(), postReaction.userId());
        if (existing.isPresent()) {
            existing.get().changeType(postReaction.reactionType());
            return PostReactionPersistenceAssembler.toDomain(existing.get());
        }
        return PostReactionPersistenceAssembler.toDomain(repository.save(PostReactionPersistenceAssembler.toEntity(postReaction)));
    }

    @Override
    public List<PostReaction> findByPostId(Long id) {
        return repository.findByPostId(id).stream().map(PostReactionPersistenceAssembler::toDomain).toList();
    }

    @Override
    public Optional<PostReaction> findByPostIdAndUserId(Long postId, Long userId) {
        return repository.findByPostIdAndUserId(postId, userId).map(PostReactionPersistenceAssembler::toDomain);
    }

    @Override
    public long countByPostId(Long id) {
        return repository.countByPostId(id);
    }

    @Override
    public void delete(PostReaction postReaction) {
        repository.findByPostIdAndUserId(postReaction.postId(), postReaction.userId()).ifPresent(repository::delete);
    }
}
