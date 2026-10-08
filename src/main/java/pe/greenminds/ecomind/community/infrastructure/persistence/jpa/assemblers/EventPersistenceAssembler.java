package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.EventPersistenceEntity;

public final class EventPersistenceAssembler {
    private EventPersistenceAssembler() {
    }

    public static Event toDomain(EventPersistenceEntity e) {
        return new Event(e.getId(), e.getCommunityId(), e.getAuthorId(), e.getName(), e.getDescription(), e.getDate(),
                e.getStartTime(), e.getLocation(), e.getLatitude(), e.getLongitude(), e.getCapacity(), e.getImageUrl());
    }

    public static EventPersistenceEntity toEntity(Event e) {
        return new EventPersistenceEntity(e.getId(), e.getCommunityId(), e.getAuthorId(), e.getName(),
                e.getDescription(), e.getDate(), e.getStartTime(), e.getLocation(), e.getLatitude(), e.getLongitude(),
                e.getCapacity(), e.getImageUrl());
    }
}
