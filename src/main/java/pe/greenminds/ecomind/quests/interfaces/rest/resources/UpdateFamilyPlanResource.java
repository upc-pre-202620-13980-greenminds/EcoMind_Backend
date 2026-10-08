package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import jakarta.validation.Valid;

import java.util.List;

public record UpdateFamilyPlanResource(
        @Valid
        List<FamilyPlanItemRequestResource> items
) {
}
