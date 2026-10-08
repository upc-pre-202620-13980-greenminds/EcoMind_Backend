package pe.greenminds.ecomind.gamification.application.internal.eventhandlers;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.domain.model.events.UserScoreUpdatedEvent;

@Component("gamificationUserScoreUpdatedEventHandler")
public class UserScoreUpdatedEventHandler {
    private final AchievementCommandService achievements;

    public UserScoreUpdatedEventHandler(@Lazy AchievementCommandService achievements) {
        this.achievements = achievements;
    }

    @EventListener
    public void on(UserScoreUpdatedEvent e) {
        achievements.evaluateUser(e.userId(), e.executionId(), e.occurredAt());
    }
}
