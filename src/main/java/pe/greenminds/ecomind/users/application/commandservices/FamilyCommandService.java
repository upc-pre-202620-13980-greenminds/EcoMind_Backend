package pe.greenminds.ecomind.users.application.commandservices;

import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.commands.AddFamilyMemberCommand;
import pe.greenminds.ecomind.users.domain.model.commands.CreateFamilyCommand;
import pe.greenminds.ecomind.users.domain.model.commands.RemoveFamilyMemberCommand;

public interface FamilyCommandService {

  Result<Family, ApplicationError> handle(CreateFamilyCommand command);

  Result<Family, ApplicationError> handle(AddFamilyMemberCommand command);

  Result<Family, ApplicationError> handle(RemoveFamilyMemberCommand command);
}
