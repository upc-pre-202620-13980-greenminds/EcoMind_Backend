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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.quests.application.commandservices.MinigameCommandService;
import pe.greenminds.ecomind.quests.application.queryservices.MinigameQueryService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Minigame;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteMinigameCommand;
import pe.greenminds.ecomind.quests.domain.model.queries.GetAllMinigamesQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetMinigameByIdQuery;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateMinigameResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.MinigameResource;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.CreateMinigameCommandFromResourceAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.MinigameResourceFromEntityAssembler;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ErrorResponseAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/minigames", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Minigames", description = "Minigame catalog endpoints")
public class MinigameController {
    private final MinigameCommandService minigameCommandService;
    private final MinigameQueryService minigameQueryService;

    public MinigameController(
            MinigameCommandService minigameCommandService,
            MinigameQueryService minigameQueryService
    ) {
        this.minigameCommandService = minigameCommandService;
        this.minigameQueryService = minigameQueryService;
    }

    @PostMapping
    @Operation(summary = "Create a minigame")
    public ResponseEntity<?> createMinigame(@Valid @RequestBody CreateMinigameResource resource) {
        var command = CreateMinigameCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = minigameCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MinigameResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(summary = "Get all minigames")
    public ResponseEntity<List<MinigameResource>> getAllMinigames() {
        var minigames = minigameQueryService.handle(new GetAllMinigamesQuery());
        var resources = minigames.stream()
                .map(MinigameResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{minigameId}")
    @Operation(summary = "Get a minigame by id")
    public ResponseEntity<?> getMinigameById(@PathVariable Long minigameId) {
        var minigame = minigameQueryService.handle(new GetMinigameByIdQuery(minigameId));
        if (minigame.isEmpty()) {
            var error = ApplicationError.notFound("Minigame", minigameId.toString());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
        }
        return ResponseEntity.ok(
                MinigameResourceFromEntityAssembler.toResourceFromEntity(minigame.get())
        );
    }

    @DeleteMapping("/{minigameId}")
    @Operation(summary = "Delete a minigame")
    public ResponseEntity<?> deleteMinigame(@PathVariable Long minigameId) {
        var result = minigameCommandService.handle(new DeleteMinigameCommand(minigameId));

        return switch (result) {
            case Result.Success<Minigame, ApplicationError> ignored ->
                    ResponseEntity.noContent().build();
            case Result.Failure<Minigame, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
