package pe.greenminds.ecomind.community.application.queryservices;
import java.util.List;import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityAchievement;import pe.greenminds.ecomind.community.domain.model.queries.SearchCommunityAchievementsQuery;
public interface CommunityAchievementQueryService{List<CommunityAchievement> handle(SearchCommunityAchievementsQuery query);}
