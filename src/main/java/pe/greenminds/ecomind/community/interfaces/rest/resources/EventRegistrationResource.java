package pe.greenminds.ecomind.community.interfaces.rest.resources;

import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;

public record EventRegistrationResource(Long id, Long event_id, Long user_id, EventRegistrationType registration_type,
        Long family_id, Integer participant_count, EventRegistrationStatus status) {
}
