package pe.greenminds.ecomind.users.interfaces.acl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.entities.FamilyMember;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FriendshipStatus;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository;
import pe.greenminds.ecomind.users.domain.repositories.FriendshipRepository;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

@ExtendWith(MockitoExtension.class)
class UsersContextFacadeTests {

  private static final Long LUIS = 1L;
  private static final Long ANA = 2L;
  private static final Long PARENT = 7L;
  private static final Long CHILD = 8L;
  private static final Long OUTSIDER = 9L;
  private static final Long FAMILY = 10L;
  private static final Long UNKNOWN_FAMILY = 99L;

  @Mock
  private UserProfileRepository userProfileRepository;
  @Mock
  private FriendshipRepository friendshipRepository;
  @Mock
  private FamilyRepository familyRepository;

  private UsersContextFacade facade;

  @BeforeEach
  void setUp() {
    facade = new UsersContextFacade(userProfileRepository, friendshipRepository, familyRepository);
  }

  @Test
  void existsUserTellsWhetherTheUserHasAProfile() {
    when(userProfileRepository.existsById(new UserId(LUIS))).thenReturn(true);
    when(userProfileRepository.existsById(new UserId(OUTSIDER))).thenReturn(false);

    assertThat(facade.existsUser(LUIS)).isTrue();
    assertThat(facade.existsUser(OUTSIDER)).isFalse();
  }

  @Test
  void usersWithAnAcceptedFriendshipAreFriends() {
    givenFriendshipBetween(LUIS, ANA, FriendshipStatus.ACCEPTED);

    assertThat(facade.areFriends(LUIS, ANA)).isTrue();
  }

  @Test
  void friendshipDoesNotDependOnWhoSentTheRequest() {
    // Luis sent the request; the question is asked from Ana's side.
    Friendship sentByLuis = friendship(LUIS, ANA, FriendshipStatus.ACCEPTED);
    when(friendshipRepository.findBetween(new UserId(ANA), new UserId(LUIS)))
        .thenReturn(Optional.of(sentByLuis));

    assertThat(facade.areFriends(ANA, LUIS)).isTrue();
  }

  @Test
  void usersWithAPendingRequestAreNotFriends() {
    givenFriendshipBetween(LUIS, ANA, FriendshipStatus.PENDING);

    assertThat(facade.areFriends(LUIS, ANA)).isFalse();
  }

  @Test
  void usersWithARejectedRequestAreNotFriends() {
    givenFriendshipBetween(LUIS, ANA, FriendshipStatus.REJECTED);

    assertThat(facade.areFriends(LUIS, ANA)).isFalse();
  }

  @Test
  void usersWithoutAnyRequestAreNotFriends() {
    when(friendshipRepository.findBetween(new UserId(LUIS), new UserId(ANA)))
        .thenReturn(Optional.empty());

    assertThat(facade.areFriends(LUIS, ANA)).isFalse();
  }

  @Test
  void aMemberOfTheFamilyIsAFamilyMember() {
    givenFamilyWithParentAndChild();

    assertThat(facade.isFamilyMember(FAMILY, PARENT)).isTrue();
    assertThat(facade.isFamilyMember(FAMILY, CHILD)).isTrue();
  }

  @Test
  void aUserOutsideTheFamilyIsNotAFamilyMember() {
    givenFamilyWithParentAndChild();

    assertThat(facade.isFamilyMember(FAMILY, OUTSIDER)).isFalse();
  }

  @Test
  void nobodyIsAMemberOfAFamilyThatDoesNotExist() {
    givenUnknownFamily();

    assertThat(facade.isFamilyMember(UNKNOWN_FAMILY, PARENT)).isFalse();
  }

  @Test
  void getFamilyMemberIdsReturnsTheIdsOfEveryMember() {
    givenFamilyWithParentAndChild();

    assertThat(facade.getFamilyMemberIds(FAMILY)).containsExactly(PARENT, CHILD);
  }

  @Test
  void getFamilyMemberIdsIsEmptyForAFamilyThatDoesNotExist() {
    givenUnknownFamily();

    assertThat(facade.getFamilyMemberIds(UNKNOWN_FAMILY)).isEmpty();
  }

  @Test
  void getFamilyRoleReturnsTheRoleOfEachMember() {
    givenFamilyWithParentAndChild();

    assertThat(facade.getFamilyRole(FAMILY, PARENT)).contains("PARENT");
    assertThat(facade.getFamilyRole(FAMILY, CHILD)).contains("CHILD");
  }

  @Test
  void getFamilyRoleIsEmptyForAUserOutsideTheFamily() {
    givenFamilyWithParentAndChild();

    assertThat(facade.getFamilyRole(FAMILY, OUTSIDER)).isEmpty();
  }

  @Test
  void getFamilyRoleIsEmptyForAFamilyThatDoesNotExist() {
    givenUnknownFamily();

    assertThat(facade.getFamilyRole(UNKNOWN_FAMILY, PARENT)).isEmpty();
  }

  @Test
  void getFamilyIdOfUserReturnsTheFamilyTheUserBelongsTo() {
    when(familyRepository.findByMemberUserId(new UserId(CHILD)))
        .thenReturn(Optional.of(familyWithParentAndChild()));

    assertThat(facade.getFamilyIdOfUser(CHILD)).contains(FAMILY);
  }

  @Test
  void getFamilyIdOfUserIsEmptyForAUserWithoutFamily() {
    when(familyRepository.findByMemberUserId(new UserId(OUTSIDER))).thenReturn(Optional.empty());

    assertThat(facade.getFamilyIdOfUser(OUTSIDER)).isEmpty();
  }

  @Test
  void idsThatAreNullOrNotPositiveAreRejected() {
    assertThatThrownBy(() -> facade.existsUser(null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> facade.areFriends(LUIS, 0L))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> facade.isFamilyMember(-1L, PARENT))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> facade.getFamilyMemberIds(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private void givenFriendshipBetween(Long requesterId, Long receiverId, FriendshipStatus status) {
    when(friendshipRepository.findBetween(new UserId(requesterId), new UserId(receiverId)))
        .thenReturn(Optional.of(friendship(requesterId, receiverId, status)));
  }

  private void givenFamilyWithParentAndChild() {
    when(familyRepository.findById(new FamilyId(FAMILY)))
        .thenReturn(Optional.of(familyWithParentAndChild()));
  }

  private void givenUnknownFamily() {
    when(familyRepository.findById(new FamilyId(UNKNOWN_FAMILY))).thenReturn(Optional.empty());
  }

  private static Friendship friendship(Long requesterId, Long receiverId, FriendshipStatus status) {
    return new Friendship(
        50L, new UserId(requesterId), new UserId(receiverId), status, Instant.now());
  }

  private static Family familyWithParentAndChild() {
    return new Family(
        new FamilyId(FAMILY),
        "Torres Family",
        "Save water",
        List.of(
            new FamilyMember(100L, new UserId(PARENT), FamilyRole.PARENT),
            new FamilyMember(101L, new UserId(CHILD), FamilyRole.CHILD)));
  }
}
