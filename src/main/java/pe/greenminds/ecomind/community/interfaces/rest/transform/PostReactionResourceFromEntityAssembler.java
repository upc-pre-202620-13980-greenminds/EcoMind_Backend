package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.PostReaction;
import pe.greenminds.ecomind.community.interfaces.rest.resources.PostReactionResource;

public final class PostReactionResourceFromEntityAssembler {
    private PostReactionResourceFromEntityAssembler() {
    }

    public static PostReactionResource toResourceFromEntity(PostReaction r) {
        return new PostReactionResource(r.id(), r.postId(), r.userId(), r.reactionType());
    }
}
