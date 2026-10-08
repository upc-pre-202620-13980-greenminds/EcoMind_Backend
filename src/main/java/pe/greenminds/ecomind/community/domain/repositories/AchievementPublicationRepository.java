package pe.greenminds.ecomind.community.domain.repositories;

import pe.greenminds.ecomind.community.domain.model.aggregates.AchievementNotice;
import pe.greenminds.ecomind.community.domain.model.aggregates.AchievementPost;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementPublicationRepository {
    void lockCommunity(Long communityId);

    Optional<AchievementPost> findByRequestId(UUID requestId);

    AchievementPost save(AchievementPost post);

    AchievementNotice recordNotice(AchievementNotice notice);

    List<AchievementPost> findPosts(
            Long communityId, Long authorId, UUID awardId, int page, int size);
}
