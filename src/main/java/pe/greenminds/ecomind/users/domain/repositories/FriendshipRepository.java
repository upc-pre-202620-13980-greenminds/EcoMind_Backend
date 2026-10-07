package pe.greenminds.ecomind.users.domain.repositories;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

public interface FriendshipRepository {

  Friendship save(Friendship friendship);

  void delete(Friendship friendship);

  Optional<Friendship> findById(Long friendshipId);

  List<Friendship> findAll();

  /** Friendships where the user is the requester or the receiver, in any state. */
  List<Friendship> findByUserId(UserId userId);

  /** Friendship between two users, whoever sent the request. */
  Optional<Friendship> findBetween(UserId firstUserId, UserId secondUserId);
}
