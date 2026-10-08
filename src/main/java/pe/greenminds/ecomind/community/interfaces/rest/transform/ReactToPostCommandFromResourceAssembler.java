package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.ReactToPostCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CreatePostReactionResource;

public final class ReactToPostCommandFromResourceAssembler {
    private ReactToPostCommandFromResourceAssembler() {}

    public static ReactToPostCommand toCommandFromResource(CreatePostReactionResource resource, Long userId) {
        return new ReactToPostCommand(resource.post_id(), userId, resource.reaction_type());
    }
}
