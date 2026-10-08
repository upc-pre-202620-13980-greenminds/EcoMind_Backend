package pe.greenminds.ecomind.gamification.application.commandservices;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.StreakProtectionRequest;
import pe.greenminds.ecomind.gamification.domain.model.commands.RequestStreakProtectionCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.ResolveStreakProtectionCommand;

public interface StreakProtectionCommandService {
    StreakProtectionRequest handle(RequestStreakProtectionCommand command);

    void handle(ResolveStreakProtectionCommand command);
}
