package pe.greenminds.ecomind.iam.application.outboundservices;

import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.SocialRole;

/**
 * Outbound port towards the Users bounded context.
 */
public interface UsersContextGateway {

  /** Asks Users to create the profile of an account that has just been created. */
  void createProfile(AccountId accountId, String name, SocialRole socialRole);
}
