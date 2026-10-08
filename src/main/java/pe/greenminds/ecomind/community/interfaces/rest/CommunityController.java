package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.community.application.commandservices.CommunityCommandService;
import pe.greenminds.ecomind.community.application.queryservices.CommunityQueryService;
import pe.greenminds.ecomind.community.domain.model.queries.SearchCommunitiesQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CommunityResource;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CreateLocalCommunityResource;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CreateTopicCommunityResource;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CommunityResourceFromEntityAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CreateLocalCommunityCommandFromResourceAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CreateTopicCommunityCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@Tag(name = "Community", description = "Community discovery, creation and membership")
@RequestMapping(value = "/api/v1/Community/Communities", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommunityController {
    private final CommunityCommandService commandService;
    private final CommunityQueryService queryService;
    private final ResponseEntityAssembler responseEntityAssembler;

    public CommunityController(CommunityCommandService commandService, CommunityQueryService queryService,
            ResponseEntityAssembler responseEntityAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.responseEntityAssembler = responseEntityAssembler;
    }

    @GetMapping
    public List<CommunityResource> search(@RequestParam(required = false) String type,
            @RequestParam(required = false) String locality) {
        return queryService.handle(new SearchCommunitiesQuery(type, locality)).stream()
                .map(CommunityResourceFromEntityAssembler::toResourceFromEntity).toList();
    }

    @PostMapping(value = "/Local", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createLocal(@Valid @RequestBody CreateLocalCommunityResource resource) {
        var command = CreateLocalCommunityCommandFromResourceAssembler.toCommandFromResource(resource);
        return responseEntityAssembler.toResponseEntityFromResult(commandService.handle(command),
                CommunityResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @PostMapping(value = "/Topics", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createTopic(@Valid @RequestBody CreateTopicCommunityResource resource) {
        var command = CreateTopicCommunityCommandFromResourceAssembler.toCommandFromResource(resource);
        return responseEntityAssembler.toResponseEntityFromResult(commandService.handle(command),
                CommunityResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

}
