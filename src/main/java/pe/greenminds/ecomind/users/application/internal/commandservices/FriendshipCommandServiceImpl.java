package pe.greenminds.ecomind.users.application.internal.commandservices;

import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.users.application.commandservices.FriendshipCommandService;
import pe.greenminds.ecomind.users.application.internal.UsersErrors;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.commands.RespondFriendRequestCommand;
import pe.greenminds.ecomind.users.domain.model.commands.SendFriendRequestCommand;
import pe.greenminds.ecomind.users.domain.model.events.FriendRequestAcceptedEvent;
import pe.greenminds.ecomind.users.domain.repositories.FriendshipRepository;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;
import pe.greenminds.ecomind.users.domain.services.FriendshipPolicy;

@Service
public class FriendshipCommandServiceImpl implements FriendshipCommandService {

  private final FriendshipRepository friendshipRepository;
  private final UserProfileRepository userProfileRepository;
  private final FriendshipPolicy friendshipPolicy;
  private final ApplicationEventPublisher eventPublisher;

  public FriendshipCommandServiceImpl(
      FriendshipRepository friendshipRepository,
      UserProfileRepository userProfileRepository,
      FriendshipPolicy friendshipPolicy,
      ApplicationEventPublisher eventPublisher) {
    this.friendshipRepository = friendshipRepository;
    this.userProfileRepository = userProfileRepository;
    this.friendshipPolicy = friendshipPolicy;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public Result<Friendship, ApplicationError> handle(SendFriendRequestCommand command) {
    var violation =
        friendshipPolicy.validate(command.requesterId(), command.receiverId(), Instant.now());
    if (violation.isPresent()) {
      return Result.failure(toError(violation.get()));
    }
    if (!userProfileRepository.existsById(command.receiverId())) {
      return Result.failure(UsersErrors.userProfileNotFound(command.receiverId().value()));
    }

    // An old rejected request between the two users is replaced by the new one.
    friendshipRepository
        .findBetween(command.requesterId(), command.receiverId())
        .ifPresent(friendshipRepository::delete);
    return Result.success(
        friendshipRepository.save(
            Friendship.request(command.requesterId(), command.receiverId())));
  }

  @Override
  @Transactional
  public Result<Friendship, ApplicationError> handle(RespondFriendRequestCommand command) {
    var found = friendshipRepository.findById(command.friendshipId());
    if (found.isEmpty()) {
      return Result.failure(UsersErrors.friendshipNotFound(command.friendshipId()));
    }
    Friendship friendship = found.get();
    if (!friendship.isReceivedBy(command.respondedBy())) {
      return Result.failure(UsersErrors.friendRequestAccessForbidden());
    }
    if (!friendship.isPending()) {
      return Result.failure(UsersErrors.friendRequestAlreadyAnswered());
    }

    if (command.accepted()) {
      friendship.accept();
    } else {
      friendship.reject();
    }
    Friendship saved = friendshipRepository.save(friendship);
    if (command.accepted()) {
      eventPublisher.publishEvent(new FriendRequestAcceptedEvent(saved.getId(), Instant.now()));
    }
    return Result.success(saved);
  }

  private static ApplicationError toError(FriendshipPolicy.Violation violation) {
    return switch (violation) {
      case SELF_REQUEST -> UsersErrors.friendRequestToSelf();
      case ALREADY_EXISTS -> UsersErrors.friendshipConflict();
      case REJECTED_RECENTLY -> UsersErrors.friendRequestRejectedRecently();
    };
  }
}
