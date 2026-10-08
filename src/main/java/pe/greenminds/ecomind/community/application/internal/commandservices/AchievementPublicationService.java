package pe.greenminds.ecomind.community.application.internal.commandservices;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.community.domain.model.aggregates.AchievementNotice;
import pe.greenminds.ecomind.community.domain.model.aggregates.AchievementPost;
import pe.greenminds.ecomind.community.domain.repositories.AchievementPublicationRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityMembershipRepository;
import pe.greenminds.ecomind.community.interfaces.acl.CommunityContextFacade.PublishAchievement;
import pe.greenminds.ecomind.community.interfaces.acl.events.PublicationCreatedIntegrationEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AchievementPublicationService {
    private final AchievementPublicationRepository publications;
    private final CommunityMembershipRepository memberships;
    private final ApplicationEventPublisher events;

    public AchievementPublicationService(
            AchievementPublicationRepository publications,
            CommunityMembershipRepository memberships,
            ApplicationEventPublisher events) {
        this.publications = publications;
        this.memberships = memberships;
        this.events = events;
    }

    public void recordNotice(AchievementNotice notice) {
        if (!publications.recordNotice(notice).equals(notice))
            throw new IllegalArgumentException("Award id was reused with different notice data");
    }

    /**
     * Post creation and synchronous confirmation share the delivery transaction. Failure rolls both
     * back.
     */
    public void publish(PublishAchievement command) {
        publications.lockCommunity(command.communityId());
        var previous = publications.findByRequestId(command.requestId());
        AchievementPost post;
        if (previous.isPresent()) {
            post = previous.get();
            post.requireSameRequest(
                    command.awardId(), command.requestedBy(), command.communityId());
        } else {
            requireMembership(command.communityId(), command.requestedBy());
            post =
                    publications.save(
                            new AchievementPost(
                                    null,
                                    command.requestId(),
                                    command.awardId(),
                                    command.requestedBy(),
                                    command.communityId(),
                                    Instant.now()));
        }
        events.publishEvent(
                new PublicationCreatedIntegrationEvent(
                        UUID.randomUUID(),
                        post.requestId(),
                        post.awardId(),
                        post.authorId(),
                        post.communityId(),
                        post.id(),
                        post.publishedAt()));
    }

    @Transactional(readOnly = true)
    public List<AchievementPost> findPosts(
            Long communityId, Long requester, Long authorId, UUID awardId, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid page or size");
        requireMembership(communityId, requester);
        return publications.findPosts(communityId, authorId, awardId, page, size);
    }

    private void requireMembership(Long communityId, Long requester) {
        if (memberships.findByCommunityIdAndUserId(communityId, requester).isEmpty())
            throw new SecurityException(
                    "Community membership is required to publish or read posts");
    }
}
