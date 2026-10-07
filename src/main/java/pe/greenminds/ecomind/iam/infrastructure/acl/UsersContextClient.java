package pe.greenminds.ecomind.iam.infrastructure.acl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.iam.application.outboundservices.UsersContextGateway;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.SocialRole;

/**
 * Client of the Users bounded context.
 */
@Component
public class UsersContextClient implements UsersContextGateway {

  private static final Logger LOGGER = LoggerFactory.getLogger(UsersContextClient.class);

  // TODO: send the CreateProfile command to Users once that bounded context is implemented.
  @Override
  public void createProfile(AccountId accountId, String name, SocialRole socialRole) {
    LOGGER.warn(
        "Profile of account {} was not created: the Users context is not available yet",
        accountId.value());
  }
}
