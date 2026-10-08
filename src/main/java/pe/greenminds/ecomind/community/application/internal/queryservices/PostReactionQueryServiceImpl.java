package pe.greenminds.ecomind.community.application.internal.queryservices;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.community.application.queryservices.PostReactionQueryService;
import pe.greenminds.ecomind.community.domain.model.aggregates.PostReaction;
import pe.greenminds.ecomind.community.domain.model.queries.GetPostReactionsQuery;
import pe.greenminds.ecomind.community.domain.repositories.PostReactionRepository;
import pe.greenminds.ecomind.community.domain.repositories.PostRepository;

@Service
public class PostReactionQueryServiceImpl implements PostReactionQueryService {
    private final PostReactionRepository reactions;
    private final PostRepository posts;

    public PostReactionQueryServiceImpl(PostReactionRepository r, PostRepository p) {
        reactions = r;
        posts = p;
    }

    public List<PostReaction> handle(GetPostReactionsQuery q) {
        if (!posts.existsById(q.postId()))
            throw new IllegalArgumentException("Post not found");
        return reactions.findByPostId(q.postId());
    }

    public long countByPostId(Long id) {
        return reactions.countByPostId(id);
    }
}
