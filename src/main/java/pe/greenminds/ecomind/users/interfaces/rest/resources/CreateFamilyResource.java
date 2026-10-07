package pe.greenminds.ecomind.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required to create a family")
public record CreateFamilyResource(
    @Schema(example = "Torres Family")
    @NotBlank @Size(max = 150)
    String name,
    @Schema(description = "Commitment the family declares",
        example = "We will separate our waste every day")
    @NotBlank @Size(max = 255)
    String commitment) {
}
