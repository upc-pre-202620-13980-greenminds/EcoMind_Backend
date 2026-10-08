package pe.greenminds.ecomind.community.interfaces.rest.resources;

import jakarta.validation.constraints.*;
public record CreateEventRegistrationResource(@NotBlank String registration_type,Long family_id,@NotNull Long user_id){}
