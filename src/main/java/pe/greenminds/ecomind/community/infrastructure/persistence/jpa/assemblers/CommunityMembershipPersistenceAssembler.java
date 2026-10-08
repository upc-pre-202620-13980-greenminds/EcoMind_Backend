package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityMembership;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityMembershipPersistenceEntity;

public final class CommunityMembershipPersistenceAssembler {
    private CommunityMembershipPersistenceAssembler() {
    }

    public static CommunityMembership toDomain(CommunityMembershipPersistenceEntity e) {
        return new CommunityMembership(e.getId(), e.getCommunityId(), e.getUserId(), e.getRole());
    }

    public static CommunityMembershipPersistenceEntity toEntity(CommunityMembership m) {
        return new CommunityMembershipPersistenceEntity(m.communityId(), m.userId(), m.role());
    }
}
