package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.community.application.commandservices.EventChatCommandService;
import pe.greenminds.ecomind.community.application.queryservices.EventChatQueryService;
import pe.greenminds.ecomind.community.domain.model.queries.GetEventChatMessagesQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.SendEventChatMessageResource;
import pe.greenminds.ecomind.community.interfaces.rest.transform.EventChatMessageResourceFromEntityAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.EventChatPageResourceFromEntityAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.SendEventChatMessageCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@Tag(name = "Event Chat", description = "Temporary event participant chat")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
@RequestMapping(
        value = "/api/v1/Community/Events/{eventId}/Chat/Messages",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class EventChatController {
    private final EventChatCommandService commands;
    private final EventChatQueryService queries;
    private final ResponseEntityAssembler responses;

    public EventChatController(
            EventChatCommandService commands,
            EventChatQueryService queries,
            ResponseEntityAssembler responses) {
        this.commands = commands;
        this.queries = queries;
        this.responses = responses;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @PathVariable Long eventId,
            @RequestParam(name = "after_id", defaultValue = "0") Long afterId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        var query = new GetEventChatMessagesQuery(
                eventId, principal.accountId(), afterId);

        return responses.toResponseEntityFromResult(
                queries.handle(query),
                page -> EventChatPageResourceFromEntityAssembler
                        .toResourceFromEntity(page, principal.accountId()),
                HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> send(
            @PathVariable Long eventId,
            @Valid @RequestBody SendEventChatMessageResource resource,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        var command = SendEventChatMessageCommandFromResourceAssembler
                .toCommandFromResource(resource, eventId, principal.accountId());

        return responses.toResponseEntityFromResult(
                commands.handle(command),
                message -> EventChatMessageResourceFromEntityAssembler
                        .toResourceFromEntity(message, principal.accountId()),
                HttpStatus.CREATED);
    }
}