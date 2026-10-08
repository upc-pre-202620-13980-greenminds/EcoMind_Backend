package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.commands.CreateQuestUserCommand;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateQuestUserResource;

public final class CreateQuestUserCommandFromResourceAssembler {
    private CreateQuestUserCommandFromResourceAssembler() {
    }

    public static CreateQuestUserCommand toCommandFromResource(
            CreateQuestUserResource resource,
            Long authenticatedUserId
    ) {
        return new CreateQuestUserCommand(
                authenticatedUserId,
                resource.questId(),
                resource.collaborativeSessionId()
        );
    }
}
