package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.UpdatePostReactionTypeCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.UpdatePostReactionTypeResource;

public final class UpdatePostReactionTypeCommandFromResourceAssembler {
    private UpdatePostReactionTypeCommandFromResourceAssembler() {}

    public static UpdatePostReactionTypeCommand toCommandFromResource(
            UpdatePostReactionTypeResource resource, Long postId, Long userId) {
        return new UpdatePostReactionTypeCommand(postId, userId, resource.reaction_type());
    }
}
