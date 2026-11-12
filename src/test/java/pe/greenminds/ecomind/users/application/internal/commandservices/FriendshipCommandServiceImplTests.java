package pe.greenminds.ecomind.users.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.commands.RemoveFriendshipCommand;
import pe.greenminds.ecomind.users.domain.model.commands.RespondFriendRequestCommand;
import pe.greenminds.ecomind.users.domain.model.commands.SendFriendRequestCommand;
import pe.greenminds.ecomind.users.domain.model.events.FriendRequestAcceptedEvent;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FriendshipStatus;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.InvitationCode;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.FriendshipRepository;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;
import pe.greenminds.ecomind.users.domain.services.FriendshipPolicy;

@ExtendWith(MockitoExtension.class)
class FriendshipCommandServiceImplTests {

  private static final UserId LUIS = new UserId(1L);
  private static final UserId ANA = new UserId(2L);
  private static final Long FRIENDSHIP_ID = 50L;
  private static final InvitationCode ANA_CODE = new InvitationCode("K7MP4QXZ");

  @Mock
  private FriendshipRepository friendshipRepository;
  @Mock
  private UserProfileRepository userProfileRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  private FriendshipCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new FriendshipCommandServiceImpl(
            friendshipRepository,
            userProfileRepository,
            new FriendshipPolicy(friendshipRepository),
            eventPublisher);
  }

  @Test
  void sendFriendRequestCreatesAPendingRequest() {
    when(friendshipRepository.findBetween(LUIS, ANA)).thenReturn(Optional.empty());
    when(userProfileRepository.existsById(ANA)).thenReturn(true);
    when(friendshipRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result = service.handle(new SendFriendRequestCommand(LUIS, ANA));

    Friendship friendship = result.toOptional().orElseThrow();
    assertThat(friendship.getRequesterId()).isEqualTo(LUIS);
    assertThat(friendship.getReceiverId()).isEqualTo(ANA);
    assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.PENDING);
  }

  @Test
  void sendFriendRequestToOneselfFails() {
    var result = service.handle(new SendFriendRequestCommand(LUIS, LUIS));

    assertThat(errorCodeOf(result)).isEqualTo("FRIEND_REQUEST_TO_SELF");
    verify(friendshipRepository, never()).save(any());
  }

  @Test
  void sendFriendRequestFailsWhenOneAlreadyExistsInAnyDirection() {
    // Ana already sent a request to Luis; the repository finds it whoever asks.
    when(friendshipRepository.findBetween(LUIS, ANA))
        .thenReturn(Optional.of(friendship(ANA, LUIS, FriendshipStatus.PENDING, Instant.now())));

    var result = service.handle(new SendFriendRequestCommand(LUIS, ANA));

    assertThat(errorCodeOf(result)).isEqualTo("FRIENDSHIP_CONFLICT");
    verify(friendshipRepository, never()).save(any());
  }

  @Test
  void sendFriendRequestFailsWhenItWasRejectedLessThanSevenDaysAgo() {
    Instant rejectedAt = Instant.now().minus(6, ChronoUnit.DAYS);
    when(friendshipRepository.findBetween(LUIS, ANA))
        .thenReturn(Optional.of(friendship(LUIS, ANA, FriendshipStatus.REJECTED, rejectedAt)));

    var result = service.handle(new SendFriendRequestCommand(LUIS, ANA));

    assertThat(errorCodeOf(result)).isEqualTo("FRIEND_REQUEST_REJECTED_RECENTLY");
    verify(friendshipRepository, never()).save(any());
  }

  @Test
  void sendFriendRequestReplacesARequestRejectedMoreThanSevenDaysAgo() {
    Instant rejectedAt = Instant.now().minus(8, ChronoUnit.DAYS);
    Friendship rejected = friendship(LUIS, ANA, FriendshipStatus.REJECTED, rejectedAt);
    when(friendshipRepository.findBetween(LUIS, ANA)).thenReturn(Optional.of(rejected));
    when(userProfileRepository.existsById(ANA)).thenReturn(true);
    when(friendshipRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result = service.handle(new SendFriendRequestCommand(LUIS, ANA));

    assertThat(result.toOptional().orElseThrow().getStatus()).isEqualTo(FriendshipStatus.PENDING);
    verify(friendshipRepository).delete(rejected);
  }

  @Test
  void sendFriendRequestFailsWhileAnUnansweredRequestIsWithinTheDeadline() {
    when(friendshipRepository.findBetween(LUIS, ANA))
        .thenReturn(Optional.of(pendingSentHoursAgo(47)));

    var result = service.handle(new SendFriendRequestCommand(LUIS, ANA));

    assertThat(errorCodeOf(result)).isEqualTo("FRIENDSHIP_CONFLICT");
    verify(friendshipRepository, never()).save(any());
  }

  @Test
  void sendFriendRequestFailsLessThanSevenDaysAfterAnUnansweredRequestExpired() {
    // Sent 8 days ago: it expired 6 days ago, so one more day is still needed
    when(friendshipRepository.findBetween(LUIS, ANA))
        .thenReturn(Optional.of(pendingSentHoursAgo(8 * 24)));

    var result = service.handle(new SendFriendRequestCommand(LUIS, ANA));

    assertThat(errorCodeOf(result)).isEqualTo("FRIEND_REQUEST_UNANSWERED_RECENTLY");
    verify(friendshipRepository, never()).save(any());
  }

  @Test
  void sendFriendRequestReplacesARequestThatExpiredMoreThanSevenDaysAgo() {
    // Sent 10 days ago: it expired 8 days ago
    Friendship expired = pendingSentHoursAgo(10 * 24);
    when(friendshipRepository.findBetween(LUIS, ANA)).thenReturn(Optional.of(expired));
    when(userProfileRepository.existsById(ANA)).thenReturn(true);
    when(friendshipRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result = service.handle(new SendFriendRequestCommand(LUIS, ANA));

    assertThat(result.toOptional().orElseThrow().getStatus()).isEqualTo(FriendshipStatus.PENDING);
    verify(friendshipRepository).delete(expired);
  }

  @Test
  void sendFriendRequestWithAnInvitationCodeGoesToItsOwner() {
    when(userProfileRepository.findByInvitationCode(ANA_CODE))
        .thenReturn(Optional.of(anaProfile()));
    when(friendshipRepository.findBetween(LUIS, ANA)).thenReturn(Optional.empty());
    when(userProfileRepository.existsById(ANA)).thenReturn(true);
    when(friendshipRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result = service.handle(new SendFriendRequestCommand(LUIS, ANA_CODE));

    Friendship friendship = result.toOptional().orElseThrow();
    assertThat(friendship.getReceiverId()).isEqualTo(ANA);
    assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.PENDING);
  }

  @Test
  void sendFriendRequestFailsWhenNobodyHasTheInvitationCode() {
    when(userProfileRepository.findByInvitationCode(ANA_CODE)).thenReturn(Optional.empty());

    var result = service.handle(new SendFriendRequestCommand(LUIS, ANA_CODE));

    assertThat(errorCodeOf(result)).isEqualTo("INVITATION_CODE_NOT_FOUND");
    verify(friendshipRepository, never()).save(any());
  }

  @Test
  void sendFriendRequestWithOwnInvitationCodeFails() {
    // Ana uses her own code: the rule against requests to oneself still applies
    when(userProfileRepository.findByInvitationCode(ANA_CODE))
        .thenReturn(Optional.of(anaProfile()));

    var result = service.handle(new SendFriendRequestCommand(ANA, ANA_CODE));

    assertThat(errorCodeOf(result)).isEqualTo("FRIEND_REQUEST_TO_SELF");
    verify(friendshipRepository, never()).save(any());
  }

  @Test
  void sendFriendRequestFailsWhenTheReceiverHasNoProfile() {
    when(friendshipRepository.findBetween(LUIS, ANA)).thenReturn(Optional.empty());
    when(userProfileRepository.existsById(ANA)).thenReturn(false);

    var result = service.handle(new SendFriendRequestCommand(LUIS, ANA));

    assertThat(errorCodeOf(result)).isEqualTo("USER_PROFILE_NOT_FOUND");
  }

  @Test
  void acceptingARequestPublishesTheEvent() {
    Friendship pending = friendship(LUIS, ANA, FriendshipStatus.PENDING, Instant.now());
    when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(pending));
    when(friendshipRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result = service.handle(new RespondFriendRequestCommand(ANA, FRIENDSHIP_ID, true));

    assertThat(result.toOptional().orElseThrow().getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    verify(eventPublisher).publishEvent(any(FriendRequestAcceptedEvent.class));
  }

  @Test
  void rejectingARequestDoesNotPublishTheEvent() {
    Friendship pending = friendship(LUIS, ANA, FriendshipStatus.PENDING, Instant.now());
    when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(pending));
    when(friendshipRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result = service.handle(new RespondFriendRequestCommand(ANA, FRIENDSHIP_ID, false));

    assertThat(result.toOptional().orElseThrow().getStatus()).isEqualTo(FriendshipStatus.REJECTED);
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void onlyTheReceiverCanAnswerARequest() {
    Friendship pending = friendship(LUIS, ANA, FriendshipStatus.PENDING, Instant.now());
    when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(pending));

    var result = service.handle(new RespondFriendRequestCommand(LUIS, FRIENDSHIP_ID, true));

    assertThat(errorCodeOf(result)).isEqualTo("FRIEND_REQUEST_ACCESS_FORBIDDEN");
    verify(friendshipRepository, never()).save(any());
  }

  @Test
  void aRequestCanStillBeAcceptedBeforeTheDeadline() {
    when(friendshipRepository.findById(FRIENDSHIP_ID))
        .thenReturn(Optional.of(pendingSentHoursAgo(47)));
    when(friendshipRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result = service.handle(new RespondFriendRequestCommand(ANA, FRIENDSHIP_ID, true));

    assertThat(result.toOptional().orElseThrow().getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
  }

  @Test
  void anExpiredRequestCannotBeAccepted() {
    when(friendshipRepository.findById(FRIENDSHIP_ID))
        .thenReturn(Optional.of(pendingSentHoursAgo(49)));

    var result = service.handle(new RespondFriendRequestCommand(ANA, FRIENDSHIP_ID, true));

    assertThat(errorCodeOf(result)).isEqualTo("FRIEND_REQUEST_EXPIRED");
    verify(friendshipRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void anExpiredRequestCannotBeRejected() {
    when(friendshipRepository.findById(FRIENDSHIP_ID))
        .thenReturn(Optional.of(pendingSentHoursAgo(49)));

    var result = service.handle(new RespondFriendRequestCommand(ANA, FRIENDSHIP_ID, false));

    assertThat(errorCodeOf(result)).isEqualTo("FRIEND_REQUEST_EXPIRED");
    verify(friendshipRepository, never()).save(any());
  }

  @Test
  void theRequesterCanRemoveAFriendship() {
    Friendship accepted = friendship(LUIS, ANA, FriendshipStatus.ACCEPTED, Instant.now());
    when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(accepted));

    var result = service.handle(new RemoveFriendshipCommand(LUIS, FRIENDSHIP_ID));

    assertThat(result.toOptional()).isPresent();
    verify(friendshipRepository).delete(accepted);
  }

  @Test
  void theReceiverCanRemoveAFriendship() {
    Friendship accepted = friendship(LUIS, ANA, FriendshipStatus.ACCEPTED, Instant.now());
    when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(accepted));

    var result = service.handle(new RemoveFriendshipCommand(ANA, FRIENDSHIP_ID));

    assertThat(result.toOptional()).isPresent();
    verify(friendshipRepository).delete(accepted);
  }

  @Test
  void aPendingRequestWithinTheDeadlineCanBeRemoved() {
    Friendship pending = pendingSentHoursAgo(10);
    when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(pending));

    var result = service.handle(new RemoveFriendshipCommand(LUIS, FRIENDSHIP_ID));

    assertThat(result.toOptional()).isPresent();
    verify(friendshipRepository).delete(pending);
  }

  @Test
  void someoneOutsideTheFriendshipCannotRemoveIt() {
    UserId marta = new UserId(3L);
    when(friendshipRepository.findById(FRIENDSHIP_ID))
        .thenReturn(Optional.of(friendship(LUIS, ANA, FriendshipStatus.ACCEPTED, Instant.now())));

    var result = service.handle(new RemoveFriendshipCommand(marta, FRIENDSHIP_ID));

    assertThat(errorCodeOf(result)).isEqualTo("FRIENDSHIP_ACCESS_FORBIDDEN");
    verify(friendshipRepository, never()).delete(any());
  }

  @Test
  void removingAFriendshipThatDoesNotExistFails() {
    when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.empty());

    var result = service.handle(new RemoveFriendshipCommand(LUIS, FRIENDSHIP_ID));

    assertThat(errorCodeOf(result)).isEqualTo("FRIENDSHIP_NOT_FOUND");
    verify(friendshipRepository, never()).delete(any());
  }

  @Test
  void aRequestRejectedLessThanSevenDaysAgoCannotBeRemoved() {
    Instant rejectedAt = Instant.now().minus(3, ChronoUnit.DAYS);
    when(friendshipRepository.findById(FRIENDSHIP_ID))
        .thenReturn(Optional.of(friendship(LUIS, ANA, FriendshipStatus.REJECTED, rejectedAt)));

    var result = service.handle(new RemoveFriendshipCommand(LUIS, FRIENDSHIP_ID));

    assertThat(errorCodeOf(result)).isEqualTo("FRIENDSHIP_IN_WAITING_PERIOD");
    verify(friendshipRepository, never()).delete(any());
  }

  @Test
  void anExpiredRequestCannotBeRemovedDuringItsWaitingPeriod() {
    // Sent 3 days ago: expired 1 day ago, so 6 more days of waiting are left
    when(friendshipRepository.findById(FRIENDSHIP_ID))
        .thenReturn(Optional.of(pendingSentHoursAgo(3 * 24)));

    var result = service.handle(new RemoveFriendshipCommand(LUIS, FRIENDSHIP_ID));

    assertThat(errorCodeOf(result)).isEqualTo("FRIENDSHIP_IN_WAITING_PERIOD");
    verify(friendshipRepository, never()).delete(any());
  }

  @Test
  void aRejectedRequestCanBeRemovedOnceTheWaitingPeriodEnded() {
    Instant rejectedAt = Instant.now().minus(8, ChronoUnit.DAYS);
    Friendship rejected = friendship(LUIS, ANA, FriendshipStatus.REJECTED, rejectedAt);
    when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(rejected));

    var result = service.handle(new RemoveFriendshipCommand(LUIS, FRIENDSHIP_ID));

    assertThat(result.toOptional()).isPresent();
    verify(friendshipRepository).delete(rejected);
  }

  @Test
  void aRequestCannotBeAnsweredTwice() {
    Friendship accepted = friendship(LUIS, ANA, FriendshipStatus.ACCEPTED, Instant.now());
    when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(accepted));

    var result = service.handle(new RespondFriendRequestCommand(ANA, FRIENDSHIP_ID, false));

    assertThat(errorCodeOf(result)).isEqualTo("FRIEND_REQUEST_ALREADY_ANSWERED");
  }

  private static Friendship friendship(
      UserId requester, UserId receiver, FriendshipStatus status, Instant updatedAt) {
    return new Friendship(FRIENDSHIP_ID, requester, receiver, status, updatedAt, updatedAt);
  }

  private static UserProfile anaProfile() {
    return new UserProfile(ANA, "Ana", SocialRole.STUDENT, 0, null, 0, 0, null, ANA_CODE);
  }

  // A request Luis sent to Ana that nobody answered
  private static Friendship pendingSentHoursAgo(long hours) {
    Instant sentAt = Instant.now().minus(hours, ChronoUnit.HOURS);
    return new Friendship(FRIENDSHIP_ID, LUIS, ANA, FriendshipStatus.PENDING, sentAt, sentAt);
  }

  private static String errorCodeOf(Result<?, ApplicationError> result) {
    assertThat(result.isFailure()).isTrue();
    return ((Result.Failure<?, ApplicationError>) result).error().code();
  }
}
