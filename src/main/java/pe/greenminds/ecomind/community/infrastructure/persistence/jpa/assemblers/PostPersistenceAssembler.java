package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.Post;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.PostPersistenceEntity;

public final class PostPersistenceAssembler {
    private PostPersistenceAssembler() {
    }

    public static Post toDomain(PostPersistenceEntity entity) {
        return new Post(
                entity.getId(),
                entity.getCommunityId(),
                entity.getAuthorId(),
                entity.getContent(),
                entity.getPostType(),
                entity.getImageUrl(),
                entity.getRelatedEventId(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toInstant());
    }

    public static PostPersistenceEntity toEntity(Post post) {
        return new PostPersistenceEntity(
                post.communityId(),
                post.authorId(),
                post.content(),
                post.postType(),
                post.imageUrl(),
                post.relatedEventId());
    }
}