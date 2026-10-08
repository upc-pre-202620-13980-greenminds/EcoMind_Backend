package pe.greenminds.ecomind.users.interfaces.acl;

import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository;
import pe.greenminds.ecomind.users.domain.repositories.FriendshipRepository;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

/**
 * Read-only entry point of Users for the other bounded contexts, to validate users, friendships
 * and families.
 *
 * <p>It only receives and returns simple values, so the callers never depend on the Users model.
 * Every id must be a positive number: a null or non-positive id is a mistake of the caller and
 * raises an {@link IllegalArgumentException}.</p>
 */
@Component
@Transactional(readOnly = true)
public class UsersContextFacade {

  private final UserProfileRepository userProfileRepository;
  private final FriendshipRepository friendshipRepository;
  private final FamilyRepository familyRepository;

  public UsersContextFacade(
      UserProfileRepository userProfileRepository,
      FriendshipRepository friendshipRepository,
      FamilyRepository familyRepository) {
    this.userProfileRepository = userProfileRepository;
    this.friendshipRepository = friendshipRepository;
    this.familyRepository = familyRepository;
  }

  /** True when the user has a profile. */
  public boolean existsUser(Long userId) {
    return userProfileRepository.existsById(new UserId(userId));
  }

  /**
   * True only when a friendship between the two users exists and was accepted. It does not matter
   * which of them sent the request; a pending or rejected request is not a friendship.
   */
  public boolean areFriends(Long userId, Long otherUserId) {
    return friendshipRepository
        .findBetween(new UserId(userId), new UserId(otherUserId))
        .map(Friendship::isAccepted)
        .orElse(false);
  }

  /** True when the user belongs to the family; false also when the family does not exist. */
  public boolean isFamilyMember(Long familyId, Long userId) {
    UserId memberId = new UserId(userId);
    return findFamily(familyId).map(family -> family.hasMember(memberId)).orElse(false);
  }

  /** Ids of the users who belong to the family; empty when the family does not exist. */
  public List<Long> getFamilyMemberIds(Long familyId) {
    return findFamily(familyId)
        .map(Family::getMembers)
        .orElseGet(List::of)
        .stream()
        .map(member -> member.getUserId().value())
        .toList();
  }

  /**
   * Role of the user in the family, PARENT or CHILD; empty when the family does not exist or the
   * user is not one of its members.
   */
  public Optional<String> getFamilyRole(Long familyId, Long userId) {
    UserId memberId = new UserId(userId);
    return findFamily(familyId)
        .flatMap(family -> family.findMemberByUser(memberId))
        .map(member -> member.getFamilyRole().name());
  }

  /** Id of the family the user belongs to; empty when the user has no family. */
  public Optional<Long> getFamilyIdOfUser(Long userId) {
    return familyRepository
        .findByMemberUserId(new UserId(userId))
        .map(family -> family.getId().value());
  }

  /** Permitted public names and canonical identities; no profile scores are ranking truth. */
  public record RankingParticipant(Long id, String displayName) {}

  public List<RankingParticipant> rankingUsers() {
    return userProfileRepository.findAll().stream()
      .map(profile -> new RankingParticipant(profile.getUserId().value(), profile.getName())).toList();
  }

  public List<RankingParticipant> rankingFriends(Long requestedBy) {
    var requester = new UserId(requestedBy);
    var allowed = new HashSet<Long>();
    allowed.add(requestedBy);
    for (var friendship : friendshipRepository.findByUserId(requester)) {
      if (friendship.isAccepted()) {
        allowed.add(friendship.getRequesterId().value());
        allowed.add(friendship.getReceiverId().value());
      }
    }
    return rankingUsers().stream().filter(user -> allowed.contains(user.id())).toList();
  }

  public List<RankingParticipant> rankingFamilies() {
    return familyRepository.findAll().stream()
      .map(family -> new RankingParticipant(family.getId().value(), family.getName())).toList();
  }

  public boolean familyExists(Long familyId) {
    return findFamily(familyId).isPresent();
  }

  private Optional<Family> findFamily(Long familyId) {
    return familyRepository.findById(new FamilyId(familyId));
  }
}
