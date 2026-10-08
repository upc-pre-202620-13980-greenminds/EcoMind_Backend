package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.community.application.commandservices.CommunityCommandService;
import pe.greenminds.ecomind.community.application.queryservices.CommunityQueryService;
import pe.greenminds.ecomind.community.domain.model.commands.JoinCommunityCommand;
import pe.greenminds.ecomind.community.domain.model.queries.GetCommunityMembershipsByCommunityQuery;
import pe.greenminds.ecomind.community.domain.model.queries.GetCommunityMembershipsByUserQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CommunityMembershipResource;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CommunityMembershipResourceFromEntityAssembler;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

@RestController
@Tag(name = "Community Membership", description = "Community membership and member listings")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
@RequestMapping(value = "/api/v1/Community", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommunityMembershipController {
    private final CommunityCommandService commandService;
    private final CommunityQueryService queryService;
    private final ResponseEntityAssembler responseEntityAssembler;

    public CommunityMembershipController(CommunityCommandService commandService, CommunityQueryService queryService,
            ResponseEntityAssembler responseEntityAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.responseEntityAssembler = responseEntityAssembler;
    }

    @PostMapping("/Communities/{communityId}/Memberships")
    public ResponseEntity<?> join(@PathVariable Long communityId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responseEntityAssembler.toResponseEntityFromResult(
                commandService.handle(new JoinCommunityCommand(communityId, principal.accountId())),
                CommunityMembershipResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping("/Memberships")
    public List<CommunityMembershipResource> membershipsByUser(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return queryService.handle(new GetCommunityMembershipsByUserQuery(principal.accountId())).stream()
                .map(CommunityMembershipResourceFromEntityAssembler::toResourceFromEntity).toList();
    }

    @GetMapping("/Communities/{communityId}/Memberships")
    public List<CommunityMembershipResource> membershipsByCommunity(@PathVariable Long communityId) {
        return queryService.handle(new GetCommunityMembershipsByCommunityQuery(communityId)).stream()
                .map(CommunityMembershipResourceFromEntityAssembler::toResourceFromEntity).toList();
    }
}
