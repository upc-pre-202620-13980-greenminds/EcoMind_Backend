package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalTopic;

public record CreateCommunityGoalResource(
        @Schema(description = "Community identifier", example = "1")
        @NotNull Long community_id,
        @Schema(description = "Quest topic tracked by this goal", example = "energy",
                allowableValues = {"water", "energy", "recycle"})
        @NotNull CommunityGoalTopic topic,
        @Schema(description = "Number of quests required to complete the goal", example = "500")
        @NotNull @Positive Integer target) {}
