package pe.greenminds.ecomind.community.domain.repositories;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.community.domain.model.aggregates.PostReaction;

public interface PostReactionRepository {
    PostReaction save(PostReaction reaction);

    List<PostReaction> findByPostId(Long postId);

    Optional<PostReaction> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    void delete(PostReaction reaction);
}
