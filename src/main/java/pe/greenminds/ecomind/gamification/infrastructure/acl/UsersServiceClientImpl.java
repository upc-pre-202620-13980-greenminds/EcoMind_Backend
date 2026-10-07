package pe.greenminds.ecomind.gamification.infrastructure.acl;

import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.gamification.application.outboundservices.UsersServiceClient;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade;

@Component
public class UsersServiceClientImpl implements UsersServiceClient {
  private final UsersContextFacade users;
  public UsersServiceClientImpl(UsersContextFacade users) { this.users = users; }
  public boolean familyExists(FamilyId id) { return users.familyExists(id.value()); }
  public boolean isFamilyMember(FamilyId id, UserId user) {
    return users.isFamilyMember(id.value(), user.value());
  }
}
