package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.community.application.commandservices.EventCommandService;
import pe.greenminds.ecomind.community.application.queryservices.EventQueryService;
import pe.greenminds.ecomind.community.domain.model.commands.DeleteEventCommand;
import pe.greenminds.ecomind.community.domain.model.queries.SearchEventsQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CreateEventResource;
import pe.greenminds.ecomind.community.interfaces.rest.resources.EventResource;
import pe.greenminds.ecomind.community.interfaces.rest.transform.EventResourceFromEntityAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CreateEventCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

@RestController
@Tag(name = "Event", description = "Community event management")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
@RequestMapping(value = "/api/v1/Community/Events", produces = MediaType.APPLICATION_JSON_VALUE)
public class EventController {
    private final EventCommandService commands;
    private final EventQueryService queries;
    private final ResponseEntityAssembler responses;

    public EventController(EventCommandService c, EventQueryService q, ResponseEntityAssembler r) {
        commands = c;
        queries = q;
        responses = r;
    }

    @GetMapping
    public List<EventResource> list(@RequestParam(required = false) Long community_id) {
        return queries.handle(new SearchEventsQuery(community_id)).stream()
                .map(EventResourceFromEntityAssembler::toResourceFromEntity).toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody CreateEventResource r,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        var c = CreateEventCommandFromResourceAssembler.toCommandFromResource(r, principal.accountId());
        return responses.toResponseEntityFromResult(commands.handle(c),
                EventResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responses.toResponseEntityFromResult(commands.handle(new DeleteEventCommand(id, principal.accountId())), v -> null,
                HttpStatus.NO_CONTENT);
    }
}
