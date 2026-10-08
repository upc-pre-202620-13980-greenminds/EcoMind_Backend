package pe.greenminds.ecomind.iam.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.iam.application.commandservices.SessionCommandService;
import pe.greenminds.ecomind.iam.domain.model.commands.LogoutCommand;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class SessionCommandServiceImpl implements SessionCommandService {

  /**
   * Access tokens are stateless, so there is no session to delete on the server: the client
   * finishes the session by discarding its token, which stays valid until it expires.
   */
  @Override
  public Result<AccountId, ApplicationError> handle(LogoutCommand command) {
    return Result.success(command.accountId());
  }
}
