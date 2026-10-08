package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.interfaces.rest.resources.EventRegistrationResource;

public final class EventRegistrationResourceFromEntityAssembler {
    private EventRegistrationResourceFromEntityAssembler() {
    }

    public static EventRegistrationResource toResourceFromEntity(EventRegistration r) {
        return new EventRegistrationResource(r.id(), r.eventId(), r.userId(), r.registrationType(), r.familyId(),
                r.participantCount(), r.status());
    }
}
