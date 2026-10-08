package pe.greenminds.ecomind.community.application.queryservices;
import java.util.List;import pe.greenminds.ecomind.community.domain.model.aggregates.PostReaction;import pe.greenminds.ecomind.community.domain.model.queries.GetPostReactionsQuery;
public interface PostReactionQueryService{List<PostReaction> handle(GetPostReactionsQuery query);long countByPostId(Long postId);}
