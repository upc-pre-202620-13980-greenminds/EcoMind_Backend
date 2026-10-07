package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.commands.SubmitActivityUserCommand;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.SubmitActivityUserResource;

public class SubmitActivityUserCommandFromResourceAssembler {
    private SubmitActivityUserCommandFromResourceAssembler() {
    }

    public static SubmitActivityUserCommand toCommandFromResource(
            Long activityUserId,
            SubmitActivityUserResource resource
    ) {
        return new SubmitActivityUserCommand(activityUserId, resource.data());
    }
}
