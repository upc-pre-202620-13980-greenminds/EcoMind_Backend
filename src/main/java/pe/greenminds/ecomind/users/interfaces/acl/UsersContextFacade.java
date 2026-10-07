package pe.greenminds.ecomind.users.interfaces.acl;

import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.users.application.queryservices.FamilyQueryService;
import pe.greenminds.ecomind.users.application.queryservices.FriendshipQueryService;
import pe.greenminds.ecomind.users.application.queryservices.ProfileQueryService;
import pe.greenminds.ecomind.users.domain.model.queries.GetAllFamiliesQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetAllUserProfilesQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetFamilyQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetFriendsByUserQuery;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FriendshipStatus;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

/** Public integration boundary; other contexts receive primitive identities only. */
@Service
public class UsersContextFacade {
  private final FamilyQueryService families;

  private final ProfileQueryService profiles;
  private final FriendshipQueryService friendships;

  public UsersContextFacade(FamilyQueryService families, ProfileQueryService profiles,
      FriendshipQueryService friendships) {
    this.families = families;
    this.profiles = profiles;
    this.friendships = friendships;
  }

  public record RankingParticipant(Long id, String displayName) {}

  public List<RankingParticipant> rankingUsers() {
    return profiles.handle(new GetAllUserProfilesQuery()).stream()
        .map(profile -> new RankingParticipant(profile.getUserId().value(), profile.getName())).toList();
  }

  public List<RankingParticipant> rankingFriends(Long requestedBy) {
    var allowed = new HashSet<Long>();
    allowed.add(requestedBy);
    for (var friendship : friendships.handle(new GetFriendsByUserQuery(new UserId(requestedBy)))) {
      if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
        allowed.add(friendship.getRequesterId().value());
        allowed.add(friendship.getReceiverId().value());
      }
    }
    return rankingUsers().stream().filter(user -> allowed.contains(user.id())).toList();
  }

  public List<RankingParticipant> rankingFamilies() {
    return families.handle(new GetAllFamiliesQuery()).stream()
        .map(family -> new RankingParticipant(family.getId().value(), family.getName())).toList();
  }

  public boolean familyExists(Long familyId) {
    return families.handle(new GetFamilyQuery(new FamilyId(familyId))).isPresent();
  }

  public boolean isFamilyMember(Long familyId, Long userId) {
    return families.handle(new GetFamilyQuery(new FamilyId(familyId)))
        .map(family -> family.hasMember(new UserId(userId))).orElse(false);
  }
}
