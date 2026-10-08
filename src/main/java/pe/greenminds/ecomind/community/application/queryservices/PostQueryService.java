package pe.greenminds.ecomind.community.application.queryservices;

import java.util.List;
import pe.greenminds.ecomind.community.domain.model.aggregates.Post;
import pe.greenminds.ecomind.community.domain.model.queries.SearchPostsQuery;

public interface PostQueryService {
    List<Post> handle(SearchPostsQuery query);
}
