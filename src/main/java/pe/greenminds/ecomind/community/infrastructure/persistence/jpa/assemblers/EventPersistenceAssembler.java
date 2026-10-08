package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.EventPersistenceEntity;

public final class EventPersistenceAssembler {
    private EventPersistenceAssembler() {
    }

    public static Event toDomain(EventPersistenceEntity persistenceEntity) {
        return new Event(persistenceEntity.getId(), persistenceEntity.getCommunityId(), persistenceEntity.getAuthorId(),
                persistenceEntity.getName(), persistenceEntity.getDescription(), persistenceEntity.getDate(),
                persistenceEntity.getStartTime(), persistenceEntity.getLocation(), persistenceEntity.getLatitude(),
                persistenceEntity.getLongitude(), persistenceEntity.getCapacity(), persistenceEntity.getImageUrl());
    }

    public static EventPersistenceEntity toEntity(Event event) {
        return new EventPersistenceEntity(event.getId(), event.getCommunityId(), event.getAuthorId(), event.getName(),
                event.getDescription(), event.getDate(), event.getStartTime(), event.getLocation(), event.getLatitude(),
                event.getLongitude(), event.getCapacity(), event.getImageUrl());
    }
}
