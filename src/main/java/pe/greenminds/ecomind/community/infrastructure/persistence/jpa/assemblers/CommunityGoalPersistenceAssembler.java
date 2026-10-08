package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityGoalPersistenceEntity;

public final class CommunityGoalPersistenceAssembler{
    private CommunityGoalPersistenceAssembler(){}
    public static CommunityGoal toDomain(CommunityGoalPersistenceEntity persistenceEntity){
        return new CommunityGoal(persistenceEntity.getId(), persistenceEntity.getCommunityId(),
                persistenceEntity.getTopic(), persistenceEntity.getTarget(), persistenceEntity.getProgress(),
                persistenceEntity.getParticipants(), persistenceEntity.getStatus());
    }
    public static CommunityGoalPersistenceEntity toEntity(CommunityGoal communityGoal){
        return new CommunityGoalPersistenceEntity(communityGoal.communityId(), communityGoal.topic(),
                communityGoal.target(), communityGoal.progress(), communityGoal.participants(), communityGoal.status());
    }
}
