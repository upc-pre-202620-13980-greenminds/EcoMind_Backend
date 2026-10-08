package pe.greenminds.ecomind.gamification.infrastructure.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationEventPublisher;
import pe.greenminds.ecomind.gamification.domain.model.events.AchievementShareRequestedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.AchievementSharedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.AchievementUnlockedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.DailyStreakAtRiskEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.FamilyScoreUpdatedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.RewardGrantedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.UserScoreUpdatedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.UserStreakUpdatedEvent;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.GamificationOutboxPersistenceEntity;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.GamificationOutboxPersistenceRepository;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.AchievementShareRequestedIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.AchievementUnlockedIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.CosmeticRewardRequestedIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.DailyStreakAtRiskIntegrationEvent;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.RewardGrantedIntegrationEvent;

import java.time.Instant;
import java.util.UUID;

@Component
@Transactional(propagation = Propagation.MANDATORY)
public class SpringGamificationEventPublisher implements GamificationEventPublisher {
    private final GamificationOutboxPersistenceRepository messages;
    private final ApplicationEventPublisher applicationEvents;

    public SpringGamificationEventPublisher(
            GamificationOutboxPersistenceRepository messages,
            ApplicationEventPublisher applicationEvents) {
        this.messages = messages;
        this.applicationEvents = applicationEvents;
    }

    public void publish(RewardGrantedEvent e) {
        applicationEvents.publishEvent(e);
    }

    public void publish(UserScoreUpdatedEvent e) {
        applicationEvents.publishEvent(e);
    }

    public void publish(FamilyScoreUpdatedEvent e) {
        applicationEvents.publishEvent(e);
    }

    public void publish(UserStreakUpdatedEvent e) {
        applicationEvents.publishEvent(e);
    }

    public void publish(AchievementUnlockedEvent e) {
        applicationEvents.publishEvent(e);
    }

    public void publish(DailyStreakAtRiskEvent e) {
        applicationEvents.publishEvent(e);
    }

    public void publish(AchievementShareRequestedEvent e) {
        applicationEvents.publishEvent(e);
    }

    public void publish(AchievementSharedEvent e) {
        applicationEvents.publishEvent(e);
    }

    private GamificationOutboxPersistenceEntity message(
            UUID event, String type, UUID correlation, Long user, Instant at) {
        var row = new GamificationOutboxPersistenceEntity();
        row.setId(event.toString());
        row.setMessageType(type);
        row.setCorrelationId(correlation.toString());
        row.setUserId(user);
        row.setOccurredAt(at);
        row.setNextAttemptAt(Instant.now());
        return row;
    }

    public void publish(RewardGrantedIntegrationEvent e) {
        var row = message(e.eventId(), "GEMS", e.rewardId(), e.userId(), e.occurredAt());
        row.setGems(e.gems());
        messages.save(row);
    }

    public void publish(DailyStreakAtRiskIntegrationEvent e) {
        var row = message(e.eventId(), "PROTECT_STREAK", e.requestId(), e.userId(), e.occurredAt());
        row.setStreakDate(e.streakDate());
        messages.save(row);
    }

    public void publish(AchievementUnlockedIntegrationEvent e) {
        var row =
                message(e.eventId(), "ACHIEVEMENT_NOTICE", e.awardId(), e.userId(), e.occurredAt());
        row.setAchievementId(e.achievementId().toString());
        row.setAwardId(e.awardId().toString());
        messages.save(row);
    }

    public void publish(AchievementShareRequestedIntegrationEvent e) {
        var row =
                message(
                        e.eventId(),
                        "SHARE_ACHIEVEMENT",
                        e.requestId(),
                        e.requestedBy(),
                        e.occurredAt());
        row.setAwardId(e.awardId().toString());
        row.setCommunityId(e.communityId().toString());
        messages.save(row);
    }

    public void publish(CosmeticRewardRequestedIntegrationEvent e) {
        var row = message(e.eventId(), "COSMETIC", e.awardId(), e.userId(), e.occurredAt());
        row.setCosmeticId(e.cosmeticId().toString());
        messages.save(row);
    }
}
