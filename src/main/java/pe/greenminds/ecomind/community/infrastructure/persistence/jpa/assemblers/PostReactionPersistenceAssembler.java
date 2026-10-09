package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.PostReaction;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.PostReactionPersistenceEntity;

public final class PostReactionPersistenceAssembler {
    private PostReactionPersistenceAssembler() {
    }

    public static PostReaction toDomain(PostReactionPersistenceEntity persistenceEntity) {
        return new PostReaction(persistenceEntity.getId(), persistenceEntity.getPostId(), persistenceEntity.getUserId(),
                persistenceEntity.getReactionType());
    }

    public static PostReactionPersistenceEntity toEntity(PostReaction postReaction) {
        var persistenceEntity = new PostReactionPersistenceEntity(postReaction.postId(), postReaction.userId(),
                postReaction.reactionType());
        if (postReaction.id() != null)
            persistenceEntity.changeType(postReaction.reactionType());
        return persistenceEntity;
    }
}
