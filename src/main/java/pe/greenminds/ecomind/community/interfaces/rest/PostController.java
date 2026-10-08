package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.community.application.commandservices.PostCommandService;
import pe.greenminds.ecomind.community.application.queryservices.PostQueryService;
import pe.greenminds.ecomind.community.application.queryservices.PostReactionQueryService;
import pe.greenminds.ecomind.community.domain.model.commands.DeletePostCommand;
import pe.greenminds.ecomind.community.domain.model.queries.SearchPostsQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.*;
import pe.greenminds.ecomind.community.interfaces.rest.transform.PostResourceFromEntityAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CreatePostCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

@RestController
@Tag(name = "Post", description = "Community feed posts")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
@RequestMapping(value = "/api/v1/Community/Posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostController {
    private final PostCommandService commands;
    private final PostQueryService queries;
    private final PostReactionQueryService reactions;
    private final ResponseEntityAssembler responses;

    public PostController(PostCommandService c, PostQueryService q, PostReactionQueryService r,
            ResponseEntityAssembler a) {
        commands = c;
        queries = q;
        reactions = r;
        responses = a;
    }

    @GetMapping
    public List<PostResource> list(@RequestParam(required = false) Long community_id) {
        return queries.handle(new SearchPostsQuery(community_id)).stream()
                .map(p -> PostResourceFromEntityAssembler.toResourceFromEntity(p, reactions.countByPostId(p.id())))
                .toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody CreatePostResource r,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        var c = CreatePostCommandFromResourceAssembler.toCommandFromResource(r, principal.accountId());
        return responses.toResponseEntityFromResult(commands.handle(c),
                post -> PostResourceFromEntityAssembler.toResourceFromEntity(post, 0), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responses.toResponseEntityFromResult(commands.handle(new DeletePostCommand(id, principal.accountId())), v -> null,
                HttpStatus.NO_CONTENT);
    }
}
