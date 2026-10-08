package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.CreateLocalCommunityCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CreateLocalCommunityResource;

public final class CreateLocalCommunityCommandFromResourceAssembler {
    private CreateLocalCommunityCommandFromResourceAssembler() {}

    public static CreateLocalCommunityCommand toCommandFromResource(CreateLocalCommunityResource resource) {
        return new CreateLocalCommunityCommand(resource.name(), resource.description(), resource.locality(),
                resource.icon_url(), resource.user_id());
    }
}
