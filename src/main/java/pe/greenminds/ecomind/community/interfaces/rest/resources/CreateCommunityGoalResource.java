package pe.greenminds.ecomind.community.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalTopic;

public record CreateCommunityGoalResource(
        @NotNull Long community_id,
        @NotNull CommunityGoalTopic topic,
        @NotNull @Positive Integer target,
        @NotNull Long user_id) {}
