package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.community.application.commandservices.EventRegistrationCommandService;
import pe.greenminds.ecomind.community.application.queryservices.EventRegistrationQueryService;
import pe.greenminds.ecomind.community.domain.model.commands.CancelEventRegistrationCommand;
import pe.greenminds.ecomind.community.domain.model.queries.GetEventRegistrationsQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.*;
import pe.greenminds.ecomind.community.interfaces.rest.transform.EventRegistrationResourceFromEntityAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.RegisterForEventCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

@RestController
@Tag(name = "Event Registration", description = "Individual and family event participation")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
@RequestMapping(value = "/api/v1/Community/Events/{eventId}/Registrations", produces = MediaType.APPLICATION_JSON_VALUE)
public class EventRegistrationController {
    private final EventRegistrationCommandService commands;
    private final EventRegistrationQueryService queries;
    private final ResponseEntityAssembler responses;

    public EventRegistrationController(EventRegistrationCommandService eventRegistrationCommandService,
            EventRegistrationQueryService eventRegistrationQueryService,
            ResponseEntityAssembler responseEntityAssembler) {
        commands = eventRegistrationCommandService;
        queries = eventRegistrationQueryService;
        responses = responseEntityAssembler;
    }

    @GetMapping
    public List<EventRegistrationResource> list(@PathVariable Long eventId) {
        return queries.handle(new GetEventRegistrationsQuery(eventId)).stream()
                .map(EventRegistrationResourceFromEntityAssembler::toResourceFromEntity).toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@PathVariable Long eventId,
            @Valid @RequestBody CreateEventRegistrationResource resource,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        var command = RegisterForEventCommandFromResourceAssembler.toCommandFromResource(
                resource, eventId, principal.accountId());
        return responses.toResponseEntityFromResult(commands.handle(command),
                EventRegistrationResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @PatchMapping("/{registrationId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long eventId, @PathVariable Long registrationId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responses.toResponseEntityFromResult(
                commands.handle(new CancelEventRegistrationCommand(eventId, registrationId, principal.accountId())), result -> null,
                HttpStatus.NO_CONTENT);
    }
}
