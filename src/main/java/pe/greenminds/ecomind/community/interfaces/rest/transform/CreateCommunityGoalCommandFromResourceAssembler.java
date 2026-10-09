package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.CreateCommunityGoalCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CreateCommunityGoalResource;

public final class CreateCommunityGoalCommandFromResourceAssembler {
    private CreateCommunityGoalCommandFromResourceAssembler() {}

    public static CreateCommunityGoalCommand toCommandFromResource(CreateCommunityGoalResource resource, Long userId) {
        return new CreateCommunityGoalCommand(resource.community_id(), resource.topic(), resource.target(),
                userId);
    }
}
