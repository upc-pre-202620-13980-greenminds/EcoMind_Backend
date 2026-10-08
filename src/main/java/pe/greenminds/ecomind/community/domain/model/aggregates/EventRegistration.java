package pe.greenminds.ecomind.community.domain.model.aggregates;

import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;

public record EventRegistration(Long id, Long eventId, Long userId, EventRegistrationType registrationType, Long familyId,
        Integer participantCount, EventRegistrationStatus status) {
    public EventRegistration {
        if (eventId == null || userId == null || participantCount == null || participantCount < 1)
            throw new IllegalArgumentException("Event, user and participant count are required");
        if (registrationType == null || status == null)
            throw new IllegalArgumentException("Registration type and status are required");
    }
}
