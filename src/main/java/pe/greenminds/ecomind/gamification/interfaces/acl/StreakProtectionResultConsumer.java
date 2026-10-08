package pe.greenminds.ecomind.gamification.interfaces.acl;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.commandservices.StreakProtectionCommandService;
import pe.greenminds.ecomind.gamification.domain.model.commands.ResolveStreakProtectionCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.StreakProtectionStatus;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.monetization.interfaces.acl.events.StreakProtectedIntegrationEvent;
import pe.greenminds.ecomind.monetization.interfaces.acl.events.StreakProtectionUnavailableIntegrationEvent;

@Component
@Transactional
public class StreakProtectionResultConsumer {
    private final StreakProtectionCommandService commands;

    public StreakProtectionResultConsumer(StreakProtectionCommandService commands) {
        this.commands = commands;
    }

    @EventListener
    public void on(StreakProtectedIntegrationEvent e) {
        commands.handle(
                new ResolveStreakProtectionCommand(
                        e.requestId(),
                        new UserId(e.userId()),
                        e.streakDate(),
                        StreakProtectionStatus.PROTECTED));
    }

    @EventListener
    public void on(StreakProtectionUnavailableIntegrationEvent e) {
        commands.handle(
                new ResolveStreakProtectionCommand(
                        e.requestId(),
                        new UserId(e.userId()),
                        e.streakDate(),
                        StreakProtectionStatus.UNAVAILABLE));
    }
}
