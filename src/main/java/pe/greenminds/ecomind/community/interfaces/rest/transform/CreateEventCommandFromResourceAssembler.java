package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.CreateEventCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CreateEventResource;

public final class CreateEventCommandFromResourceAssembler {
    private CreateEventCommandFromResourceAssembler() {}

    public static CreateEventCommand toCommandFromResource(CreateEventResource resource) {
        return new CreateEventCommand(resource.community_id(), resource.author_id(), resource.name(), resource.description(),
                resource.date(), resource.start_time(), resource.location(), resource.latitude(),
                resource.longitude(), resource.capacity(), resource.image_url());
    }
}
