package pe.greenminds.ecomind.community.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.commandservices.CommunityCommandService;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.domain.model.aggregates.Community;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityAchievement;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityMembership;
import pe.greenminds.ecomind.community.domain.model.commands.CreateLocalCommunityCommand;
import pe.greenminds.ecomind.community.domain.model.commands.CreateTopicCommunityCommand;
import pe.greenminds.ecomind.community.domain.model.commands.JoinCommunityCommand;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityRole;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.repositories.CommunityAchievementRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityMembershipRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRegistrationRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.community.domain.model.commands.LeaveCommunityCommand;
import pe.greenminds.ecomind.community.application.commandservices.EventLifecycleService;
import pe.greenminds.ecomind.community.domain.model.aggregates.Post;
import pe.greenminds.ecomind.community.domain.repositories.PostRepository;
import java.util.Locale;

@Service
public class CommunityCommandServiceImpl implements CommunityCommandService {
    private final CommunityRepository communities;
    private final CommunityMembershipRepository memberships;
    private final CommunityAchievementRepository achievements;
    private final CommunityActorGateway actors;
    private final EventRepository events;
    private final EventRegistrationRepository registrations;
    private final EventLifecycleService lifecycle;
    private final PostRepository posts;


    public CommunityCommandServiceImpl(CommunityRepository communities, CommunityMembershipRepository memberships, CommunityAchievementRepository achievements, CommunityActorGateway actors, EventRepository events, EventRegistrationRepository registrations, EventLifecycleService lifecycle, PostRepository posts) {
        this.communities = communities;
        this.memberships = memberships;
        this.achievements = achievements;
        this.actors = actors;
        this.events = events;
        this.registrations = registrations;
        this.lifecycle = lifecycle;
        this.posts = posts;
    }
    @Override
    @Transactional
    public Result<Community, ApplicationError> handle(CreateLocalCommunityCommand command) {
        return create(
                CommunityType.LOCAL,
                command.name(),
                command.description(),
                null,
                command.locality(),
                null,
                command.iconUrl(),
                command.userId());
    }

    @Override
    @Transactional
    public Result<Community, ApplicationError> handle(CreateTopicCommunityCommand command) {
        try {
            actors.requireParent(command.userId());
        } catch (SecurityException exception) {
            return Result.failure(ApplicationError.forbidden(
                    "COMMUNITY_CREATOR_NOT_PARENT",
                    exception.getMessage()));
        }

        return create(
                CommunityType.TOPIC,
                command.name(),
                command.description(),
                command.topic(),
                null,
                command.memberLimit(),
                command.iconUrl(),
                command.userId());
    }

    private Result<Community, ApplicationError> create(
            CommunityType type,
            String name,
            String description,
            String topic,
            String locality,
            Integer memberLimit,
            String iconUrl,
            Long creatorId) {
        Community candidate;

        try {
            candidate = new Community(
                    null,
                    name,
                    description,
                    type,
                    topic,
                    locality,
                    memberLimit,
                    iconUrl,
                    creatorId);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(ApplicationError.validationError(
                    "Community",
                    exception.getMessage()));
        }

        Community saved = communities.save(candidate);

        if (type == CommunityType.TOPIC && creatorId != null) {
            memberships.save(new CommunityMembership(
                    null,
                    saved.getId(),
                    creatorId,
                    CommunityRole.ADMIN));
        }

        awardCommunityAchievement(
                saved.getId(),
                "Community created",
                "Our community has been created. Welcome to everyone!");

        return Result.success(saved);
    }

    @Override
    @Transactional
    public Result<CommunityMembership, ApplicationError> handle(JoinCommunityCommand command) {
        var community = communities.findByIdForUpdate(command.communityId());
        if (community.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Community", String.valueOf(command.communityId())));
        }
        if (memberships.findByCommunityIdAndUserId(command.communityId(), command.userId()).isPresent()) {
            return Result.failure(ApplicationError.conflict("Community membership", "The user is already registered"));
        }
        if (community.get().getType() == CommunityType.LOCAL) {
            for (var membership : memberships.findByUserId(command.userId())) {
                var existingCommunity = communities.findById(membership.communityId());
                if (existingCommunity.isPresent() && existingCommunity.get().getType() == CommunityType.LOCAL) {
                    return Result.failure(ApplicationError.conflict("Community membership", "The user already has a local community"));
                }
            }
        }
        if (community.get().getMemberLimit() != null
                && memberships.countByCommunityId(command.communityId()) >= community.get().getMemberLimit()) {
            return Result.failure(ApplicationError.conflict("Community capacity", "Community is full"));
        }
        CommunityMembership saved = memberships.save(
                new CommunityMembership(null, command.communityId(), command.userId(), CommunityRole.MEMBER));
        awardMembershipMilestone(command.communityId());
        return Result.success(saved);
    }

    @Override
    @Transactional
    public Result<Void, ApplicationError> handle(LeaveCommunityCommand command) {
        var membership = memberships.findByCommunityIdAndUserId(
                command.communityId(),
                command.userId()
        );

        if (communities.findByIdForUpdate(command.communityId()).isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Community",
                    String.valueOf(command.communityId())));
        }

        if (membership.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "Community membership",
                            command.communityId() + ":" + command.userId()
                    )
            );
        }

        for (var event : events.findByCommunityIdForUpdate(command.communityId())) {
            if (event.getAuthorId().equals(command.userId())) {
                lifecycle.delete(event.getId());
                continue;
            }

            int releasedPlaces = 0;

            for (var registration : registrations.findByEventId(event.getId())) {
                if (registration.status() != EventRegistrationStatus.REGISTERED) {
                    continue;
                }

                if (registration.userId().equals(command.userId())) {
                    releasedPlaces += registration.activeParticipantCount();
                    registrations.save(registration.cancel());
                } else if (registration.includes(command.userId())) {
                    releasedPlaces++;
                    registrations.save(registration.removeParticipant(command.userId()));
                }
            }

            if (releasedPlaces > 0) {
                events.save(event.releasePlaces(releasedPlaces));
            }
        }

        memberships.delete(membership.get());
        return Result.success(null);
    }

    private void awardCommunityAchievement(
            Long communityId,
            String title,
            String description) {
        boolean alreadyAwarded = achievements.findAll(communityId)
                .stream()
                .anyMatch(achievement ->
                        achievement.communityGoalId() == null
                                && title.equals(achievement.title()));

        if (alreadyAwarded) {
            return;
        }

        achievements.save(new CommunityAchievement(
                null,
                communityId,
                title,
                description,
                null));

        posts.save(new Post(
                null,
                communityId,
                null,
                description,
                "COMMUNITY_ACHIEVEMENT_EARNED",
                null,
                null));
    }

    private void awardMembershipMilestone(Long communityId) {
        long memberCount = memberships.countByCommunityId(communityId);

        boolean firstMilestone = memberCount == 10;
        boolean hundredMilestone = memberCount >= 100 && memberCount % 100 == 0;

        if (!firstMilestone && !hundredMilestone) {
            return;
        }

        String formattedCount = String.format(Locale.US, "%,d", memberCount);

        awardCommunityAchievement(
                communityId,
                formattedCount + " members",
                "Our community reached " + formattedCount + " members!");
    }
}
