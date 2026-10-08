package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.Community;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CommunityResource;

public final class CommunityResourceFromEntityAssembler {
    private CommunityResourceFromEntityAssembler() {
    }

    public static CommunityResource toResourceFromEntity(Community c) {
        return new CommunityResource(c.getId(), c.getName(), c.getDescription(), c.getType(), c.getTopic(),
                c.getLocality(), c.getMemberLimit(), c.getIconUrl(), c.getCreatedBy());
    }
}
