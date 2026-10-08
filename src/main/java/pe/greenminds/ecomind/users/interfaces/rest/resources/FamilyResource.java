package pe.greenminds.ecomind.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Family group with its members")
public record FamilyResource(
    @Schema(example = "1")
    Long id,
    @Schema(example = "Torres Family")
    String name,
    @Schema(example = "We will separate our waste every day")
    String commitment,
    List<FamilyMemberResource> members) {
}
