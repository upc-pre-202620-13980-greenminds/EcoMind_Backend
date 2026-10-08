package pe.greenminds.ecomind.gamification.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.greenminds.ecomind.gamification.application.queryservices.GamificationQueryService;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetUserProgressQuery;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.RewardTransactionResource;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.UserProgressResource;
import pe.greenminds.ecomind.gamification.interfaces.rest.transform.RewardTransactionResourceFromEntityAssembler;
import pe.greenminds.ecomind.gamification.interfaces.rest.transform.UserProgressResourceFromEntityAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/gamification/me", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Gamification", description = "Authenticated user's progress and reward history")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class GamificationController {
    private final GamificationQueryService queryService;

    public GamificationController(GamificationQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/progress")
    @Operation(summary = "Get my ecopoints, experience and daily streak")
    public UserProgressResource getMyProgress(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return UserProgressResourceFromEntityAssembler.toResourceFromEntity(
                queryService.handle(new GetUserProgressQuery(new UserId(principal.accountId()))));
    }

    @GetMapping("/rewards")
    @Operation(summary = "Get my 100 most recent reward transactions")
    public List<RewardTransactionResource> getMyRewards(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return queryService.getRecentRewards(new UserId(principal.accountId())).stream()
                .map(RewardTransactionResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }
}
