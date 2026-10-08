package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.RegisterForEventCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CreateEventRegistrationResource;

public final class RegisterForEventCommandFromResourceAssembler {
    private RegisterForEventCommandFromResourceAssembler() {}

    public static RegisterForEventCommand toCommandFromResource(CreateEventRegistrationResource resource,
                                                                 Long eventId, Long userId) {
        return new RegisterForEventCommand(eventId, userId, resource.registration_type(),
                resource.family_id());
    }
}
