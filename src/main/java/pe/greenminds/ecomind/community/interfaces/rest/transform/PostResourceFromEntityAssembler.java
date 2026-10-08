package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.Post;
import pe.greenminds.ecomind.community.interfaces.rest.resources.PostResource;

public final class PostResourceFromEntityAssembler {
    private PostResourceFromEntityAssembler() {
    }

    public static PostResource toResourceFromEntity(Post p, long likes) {
        return new PostResource(p.id(), p.communityId(), p.authorId(), p.content(), p.postType(), p.imageUrl(),
                p.relatedEventId(), likes);
    }
}
