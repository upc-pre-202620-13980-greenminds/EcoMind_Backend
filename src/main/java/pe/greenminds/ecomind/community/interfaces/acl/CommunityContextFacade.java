package pe.greenminds.ecomind.community.interfaces.acl;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Public membership and voluntary publication contract; canonical Community ids are numeric. */
public interface CommunityContextFacade {
    record Member(Long userId, String displayName) {}

    record AchievementNotice(UUID awardId, UUID achievementId, Long userId, Instant occurredAt) {
        public AchievementNotice {
            Objects.requireNonNull(awardId);
            Objects.requireNonNull(achievementId);
            Objects.requireNonNull(occurredAt);
            requireUser(userId);
        }
    }

    record PublishAchievement(UUID requestId, UUID awardId, Long requestedBy, Long communityId) {
        public PublishAchievement {
            Objects.requireNonNull(requestId);
            Objects.requireNonNull(awardId);
            Objects.requireNonNull(communityId);
            requireUser(requestedBy);
        }
    }

    Optional<Long> findLocalCommunity(Long userId);

    boolean isMember(Long communityId, Long userId);

    boolean mayPublishAchievement(Long communityId, Long userId);

    List<Member> findMembers(Long communityId);

    /** Deduplicate by awardId. Receiving this notice does not create a feed post. */
    void recordAchievementNotice(AchievementNotice notice);

    /**
     * Deduplicate by requestId, verify membership, then confirm with
     * PublicationCreatedIntegrationEvent.
     */
    void requestAchievementPublication(PublishAchievement command);

    private static void requireUser(Long userId) {
        if (userId == null || userId <= 0)
            throw new IllegalArgumentException("User id must be positive");
    }
}
