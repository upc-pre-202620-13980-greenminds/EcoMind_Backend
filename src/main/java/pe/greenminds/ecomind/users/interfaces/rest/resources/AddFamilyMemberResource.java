package pe.greenminds.ecomind.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Schema(description = "User to add to a family and the role they will have")
public record AddFamilyMemberResource(
    @Schema(example = "1")
    @NotNull @Positive
    Long familyId,
    @Schema(example = "8")
    @NotNull @Positive
    Long userId,
    @Schema(example = "CHILD", allowableValues = {"PARENT", "CHILD"})
    @NotBlank @Pattern(regexp = "PARENT|CHILD", message = "{users.validation.family-role}")
    String familyRole) {
}
