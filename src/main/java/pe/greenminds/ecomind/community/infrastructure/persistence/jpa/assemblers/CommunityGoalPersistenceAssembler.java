package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityGoalPersistenceEntity;

public final class CommunityGoalPersistenceAssembler{
    private CommunityGoalPersistenceAssembler(){}
    public static CommunityGoal toDomain(CommunityGoalPersistenceEntity e){
        return new CommunityGoal(e.getId(), e.getCommunityId(), e.getTopic(), e.getTarget(),
                e.getProgress(), e.getParticipants(), e.getStatus());
    }
    public static CommunityGoalPersistenceEntity toEntity(CommunityGoal g){
        return new CommunityGoalPersistenceEntity(g.communityId(), g.topic(), g.target(),
                g.progress(), g.participants(), g.status());
    }
}
