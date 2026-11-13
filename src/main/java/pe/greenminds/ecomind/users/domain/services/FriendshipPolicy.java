package pe.greenminds.ecomind.users.domain.services;

import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FriendshipStatus;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.FriendshipRepository;

/**
 * Prevents friend requests to oneself and duplicated requests between the same two users, and
 * makes the requester wait after a rejected or an unanswered request.
 */
@Service
public class FriendshipPolicy {

  /** Reason a friend request is not allowed. */
  public enum Violation {
    SELF_REQUEST,
    ALREADY_EXISTS,
    REJECTED_RECENTLY,
    UNANSWERED_RECENTLY
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
    Friendship friendship = existing.get();
    if (friendship.getStatus() == FriendshipStatus.REJECTED) {
      return Optional.of(Violation.REJECTED_RECENTLY);
    }
    // Expired without an answer, but the 7 days counted from the end of the 48 hours did not pass
    if (friendship.isExpired(now)) {
      return Optional.of(Violation.UNANSWERED_RECENTLY);
    }
    return Optional.of(Violation.ALREADY_EXISTS);
  }
}
