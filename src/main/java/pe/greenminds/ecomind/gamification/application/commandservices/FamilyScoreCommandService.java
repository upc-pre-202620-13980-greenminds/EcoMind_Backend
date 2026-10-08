package pe.greenminds.ecomind.gamification.application.commandservices;

import pe.greenminds.ecomind.gamification.domain.model.commands.UpdateFamilyScoreCommand;

public interface FamilyScoreCommandService {
    void handle(UpdateFamilyScoreCommand command);
}
