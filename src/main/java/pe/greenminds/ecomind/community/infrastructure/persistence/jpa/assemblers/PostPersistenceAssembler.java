package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.Post;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.PostPersistenceEntity;

public final class PostPersistenceAssembler {
    private PostPersistenceAssembler() {
    }

    public static Post toDomain(PostPersistenceEntity persistenceEntity) {
        return new Post(persistenceEntity.getId(), persistenceEntity.getCommunityId(), persistenceEntity.getAuthorId(),
                persistenceEntity.getContent(), persistenceEntity.getPostType(), persistenceEntity.getImageUrl(),
                persistenceEntity.getRelatedEventId());
    }

    public static PostPersistenceEntity toEntity(Post post) {
        return new PostPersistenceEntity(post.communityId(), post.authorId(), post.content(), post.postType(),
                post.imageUrl(), post.relatedEventId());
    }
}
