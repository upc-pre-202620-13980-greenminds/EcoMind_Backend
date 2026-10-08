package pe.greenminds.ecomind.gamification.application.commandservices;

import pe.greenminds.ecomind.gamification.domain.model.commands.GrantFamilyPlanRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.entities.FamilyRewardTransaction;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface FamilyRewardCommandService {
    Result<FamilyRewardTransaction, ApplicationError> handle(GrantFamilyPlanRewardCommand command);
}
