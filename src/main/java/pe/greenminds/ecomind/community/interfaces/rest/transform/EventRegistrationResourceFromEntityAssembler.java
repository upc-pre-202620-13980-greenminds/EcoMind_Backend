package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.interfaces.rest.resources.EventRegistrationResource;

public final class EventRegistrationResourceFromEntityAssembler {
    private EventRegistrationResourceFromEntityAssembler() {
    }

    public static EventRegistrationResource toResourceFromEntity(EventRegistration eventRegistration) {
        return new EventRegistrationResource(eventRegistration.id(), eventRegistration.eventId(),
                eventRegistration.userId(), eventRegistration.registrationType(), eventRegistration.familyId(),
                eventRegistration.participantCount(), eventRegistration.status());
    }
}
