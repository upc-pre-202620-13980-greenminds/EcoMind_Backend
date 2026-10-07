package pe.greenminds.ecomind.users.application.internal.queryservices;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.users.application.queryservices.FriendshipQueryService;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.queries.GetAllFriendshipsQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetFriendsByUserQuery;
import pe.greenminds.ecomind.users.domain.repositories.FriendshipRepository;

@Service
@Transactional(readOnly = true)
public class FriendshipQueryServiceImpl implements FriendshipQueryService {

  private final FriendshipRepository friendshipRepository;

  public FriendshipQueryServiceImpl(FriendshipRepository friendshipRepository) {
    this.friendshipRepository = friendshipRepository;
  }

  @Override
  public List<Friendship> handle(GetAllFriendshipsQuery query) {
    return friendshipRepository.findAll();
  }

  @Override
  public List<Friendship> handle(GetFriendsByUserQuery query) {
    return friendshipRepository.findByUserId(query.userId());
  }
}
