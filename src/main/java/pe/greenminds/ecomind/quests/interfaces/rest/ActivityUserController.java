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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.quests.application.queryservices.QuestUserQueryService;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUserByIdQuery;
import pe.greenminds.ecomind.quests.application.commandservices.ActivityUserCommandService;
import pe.greenminds.ecomind.quests.application.queryservices.ActivityUserQueryService;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivityUserByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivityUserByQuestUserIdAndActivityIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivityUsersByQuestUserIdQuery;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.ActivityUserResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateActivityUserResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.SubmitActivityUserResource;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ActivityUserResourceFromEntityAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.CreateActivityUserCommandFromResourceAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.SubmitActivityUserCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ErrorResponseAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/activity-users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Activity Users", description = "User activity assignment endpoints")
public class ActivityUserController {
    private final ActivityUserCommandService activityUserCommandService;
    private final ActivityUserQueryService activityUserQueryService;
    private final QuestUserQueryService questUserQueryService;

    public ActivityUserController(
            ActivityUserCommandService activityUserCommandService,
            ActivityUserQueryService activityUserQueryService,
            QuestUserQueryService questUserQueryService
    ) {
        this.activityUserCommandService = activityUserCommandService;
        this.activityUserQueryService = activityUserQueryService;
        this.questUserQueryService = questUserQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Assign an activity to a quest user",
            description = "Creates an activity assignment with zero progress"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Activity assigned successfully",
                    content = @Content(schema = @Schema(implementation = ActivityUserResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Quest user or activity not found"),
            @ApiResponse(responseCode = "409", description = "Activity already assigned")
    })
    public ResponseEntity<?> createActivityUser(
            @Valid @RequestBody CreateActivityUserResource resource,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        var denied = checkOwner(resource.questUserId(), principal.accountId());
        if (denied != null) return denied;
        var command =
                CreateActivityUserCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = activityUserCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ActivityUserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @PostMapping("/{activityUserId}/submit")
    @Operation(
            summary = "Submit progress for an activity",
            description = "For CHECKBOX activities, send {\"data\":{\"checked\":true}}"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Activity submitted",
                    content = @Content(schema = @Schema(implementation = ActivityUserResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid submission data"),
            @ApiResponse(responseCode = "404", description = "Activity assignment not found"),
            @ApiResponse(responseCode = "422", description = "Submission is not allowed")
    })
    public ResponseEntity<?> submitActivity(
            @PathVariable Long activityUserId,
            @Valid @RequestBody SubmitActivityUserResource resource,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        var denied = checkActivityOwner(activityUserId, principal.accountId());
        if (denied != null) return denied;
        var command = SubmitActivityUserCommandFromResourceAssembler.toCommandFromResource(
                activityUserId,
                resource
        );
        var result = activityUserCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ActivityUserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @GetMapping("/{activityUserId}")
    @Operation(summary = "Get activity assignment by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Activity assignment found",
                    content = @Content(schema = @Schema(implementation = ActivityUserResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Activity assignment not found")
    })
    public ResponseEntity<?> getActivityUserById(@PathVariable Long activityUserId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        var denied = checkActivityOwner(activityUserId, principal.accountId());
        if (denied != null) return denied;
        var activityUser =
                activityUserQueryService.handle(new GetActivityUserByIdQuery(activityUserId));

        if (activityUser.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("ActivityUser", activityUserId.toString())
            );
        }

        return ResponseEntity.ok(
                ActivityUserResourceFromEntityAssembler.toResourceFromEntity(activityUser.get())
        );
    }

    @GetMapping("/quest-user/{questUserId}/activity/{activityId}")
    @Operation(summary = "Get a quest user's assignment for an activity")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Activity assignment found",
                    content = @Content(schema = @Schema(implementation = ActivityUserResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Activity assignment not found")
    })
    public ResponseEntity<?> getActivityUserByQuestUserIdAndActivityId(
            @PathVariable Long questUserId,
            @PathVariable Long activityId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        var denied = checkOwner(questUserId, principal.accountId());
        if (denied != null) return denied;
        var activityUser = activityUserQueryService.handle(
                new GetActivityUserByQuestUserIdAndActivityIdQuery(questUserId, activityId)
        );

        if (activityUser.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound(
                            "ActivityUser",
                            "questUserId=%d, activityId=%d".formatted(
                                    questUserId,
                                    activityId
                            )
                    )
            );
        }

        return ResponseEntity.ok(
                ActivityUserResourceFromEntityAssembler.toResourceFromEntity(activityUser.get())
        );
    }

    @GetMapping("/quest-user/{questUserId}")
    @Operation(summary = "Get all activity assignments for a quest user")
    @ApiResponse(responseCode = "200", description = "Activity assignments retrieved successfully")
    public ResponseEntity<?> getActivityUsersByQuestUserId(
            @PathVariable Long questUserId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        var denied = checkOwner(questUserId, principal.accountId());
        if (denied != null) return denied;
        var activityUsers = activityUserQueryService.handle(
                new GetActivityUsersByQuestUserIdQuery(questUserId)
        );
        var resources = activityUsers.stream()
                .map(ActivityUserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }
    private ResponseEntity<?> checkActivityOwner(Long activityUserId, Long userId) {
        var activity = activityUserQueryService.handle(new GetActivityUserByIdQuery(activityUserId));
        if (activity.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("ActivityUser", activityUserId.toString()));
        }
        return checkOwner(activity.get().getQuestUserId(), userId);
    }

    private ResponseEntity<?> checkOwner(Long questUserId, Long userId) {
        var assignment = questUserQueryService.handle(new GetQuestUserByIdQuery(questUserId));
        if (assignment.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("QuestUser", questUserId.toString()));
        }
        if (!assignment.get().getUserId().equals(userId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("QUEST_ASSIGNMENT_FORBIDDEN",
                            "Only the assigned user can access these activities"));
        }
        return null;
    }

}
