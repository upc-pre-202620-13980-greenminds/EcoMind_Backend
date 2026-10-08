package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.commandservices.StreakProtectionCommandService;
import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationEventPublisher;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.StreakProtectionRequest;
import pe.greenminds.ecomind.gamification.domain.model.commands.RequestStreakProtectionCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.ResolveStreakProtectionCommand;
import pe.greenminds.ecomind.gamification.domain.model.events.DailyStreakAtRiskEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.UserStreakUpdatedEvent;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.StreakProtectionStatus;
import pe.greenminds.ecomind.gamification.domain.repositories.StreakProtectionRequestRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.UserProgressRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class StreakProtectionCommandServiceImpl implements StreakProtectionCommandService {
    private final UserProgressRepository progress;
    private final StreakProtectionRequestRepository requests;
    private final GamificationEventPublisher events;

    public StreakProtectionCommandServiceImpl(
            UserProgressRepository progress,
            StreakProtectionRequestRepository requests,
            GamificationEventPublisher events) {
        this.progress = progress;
        this.requests = requests;
        this.events = events;
    }

    public StreakProtectionRequest handle(RequestStreakProtectionCommand c) {
        var user = progress.lockForReward(c.userId());
        var prior = requests.find(c.userId(), c.streakDate());
        if (prior.isPresent()) return prior.get();
        if (user.getCurrentStreak() == 0
                || user.getLastContinuityDate() == null
                || !c.streakDate().equals(user.getLastContinuityDate().plusDays(1)))
            throw new IllegalArgumentException("Day is not at risk");
        var request =
                new StreakProtectionRequest(
                        UUID.randomUUID(),
                        c.userId(),
                        c.streakDate(),
                        StreakProtectionStatus.PENDING,
                        Instant.now());
        requests.save(request);
        events.publish(new DailyStreakAtRiskEvent(request));
        return request;
    }

    public void handle(ResolveStreakProtectionCommand c) {
        var user = progress.lockForReward(c.userId());
        var prior = requests.lock(c.requestId());
        if (prior.isEmpty()) throw new IllegalArgumentException("Unknown protection request");
        var request = prior.get();
        var resolved = request.resolve(c.userId(), c.streakDate(), c.status(), Instant.now());
        if (request.status() != StreakProtectionStatus.PENDING) return;
        if (resolved.status() == StreakProtectionStatus.PROTECTED)
            user.protect(resolved.streakDate());
        else user.resetForMissedDay(resolved.streakDate());
        progress.save(user);
        requests.save(resolved);
        events.publish(
                new UserStreakUpdatedEvent(
                        c.userId(),
                        user.getCurrentStreak(),
                        user.getLongestStreak(),
                        user.getLastActivityDate()));
    }
}
