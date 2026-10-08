package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityMembership;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CommunityMembershipResource;

public final class CommunityMembershipResourceFromEntityAssembler {
    private CommunityMembershipResourceFromEntityAssembler() {
    }

    public static CommunityMembershipResource toResourceFromEntity(CommunityMembership communityMembership) {
        return new CommunityMembershipResource(communityMembership.id(), communityMembership.communityId(),
                communityMembership.userId(), communityMembership.role());
    }
}
