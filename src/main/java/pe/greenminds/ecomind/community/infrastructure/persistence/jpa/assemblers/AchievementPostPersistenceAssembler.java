package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.AchievementPost;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.AchievementPostPersistenceEntity;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityPersistenceEntity;

import java.util.UUID;

public final class AchievementPostPersistenceAssembler {
    private AchievementPostPersistenceAssembler() {}

    public static AchievementPost toDomain(AchievementPostPersistenceEntity row) {
        return new AchievementPost(
                row.getId(),
                UUID.fromString(row.getRequestId()),
                UUID.fromString(row.getAwardId()),
                row.getAuthorId(),
                row.getCommunity().getId(),
                row.getPublishedAt());
    }

    public static AchievementPostPersistenceEntity toEntity(
            AchievementPost post, CommunityPersistenceEntity community) {
        var row = new AchievementPostPersistenceEntity();
        row.setRequestId(post.requestId().toString());
        row.setAwardId(post.awardId().toString());
        row.setAuthorId(post.authorId());
        row.setCommunity(community);
        row.setPublishedAt(post.publishedAt());
        return row;
    }
}
