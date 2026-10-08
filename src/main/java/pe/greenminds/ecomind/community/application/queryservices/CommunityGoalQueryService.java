package pe.greenminds.ecomind.community.application.queryservices;
import java.util.List;import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;import pe.greenminds.ecomind.community.domain.model.queries.SearchCommunityGoalsQuery;
public interface CommunityGoalQueryService{List<CommunityGoal> handle(SearchCommunityGoalsQuery query);}
