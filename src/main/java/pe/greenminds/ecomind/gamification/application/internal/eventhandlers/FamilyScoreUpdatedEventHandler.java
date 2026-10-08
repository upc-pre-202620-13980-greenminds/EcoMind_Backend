package pe.greenminds.ecomind.gamification.application.internal.eventhandlers;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.domain.model.events.FamilyScoreUpdatedEvent;

@Component("gamificationFamilyScoreUpdatedEventHandler")
public class FamilyScoreUpdatedEventHandler {
    private final AchievementCommandService achievements;

    public FamilyScoreUpdatedEventHandler(@Lazy AchievementCommandService achievements) {
        this.achievements = achievements;
    }

    @EventListener
    public void on(FamilyScoreUpdatedEvent e) {
        achievements.evaluateFamily(e.familyId(), e.executionId(), e.occurredAt());
    }
}
