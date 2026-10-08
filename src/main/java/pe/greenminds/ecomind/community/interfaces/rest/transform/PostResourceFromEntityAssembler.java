package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.Post;
import pe.greenminds.ecomind.community.interfaces.rest.resources.PostResource;

public final class PostResourceFromEntityAssembler {
    private PostResourceFromEntityAssembler() {
    }

    public static PostResource toResourceFromEntity(Post post, long likes) {
        return new PostResource(post.id(), post.communityId(), post.authorId(), post.content(), post.postType(),
                post.imageUrl(), post.relatedEventId(), likes);
    }
}
