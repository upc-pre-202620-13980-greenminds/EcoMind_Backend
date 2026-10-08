package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityMembership;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CommunityMembershipResource;

public final class CommunityMembershipResourceFromEntityAssembler {
    private CommunityMembershipResourceFromEntityAssembler() {
    }

    public static CommunityMembershipResource toResourceFromEntity(CommunityMembership m) {
        return new CommunityMembershipResource(m.id(), m.communityId(), m.userId(), m.role());
    }
}
