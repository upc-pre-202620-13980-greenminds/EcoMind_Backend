package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityMembership;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityMembershipPersistenceEntity;

public final class CommunityMembershipPersistenceAssembler {
    private CommunityMembershipPersistenceAssembler() {
    }

    public static CommunityMembership toDomain(CommunityMembershipPersistenceEntity persistenceEntity) {
        return new CommunityMembership(persistenceEntity.getId(), persistenceEntity.getCommunityId(),
                persistenceEntity.getUserId(), persistenceEntity.getRole());
    }

    public static CommunityMembershipPersistenceEntity toEntity(CommunityMembership communityMembership) {
        return new CommunityMembershipPersistenceEntity(communityMembership.communityId(),
                communityMembership.userId(), communityMembership.role());
    }
}
