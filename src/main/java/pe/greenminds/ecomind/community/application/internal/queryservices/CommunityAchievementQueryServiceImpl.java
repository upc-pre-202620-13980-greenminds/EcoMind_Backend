package pe.greenminds.ecomind.community.application.internal.queryservices;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.community.application.queryservices.CommunityAchievementQueryService;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityAchievement;
import pe.greenminds.ecomind.community.domain.model.queries.SearchCommunityAchievementsQuery;
import pe.greenminds.ecomind.community.domain.repositories.CommunityAchievementRepository;

@Service
public class CommunityAchievementQueryServiceImpl implements CommunityAchievementQueryService {
    private final CommunityAchievementRepository achievements;

    public CommunityAchievementQueryServiceImpl(CommunityAchievementRepository a) {
        achievements = a;
    }

    public List<CommunityAchievement> handle(SearchCommunityAchievementsQuery q) {
        return achievements.findAll(q.communityId());
    }
}
