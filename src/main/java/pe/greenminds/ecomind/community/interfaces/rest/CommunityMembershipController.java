package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@RestController
@Tag(name = "Community Membership", description = "Community membership and member listings")
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
    public ResponseEntity<?> join(@PathVariable Long communityId, @RequestParam Long user_id) {
        return responseEntityAssembler.toResponseEntityFromResult(
                commandService.handle(new JoinCommunityCommand(communityId, user_id)),
                CommunityMembershipResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping("/Memberships")
    public List<CommunityMembershipResource> membershipsByUser(@RequestParam Long user_id) {
        return queryService.handle(new GetCommunityMembershipsByUserQuery(user_id)).stream()
                .map(CommunityMembershipResourceFromEntityAssembler::toResourceFromEntity).toList();
    }

    @GetMapping("/Communities/{communityId}/Memberships")
    public List<CommunityMembershipResource> membershipsByCommunity(@PathVariable Long communityId) {
        return queryService.handle(new GetCommunityMembershipsByCommunityQuery(communityId)).stream()
                .map(CommunityMembershipResourceFromEntityAssembler::toResourceFromEntity).toList();
    }
}
