package pe.greenminds.ecomind.community.domain.repositories;

import java.util.List;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityAchievement;

public interface CommunityAchievementRepository {
    CommunityAchievement save(CommunityAchievement achievement);

    List<CommunityAchievement> findAll(Long communityId);
}
