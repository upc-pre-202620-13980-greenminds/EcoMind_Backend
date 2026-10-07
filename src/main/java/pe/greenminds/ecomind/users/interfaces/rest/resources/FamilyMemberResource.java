package pe.greenminds.ecomind.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Member of a family")
public record FamilyMemberResource(
    @Schema(description = "Id of the membership", example = "3")
    Long id,
    @Schema(example = "1")
    Long familyId,
    @Schema(example = "8")
    Long userId,
    @Schema(example = "CHILD", allowableValues = {"PARENT", "CHILD"})
    String familyRole) {
}
