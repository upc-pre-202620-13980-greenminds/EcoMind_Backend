package pe.greenminds.ecomind.users.application.queryservices;

import java.util.List;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.queries.GetAllFriendshipsQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetFriendsByUserQuery;

public interface FriendshipQueryService {

  List<Friendship> handle(GetAllFriendshipsQuery query);

  /** Friendships the user takes part in, as requester or receiver. */
  List<Friendship> handle(GetFriendsByUserQuery query);
}
