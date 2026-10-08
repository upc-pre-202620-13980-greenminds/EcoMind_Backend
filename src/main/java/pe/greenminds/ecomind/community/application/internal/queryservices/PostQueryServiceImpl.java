package pe.greenminds.ecomind.community.application.internal.queryservices;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.community.application.queryservices.PostQueryService;
import pe.greenminds.ecomind.community.domain.model.aggregates.Post;
import pe.greenminds.ecomind.community.domain.model.queries.SearchPostsQuery;
import pe.greenminds.ecomind.community.domain.repositories.PostRepository;

@Service
public class PostQueryServiceImpl implements PostQueryService {
    private final PostRepository posts;

    public PostQueryServiceImpl(PostRepository postRepository) {
        posts = postRepository;
    }

    @Override
    public List<Post> handle(SearchPostsQuery query) {
        return posts.findAll(query.communityId());
    }
}
