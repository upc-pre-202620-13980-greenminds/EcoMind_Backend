package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.community.application.commandservices.EventRegistrationCommandService;
import pe.greenminds.ecomind.community.application.queryservices.EventRegistrationQueryService;
import pe.greenminds.ecomind.community.domain.model.commands.CancelEventRegistrationCommand;
import pe.greenminds.ecomind.community.domain.model.queries.GetEventRegistrationsQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.*;
import pe.greenminds.ecomind.community.interfaces.rest.transform.EventRegistrationResourceFromEntityAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.RegisterForEventCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@Tag(name = "Event Registration", description = "Individual and family event participation")
@RequestMapping(value = "/api/v1/Community/Events/{eventId}/Registrations", produces = MediaType.APPLICATION_JSON_VALUE)
public class EventRegistrationController {
    private final EventRegistrationCommandService commands;
    private final EventRegistrationQueryService queries;
    private final ResponseEntityAssembler responses;

    public EventRegistrationController(EventRegistrationCommandService c, EventRegistrationQueryService q,
            ResponseEntityAssembler r) {
        commands = c;
        queries = q;
        responses = r;
    }

    @GetMapping
    public List<EventRegistrationResource> list(@PathVariable Long eventId) {
        return queries.handle(new GetEventRegistrationsQuery(eventId)).stream()
                .map(EventRegistrationResourceFromEntityAssembler::toResourceFromEntity).toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@PathVariable Long eventId,
            @Valid @RequestBody CreateEventRegistrationResource r) {
        var c = RegisterForEventCommandFromResourceAssembler.toCommandFromResource(r, eventId);
        return responses.toResponseEntityFromResult(commands.handle(c),
                EventRegistrationResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @PatchMapping("/{registrationId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long eventId, @PathVariable Long registrationId,
            @RequestParam Long user_id) {
        return responses.toResponseEntityFromResult(
                commands.handle(new CancelEventRegistrationCommand(eventId, registrationId, user_id)), v -> null,
                HttpStatus.NO_CONTENT);
    }
}
