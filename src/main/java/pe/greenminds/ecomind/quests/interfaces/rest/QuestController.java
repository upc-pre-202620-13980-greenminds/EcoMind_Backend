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
import pe.greenminds.ecomind.quests.application.commandservices.QuestCommandService;
import pe.greenminds.ecomind.quests.application.queryservices.QuestQueryService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.queries.GetAllQuestsQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.SearchQuestQuery;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateQuestResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.QuestResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.UpdateQuestResource;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.CreateQuestCommandFromResourceAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.QuestResourceFromEntityAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.UpdateQuestCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ErrorResponseAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value="/api/v1/quests", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Quests", description = "Quest management endpoints")
public class QuestController {
    private final QuestCommandService questCommandService;
    private final QuestQueryService questQueryService;

    public QuestController(QuestCommandService questCommandService, QuestQueryService questQueryService) {
        this.questCommandService = questCommandService;
        this.questQueryService = questQueryService;
    }

    @PostMapping
    @Operation(
       summary = "Create a new quest",
       description = "Creates a new quest with all neccesary information."
    )
    @ApiResponses(value={
            @ApiResponse(
                    responseCode = "201",
                    description = "Quest created succesfully",
                    content = @Content(schema = @Schema(implementation = QuestResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Conflict: quest already exists")
    })
    public ResponseEntity<?> createQuest(@Valid @RequestBody CreateQuestResource resource){
        var createQuestCommand = CreateQuestCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = questCommandService.handle(createQuestCommand);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                QuestResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(
            summary = "Get all quests",
            description = "Retrieves all available quests."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Quests retrieved succesfully."
    )
    public ResponseEntity<List<QuestResource>> getAllQuests(){
        var quests = questQueryService.handle(new GetAllQuestsQuery());

        var resources = quests.stream()
                .map(QuestResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{questId}")
    @Operation(
            summary = "Get quest by ID",
            description = "Retrieves a quest using its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Quest found",
                    content = @Content(
                            schema = @Schema(implementation = QuestResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Quest not found"
            )
    })
    public ResponseEntity<?> getQuestById(
            @PathVariable Long questId
    ) {
        var quest = questQueryService.handle(
                new GetQuestByIdQuery(questId)
        );
        if (quest.isEmpty()) {
            var error = ApplicationError.notFound(
                    "Quest",
                    questId.toString()
            );
            return ErrorResponseAssembler
                    .toErrorResponseFromApplicationError(error);
        }
        var resource = QuestResourceFromEntityAssembler
                .toResourceFromEntity(quest.get());
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{questId}")
    @Operation(
            summary = "Update a quest",
            description = "Completely updates a quest while preserving its identity and progress"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Quest updated successfully",
                    content = @Content(schema = @Schema(implementation = QuestResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Quest not found")
    })
    public ResponseEntity<?> updateQuest(
            @PathVariable Long questId,
            @Valid @RequestBody UpdateQuestResource resource
    ) {
        var command =
                UpdateQuestCommandFromResourceAssembler.toCommandFromResource(questId, resource);
        var result = questCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                QuestResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search quests",
            description = "Searches quests using optional filters."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully"
    )
    public ResponseEntity<List<QuestResource>> searchQuests(
            @RequestParam(defaultValue = "") String title,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) QuestType questType,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) Theme type
    ) {
        var query = new SearchQuestQuery(
                title,
                category,
                questType,
                age,
                type
        );
        var quests = questQueryService.handle(query);

        var resources = quests.stream()
                .map(QuestResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @DeleteMapping("/{questId}")
    @Operation(
            summary = "Delete a quest",
            description = """
                    Deletes the quest, its activities, all user quest progress,
                    and all user activity progress in a single transaction.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Quest and all related data deleted successfully"
            ),
            @ApiResponse(responseCode = "404", description = "Quest not found")
    })
    public ResponseEntity<?> deleteQuest(@PathVariable Long questId) {
        var result = questCommandService.handle(new DeleteQuestCommand(questId));

        return switch (result) {
            case Result.Success<Quest, ApplicationError> ignored ->
                    ResponseEntity.noContent().build();
            case Result.Failure<Quest, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
