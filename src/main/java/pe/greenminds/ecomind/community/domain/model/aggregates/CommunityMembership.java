package pe.greenminds.ecomind.community.domain.model.aggregates;

public record CommunityMembership(Long id, Long communityId, Long userId, String role) {
  public CommunityMembership {
    if (communityId == null || userId == null) throw new IllegalArgumentException("Community and user are required");
    if (!"ADMIN".equals(role) && !"MEMBER".equals(role)) throw new IllegalArgumentException("Invalid membership role");
  }
}
