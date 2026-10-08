package pe.greenminds.ecomind.gamification.application.internal.eventhandlers;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationEventPublisher;
import pe.greenminds.ecomind.gamification.domain.model.events.DailyStreakAtRiskEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.DailyStreakAtRiskIntegrationEvent;

import java.util.UUID;

@Component("gamificationDailyStreakAtRiskEventHandler")
public class DailyStreakAtRiskEventHandler {
    private final GamificationEventPublisher events;

    public DailyStreakAtRiskEventHandler(@Lazy GamificationEventPublisher events) {
        this.events = events;
    }

    @EventListener
    public void on(DailyStreakAtRiskEvent e) {
        var r = e.request();
        events.publish(
                new DailyStreakAtRiskIntegrationEvent(
                        UUID.randomUUID(),
                        r.id(),
                        r.userId().value(),
                        r.streakDate(),
                        r.createdAt()));
    }
}
