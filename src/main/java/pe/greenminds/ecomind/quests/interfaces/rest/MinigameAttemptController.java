package pe.greenminds.ecomind.quests.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import pe.greenminds.ecomind.quests.application.commandservices.MinigameAttemptCommandService;
import pe.greenminds.ecomind.quests.application.queryservices.MinigameAttemptQueryService;
import pe.greenminds.ecomind.quests.domain.model.commands.CancelMinigameAttemptCommand;
import pe.greenminds.ecomind.quests.domain.model.queries.GetMinigameAttemptsByUserAndMinigameQuery;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateMinigameAttemptResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.FinishMinigameAttemptResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.MinigameAttemptResource;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.CreateMinigameAttemptCommandFromResourceAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.FinishMinigameAttemptCommandFromResourceAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.MinigameAttemptResourceFromEntityAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ResponseEntityAssembler;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

import java.util.List;
import pe.greenminds.ecomind.quests.domain.model.queries.GetMinigameAttemptByIdQuery;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ErrorResponseAssembler;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;

@RestController
@RequestMapping(value = "/api/v1/minigame-attempts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Minigame Attempts", description = "Minigame attempt endpoints")
public class MinigameAttemptController {
    private final MinigameAttemptCommandService minigameAttemptCommandService;
    private final MinigameAttemptQueryService minigameAttemptQueryService;

    public MinigameAttemptController(
            MinigameAttemptCommandService minigameAttemptCommandService,
            MinigameAttemptQueryService minigameAttemptQueryService
    ) {
        this.minigameAttemptCommandService = minigameAttemptCommandService;
        this.minigameAttemptQueryService = minigameAttemptQueryService;
    }

    @GetMapping
    @Operation(summary = "Get minigame attempts by user and minigame")
    public ResponseEntity<List<MinigameAttemptResource>> getAttemptsByUserAndMinigame(
            @RequestParam Long minigameId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        var attempts = minigameAttemptQueryService.handle(
                new GetMinigameAttemptsByUserAndMinigameQuery(principal.accountId(), minigameId)
        );
        var resources = attempts.stream()
                .map(MinigameAttemptResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    @Operation(summary = "Create a minigame attempt")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Minigame attempt created successfully",
                    content = @Content(schema = @Schema(implementation = MinigameAttemptResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "User, quest, or minigame not found"),
            @ApiResponse(responseCode = "409", description = "User already has a started attempt"),
            @ApiResponse(responseCode = "422", description = "Quest is not a valid minigame quest")
    })
    public ResponseEntity<?> createAttempt(
            @Valid @RequestBody CreateMinigameAttemptResource resource,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        var command =
                CreateMinigameAttemptCommandFromResourceAssembler.toCommandFromResource(
                        resource, principal.accountId());
        var result = minigameAttemptCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MinigameAttemptResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @PostMapping("/{attemptId}/finish")
    @Operation(summary = "Finish a minigame attempt")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Minigame attempt finished successfully",
                    content = @Content(schema = @Schema(implementation = MinigameAttemptResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Attempt, quest, or minigame not found"),
            @ApiResponse(responseCode = "422", description = "Attempt cannot be finished")
    })
    public ResponseEntity<?> finishAttempt(
            @PathVariable Long attemptId,
            @Valid @RequestBody FinishMinigameAttemptResource resource,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        var denied = checkAttemptOwner(attemptId, principal.accountId());
        if (denied != null) return denied;
        var command =
                FinishMinigameAttemptCommandFromResourceAssembler.toCommandFromResource(
                        attemptId,
                        resource
                );
        var result = minigameAttemptCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MinigameAttemptResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PostMapping("/{attemptId}/cancel")
    @Operation(summary = "Cancel a minigame attempt")
    public ResponseEntity<?> cancelAttempt(@PathVariable Long attemptId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        var denied = checkAttemptOwner(attemptId, principal.accountId());
        if (denied != null) return denied;
        var result = minigameAttemptCommandService.handle(
                new CancelMinigameAttemptCommand(attemptId)
        );

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MinigameAttemptResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }
    private ResponseEntity<?> checkAttemptOwner(Long attemptId, Long userId) {
        var attempt = minigameAttemptQueryService.handle(new GetMinigameAttemptByIdQuery(attemptId));
        if (attempt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("MinigameAttempt", attemptId.toString()));
        }
        if (!attempt.get().getUserId().equals(userId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("MINIGAME_ATTEMPT_FORBIDDEN", "Only the participant can change the minigame attempt"));
        }
        return null;
    }
}
