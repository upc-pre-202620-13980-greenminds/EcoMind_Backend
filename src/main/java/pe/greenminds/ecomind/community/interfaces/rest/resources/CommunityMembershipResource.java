package pe.greenminds.ecomind.community.interfaces.rest.resources;

import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityRole;

public record CommunityMembershipResource(Long id,Long community_id,Long user_id,CommunityRole role) {}
