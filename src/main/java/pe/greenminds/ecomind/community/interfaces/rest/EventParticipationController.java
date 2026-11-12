package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.community.application.queryservices.EventParticipationQueryService;
import pe.greenminds.ecomind.community.domain.model.queries.SearchEventParticipationQuery;
import pe.greenminds.ecomind.community.interfaces.rest.transform.EventParticipationResourceFromEntityAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@Tag(name = "Event Participation", description = "Event participation for the authenticated user")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
@RequestMapping(
        value = "/api/v1/Community/Events",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class EventParticipationController {
    private final EventParticipationQueryService queries;
    private final ResponseEntityAssembler responses;

    public EventParticipationController(
            EventParticipationQueryService queries,
            ResponseEntityAssembler responses) {
        this.queries = queries;
        this.responses = responses;
    }

    @GetMapping("/Participation")
    public ResponseEntity<?> list(
            @RequestParam(name = "community_id") Long communityId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        var query = new SearchEventParticipationQuery(
                communityId, principal.accountId(), false);

        return responses.toResponseEntityFromResult(
                queries.handle(query),
                result -> result.stream()
                        .map(EventParticipationResourceFromEntityAssembler::toResourceFromEntity)
                        .toList(),
                HttpStatus.OK);
    }

    @GetMapping("/Mine")
    public ResponseEntity<?> mine(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        var query = new SearchEventParticipationQuery(
                null, principal.accountId(), true);

        return responses.toResponseEntityFromResult(
                queries.handle(query),
                result -> result.stream()
                        .map(EventParticipationResourceFromEntityAssembler::toResourceFromEntity)
                        .toList(),
                HttpStatus.OK);
    }
}