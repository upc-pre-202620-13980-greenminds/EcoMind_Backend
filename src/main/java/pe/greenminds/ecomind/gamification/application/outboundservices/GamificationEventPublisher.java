package pe.greenminds.ecomind.gamification.application.outboundservices;

import pe.greenminds.ecomind.gamification.domain.model.events.AchievementShareRequestedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.AchievementSharedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.AchievementUnlockedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.DailyStreakAtRiskEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.FamilyScoreUpdatedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.RewardGrantedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.UserScoreUpdatedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.UserStreakUpdatedEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.AchievementShareRequestedIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.AchievementUnlockedIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.CosmeticRewardRequestedIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.DailyStreakAtRiskIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.RewardGrantedIntegrationEvent;

/** Communications are saved in the same transaction as their originating facts. */
public interface GamificationEventPublisher {
    void publish(AchievementSharedEvent event);

    void publish(AchievementShareRequestedEvent event);

    void publish(DailyStreakAtRiskEvent event);

    void publish(AchievementUnlockedEvent event);

    void publish(UserStreakUpdatedEvent event);

    void publish(FamilyScoreUpdatedEvent event);

    void publish(UserScoreUpdatedEvent event);

    void publish(RewardGrantedEvent event);

    void publish(CosmeticRewardRequestedIntegrationEvent event);

    void publish(RewardGrantedIntegrationEvent event);

    void publish(DailyStreakAtRiskIntegrationEvent event);

    void publish(AchievementUnlockedIntegrationEvent event);

    void publish(AchievementShareRequestedIntegrationEvent event);
}
