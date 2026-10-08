package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;

public record CreateEventRegistrationResource(
        @Schema(description = "Registration type", example = "INDIVIDUAL", allowableValues = {"INDIVIDUAL", "FAMILY"})
        @NotNull EventRegistrationType registration_type,
        @Schema(description = "Family identifier; required for FAMILY registrations", example = "1", nullable = true)
        Long family_id) {}
