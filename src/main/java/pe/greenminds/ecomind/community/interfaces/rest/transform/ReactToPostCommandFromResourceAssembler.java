package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.ReactToPostCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CreatePostReactionResource;

public final class ReactToPostCommandFromResourceAssembler {
    private ReactToPostCommandFromResourceAssembler() {}

    public static ReactToPostCommand toCommandFromResource(CreatePostReactionResource resource) {
        return new ReactToPostCommand(resource.post_id(), resource.user_id(), resource.reaction_type());
    }
}
