package pe.greenminds.ecomind.community.domain.model.aggregates;

import java.util.Objects;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityRole;

public record CommunityMembership(Long id, Long communityId, Long userId, CommunityRole role) {
  public CommunityMembership {
    if (communityId == null || userId == null) throw new IllegalArgumentException("Community and user are required");
    Objects.requireNonNull(role, "Membership role is required");
  }
}
