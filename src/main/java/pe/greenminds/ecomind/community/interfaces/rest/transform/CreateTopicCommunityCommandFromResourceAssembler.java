package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.CreateTopicCommunityCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CreateTopicCommunityResource;

public final class CreateTopicCommunityCommandFromResourceAssembler {
    private CreateTopicCommunityCommandFromResourceAssembler() {}

    public static CreateTopicCommunityCommand toCommandFromResource(CreateTopicCommunityResource resource) {
        return new CreateTopicCommunityCommand(resource.name(), resource.description(), resource.topic(),
                resource.member_limit(), resource.icon_url(), resource.user_id());
    }
}
