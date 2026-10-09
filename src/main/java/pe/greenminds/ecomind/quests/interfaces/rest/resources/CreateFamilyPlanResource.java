package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateFamilyPlanResource(
        @NotNull
        @Positive
        Long familyId,

        @Valid
        List<FamilyPlanItemRequestResource> items
) {
}
