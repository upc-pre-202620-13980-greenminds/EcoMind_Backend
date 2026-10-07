package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.commands.CreateQuestUserCommand;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateQuestUserResource;

public final class CreateQuestUserCommandFromResourceAssembler {
    private CreateQuestUserCommandFromResourceAssembler() {
    }

    public static CreateQuestUserCommand toCommandFromResource(CreateQuestUserResource resource) {
        return new CreateQuestUserCommand(
                resource.userId(),
                resource.questId(),
                resource.collaborativeSessionId()
        );
    }
}
