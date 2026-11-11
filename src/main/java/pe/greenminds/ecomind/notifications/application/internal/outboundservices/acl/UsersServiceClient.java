package pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl;

import java.util.Optional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade;

/**
 * Anti-corruption layer used by Notifications to read what it needs from Users without depending
 * on its domain model.
 */
// Own bean name: Quests already has a UsersServiceClient and Spring names beans after the class
@Service("notificationsUsersServiceClient")
public class UsersServiceClient {

  private final UsersContextFacade usersContextFacade;

  public UsersServiceClient(UsersContextFacade usersContextFacade) {
    this.usersContextFacade = usersContextFacade;
  }

  public Optional<String> getUserName(Long userId) {
    return usersContextFacade.getUserName(userId);
  }
}
