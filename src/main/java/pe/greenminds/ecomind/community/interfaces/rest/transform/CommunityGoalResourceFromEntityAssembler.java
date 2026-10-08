package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CommunityGoalResource;

public final class CommunityGoalResourceFromEntityAssembler{
    private CommunityGoalResourceFromEntityAssembler(){}
    public static CommunityGoalResource toResourceFromEntity(CommunityGoal communityGoal){
        return new CommunityGoalResource(communityGoal.id(), communityGoal.communityId(), communityGoal.topic(),
                communityGoal.title(), communityGoal.target(), communityGoal.progress(), communityGoal.participants(),
                communityGoal.status());
    }
}
