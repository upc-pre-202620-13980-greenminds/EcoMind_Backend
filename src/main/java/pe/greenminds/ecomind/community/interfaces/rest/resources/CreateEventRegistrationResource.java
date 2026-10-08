package pe.greenminds.ecomind.community.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;

public record CreateEventRegistrationResource(@NotNull EventRegistrationType registration_type, Long family_id,
        @NotNull Long user_id) {}
