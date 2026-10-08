package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CommunityGoalResource;

public final class CommunityGoalResourceFromEntityAssembler{
    private CommunityGoalResourceFromEntityAssembler(){}
    public static CommunityGoalResource toResourceFromEntity(CommunityGoal g){
        return new CommunityGoalResource(g.id(), g.communityId(), g.topic(), g.title(), g.target(),
                g.progress(), g.participants(), g.status());
    }
}
