package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.UserProgressCommandService;
import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationEventPublisher;
import pe.greenminds.ecomind.gamification.domain.model.commands.UpdateUserScoreCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.UpdateUserStreakCommand;
import pe.greenminds.ecomind.gamification.domain.model.events.UserScoreUpdatedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.UserStreakUpdatedEvent;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.repositories.StreakProtectionRequestRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.UserProgressRepository;

import java.util.Objects;

/** Internal score updates always join the recorded grant's transaction. */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class UserProgressCommandServiceImpl implements UserProgressCommandService {
    private final UserProgressRepository users;
    private final GamificationEventPublisher events;
    private final StreakProtectionRequestRepository protection;
    private final AchievementCommandService achievements;

    public UserProgressCommandServiceImpl(
            UserProgressRepository users,
            GamificationEventPublisher events,
            StreakProtectionRequestRepository protection,
            AchievementCommandService achievements) {
        this.users = users;
        this.events = events;
        this.protection = protection;
        this.achievements = achievements;
    }

    public void handle(UpdateUserScoreCommand command) {
        var progress = users.lockForReward(command.userId());
        progress.applyReward(command.reward(), null, false);
        users.save(progress);
        events.publish(
                new UserScoreUpdatedEvent(
                        command.userId(), command.executionId(), command.occurredAt()));
    }

    public void handle(UpdateUserStreakCommand command) {
        if (protection.hasPendingBefore(command.userId(), command.activityDate()))
            throw new IllegalStateException(
                    "A previous streak day is awaiting protection confirmation");
        var progress = users.lockForReward(command.userId());
        var oldStreak = progress.getCurrentStreak();
        var oldDate = progress.getLastActivityDate();
        progress.applyReward(new Reward(0, 0, 0), command.activityDate(), true);
        users.save(progress);
        if (oldStreak != progress.getCurrentStreak()
                || !Objects.equals(oldDate, progress.getLastActivityDate())) {
            events.publish(
                    new UserStreakUpdatedEvent(
                            command.userId(),
                            progress.getCurrentStreak(),
                            progress.getLongestStreak(),
                            progress.getLastActivityDate()));
            achievements.evaluateUser(
                    command.userId(), command.executionId(), command.occurredAt());
        }
    }
}
