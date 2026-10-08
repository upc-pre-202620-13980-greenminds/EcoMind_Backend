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
import pe.greenminds.ecomind.community.domain.repositories.CommunityAchievementRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityMembershipRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class CommunityCommandServiceImpl implements CommunityCommandService {
    private final CommunityRepository communities;
    private final CommunityMembershipRepository memberships;
    private final CommunityAchievementRepository achievements;
    private final CommunityActorGateway actors;

    public CommunityCommandServiceImpl(CommunityRepository communities,
            CommunityMembershipRepository memberships, CommunityAchievementRepository achievements,
            CommunityActorGateway actors) {
        this.communities = communities;
        this.memberships = memberships;
        this.achievements = achievements;
        this.actors = actors;
    }

    @Override
    @Transactional
    public Result<Community, ApplicationError> handle(CreateLocalCommunityCommand command) {
        try {
            return create(CommunityType.LOCAL, command.name(), command.description(), null, command.locality(), null,
                    command.iconUrl(), command.userId());
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(ApplicationError.validationError("Community", exception.getMessage()));
        } catch (Exception exception) {
            return Result.failure(ApplicationError.unexpected("Community creation", exception.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<Community, ApplicationError> handle(CreateTopicCommunityCommand command) {
        try {
            actors.requireParent(command.userId());
            return create(CommunityType.TOPIC, command.name(), command.description(), command.topic(), null,
                    command.memberLimit(), command.iconUrl(), command.userId());
        } catch (SecurityException exception) {
            return Result.failure(ApplicationError.forbidden("COMMUNITY_CREATOR_NOT_PARENT", exception.getMessage()));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(ApplicationError.validationError("Community", exception.getMessage()));
        } catch (Exception exception) {
            return Result.failure(ApplicationError.unexpected("Community creation", exception.getMessage()));
        }
    }

    private Result<Community, ApplicationError> create(CommunityType type, String name, String description,
            String topic, String locality, Integer memberLimit, String iconUrl, Long creatorId) {
        Community community = new Community(null, name, description, type, topic, locality, memberLimit,
                iconUrl, creatorId);
        Community saved = communities.save(community);
        memberships.save(new CommunityMembership(null, saved.getId(), creatorId, CommunityRole.ADMIN));
        return Result.success(saved);
    }

    @Override
    @Transactional
    public Result<CommunityMembership, ApplicationError> handle(JoinCommunityCommand command) {
        var community = communities.findById(command.communityId());
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
        if (memberships.countByCommunityId(command.communityId()) == 1000) {
            achievements.save(new CommunityAchievement(null, command.communityId(), "1,000 members",
                    "The community reached 1,000 members.", null));
        }
        return Result.success(saved);
    }
}
