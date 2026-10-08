package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.community.application.commandservices.PostReactionCommandService;
import pe.greenminds.ecomind.community.application.queryservices.PostReactionQueryService;
import pe.greenminds.ecomind.community.domain.model.commands.RemovePostReactionCommand;
import pe.greenminds.ecomind.community.domain.model.queries.GetPostReactionsQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.*;
import pe.greenminds.ecomind.community.interfaces.rest.transform.PostReactionResourceFromEntityAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.ReactToPostCommandFromResourceAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.UpdatePostReactionTypeCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@Tag(name = "Post Reaction", description = "Reactions to community feed posts")
@RequestMapping(value = "/api/v1/Community", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostReactionController {
    private final PostReactionCommandService commands;
    private final PostReactionQueryService queries;
    private final ResponseEntityAssembler responses;

    public PostReactionController(PostReactionCommandService c, PostReactionQueryService q, ResponseEntityAssembler r) {
        commands = c;
        queries = q;
        responses = r;
    }

    @GetMapping("/Posts/{postId}/Reactions")
    public List<PostReactionResource> list(@PathVariable Long postId) {
        return queries.handle(new GetPostReactionsQuery(postId)).stream()
                .map(PostReactionResourceFromEntityAssembler::toResourceFromEntity).toList();
    }

    @PostMapping(value = "/Post-Reactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody CreatePostReactionResource r) {
        return responses.toResponseEntityFromResult(
                commands.handle(ReactToPostCommandFromResourceAssembler.toCommandFromResource(r)),
                PostReactionResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @PatchMapping(value = "/Posts/{postId}/Reactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable Long postId, @Valid @RequestBody UpdatePostReactionTypeResource r) {
        return responses.toResponseEntityFromResult(
                commands.handle(UpdatePostReactionTypeCommandFromResourceAssembler.toCommandFromResource(r, postId)),
                PostReactionResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @DeleteMapping("/Posts/{postId}/Reactions")
    public ResponseEntity<?> delete(@PathVariable Long postId, @RequestParam Long user_id) {
        return responses.toResponseEntityFromResult(commands.handle(new RemovePostReactionCommand(postId, user_id)),
                v -> null, HttpStatus.NO_CONTENT);
    }
}
