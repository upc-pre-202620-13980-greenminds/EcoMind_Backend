package pe.greenminds.ecomind.users.domain.services;

import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FriendshipStatus;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.FriendshipRepository;

/**
 * Prevents friend requests to oneself and duplicated requests between the same two users.
 */
@Service
public class FriendshipPolicy {

  /** Reason a friend request is not allowed. */
  public enum Violation {
    SELF_REQUEST,
    ALREADY_EXISTS,
    REJECTED_RECENTLY
  }

  private final FriendshipRepository friendshipRepository;

  public FriendshipPolicy(FriendshipRepository friendshipRepository) {
    this.friendshipRepository = friendshipRepository;
  }

  /** Returns the rule broken by the request, or empty when it can be sent. */
  public Optional<Violation> validate(UserId requesterId, UserId receiverId, Instant now) {
    if (requesterId.equals(receiverId)) {
      return Optional.of(Violation.SELF_REQUEST);
    }
    Optional<Friendship> existing = friendshipRepository.findBetween(requesterId, receiverId);
    if (existing.isEmpty() || existing.get().canBeRequestedAgain(now)) {
      return Optional.empty();
    }
    boolean rejected = existing.get().getStatus() == FriendshipStatus.REJECTED;
    return Optional.of(rejected ? Violation.REJECTED_RECENTLY : Violation.ALREADY_EXISTS);
  }
}
