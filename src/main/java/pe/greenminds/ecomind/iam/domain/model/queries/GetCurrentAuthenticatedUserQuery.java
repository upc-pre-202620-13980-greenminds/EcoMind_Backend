package pe.greenminds.ecomind.iam.domain.model.queries;

import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;

public record GetCurrentAuthenticatedUserQuery(AccountId accountId) {
}
