package pe.greenminds.ecomind.gamification.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.application.queryservices.AchievementQueryService;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.AchievementAwardResource;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.AchievementResource;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.ShareAchievementResource;
import pe.greenminds.ecomind.gamification.interfaces.rest.transform.AchievementAwardResourceFromEntityAssembler;
import pe.greenminds.ecomind.gamification.interfaces.rest.transform.AchievementResourceFromEntityAssembler;
import pe.greenminds.ecomind.gamification.interfaces.rest.transform.AchievementShareResourceFromEntityAssembler;
import pe.greenminds.ecomind.gamification.interfaces.rest.transform.ShareAchievementCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/gamification", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Gamification")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class AchievementController {
    private final AchievementQueryService queries;
    private final AchievementCommandService commands;
    private final ResponseEntityAssembler responses;

    public AchievementController(
            AchievementQueryService queries,
            ResponseEntityAssembler responses,
            AchievementCommandService commands) {
        this.queries = queries;
        this.commands = commands;
        this.responses = responses;
    }

    @PostMapping("/achievement-shares")
    @Operation(
            summary = "Request publication of my individual achievement",
            description =
                    "Use the same requestId when retrying. PENDING does not mean a post was"
                            + " created.")
    public ResponseEntity<?> share(
            @Valid @RequestBody ShareAchievementResource resource,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responses.toResponseEntityFromResult(
                commands.handle(
                        ShareAchievementCommandFromResourceAssembler.toCommandFromResource(
                                resource, principal.accountId())),
                AchievementShareResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.ACCEPTED);
    }

    @GetMapping("/achievement-shares/{requestId}")
    @Operation(summary = "Get the status of my publication request")
    public ResponseEntity<?> shareStatus(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responses.toResponseEntityFromResult(
                queries.shareStatus(requestId, new UserId(principal.accountId())),
                AchievementShareResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    @GetMapping("/communities/{communityId}/achievements")
    @Operation(
            summary = "Get collective achievements awarded to my community",
            description =
                    "Requires community membership. Shared individual achievements remain in"
                            + " Community's feed.")
    public ResponseEntity<?> forCommunity(
            @PathVariable UUID communityId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return responses.toResponseEntityFromResult(
                queries.forCommunity(communityId, new UserId(principal.accountId()), page, size),
                awards ->
                        awards.stream()
                                .map(
                                        AchievementAwardResourceFromEntityAssembler
                                                ::toResourceFromEntity)
                                .toList(),
                HttpStatus.OK);
    }

    @GetMapping("/achievements")
    @Operation(
            summary = "Browse the achievement catalog",
            description =
                    "Optional scope filter. Pages start at zero; maximum size is 100. Includes"
                            + " inactive definitions.")
    public List<AchievementResource> search(
            @RequestParam(required = false) AchievementScope scope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return queries.search(scope, page, size).stream()
                .map(AchievementResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }

    @GetMapping("/achievements/{achievementId}")
    @Operation(summary = "Get an achievement definition")
    public ResponseEntity<?> find(@PathVariable UUID achievementId) {
        return responses.toResponseEntityFromResult(
                queries.find(achievementId),
                AchievementResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    @GetMapping("/me/achievements")
    @Operation(
            summary = "Get my awarded achievements",
            description = "Pages start at zero; maximum size is 100.")
    public List<AchievementAwardResource> forUser(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return queries.forUser(new UserId(principal.accountId()), page, size).stream()
                .map(AchievementAwardResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }

    @GetMapping("/families/{familyId}/achievements")
    @Operation(
            summary = "Get a family's awarded achievements",
            description =
                    "Requires current family membership. Pages start at zero; maximum size is 100.")
    public ResponseEntity<?> forFamily(
            @PathVariable Long familyId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return responses.toResponseEntityFromResult(
                queries.forFamily(
                        new FamilyId(familyId), new UserId(principal.accountId()), page, size),
                awards ->
                        awards.stream()
                                .map(
                                        AchievementAwardResourceFromEntityAssembler
                                                ::toResourceFromEntity)
                                .toList(),
                HttpStatus.OK);
    }
}
