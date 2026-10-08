package pe.greenminds.ecomind.gamification.application.commandservices;

import pe.greenminds.ecomind.gamification.domain.model.commands.UpdateUserScoreCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.UpdateUserStreakCommand;

public interface UserProgressCommandService {
    void handle(UpdateUserScoreCommand command);

    void handle(UpdateUserStreakCommand command);
}
