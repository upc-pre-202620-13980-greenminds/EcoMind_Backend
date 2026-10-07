package pe.greenminds.ecomind.iam.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.iam.application.internal.IamErrors;
import pe.greenminds.ecomind.iam.application.queryservices.CurrentAuthenticatedUserService;
import pe.greenminds.ecomind.iam.domain.model.queries.GetCurrentAuthenticatedUserQuery;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.repositories.AccountRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class CurrentAuthenticatedUserServiceImpl implements CurrentAuthenticatedUserService {

  private final AccountRepository accountRepository;

  public CurrentAuthenticatedUserServiceImpl(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Result<AuthenticatedUser, ApplicationError> handle(
      GetCurrentAuthenticatedUserQuery query) {
    return accountRepository
        .findById(query.accountId())
        .map(account -> new AuthenticatedUser(account.getId(), account.getEmail()))
        .<Result<AuthenticatedUser, ApplicationError>>map(Result::success)
        .orElseGet(
            () -> Result.failure(IamErrors.accountNotFound(query.accountId().value())));
  }
}
