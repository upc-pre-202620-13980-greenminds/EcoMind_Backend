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
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.quests.application.commandservices.QuestUserCommandService;
import pe.greenminds.ecomind.quests.application.queryservices.QuestUserQueryService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser;
import pe.greenminds.ecomind.quests.domain.model.commands.CompleteQuestUserCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteQuestUserCommand;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUserByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUserByUserIdAndQuestIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUsersByUserIdAndStatusQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUserVersionStatusQuery;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateQuestUserResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.QuestUserResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.QuestUserVersionStatusResource;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.CreateQuestUserCommandFromResourceAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.QuestUserResourceFromEntityAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.QuestUserVersionStatusResourceAssembler;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ErrorResponseAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/quest-users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Quest Users", description = "User quest assignment endpoints")
public class QuestUserController {
    private final QuestUserCommandService questUserCommandService;
    private final QuestUserQueryService questUserQueryService;

    public QuestUserController(
            QuestUserCommandService questUserCommandService,
            QuestUserQueryService questUserQueryService
    ) {
        this.questUserCommandService = questUserCommandService;
        this.questUserQueryService = questUserQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Assign a quest to a user",
            description = """
                    Creates a quest assignment with IN_PROGRESS status and zero progress.
                    It also creates an ActivityUser with zero progress for each activity
                    belonging to the assigned quest.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Quest and its activity assignments created successfully",
                    content = @Content(schema = @Schema(implementation = QuestUserResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Quest not found"),
            @ApiResponse(responseCode = "409", description = "Quest already assigned to user")
    })
    public ResponseEntity<?> createQuestUser(
            @Valid @RequestBody CreateQuestUserResource resource
    ) {
        var command = CreateQuestUserCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = questUserCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                QuestUserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{questUserId}")
    @Operation(summary = "Get quest assignment by ID")
    public ResponseEntity<?> getQuestUserById(@PathVariable Long questUserId) {
        var questUser = questUserQueryService.handle(new GetQuestUserByIdQuery(questUserId));

        if (questUser.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("QuestUser", questUserId.toString())
            );
        }

        return ResponseEntity.ok(
                QuestUserResourceFromEntityAssembler.toResourceFromEntity(questUser.get())
        );
    }

    @GetMapping("/{questUserId}/version-status")
    @Operation(
            summary = "Check if a quest assignment is up to date"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Version status",
                    content = @Content(
                            schema = @Schema(
                                    implementation = QuestUserVersionStatusResource.class
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Quest assignment not found")
    })
    public ResponseEntity<?> getQuestUserVersionStatus(
            @PathVariable Long questUserId
    ) {
        var versionStatus = questUserQueryService.handle(
                new GetQuestUserVersionStatusQuery(questUserId)
        );

        if (versionStatus.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("QuestUser", questUserId.toString())
            );
        }

        return ResponseEntity.ok(
                QuestUserVersionStatusResourceAssembler.toResource(versionStatus.get())
        );
    }

    @PostMapping("/{questUserId}/complete")
    @Operation(
            summary = "Complete a quest assignment"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Quest completed",
                    content = @Content(schema = @Schema(implementation = QuestUserResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Quest assignment not found"),
            @ApiResponse(responseCode = "422", description = "Quest is not ready to complete")
    })
    public ResponseEntity<?> completeQuestUser(@PathVariable Long questUserId) {
        var result = questUserCommandService.handle(
                new CompleteQuestUserCommand(questUserId)
        );

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                QuestUserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @GetMapping("/user/{userId}/quest/{questId}")
    @Operation(summary = "Get a user's assignment for a quest")
    public ResponseEntity<?> getQuestUserByUserIdAndQuestId(
            @PathVariable Long userId,
            @PathVariable Long questId
    ) {
        var questUser = questUserQueryService.handle(
                new GetQuestUserByUserIdAndQuestIdQuery(userId, questId)
        );

        if (questUser.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound(
                            "QuestUser",
                            "userId=%d, questId=%d".formatted(userId, questId)
                    )
            );
        }

        return ResponseEntity.ok(
                QuestUserResourceFromEntityAssembler.toResourceFromEntity(questUser.get())
        );
    }

    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Get a user's quest assignments by status")
    public ResponseEntity<List<QuestUserResource>> getQuestUsersByUserIdAndStatus(
            @PathVariable Long userId,
            @PathVariable QuestStatus status
    ) {
        var questUsers = questUserQueryService.handle(
                new GetQuestUsersByUserIdAndStatusQuery(userId, status)
        );
        var resources = questUsers.stream()
                .map(QuestUserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    @DeleteMapping("/{questUserId}")
    @Operation(
            summary = "Delete a user's quest assignment",
            description = """
                    Deletes the quest assignment and all ActivityUser records associated
                    with it in a single transaction.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Quest assignment and activity assignments deleted successfully"
            ),
            @ApiResponse(responseCode = "404", description = "Quest assignment not found")
    })
    public ResponseEntity<?> deleteQuestUser(@PathVariable Long questUserId) {
        var result = questUserCommandService.handle(new DeleteQuestUserCommand(questUserId));

        return switch (result) {
            case Result.Success<QuestUser, ApplicationError> ignored ->
                    ResponseEntity.noContent().build();
            case Result.Failure<QuestUser, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
