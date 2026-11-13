package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.EventRegistrationPersistenceEntity;

public final class EventRegistrationPersistenceAssembler {

    private EventRegistrationPersistenceAssembler() {
    }

    public static EventRegistration toDomain(EventRegistrationPersistenceEntity entity) {
        if (entity.getParticipantIds().isEmpty()
                && entity.getRegistrationType() == EventRegistrationType.INDIVIDUAL
                && entity.getStatus() == EventRegistrationStatus.REGISTERED) {
            return new EventRegistration(
                    entity.getId(),
                    entity.getEventId(),
                    entity.getUserId(),
                    entity.getRegistrationType(),
                    entity.getFamilyId(),
                    entity.getParticipantCount(),
                    entity.getStatus());
        }

        return new EventRegistration(
                entity.getId(),
                entity.getEventId(),
                entity.getUserId(),
                entity.getRegistrationType(),
                entity.getFamilyId(),
                entity.getParticipantCount(),
                entity.getStatus(),
                entity.getParticipantIds());
    }

    public static EventRegistrationPersistenceEntity toEntity(EventRegistration registration) {
        var entity = new EventRegistrationPersistenceEntity(
                registration.eventId(),
                registration.userId(),
                registration.registrationType(),
                registration.familyId(),
                registration.participantCount());

        entity.replaceParticipants(registration.participantIds());

        if (registration.status() == EventRegistrationStatus.CANCELLED) {
            entity.cancel();
        }

        return entity;
    }
}