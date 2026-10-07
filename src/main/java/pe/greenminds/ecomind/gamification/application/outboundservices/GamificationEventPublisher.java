package pe.greenminds.ecomind.gamification.application.outboundservices;

import pe.greenminds.ecomind.gamification.interfaces.acl.events.RewardGrantedIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.DailyStreakAtRiskIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.AchievementUnlockedIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.AchievementShareRequestedIntegrationEvent;

/** Durable delivery port to implement with transactional outbox. No publisher implementation is registered yet. */
public interface GamificationEventPublisher {
  void publish(RewardGrantedIntegrationEvent event);
  void publish(DailyStreakAtRiskIntegrationEvent event);
  void publish(AchievementUnlockedIntegrationEvent event);
  void publish(AchievementShareRequestedIntegrationEvent event);
}
