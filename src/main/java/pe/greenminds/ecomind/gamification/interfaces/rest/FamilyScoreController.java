package pe.greenminds.ecomind.gamification.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.greenminds.ecomind.gamification.application.queryservices.FamilyScoreQueryService;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.interfaces.rest.transform.FamilyRewardResourceFromEntityAssembler;
import pe.greenminds.ecomind.gamification.interfaces.rest.transform.FamilyScoreResourceFromEntityAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@RequestMapping(
        value = "/api/v1/gamification/families/{familyId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Gamification")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class FamilyScoreController {
    private final FamilyScoreQueryService queries;
    private final ResponseEntityAssembler responses;

    public FamilyScoreController(
            FamilyScoreQueryService queries, ResponseEntityAssembler responses) {
        this.queries = queries;
        this.responses = responses;
    }

    @GetMapping("/score")
    @Operation(
            summary = "Get a family's ecopoints",
            description = "Available to current family members only.")
    public ResponseEntity<?> getScore(
            @PathVariable Long familyId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responses.toResponseEntityFromResult(
                queries.getScore(new FamilyId(familyId), new UserId(principal.accountId())),
                FamilyScoreResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    @GetMapping("/rewards")
    @Operation(
            summary = "Get a family's 100 most recent rewards",
            description = "Available to current family members only.")
    public ResponseEntity<?> getRewards(
            @PathVariable Long familyId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responses.toResponseEntityFromResult(
                queries.getRewards(new FamilyId(familyId), new UserId(principal.accountId())),
                rewards ->
                        rewards.stream()
                                .map(FamilyRewardResourceFromEntityAssembler::toResourceFromEntity)
                                .toList(),
                HttpStatus.OK);
    }
}
