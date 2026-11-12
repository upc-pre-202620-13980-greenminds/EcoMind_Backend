package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.UpdateEventCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.UpdateEventResource;

public final class UpdateEventCommandFromResourceAssembler {

    private UpdateEventCommandFromResourceAssembler() {
    }

    public static UpdateEventCommand toCommandFromResource(
            UpdateEventResource resource,
            Long eventId,
            Long requestedBy) {

        return new UpdateEventCommand(
                eventId,
                requestedBy,
                resource.name(),
                resource.description(),
                resource.date(),
                resource.start_time(),
                resource.location(),
                resource.latitude(),
                resource.longitude(),
                resource.capacity(),
                resource.image_url());
    }
}