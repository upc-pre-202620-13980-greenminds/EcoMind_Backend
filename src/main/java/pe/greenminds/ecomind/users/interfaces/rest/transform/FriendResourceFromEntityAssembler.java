package pe.greenminds.ecomind.users.interfaces.rest.transform;

import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.interfaces.rest.resources.FriendResource;

public final class FriendResourceFromEntityAssembler {

  private FriendResourceFromEntityAssembler() {
  }

  public static FriendResource toResourceFromEntity(Friendship friendship) {
    return new FriendResource(
        friendship.getId(),
        friendship.getRequesterId().value(),
        friendship.getReceiverId().value(),
        friendship.getStatus().name());
  }
}
