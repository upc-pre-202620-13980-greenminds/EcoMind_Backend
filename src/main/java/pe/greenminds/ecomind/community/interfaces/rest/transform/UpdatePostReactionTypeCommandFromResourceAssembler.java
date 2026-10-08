package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.UpdatePostReactionTypeCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.UpdatePostReactionTypeResource;

public final class UpdatePostReactionTypeCommandFromResourceAssembler {
    private UpdatePostReactionTypeCommandFromResourceAssembler() {}

    public static UpdatePostReactionTypeCommand toCommandFromResource(
            UpdatePostReactionTypeResource resource, Long postId) {
        return new UpdatePostReactionTypeCommand(postId, resource.user_id(), resource.reaction_type());
    }
}
