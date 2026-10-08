package pe.greenminds.ecomind.iam.domain.model.commands;

import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;

public record LogoutCommand(AccountId accountId) {
}
