package pe.greenminds.ecomind.community.interfaces.acl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.community.application.internal.commandservices.AchievementPublicationService;
import pe.greenminds.ecomind.community.domain.repositories.CommunityMembershipRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository;
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Public boundary backed by the published Community repositories and the Users public facade. */
@Component
@Transactional(readOnly = true)
public class CommunityContextFacadeImpl implements CommunityContextFacade {
    private final CommunityRepository communities;
    private final CommunityMembershipRepository memberships;
    private final UsersContextFacade users;
    private final AchievementPublicationService publications;

    public CommunityContextFacadeImpl(
            CommunityRepository communities,
            CommunityMembershipRepository memberships,
            UsersContextFacade users,
            AchievementPublicationService publications) {
        this.communities = communities;
        this.memberships = memberships;
        this.users = users;
        this.publications = publications;
    }

    public Optional<Long> findLocalCommunity(Long userId) {
        var localIds =
                memberships.findByUserId(userId).stream()
                        .map(m -> communities.findById(m.communityId()))
                        .flatMap(Optional::stream)
                        .filter(c -> "local".equals(c.getType()))
                        .map(c -> c.getId())
                        .distinct()
                        .limit(2)
                        .toList();
        if (localIds.size() > 1) throw new AmbiguousLocalCommunityException(userId);
        return localIds.stream().findFirst();
    }

    public boolean isMember(Long communityId, Long userId) {
        return communityId != null
                && communityId > 0
                && userId != null
                && userId > 0
                && communities.existsById(communityId)
                && memberships.findByCommunityIdAndUserId(communityId, userId).isPresent();
    }

    public boolean mayPublishAchievement(Long communityId, Long userId) {
        return isMember(communityId, userId);
    }

    public List<Member> findMembers(Long communityId) {
        Set<Long> ids =
                memberships.findByCommunityId(communityId).stream()
                        .map(m -> m.userId())
                        .collect(Collectors.toSet());
        return users.rankingUsers().stream()
                .filter(p -> ids.contains(p.id()))
                .map(p -> new Member(p.id(), p.displayName()))
                .toList();
    }

    @Transactional
    public void recordAchievementNotice(AchievementNotice notice) {
        publications.recordNotice(
                new pe.greenminds.ecomind.community.domain.model.aggregates.AchievementNotice(
                        notice.awardId(),
                        notice.achievementId(),
                        notice.userId(),
                        notice.occurredAt()));
    }

    @Transactional
    public void requestAchievementPublication(PublishAchievement command) {
        publications.publish(command);
    }
}
