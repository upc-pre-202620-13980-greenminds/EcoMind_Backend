package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.Community;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CommunityResource;

public final class CommunityResourceFromEntityAssembler {
    private CommunityResourceFromEntityAssembler() {
    }

    public static CommunityResource toResourceFromEntity(Community community) {
        return new CommunityResource(community.getId(), community.getName(), community.getDescription(),
                community.getType(), community.getTopic(), community.getLocality(), community.getMemberLimit(),
                community.getIconUrl(), community.getCreatedBy());
    }
}
