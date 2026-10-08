package pe.greenminds.ecomind.gamification.application.internal.eventhandlers;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationEventPublisher;
import pe.greenminds.ecomind.gamification.domain.model.events.AchievementShareRequestedEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.AchievementShareRequestedIntegrationEvent;

import java.util.UUID;

@Component("gamificationAchievementShareRequestedEventHandler")
public class AchievementShareRequestedEventHandler {
    private final GamificationEventPublisher events;

    public AchievementShareRequestedEventHandler(@Lazy GamificationEventPublisher events) {
        this.events = events;
    }

    @EventListener
    public void on(AchievementShareRequestedEvent e) {
        var r = e.request();
        events.publish(
                new AchievementShareRequestedIntegrationEvent(
                        UUID.randomUUID(),
                        r.id(),
                        r.awardId(),
                        r.requestedBy(),
                        r.communityId(),
                        r.createdAt()));
    }
}
