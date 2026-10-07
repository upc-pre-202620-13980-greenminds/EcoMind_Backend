package pe.greenminds.ecomind.iam.application.outboundservices;

import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccessToken;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;

/**
 * Outbound port that issues signed access tokens with expiration.
 */
public interface TokenService {

  AccessToken issueAccessToken(AuthenticatedUser authenticatedUser);
}
