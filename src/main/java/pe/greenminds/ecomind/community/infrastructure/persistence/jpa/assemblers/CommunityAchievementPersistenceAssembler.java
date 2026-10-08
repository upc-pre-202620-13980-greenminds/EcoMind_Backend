package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityAchievement;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityAchievementPersistenceEntity;

public final class CommunityAchievementPersistenceAssembler {
    private CommunityAchievementPersistenceAssembler() {
    }

    public static CommunityAchievement toDomain(CommunityAchievementPersistenceEntity persistenceEntity) {
        return new CommunityAchievement(persistenceEntity.getId(), persistenceEntity.getCommunityId(),
                persistenceEntity.getTitle(), persistenceEntity.getDescription(), persistenceEntity.getCommunityGoalId());
    }

    public static CommunityAchievementPersistenceEntity toEntity(CommunityAchievement communityAchievement) {
        return new CommunityAchievementPersistenceEntity(communityAchievement.communityId(),
                communityAchievement.title(), communityAchievement.description(), communityAchievement.communityGoalId());
    }
}
