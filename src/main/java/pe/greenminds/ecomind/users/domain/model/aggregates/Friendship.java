package pe.greenminds.ecomind.users.domain.model.aggregates;

import java.time.Duration;
import java.time.Instant;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FriendshipStatus;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

/**
 * Friend request between two users and its state.
 */
public class Friendship {

  /** Time the requester must wait to send a new request after a rejection. */
  public static final Duration RETRY_WAIT_AFTER_REJECTION = Duration.ofDays(7);

  private final Long id;
  private final UserId requesterId;
  private final UserId receiverId;
  private FriendshipStatus status;
  private final Instant updatedAt;

  /** Rebuilds a friendship that already exists; id and updatedAt are null until persisted. */
  public Friendship(
      Long id, UserId requesterId, UserId receiverId, FriendshipStatus status, Instant updatedAt) {
    this.id = id;
    this.requesterId = requesterId;
    this.receiverId = receiverId;
    this.status = status;
    this.updatedAt = updatedAt;
  }

  public static Friendship request(UserId requesterId, UserId receiverId) {
    if (requesterId.equals(receiverId)) {
      throw new IllegalArgumentException("A user cannot send a friend request to themselves");
    }
    return new Friendship(null, requesterId, receiverId, FriendshipStatus.PENDING, null);
  }

  public void accept() {
    requirePending();
    status = FriendshipStatus.ACCEPTED;
  }

  public void reject() {
    requirePending();
    status = FriendshipStatus.REJECTED;
  }

  public boolean isPending() {
    return status == FriendshipStatus.PENDING;
  }

  public boolean isReceivedBy(UserId userId) {
    return receiverId.equals(userId);
  }

  /** A rejected request can be sent again once the waiting time has passed. */
  public boolean canBeRequestedAgain(Instant now) {
    return status == FriendshipStatus.REJECTED
        && updatedAt != null
        && !now.isBefore(updatedAt.plus(RETRY_WAIT_AFTER_REJECTION));
  }

  private void requirePending() {
    if (!isPending()) {
      throw new IllegalStateException("The friend request was already answered");
    }
  }

  public Long getId() {
    return id;
  }

  public UserId getRequesterId() {
    return requesterId;
  }

  public UserId getReceiverId() {
    return receiverId;
  }

  public FriendshipStatus getStatus() {
    return status;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
