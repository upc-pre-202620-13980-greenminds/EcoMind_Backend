package pe.greenminds.ecomind.gamification.infrastructure.events;

import static pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementShareStatus.PUBLISHED;
import static pe.greenminds.ecomind.gamification.domain.model.valueobjects.StreakProtectionStatus.PENDING;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.community.interfaces.acl.CommunityContextFacade.AchievementNotice;
import pe.greenminds.ecomind.community.interfaces.acl.CommunityContextFacade.PublishAchievement;
import pe.greenminds.ecomind.gamification.application.outboundservices.CommunityServiceClient;
import pe.greenminds.ecomind.gamification.application.outboundservices.MonetizationServiceClient;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementShareRequestRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.StreakProtectionRequestRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.GamificationOutboxPersistenceEntity;
import pe.greenminds.ecomind.monetization.interfaces.acl.MonetizationContextFacade.ConsumeStreakProtector;
import pe.greenminds.ecomind.monetization.interfaces.acl.MonetizationContextFacade.CreditRewardGems;
import pe.greenminds.ecomind.monetization.interfaces.acl.MonetizationContextFacade.GrantCosmeticReward;

import java.time.Instant;
import java.util.UUID;

@Service
public class GamificationOutboxDeliveryService {
    private final EntityManager entities;
    private final MonetizationServiceClient monetization;
    private final CommunityServiceClient community;
    private final AchievementShareRequestRepository shares;
    private final StreakProtectionRequestRepository protections;

    public GamificationOutboxDeliveryService(
            EntityManager entities,
            MonetizationServiceClient monetization,
            CommunityServiceClient community,
            AchievementShareRequestRepository shares,
            StreakProtectionRequestRepository protections) {
        this.entities = entities;
        this.monetization = monetization;
        this.community = community;
        this.shares = shares;
        this.protections = protections;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliver(String id) {
        var row =
                entities.find(
                        GamificationOutboxPersistenceEntity.class,
                        id,
                        LockModeType.PESSIMISTIC_WRITE);
        if (row == null
                || row.getDeliveredAt() != null
                || row.getNextAttemptAt().isAfter(Instant.now())) return;
        var correlation = UUID.fromString(row.getCorrelationId());
        if (isAcknowledged(row)) {
            row.setDeliveredAt(Instant.now());
            return;
        }
        switch (row.getMessageType()) {
            case "GEMS" ->
                    monetization.creditRewardGems(
                            new CreditRewardGems(correlation, row.getUserId(), row.getGems()));
            case "COSMETIC" ->
                    monetization.grantCosmeticReward(
                            new GrantCosmeticReward(
                                    correlation,
                                    row.getUserId(),
                                    UUID.fromString(row.getCosmeticId())));
            case "PROTECT_STREAK" ->
                    monetization.requestStreakProtection(
                            new ConsumeStreakProtector(
                                    correlation, row.getUserId(), row.getStreakDate()));
            case "ACHIEVEMENT_NOTICE" ->
                    community.recordAchievementNotice(
                            new AchievementNotice(
                                    correlation,
                                    UUID.fromString(row.getAchievementId()),
                                    row.getUserId(),
                                    row.getOccurredAt()));
            case "SHARE_ACHIEVEMENT" ->
                    community.requestAchievementPublication(
                            new PublishAchievement(
                                    correlation,
                                    UUID.fromString(row.getAwardId()),
                                    row.getUserId(),
                                    Long.valueOf(row.getCommunityId())));
            default -> throw new IllegalStateException("Unknown outbox message type");
        }
        if (!row.getMessageType().equals("SHARE_ACHIEVEMENT")
                        && !row.getMessageType().equals("PROTECT_STREAK")
                || isAcknowledged(row)) row.setDeliveredAt(Instant.now());
        else row.setNextAttemptAt(Instant.now().plusSeconds(60));
        row.setAttempts(row.getAttempts() + 1);
    }

    private boolean isAcknowledged(GamificationOutboxPersistenceEntity row) {
        var id = UUID.fromString(row.getCorrelationId());
        if (row.getMessageType().equals("SHARE_ACHIEVEMENT"))
            return shares.find(id).map(r -> r.status() == PUBLISHED).orElse(false);
        if (row.getMessageType().equals("PROTECT_STREAK"))
            return protections.find(id).map(r -> r.status() != PENDING).orElse(false);
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retryLater(String id) {
        var row =
                entities.find(
                        GamificationOutboxPersistenceEntity.class,
                        id,
                        LockModeType.PESSIMISTIC_WRITE);
        if (row == null || row.getDeliveredAt() != null) return;
        row.setAttempts(row.getAttempts() + 1);
        row.setNextAttemptAt(
                Instant.now().plusSeconds(Math.min(3600, 30L << Math.min(row.getAttempts(), 6))));
    }
}
