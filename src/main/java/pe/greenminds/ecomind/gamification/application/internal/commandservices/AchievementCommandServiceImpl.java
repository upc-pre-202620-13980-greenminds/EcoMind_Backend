package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.application.outboundservices.CommunityServiceClient;
import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationEventPublisher;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementShareRequest;
import pe.greenminds.ecomind.gamification.domain.model.commands.AwardAchievementCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.ConfirmAchievementPublicationCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.ShareAchievementCommand;
import pe.greenminds.ecomind.gamification.domain.model.events.AchievementShareRequestedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.AchievementSharedEvent;
import pe.greenminds.ecomind.gamification.domain.model.events.AchievementUnlockedEvent;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementShareStatus;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementAwardRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementMilestoneRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementShareRequestRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.FamilyScoreRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.UserProgressRepository;
import pe.greenminds.ecomind.gamification.domain.services.AchievementEvaluationService;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AchievementCommandServiceImpl implements AchievementCommandService {
    private final AchievementRepository catalog;
    private final AchievementAwardRepository awards;
    private final UserProgressRepository users;
    private final FamilyScoreRepository families;
    private final GamificationEventPublisher events;
    private final CommunityServiceClient community;
    private final AchievementShareRequestRepository shares;
    private final AchievementMilestoneRepository milestones;

    public AchievementCommandServiceImpl(
            AchievementRepository catalog,
            AchievementAwardRepository awards,
            UserProgressRepository users,
            FamilyScoreRepository families,
            GamificationEventPublisher events,
            CommunityServiceClient community,
            AchievementShareRequestRepository shares,
            AchievementMilestoneRepository milestones) {
        this.catalog = catalog;
        this.awards = awards;
        this.users = users;
        this.families = families;
        this.events = events;
        this.community = community;
        this.shares = shares;
        this.milestones = milestones;
    }

    @Transactional
    public void register(Achievement achievement) {
        catalog.add(achievement);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void evaluateUser(UserId userId, UUID sourceEventId, Instant occurredAt) {
        users.lockForReward(userId);
        for (var definition : catalog.findActive(AchievementScope.INDIVIDUAL))
            handle(
                    new AwardAchievementCommand(
                            definition.id(), userId.value(), null, sourceEventId, occurredAt));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void evaluateFamily(FamilyId familyId, UUID sourceEventId, Instant occurredAt) {
        families.lockForReward(familyId);
        for (var definition : catalog.findActive(AchievementScope.FAMILY))
            handle(
                    new AwardAchievementCommand(
                            definition.id(), familyId.value(), null, sourceEventId, occurredAt));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recognizeFamilyPlan(FamilyId family, UUID execution, Instant at) {
        families.lockForReward(family);
        milestones.record(
                AchievementMetric.COMPLETED_FAMILY_PLANS, "FAMILY:" + family.value(), execution);
        evaluateFamily(family, execution, at);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void evaluateCommunity(
            Long communityId, List<Long> eligible, UUID execution, Instant at) {
        milestones.lock("COMMUNITY:" + communityId);
        milestones.record(
                AchievementMetric.COMPLETED_COMMUNITY_GOALS, "COMMUNITY:" + communityId, execution);
        for (var definition : catalog.findActive(AchievementScope.COMMUNITY))
            handle(new AwardAchievementCommand(definition.id(), null, communityId, execution, at));
        for (var user : eligible.stream().distinct().sorted().toList()) {
            users.lockForReward(new UserId(user));
            milestones.record(
                    AchievementMetric.COMPLETED_COMMUNITY_GOALS, "USER:" + user, execution);
            evaluateUser(new UserId(user), execution, at);
        }
    }

    @Transactional
    public Result<AchievementShareRequest, ApplicationError> handle(ShareAchievementCommand c) {
        users.lockForReward(new UserId(c.requestedBy()));
        var prior = shares.find(c.requestId());
        if (prior.isPresent()) {
            var r = prior.get();
            if (!r.requestedBy().equals(c.requestedBy()))
                return Result.failure(
                        ApplicationError.forbidden(
                                "SHARE_ACCESS_FORBIDDEN",
                                "Only the requester can access this share"));
            if (!r.awardId().equals(c.awardId()) || !r.communityId().equals(c.communityId()))
                return Result.failure(
                        ApplicationError.conflict(
                                "SHARE_REQUEST", "Request id already used for another selection"));
            return Result.success(r);
        }
        var award = awards.findById(c.awardId());
        if (award.isEmpty())
            return Result.failure(
                    ApplicationError.notFound("ACHIEVEMENT_AWARD", c.awardId().toString()));
        if (award.get().scope() != AchievementScope.INDIVIDUAL
                || !award.get().beneficiaryId().equals(c.requestedBy()))
            return Result.failure(
                    ApplicationError.forbidden(
                            "ACHIEVEMENT_OWNER_REQUIRED",
                            "Only the individual award owner may share it"));
        if (!community.isMember(c.communityId(), c.requestedBy())
                || !community.mayPublishAchievement(c.communityId(), c.requestedBy()))
            return Result.failure(
                    ApplicationError.forbidden(
                            "COMMUNITY_PUBLICATION_FORBIDDEN",
                            "Membership and publication permission are required"));
        var r =
                new AchievementShareRequest(
                        c.requestId(),
                        c.awardId(),
                        c.requestedBy(),
                        c.communityId(),
                        AchievementShareStatus.PENDING,
                        null,
                        Instant.now());
        shares.save(r);
        events.publish(new AchievementShareRequestedEvent(r));
        return Result.success(r);
    }

    @Transactional
    public void handle(ConfirmAchievementPublicationCommand c) {
        var request = shares.lock(c.requestId());
        if (request.isEmpty()) return; // Other Community publication flows do not belong here.
        var confirmed =
                request.get()
                        .confirm(
                                c.awardId(),
                                c.requestedBy(),
                                c.communityId(),
                                c.publicationId(),
                                Instant.now());
        shares.save(confirmed);
        if (request.get().status() != AchievementShareStatus.PUBLISHED)
            events.publish(new AchievementSharedEvent(confirmed));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(AwardAchievementCommand command) {
        var definition =
                catalog.findById(command.achievementId())
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Achievement definition not found"));
        long confirmedValue;
        switch (definition.scope()) {
            case INDIVIDUAL -> {
                if (command.communityId() != null)
                    throw new IllegalArgumentException(
                            "An individual award cannot have a community beneficiary");
                var userId = new UserId(command.beneficiaryId());
                var progress = users.lockForReward(userId);
                confirmedValue =
                        switch (definition.metric()) {
                            case ECOPOINTS -> progress.getTotalEcopoints();
                            case EXPERIENCE -> progress.getTotalExperience();
                            case LONGEST_STREAK -> progress.getLongestStreak();
                            case COMPLETED_COMMUNITY_GOALS ->
                                    milestones.count(
                                            AchievementMetric.COMPLETED_COMMUNITY_GOALS,
                                            "USER:" + userId.value());
                            case COMPLETED_FAMILY_PLANS -> 0;
                        };
            }
            case FAMILY -> {
                if (command.communityId() != null)
                    throw new IllegalArgumentException(
                            "A family award cannot have a community beneficiary");
                var familyId = new FamilyId(command.beneficiaryId());
                var score = families.lockForReward(familyId);
                confirmedValue =
                        definition.metric() == AchievementMetric.COMPLETED_FAMILY_PLANS
                                ? milestones.count(
                                        AchievementMetric.COMPLETED_FAMILY_PLANS,
                                        "FAMILY:" + familyId.value())
                                : score.getTotalEcopoints();
            }
            case COMMUNITY -> {
                if (command.beneficiaryId() != null || command.communityId() == null)
                    throw new IllegalArgumentException(
                            "A community award requires one community beneficiary");
                milestones.lock("COMMUNITY:" + command.communityId());
                confirmedValue =
                        milestones.count(
                                AchievementMetric.COMPLETED_COMMUNITY_GOALS,
                                "COMMUNITY:" + command.communityId());
            }
            default -> throw new IllegalArgumentException("Invalid achievement scope");
        }
        if (new AchievementEvaluationService().qualifies(definition, confirmedValue)) {
            var award =
                    new AchievementAward(
                            UUID.randomUUID(),
                            definition.id(),
                            definition.scope(),
                            command.beneficiaryId(),
                            command.sourceEventId(),
                            command.occurredAt(),
                            command.communityId());
            if (awards.addIfAbsent(award))
                events.publish(new AchievementUnlockedEvent(award, definition.cosmeticId()));
        }
    }
}
