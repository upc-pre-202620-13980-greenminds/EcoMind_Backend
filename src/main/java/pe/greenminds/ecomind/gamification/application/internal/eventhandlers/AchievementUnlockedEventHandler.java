package pe.greenminds.ecomind.gamification.application.internal.eventhandlers;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationEventPublisher;
import pe.greenminds.ecomind.gamification.domain.model.events.AchievementUnlockedEvent;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.AchievementUnlockedIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.CosmeticRewardRequestedIntegrationEvent;

import java.util.UUID;

@Component("gamificationAchievementUnlockedEventHandler")
public class AchievementUnlockedEventHandler {
    private final GamificationEventPublisher events;

    public AchievementUnlockedEventHandler(@Lazy GamificationEventPublisher events) {
        this.events = events;
    }

    @EventListener
    public void on(AchievementUnlockedEvent e) {
        var a = e.award();
        if (a.scope() == AchievementScope.INDIVIDUAL) {
            events.publish(
                    new AchievementUnlockedIntegrationEvent(
                            UUID.randomUUID(),
                            a.id(),
                            a.achievementId(),
                            a.beneficiaryId(),
                            a.awardedAt()));
            if (e.cosmeticId() != null)
                events.publish(
                        new CosmeticRewardRequestedIntegrationEvent(
                                UUID.randomUUID(),
                                a.id(),
                                a.beneficiaryId(),
                                e.cosmeticId(),
                                a.awardedAt()));
        }
    }
}
