package pe.greenminds.ecomind.gamification.interfaces.acl;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.community.interfaces.acl.events.PublicationCreatedIntegrationEvent;
import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.domain.model.commands.ConfirmAchievementPublicationCommand;

@Component
@Transactional
public class AchievementPublicationConsumer {
    private final AchievementCommandService commands;

    public AchievementPublicationConsumer(AchievementCommandService commands) {
        this.commands = commands;
    }

    @EventListener
    public void on(PublicationCreatedIntegrationEvent e) {
        commands.handle(
                new ConfirmAchievementPublicationCommand(
                        e.requestId(),
                        e.awardId(),
                        e.requestedBy(),
                        e.communityId(),
                        e.publicationId()));
    }
}
