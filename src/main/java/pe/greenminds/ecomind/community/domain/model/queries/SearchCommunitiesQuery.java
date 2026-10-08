package pe.greenminds.ecomind.community.domain.model.queries;

import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;

public record SearchCommunitiesQuery(CommunityType type, String locality) {}
