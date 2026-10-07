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
import pe.greenminds.ecomind.quests.application.commandservices.ActivityCommandService;
import pe.greenminds.ecomind.quests.application.queryservices.ActivityQueryService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Activity;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteActivityCommand;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivitiesByQuestIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivityByIdQuery;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.ActivityResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateActivityResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.UpdateActivityResource;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ActivityResourceFromEntityAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.CreateActivityCommandFromResourceAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.UpdateActivityCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ErrorResponseAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/activities", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Activities", description = "Activity management endpoints")
public class ActivityController {
    private final ActivityCommandService activityCommandService;
    private final ActivityQueryService activityQueryService;

    public ActivityController(ActivityCommandService activityCommandService, ActivityQueryService activityQueryService) {
        this.activityCommandService = activityCommandService;
        this.activityQueryService = activityQueryService;
    }

    @PostMapping
    @Operation(
            summary="Create a new activity",
            description = "Creates an activity for a quest"
    )
    @ApiResponses(value={
            @ApiResponse(
                    responseCode = "201",
                    description = "Activity created",
                    content = @Content(schema = @Schema(implementation = ActivityResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Conflict: activity already exists")
    })
    public ResponseEntity<?> createActivity(@Valid @RequestBody CreateActivityResource resource) {
        var createActivityCommand = CreateActivityCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = activityCommandService.handle(createActivityCommand);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ActivityResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{activityId}")
    @Operation(
            summary = "Get activity by Id",
            description = "Retrieve an activity using it's uniques identifier,"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Activity found",
                    content = @Content(
                            schema = @Schema(implementation = ActivityResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Activity not found"
            )
    })
    public ResponseEntity<?> getActivityById(@PathVariable Long activityId) {
        var activity  = activityQueryService.handle(
                new GetActivityByIdQuery(activityId)
        );
        if(activity.isEmpty()){
            var error = ApplicationError.notFound(
                    "Activity",
                    activityId.toString()
            );
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
        }
        var resource = ActivityResourceFromEntityAssembler.toResourceFromEntity(activity.get());
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{activityId}")
    @Operation(
            summary = "Update an activity",
            description = """
                    Completely updates an activity. If its position changes, the other
                    activities in the quest are reordered automatically. The activity
                    type cannot be changed; it must be deleted and recreated instead.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Activity updated successfully",
                    content = @Content(schema = @Schema(implementation = ActivityResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Activity not found"),
            @ApiResponse(responseCode = "422", description = "Activity type cannot be changed")
    })
    public ResponseEntity<?> updateActivity(
            @PathVariable Long activityId,
            @Valid @RequestBody UpdateActivityResource resource
    ) {
        var command = UpdateActivityCommandFromResourceAssembler
                .toCommandFromResource(activityId, resource);
        var result = activityCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ActivityResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @GetMapping("/quest/{questId}")
    @Operation(
            summary = "Get all activities from one quest",
            description = "Retrieves all available activities in a quest."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Activities retrieved succesfully."
    )
    public ResponseEntity<List<ActivityResource>> getActivitiesByQuestId(@PathVariable Long questId) {
        var activities  = activityQueryService.handle(new GetActivitiesByQuestIdQuery(questId));

        var resources = activities.stream()
                .map(ActivityResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @DeleteMapping("/{activityId}")
    @Operation(
            summary = "Delete an activity",
            description = """
                    Deletes an activity and all ActivityUser progress records associated
                    with it in a single transaction. Remaining activities are reordered
                    to keep their positions consecutive.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Activity and related user progress deleted successfully"
            ),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<?> deleteActivity(@PathVariable Long activityId) {
        var result = activityCommandService.handle(new DeleteActivityCommand(activityId));

        return switch (result) {
            case Result.Success<Activity, ApplicationError> ignored ->
                    ResponseEntity.noContent().build();
            case Result.Failure<Activity, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
