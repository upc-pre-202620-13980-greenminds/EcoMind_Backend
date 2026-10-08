package pe.greenminds.ecomind.gamification.application.internal.eventhandlers;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationEventPublisher;
import pe.greenminds.ecomind.gamification.domain.model.events.RewardGrantedEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.RewardGrantedIntegrationEvent;

import java.util.UUID;

@Component("gamificationRewardGrantedEventHandler")
public class RewardGrantedEventHandler {
    private final GamificationEventPublisher events;

    public RewardGrantedEventHandler(@Lazy GamificationEventPublisher events) {
        this.events = events;
    }

    @EventListener
    public void on(RewardGrantedEvent e) {
        var t = e.transaction();
        if (t.grantedReward().gems() > 0)
            events.publish(
                    new RewardGrantedIntegrationEvent(
                            UUID.randomUUID(),
                            t.id(),
                            t.beneficiary().value(),
                            t.grantedReward().gems(),
                            t.occurredAt()));
    }
}
