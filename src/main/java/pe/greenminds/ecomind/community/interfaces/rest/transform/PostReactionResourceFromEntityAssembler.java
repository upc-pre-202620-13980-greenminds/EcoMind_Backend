package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.PostReaction;
import pe.greenminds.ecomind.community.interfaces.rest.resources.PostReactionResource;

public final class PostReactionResourceFromEntityAssembler {
    private PostReactionResourceFromEntityAssembler() {
    }

    public static PostReactionResource toResourceFromEntity(PostReaction postReaction) {
        return new PostReactionResource(postReaction.id(), postReaction.postId(), postReaction.userId(),
                postReaction.reactionType());
    }
}
