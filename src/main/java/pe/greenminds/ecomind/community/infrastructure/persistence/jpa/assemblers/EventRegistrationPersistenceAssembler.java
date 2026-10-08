package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.EventRegistrationPersistenceEntity;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;

public final class EventRegistrationPersistenceAssembler {
    private EventRegistrationPersistenceAssembler() {
    }

    public static EventRegistration toDomain(EventRegistrationPersistenceEntity persistenceEntity) {
        return new EventRegistration(persistenceEntity.getId(), persistenceEntity.getEventId(),
                persistenceEntity.getUserId(), persistenceEntity.getRegistrationType(), persistenceEntity.getFamilyId(),
                persistenceEntity.getParticipantCount(), persistenceEntity.getStatus());
    }

    public static EventRegistrationPersistenceEntity toEntity(EventRegistration eventRegistration) {
        var persistenceEntity = new EventRegistrationPersistenceEntity(eventRegistration.eventId(),
                eventRegistration.userId(), eventRegistration.registrationType(), eventRegistration.familyId(),
                eventRegistration.participantCount());
        if (eventRegistration.status() == EventRegistrationStatus.CANCELLED)
            persistenceEntity.cancel();
        return persistenceEntity;
    }
}
