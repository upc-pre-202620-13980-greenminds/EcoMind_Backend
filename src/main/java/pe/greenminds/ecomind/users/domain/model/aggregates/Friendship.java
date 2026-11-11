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

  /** Time the receiver has to answer a request before it expires (HU-039). */
  public static final Duration RESPONSE_DEADLINE = Duration.ofHours(48);

  /** Time the requester must wait to send a new request once an unanswered one expired. */
  public static final Duration RETRY_WAIT_AFTER_EXPIRATION = Duration.ofDays(7);

  private final Long id;
  private final UserId requesterId;
  private final UserId receiverId;
  private FriendshipStatus status;
  private final Instant createdAt;
  private final Instant updatedAt;

  /** Rebuilds a friendship that already exists; id and dates are null until persisted. */
  public Friendship(
      Long id,
      UserId requesterId,
      UserId receiverId,
      FriendshipStatus status,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.requesterId = requesterId;
    this.receiverId = receiverId;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Friendship request(UserId requesterId, UserId receiverId) {
    if (requesterId.equals(receiverId)) {
      throw new IllegalArgumentException("A user cannot send a friend request to themselves");
    }
    return new Friendship(null, requesterId, receiverId, FriendshipStatus.PENDING, null, null);
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

  /** Two users are friends only once the request was accepted. */
  public boolean isAccepted() {
    return status == FriendshipStatus.ACCEPTED;
  }

  public boolean isReceivedBy(UserId userId) {
    return receiverId.equals(userId);
  }

  /** True when the request is still pending once the given time has passed since it was sent. */
  public boolean isUnansweredAfter(Duration time, Instant now) {
    return isPending() && createdAt != null && !now.isBefore(createdAt.plus(time));
  }

  /** A request that was not answered within the deadline can no longer be answered. */
  public boolean isExpired(Instant now) {
    return isUnansweredAfter(RESPONSE_DEADLINE, now);
  }

  /**
   * A rejected request can be sent again 7 days after the answer; an unanswered one, 7 days after
   * it expired, so the wait is counted from the end of the 48 hours and not from the sending.
   */
  public boolean canBeRequestedAgain(Instant now) {
    if (status == FriendshipStatus.REJECTED) {
      return updatedAt != null && !now.isBefore(updatedAt.plus(RETRY_WAIT_AFTER_REJECTION));
    }
    return isUnansweredAfter(RESPONSE_DEADLINE.plus(RETRY_WAIT_AFTER_EXPIRATION), now);
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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
