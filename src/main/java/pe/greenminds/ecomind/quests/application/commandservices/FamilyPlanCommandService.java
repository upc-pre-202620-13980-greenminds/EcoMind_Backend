package pe.greenminds.ecomind.quests.application.commandservices;

import pe.greenminds.ecomind.quests.application.queryservices.FamilyPlanState;
import pe.greenminds.ecomind.quests.domain.model.commands.ActivateFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.CompleteFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.UpdateFamilyPlanCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface FamilyPlanCommandService {
    Result<FamilyPlanState, ApplicationError> handle(CreateFamilyPlanCommand command);
    Result<FamilyPlanState, ApplicationError> handle(UpdateFamilyPlanCommand command);
    Result<FamilyPlanState, ApplicationError> handle(ActivateFamilyPlanCommand command);
    Result<FamilyPlanState, ApplicationError> handle(CompleteFamilyPlanCommand command);
    Result<FamilyPlanState, ApplicationError> handle(DeleteFamilyPlanCommand command);
}
