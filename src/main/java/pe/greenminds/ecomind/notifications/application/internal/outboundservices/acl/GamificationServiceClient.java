package pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.gamification.interfaces.acl.GamificationContextFacade;

/** Reads from Gamification only what the achievement notices and the weekly summary show. */
@Service("notificationsGamificationServiceClient")
public class GamificationServiceClient {

  private final GamificationContextFacade gamificationContextFacade;

  public GamificationServiceClient(GamificationContextFacade gamificationContextFacade) {
    this.gamificationContextFacade = gamificationContextFacade;
  }

  public Optional<String> findAchievementName(UUID achievementId) {
    return gamificationContextFacade.findAchievementName(achievementId);
  }

  public long getEcopointsEarned(Long userId, Instant from, Instant to) {
    return gamificationContextFacade.getEcopointsEarned(userId, from, to);
  }

  public int getCurrentStreak(Long userId) {
    return gamificationContextFacade.getUserProgress(userId).currentStreak();
  }
}
