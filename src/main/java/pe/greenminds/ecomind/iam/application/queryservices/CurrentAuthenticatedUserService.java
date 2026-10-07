package pe.greenminds.ecomind.iam.application.queryservices;

import pe.greenminds.ecomind.iam.domain.model.queries.GetCurrentAuthenticatedUserQuery;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface CurrentAuthenticatedUserService {

  Result<AuthenticatedUser, ApplicationError> handle(GetCurrentAuthenticatedUserQuery query);
}
