package pe.greenminds.ecomind.community.domain.repositories;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;

public interface CommunityGoalRepository{
    CommunityGoal save(CommunityGoal goal);
    Optional<CommunityGoal> findById(Long id);
    List<CommunityGoal> findAll(Long communityId);
    boolean existsActiveByCommunityId(Long communityId);
}
