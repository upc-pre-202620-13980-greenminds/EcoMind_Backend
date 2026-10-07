package pe.greenminds.ecomind.iam.infrastructure.acl;

import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.iam.application.outboundservices.UsersContextGateway;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.interfaces.acl.IamContextListener;

/**
 * Client of the Users bounded context. It talks to Users only through its entry point for IAM,
 * with simple values, in a synchronous internal call that runs in the same transaction: if the
 * profile cannot be created, the account is not created either.
 */
@Component
public class UsersContextClient implements UsersContextGateway {

  private final IamContextListener iamContextListener;

  public UsersContextClient(IamContextListener iamContextListener) {
    this.iamContextListener = iamContextListener;
  }

  @Override
  public void createProfile(AccountId accountId, String name, SocialRole socialRole) {
    iamContextListener.createProfile(accountId.value(), name, socialRole.name());
  }
}
