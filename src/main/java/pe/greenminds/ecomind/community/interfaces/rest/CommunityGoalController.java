package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.community.application.commandservices.CommunityGoalCommandService;
import pe.greenminds.ecomind.community.application.queryservices.CommunityGoalPanelQueryService;
import pe.greenminds.ecomind.community.application.queryservices.CommunityGoalQueryService;
import pe.greenminds.ecomind.community.domain.model.commands.JoinCommunityGoalCommand;
import pe.greenminds.ecomind.community.domain.model.queries.SearchCommunityGoalsQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.*;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CommunityGoalResourceFromEntityAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CreateCommunityGoalCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@Tag(name = "Community Goal")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
@RequestMapping(
        value = "/api/v1/Community/Community-goals",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class CommunityGoalController {

    private final CommunityGoalCommandService commands;
    private final CommunityGoalQueryService queries;
    private final CommunityGoalPanelQueryService panels;
    private final ResponseEntityAssembler responses;

    public CommunityGoalController(
            CommunityGoalCommandService commands,
            CommunityGoalQueryService queries,
            CommunityGoalPanelQueryService panels,
            ResponseEntityAssembler responses) {
        this.commands = commands;
        this.queries = queries;
        this.panels = panels;
        this.responses = responses;
    }

    @GetMapping
    public List<CommunityGoalResource> list(
            @RequestParam(required = false) Long community_id) {
        return queries.handle(new SearchCommunityGoalsQuery(community_id))
                .stream()
                .map(CommunityGoalResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }

    @GetMapping("/Panel")
    public ResponseEntity<?> panel(
            @RequestParam Long community_id,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responses.toResponseEntityFromResult(
                panels.get(community_id, principal.accountId()),
                panel -> new CommunityGoalPanelResource(
                        panel.goal() == null
                                ? null
                                : CommunityGoalResourceFromEntityAssembler
                                  .toResourceFromEntity(panel.goal()),
                        panel.canCreate(),
                        panel.joined(),
                        panel.otherGoalActive(),
                        panel.memberCount(),
                        panel.targetPreview()),
                HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(
            @Valid @RequestBody CreateCommunityGoalResource resource,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responses.toResponseEntityFromResult(
                commands.handle(
                        CreateCommunityGoalCommandFromResourceAssembler
                                .toCommandFromResource(resource, principal.accountId())),
                CommunityGoalResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED);
    }

    @PostMapping("/{id}/Participants")
    public ResponseEntity<?> join(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responses.toResponseEntityFromResult(
                commands.handle(new JoinCommunityGoalCommand(id, principal.accountId())),
                CommunityGoalResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }
}