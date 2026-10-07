package pe.greenminds.ecomind.quests.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.quests.application.commandservices.FamilyPlanCommandService;
import pe.greenminds.ecomind.quests.application.queryservices.FamilyPlanQueryService;
import pe.greenminds.ecomind.quests.domain.model.commands.ActivateFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.CompleteFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActiveFamilyPlanByFamilyIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetFamilyPlanByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetFamilyPlansByFamilyIdQuery;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateFamilyPlanResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.FamilyPlanResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.UpdateFamilyPlanResource;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.FamilyPlanCommandFromResourceAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.FamilyPlanResourceFromStateAssembler;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ErrorResponseAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/family-plans", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Family Plans", description = "Family plan endpoints")
public class FamilyPlanController {
    private final FamilyPlanCommandService familyPlanCommandService;
    private final FamilyPlanQueryService familyPlanQueryService;

    public FamilyPlanController(
            FamilyPlanCommandService familyPlanCommandService,
            FamilyPlanQueryService familyPlanQueryService
    ) {
        this.familyPlanCommandService = familyPlanCommandService;
        this.familyPlanQueryService = familyPlanQueryService;
    }

    @GetMapping
    @Operation(summary = "Get family plans by family")
    public ResponseEntity<List<FamilyPlanResource>> getFamilyPlans(
            @RequestParam Long familyId
    ) {
        var plans = familyPlanQueryService.handle(new GetFamilyPlansByFamilyIdQuery(familyId))
                .stream()
                .map(FamilyPlanResourceFromStateAssembler::toResourceFromState)
                .toList();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active family plan")
    public ResponseEntity<?> getActiveFamilyPlan(@RequestParam Long familyId) {
        var plan = familyPlanQueryService.handle(
                new GetActiveFamilyPlanByFamilyIdQuery(familyId)
        );
        if (plan.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("FamilyPlan", "active?familyId=" + familyId)
            );
        }
        return ResponseEntity.ok(
                FamilyPlanResourceFromStateAssembler.toResourceFromState(plan.get())
        );
    }

    @GetMapping("/{familyPlanId}")
    @Operation(summary = "Get family plan by id")
    public ResponseEntity<?> getFamilyPlanById(@PathVariable Long familyPlanId) {
        var plan = familyPlanQueryService.handle(new GetFamilyPlanByIdQuery(familyPlanId));
        if (plan.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("FamilyPlan", familyPlanId.toString())
            );
        }
        return ResponseEntity.ok(
                FamilyPlanResourceFromStateAssembler.toResourceFromState(plan.get())
        );
    }

    @PostMapping
    @Operation(summary = "Create a family plan")
    public ResponseEntity<?> createFamilyPlan(
            @Valid @RequestBody CreateFamilyPlanResource resource
    ) {
        var result = familyPlanCommandService.handle(
                FamilyPlanCommandFromResourceAssembler.toCommandFromResource(resource)
        );
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FamilyPlanResourceFromStateAssembler::toResourceFromState,
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{familyPlanId}")
    @Operation(summary = "Update a draft family plan")
    public ResponseEntity<?> updateFamilyPlan(
            @PathVariable Long familyPlanId,
            @Valid @RequestBody UpdateFamilyPlanResource resource
    ) {
        var result = familyPlanCommandService.handle(
                FamilyPlanCommandFromResourceAssembler.toCommandFromResource(
                        familyPlanId,
                        resource
                )
        );
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FamilyPlanResourceFromStateAssembler::toResourceFromState,
                HttpStatus.OK
        );
    }

    @PostMapping("/{familyPlanId}/activate")
    @Operation(summary = "Activate a family plan")
    public ResponseEntity<?> activateFamilyPlan(@PathVariable Long familyPlanId) {
        var result = familyPlanCommandService.handle(
                new ActivateFamilyPlanCommand(familyPlanId)
        );
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FamilyPlanResourceFromStateAssembler::toResourceFromState,
                HttpStatus.OK
        );
    }

    @PostMapping("/{familyPlanId}/complete")
    @Operation(summary = "Complete an active family plan")
    public ResponseEntity<?> completeFamilyPlan(
            @PathVariable Long familyPlanId,
            @RequestParam Long ownerUserId
    ) {
        var result = familyPlanCommandService.handle(
                new CompleteFamilyPlanCommand(familyPlanId, ownerUserId)
        );
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FamilyPlanResourceFromStateAssembler::toResourceFromState,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{familyPlanId}")
    @Operation(summary = "Delete or cancel a family plan")
    public ResponseEntity<?> deleteFamilyPlan(@PathVariable Long familyPlanId) {
        var result = familyPlanCommandService.handle(
                new DeleteFamilyPlanCommand(familyPlanId)
        );
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FamilyPlanResourceFromStateAssembler::toResourceFromState,
                HttpStatus.OK
        );
    }
}
