package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.AchievementPost;
import pe.greenminds.ecomind.community.interfaces.rest.resources.AchievementPostResource;

public final class AchievementPostResourceFromEntityAssembler {
    private AchievementPostResourceFromEntityAssembler() {}

    public static AchievementPostResource toResourceFromEntity(AchievementPost post) {
        return new AchievementPostResource(
                post.id(),
                post.requestId(),
                post.awardId(),
                post.authorId(),
                post.communityId(),
                post.publishedAt());
    }
}
