package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityAchievement;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityAchievementPersistenceEntity;

public final class CommunityAchievementPersistenceAssembler {
    private CommunityAchievementPersistenceAssembler() {
    }

    public static CommunityAchievement toDomain(CommunityAchievementPersistenceEntity e) {
        return new CommunityAchievement(e.getId(), e.getCommunityId(), e.getTitle(), e.getDescription(),
                e.getCommunityGoalId());
    }

    public static CommunityAchievementPersistenceEntity toEntity(CommunityAchievement a) {
        return new CommunityAchievementPersistenceEntity(a.communityId(), a.title(), a.description(),
                a.communityGoalId());
    }
}
