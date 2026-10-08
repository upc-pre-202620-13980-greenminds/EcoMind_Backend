package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.interfaces.rest.resources.EventResource;

public final class EventResourceFromEntityAssembler {
    private EventResourceFromEntityAssembler() {
    }

    public static EventResource toResourceFromEntity(Event event) {
        return new EventResource(event.getId(), event.getCommunityId(), event.getAuthorId(), event.getName(),
                event.getDescription(), event.getDate(), event.getStartTime(), event.getLocation(), event.getLatitude(),
                event.getLongitude(), event.getCapacity(), event.getImageUrl());
    }
}
