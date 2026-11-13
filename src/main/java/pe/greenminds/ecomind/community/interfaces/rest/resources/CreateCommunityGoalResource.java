package pe.greenminds.ecomind.community.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalTopic;

public record CreateCommunityGoalResource(
        @NotNull @Positive Long community_id,
        @NotNull CommunityGoalTopic topic) {
}