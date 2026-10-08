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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.quests.application.commandservices.CollabQuestSessionCommandService;
import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestSessionQueryService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import pe.greenminds.ecomind.quests.domain.model.commands.DeletePendingCollabQuestSessionCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.StartCollabQuestSessionCommand;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestSessionStateQuery;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CollabQuestSessionResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CollabQuestSessionStateResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateCollabQuestSessionResource;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.CollabQuestSessionResourceFromEntityAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.CollabQuestSessionStateResourceAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.CreateCollabQuestSessionCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ErrorResponseAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@RequestMapping(value = "/api/v1/collaborative-quests", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Collaborative Quests", description = "Collaborative quest endpoints")
public class CollaborativeQuestController {
    private final CollabQuestSessionCommandService collabQuestSessionCommandService;
    private final CollabQuestSessionQueryService collabQuestSessionQueryService;

    public CollaborativeQuestController(
            CollabQuestSessionCommandService collabQuestSessionCommandService,
            CollabQuestSessionQueryService collabQuestSessionQueryService
    ) {
        this.collabQuestSessionCommandService = collabQuestSessionCommandService;
        this.collabQuestSessionQueryService = collabQuestSessionQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Create a collaborative quest session",
            description = """
                    Creates a pending collaborative session for a collaborative quest.
                    The session starts with null startedAt and completedAt dates.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Collaborative quest session created successfully",
                    content = @Content(schema = @Schema(implementation = CollabQuestSessionResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Quest not found"),
            @ApiResponse(responseCode = "409", description = "Session already exists"),
            @ApiResponse(responseCode = "422", description = "Quest is not collaborative")
    })
    public ResponseEntity<?> createSession(
            @Valid @RequestBody CreateCollabQuestSessionResource resource
    ) {
        var command =
                CreateCollabQuestSessionCommandFromResourceAssembler.toCommandFromResource(
                        resource
                );
        var result = collabQuestSessionCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CollabQuestSessionResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @PostMapping("/{sessionId}/start")
    @Operation(summary = "Start a collaborative quest session")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Collaborative quest session started successfully",
                    content = @Content(schema = @Schema(implementation = CollabQuestSessionResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "409", description = "Quest already assigned"),
            @ApiResponse(responseCode = "422", description = "Session cannot be started")
    })
    public ResponseEntity<?> startSession(
            @PathVariable Long sessionId,
            @RequestParam Long ownerUserId
    ) {
        var result = collabQuestSessionCommandService.handle(
                new StartCollabQuestSessionCommand(sessionId, ownerUserId)
        );

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CollabQuestSessionResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete a pending collaborative quest session")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Collaborative quest session deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "422", description = "Session cannot be deleted")
    })
    public ResponseEntity<?> deletePendingSession(
            @PathVariable Long sessionId,
            @RequestParam Long ownerUserId
    ) {
        var result = collabQuestSessionCommandService.handle(
                new DeletePendingCollabQuestSessionCommand(sessionId, ownerUserId)
        );

        return switch (result) {
            case Result.Success<CollabQuestSession, ApplicationError> ignored ->
                    ResponseEntity.noContent().build();
            case Result.Failure<CollabQuestSession, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }

    @GetMapping("/state")
    @Operation(summary = "Get collaborative quest state")
    @ApiResponse(
            responseCode = "200",
            description = "Collaborative quest state retrieved successfully",
            content = @Content(
                    schema = @Schema(implementation = CollabQuestSessionStateResource.class)
            )
    )
    public ResponseEntity<?> getState(
            @RequestParam Long questId,
            @RequestParam Long userId
    ) {
        var state = collabQuestSessionQueryService.handle(
                new GetCollabQuestSessionStateQuery(questId, userId)
        );

        return ResponseEntity.ok(
                CollabQuestSessionStateResourceAssembler.toResource(state)
        );
    }
}
