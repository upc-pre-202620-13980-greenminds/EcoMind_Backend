package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.CreatePostCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CreatePostResource;

public final class CreatePostCommandFromResourceAssembler {
    private CreatePostCommandFromResourceAssembler() {}

    public static CreatePostCommand toCommandFromResource(CreatePostResource resource, Long authorId) {
        return new CreatePostCommand(resource.community_id(), authorId, resource.content(), resource.image_url());
    }
}
